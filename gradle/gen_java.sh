N=5000
ROOT_DIR="java"
SOURCES="$ROOT_DIR/src/main/java/app/cash/droidcon/to"
OUTPUT="$ROOT_DIR/javaout"

rm -rf "$SOURCES"
mkdir -p "$SOURCES"
rm -f "$OUTPUT"

for (( i=1; i<="$N"; i++ ))
do
  JAVAFILENAME="JavaTemp$i.java"
  JAVACLASSFILE="$SOURCES/$JAVAFILENAME"
  echo "package app.cash.droidcon.to;" >> "$JAVACLASSFILE"
  echo "class JavaTemp$i {}" >> "$JAVACLASSFILE"

  TIMED=$(time (./gradlew java:assemble > /dev/null) 2>&1)
  DATA=$(echo -n "$TIMED" | cut -f2 -d'm' | cut -f1 -d's' | tr '\n' '\t')

  echo "$i$DATA" >> "$OUTPUT"
done
