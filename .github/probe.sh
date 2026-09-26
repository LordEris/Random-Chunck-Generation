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

# Prints `lines` lines of a decompiled class starting at the first line matching the regex.
around() {
  local cls=$1 re=$2 lines=$3
  rm -rf "$work/in" "$work/out"
  mkdir -p "$work/in" "$work/out"
  (cd "$work/in" && unzip -qo "$MC" "${cls}.class" >/dev/null 2>&1)
  java -jar "$VF" -e="$MC" "$work/in" "$work/out" >"$work/vf.log" 2>&1 || tail -20 "$work/vf.log"
  local f
  f=$(find "$work/out" -name '*.java' | head -1)
  echo "=================== ${cls} from /${re}/ (${lines} lines)"
  grep -nE "$re" "$f" | head -3
  local n
  n=$(grep -nE "$re" "$f" | head -1 | cut -d: -f1)
  [ -n "$n" ] && sed -n "${n},$((n + lines))p" "$f"
}

case "$GROUP" in
  g)
    P=net/minecraft/world/level/levelgen
    decomp $P/placement/FeaturePlacer $P/feature/OverlayFeature $P/feature/SequenceFeature $P/feature/TemplateFeature \
           $P/feature/RandomSelectorFeature $P/feature/WeightedRandomSelectorFeature $P/feature/SimpleRandomSelectorFeature \
           $P/feature/RandomBooleanSelectorFeature $P/feature/WeightedPlacedFeature $P/feature/RootSystemFeature \
           $P/feature/VegetationPatchFeature $P/feature/SingleBlockPillarFeature $P/feature/MonsterRoomFeature
    ;;
  h)
    mkdir -p "$work/all" && (cd "$work/all" && unzip -qo "$MC" 'net/*' >/dev/null 2>&1)
    desc='(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z'
    echo "=================== classes calling Feature.place"
    for f in $(grep -rlF "$desc" "$work/all"); do
      c=${f#$work/all/}; c=${c%.class}
      hits=$(javap -c -p -cp "$work/all" "${c//\//.}" 2>/dev/null | grep -nE 'invokeinterface.*levelgen/feature/Feature\.place|^  [a-z].*\(' | grep -B1 'invokeinterface' | grep -v '^--$')
      [ -n "$hits" ] && { echo "--- $c"; echo "$hits"; }
    done
    echo "=================== classes using FeaturePlacer"
    grep -rlF 'net/minecraft/world/level/levelgen/placement/FeaturePlacer' "$work/all" | sed "s#$work/all/##" | sort
    echo "=================== classes using BulkSectionAccess"
    grep -rlF 'net/minecraft/world/level/chunk/BulkSectionAccess' "$work/all" | sed "s#$work/all/##" | sort
    echo "=================== feature classes writing LevelChunkSection directly"
    for f in $(grep -rlF 'net/minecraft/world/level/chunk/LevelChunkSection' "$work/all/net/minecraft/world/level/levelgen"); do
      c=${f#$work/all/}; c=${c%.class}
      javap -c -p -cp "$work/all" "${c//\//.}" 2>/dev/null | grep -q 'LevelChunkSection.setBlockState' && echo "$c"
    done
    echo "=================== FeatureTypes registrations"
    around net/minecraft/world/level/levelgen/feature/FeatureTypes 'class FeatureTypes' 90
    ;;
  i)
    around net/minecraft/server/MinecraftServer 'private static void setInitialSpawn' 70
    around net/minecraft/server/MinecraftServer ' void prepareLevels' 45
    around net/minecraft/server/level/ServerChunkCache 'addTicketAndLoadWithRadius' 25
    around net/minecraft/server/level/ServerChunkCache 'public void removeTicketWithRadius' 8
    around net/minecraft/commands/Commands 'getDispatcher' 4
    ;;
  j)
    jp net/minecraft/core/TypedInstance net/minecraft/world/level/ChunkPos net/minecraft/commands/arguments/coordinates/ColumnPosArgument \
       net/minecraft/server/level/ColumnPos net/minecraft/world/level/block/SpeleothemBlock net/minecraft/world/level/block/PointedDripstoneBlock \
       net/minecraft/world/level/block/StrawBedBlock net/minecraft/world/level/block/AbstractBedBlock net/minecraft/world/level/block/BedBlock \
       net/minecraft/world/level/WorldGenLevel net/minecraft/world/level/block/state/StateHolder net/minecraft/core/Holder
    ;;
esac
