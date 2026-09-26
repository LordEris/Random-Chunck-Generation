#!/usr/bin/env bash
# Starts a real Fabric dedicated server with the built jar, generates chunks in the three vanilla
# dimensions through /randomchunks pregen, then checks the saved region files: every fully
# generated chunk must be made of a single block.
set -euo pipefail

JAR=$1
SCRIPTS=$(cd "$(dirname "$0")" && pwd)
MC=$(grep -E '^minecraft_version=' gradle.properties | cut -d= -f2)
LOADER=$(grep -E '^loader_version=' gradle.properties | cut -d= -f2)
INSTALLER=$(curl -fsSL https://meta.fabricmc.net/v2/versions/installer | jq -r '[.[] | select(.stable)][0].version')

rm -rf smoke-server
mkdir -p smoke-server/mods smoke-server/config
cp "$JAR" smoke-server/mods/
cd smoke-server
echo "Fabric server: Minecraft $MC, loader $LOADER, installer $INSTALLER"
curl -fsSL -o fabric-server.jar "https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER/$INSTALLER/server/jar"
echo "eula=true" > eula.txt
cat > server.properties <<PROPS
enable-rcon=true
rcon.port=25575
rcon.password=smoke-test
broadcast-rcon-to-ops=false
level-seed=20260926
spawn-protection=0
view-distance=4
simulation-distance=4
PROPS
# Only the keys that matter here: the mod appends the missing ones, which is tested too.
echo '{"spawnProtectionRadius": 0}' > config/random-chunks.json

rcon() { python3 "$SCRIPTS/rcon.py" "$@"; }
fail() {
  echo "::error::$1"
  echo "----- server log (tail) -----"
  tail -n 200 server.log || true
  kill "$SERVER_PID" 2>/dev/null || true
  exit 1
}

java -Xmx3G -jar fabric-server.jar nogui > server.log 2>&1 &
SERVER_PID=$!

for _ in $(seq 1 180); do
  grep -q 'RCON running' server.log && break
  kill -0 "$SERVER_PID" 2>/dev/null || fail "the server stopped during startup"
  sleep 5
done
grep -q 'RCON running' server.log || fail "the server did not start within 15 minutes"

rcon "randomchunks info"
for request in "minecraft:the_nether 3" "minecraft:the_end 3" "minecraft:overworld 4 0 0"; do
  rcon "rcg pregen start $request"
  finished=
  for _ in $(seq 1 120); do
    status=$(rcon "rcg pregen status")
    echo "$status"
    if echo "$status" | grep -q 'No pregeneration'; then finished=1; break; fi
    sleep 5
  done
  [ -n "$finished" ] || fail "pregen of $request did not finish within 10 minutes"
done
rcon "save-all flush"
rcon "stop" || true

for _ in $(seq 1 60); do
  kill -0 "$SERVER_PID" 2>/dev/null || break
  sleep 2
done
kill -0 "$SERVER_PID" 2>/dev/null && fail "the server did not stop"
wait "$SERVER_PID" || true

echo "----- mod log lines -----"
grep -E 'Random Chunks|randomchunks' server.log || true

for dimension in minecraft:overworld minecraft:the_nether minecraft:the_end; do
  grep -q "Filling new chunks of $dimension" server.log || fail "no chunk was filled in $dimension"
done
if grep -E 'Mixin|mixin' server.log | grep -qiE 'error|fail|exception'; then
  fail "the log reports a mixin problem"
fi
if grep -qE 'Exception|Crash report' server.log; then
  fail "the log holds an exception"
fi
grep -q 'random-chunks.json: added missing key' server.log || fail "missing config keys were not added"

python3 "$SCRIPTS/check_world.py" random-chunks world
