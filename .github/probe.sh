#!/usr/bin/env bash
# Temporary: prints the Minecraft 26.3 API the port is written against, since the development
# environment has no route to the Mojang or Fabric hosts. Removed before merging.
set -u
GROUP=$1
MC=$2
VF=$3
work=$(mktemp -d)

list() { unzip -Z1 "$MC" | grep -E "$1" | sort; }

jp() {
  for c in "$@"; do
    echo "=================== javap ${c}"
    javap -p -cp "$MC" "${c//\//.}" 2>&1
  done
}

decomp() {
  rm -rf "$work/in" "$work/out"
  mkdir -p "$work/in" "$work/out"
  for c in "$@"; do
    (cd "$work/in" && unzip -qo "$MC" "${c}.class" "${c}\$*.class" >/dev/null 2>&1)
  done
  java -jar "$VF" -e="$MC" "$work/in" "$work/out" >"$work/vf.log" 2>&1 || tail -20 "$work/vf.log"
  find "$work/out" -name '*.java' | sort | while read -r f; do
    echo "=================== source ${f#$work/out/}"
    cat "$f"
  done
}

# Prints only the members of a decompiled class whose declaration matches the regex.
members() {
  local cls=$1 re=$2
  rm -rf "$work/in" "$work/out"
  mkdir -p "$work/in" "$work/out"
  (cd "$work/in" && unzip -qo "$MC" "${cls}.class" >/dev/null 2>&1)
  java -jar "$VF" -e="$MC" "$work/in" "$work/out" >"$work/vf.log" 2>&1 || tail -20 "$work/vf.log"
  local f
  f=$(find "$work/out" -name '*.java' | head -1)
  echo "=================== members of ${cls} matching /${re}/"
  awk -v re="$re" '
    !inm && $0 ~ re {
      inm=1; match($0, /^ */); ind=RLENGTH; field=($0 !~ /\)( throws [^{]*)? *\{? *$/)
      print
      if ($0 ~ /;[ ]*$/) { inm=0; print "" }
      next
    }
    inm {
      print
      pad=sprintf("%" ind "s", "")
      if (field && $0 ~ /;[ ]*$/) { inm=0; print "" }
      else if (!field && $0 ~ ("^" pad "}[ ]*$")) { inm=0; print "" }
    }
  ' "$f"
}

case "$GROUP" in
  a)
    list '^net/minecraft/world/level/chunk/status/'
    decomp net/minecraft/world/level/chunk/status/ChunkStatusTasks \
           net/minecraft/world/level/chunk/status/ChunkStatus \
           net/minecraft/world/level/chunk/status/ChunkPyramid \
           net/minecraft/server/level/WorldGenRegion \
           net/minecraft/world/level/chunk/ChunkGenerator
    for c in net/minecraft/world/level/levelgen/NoiseBasedChunkGenerator net/minecraft/world/level/levelgen/FlatLevelSource net/minecraft/world/level/levelgen/DebugLevelSource; do
      echo "=================== overrides in $c"
      javap -p -cp "$MC" "${c//\//.}" 2>&1 | grep -iE 'decorat|feature|class ' || true
    done
    ;;
  b)
    list '^net/minecraft/world/level/levelgen/feature/[^/]*\.class$'
    list '^net/minecraft/world/level/levelgen/(placement|structure)/[^/]*\.class$' | grep -E 'Placed|StructureStart|Template'
    decomp net/minecraft/world/level/levelgen/feature/ConfiguredFeature \
           net/minecraft/world/level/levelgen/feature/Feature \
           net/minecraft/world/level/levelgen/feature/FeatureType \
           net/minecraft/world/level/levelgen/placement/PlacedFeature \
           net/minecraft/world/level/levelgen/feature/OreFeature \
           net/minecraft/world/level/chunk/BulkSectionAccess \
           net/minecraft/world/level/levelgen/structure/StructureStart
    ;;
  c)
    jp net/minecraft/world/level/chunk/ChunkAccess net/minecraft/world/level/chunk/ProtoChunk \
       net/minecraft/world/level/chunk/ImposterProtoChunk net/minecraft/world/level/chunk/LevelChunkSection \
       net/minecraft/world/level/levelgen/Heightmap 'net/minecraft/world/level/levelgen/Heightmap$Types' \
       'net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase' net/minecraft/world/level/block/Block \
       net/minecraft/world/level/block/Fallable net/minecraft/world/level/block/LiquidBlock \
       net/minecraft/world/level/block/LeavesBlock net/minecraft/world/level/material/FlowingFluid \
       net/minecraft/world/entity/ai/village/poi/PoiTypes net/minecraft/world/level/LevelHeightAccessor \
       net/minecraft/world/level/chunk/LevelChunk
    ;;
  d)
    jp net/minecraft/server/level/ServerLevel net/minecraft/server/level/ServerChunkCache \
       net/minecraft/world/level/storage/LevelData net/minecraft/world/level/storage/ServerLevelData \
       'net/minecraft/world/level/storage/LevelData$RespawnData' net/minecraft/server/players/PlayerList
    decomp net/minecraft/server/level/TicketType
    members net/minecraft/server/MinecraftServer '^   [a-zA-Z@].* (loadLevel|createLevels|setInitialSpawn|tickServer|tickChildren|stopServer|halt|overworld|getRespawnData|findRespawnDimension|getWorldData)[(]'
    members net/minecraft/server/level/ServerLevel '^   [a-zA-Z@].* (onBlockStateChange|getSharedSpawnPos|getRespawnData|getSeed|updatePOIOnBlockStateChange)[(]'
    ;;
  e)
    list '^net/minecraft/server/permissions/'
    decomp net/minecraft/commands/Commands net/minecraft/server/commands/ForceLoadCommand \
           net/minecraft/server/commands/SetWorldSpawnCommand
    jp net/minecraft/commands/CommandSourceStack net/minecraft/commands/arguments/DimensionArgument
    for c in $(list '^net/minecraft/server/permissions/[^$]*\.class$' | sed 's/\.class$//'); do jp "$c"; done
    javap -p -cp "$MC" net.minecraft.network.chat.Component 2>&1 | grep -E 'literal|translatable|interface' || true
    ;;
  f)
    jp net/minecraft/core/Registry net/minecraft/resources/Identifier net/minecraft/resources/ResourceKey \
       net/minecraft/tags/TagKey net/minecraft/tags/BlockTags net/minecraft/util/RandomSource \
       net/minecraft/core/registries/BuiltInRegistries net/minecraft/core/registries/Registries
    list '^net/minecraft/world/level/block/[^/]*\.class$' | grep -iE 'sulfur|cinnabar|poplar|straw|shelf|shrub|bush|leaf|dried|golem|lantern|chain|bars|torch|spike'
    members net/minecraft/world/level/block/Blocks '^   public static final Block [A-Z_]*(SULFUR|CINNABAR|POPLAR|STRAW|SHELF|SHRUB|BUSH|LEAF_LITTER|DRIED_GHAST|IRON_CHAIN|COPPER_CHAIN|COPPER_BARS|COPPER_LANTERN|COPPER_TORCH|LIGHTNING_ROD|GOLDEN_DANDELION|WILDFLOWERS|CACTUS_FLOWER|EYEBLOSSOM|RESIN|CREAKING)'
    for c in $(list '^net/minecraft/world/level/block/[^/$]*\.class$' | grep -iE 'sulfur|cinnabar|poplar|straw|shelfmushroom|shrub|driedghast' | sed 's/\.class$//'); do decomp "$c"; done
    ;;
esac
