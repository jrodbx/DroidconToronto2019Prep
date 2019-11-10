CRAWLER_OUT="crawler_out"
JAVA_GENERATED_SOURCES_ROOT="java/build/generated/source/wire"
KOTLIN_GENERATED_SOURCES_ROOT="kotlin/build/generated/source/wire"
BUILD_SOURCES_ROOT="mixed-build/src/main/java"
BUILD_OUTPUT="mixed-build/build"
TIMED_OUTPUT="mixed-build/mixedout"

rm -rf "$BUILD_SOURCES_ROOT"
mkdir -p "$BUILD_SOURCES_ROOT"
rm -rf "$BUILD_OUTPUT"
rm -f "$TIMED_OUTPUT"

#./gradlew crawler:run

readarray -t PROTO_CLASSES_ORDER < "$CRAWLER_OUT"

INDEX=1
for PROTO_CLASS_ORDER in "${PROTO_CLASSES_ORDER[@]}"
do
  PROTO_CLASSES=(${PROTO_CLASS_ORDER//;/ })

  # should nearly always be array of size 1 (cuz no cycles)
  for PROTO_CLASS in "${PROTO_CLASSES[@]}"
  do
    if (( INDEX % 2 )); then #odd
      PROTO_PATH="$(echo "$PROTO_CLASS" | tr -s '.' '/').java"
      PACKAGE_PATH=$(dirname "$PROTO_PATH")
      mkdir -p "$BUILD_SOURCES_ROOT/$PACKAGE_PATH"
      cp "$JAVA_GENERATED_SOURCES_ROOT/$PROTO_PATH" "$BUILD_SOURCES_ROOT/$PACKAGE_PATH"
    else
      PROTO_PATH="$(echo "$PROTO_CLASS" | tr -s '.' '/').kt"
      PACKAGE_PATH=$(dirname "$PROTO_PATH")
      mkdir -p "$BUILD_SOURCES_ROOT/$PACKAGE_PATH"
      cp "$KOTLIN_GENERATED_SOURCES_ROOT/$PROTO_PATH" "$BUILD_SOURCES_ROOT/$PACKAGE_PATH"
    fi
  done

  TIMED=$(time (./gradlew mixed-build:assemble > /dev/null) 2>&1)
  DATA=$(echo -n "$TIMED" | cut -f2 -d'm' | cut -f1 -d's' | tr '\n' '\t')

  # output the same time for N consecutive indices
  for PROTO_CLASS in "${PROTO_CLASSES[@]}"
  do
    echo "$INDEX$DATA" >> "$TIMED_OUTPUT"
    INDEX=$((INDEX+1))
  done
done
