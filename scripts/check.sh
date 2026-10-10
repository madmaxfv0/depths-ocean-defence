#!/usr/bin/env bash
# Compile everything and run the headless game-rule checks (no window is opened).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/javafx-path.sh

rm -rf build && mkdir build
javac ${FX_JARS:+-cp "$FX_JARS"} -d build src/*.java test/*.java
java -cp "build${FX_JARS:+:$FX_JARS}" GameLogicCheck
