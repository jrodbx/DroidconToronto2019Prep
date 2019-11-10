N=5000
ROOT_DIR="kotlin"
SOURCES="$ROOT_DIR/src/main/java/app/cash/droidcon/to"
OUTPUT="$ROOT_DIR/ktout"

rm -rf "$SOURCES"
mkdir -p "$SOURCES"
rm -f "$OUTPUT"

for (( i=1; i<="$N"; i++ ))
do
  KTFILENAME="KotlinTemp$i.kt"
  KTCLASSFILE="$SOURCES/$KTFILENAME"
  echo "package app.cash.droidcon.to" >> "$KTCLASSFILE"
  echo "class KotlinTemp$i" >> "$KTCLASSFILE"

  TIMED=$(time (./gradlew kotlin:assemble > /dev/null) 2>&1)
  DATA=$(echo -n "$TIMED" | cut -f2 -d'm' | cut -f1 -d's' | tr '\n' '\t')

  echo "$i$DATA" >> "$OUTPUT"
done
