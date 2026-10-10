#!/usr/bin/env bash
# Compile and start the game at its title screen.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/javafx-path.sh

rm -rf build && mkdir build
javac ${FX_JARS:+--module-path "$FX_JARS" --add-modules javafx.controls} -d build src/*.java
exec java ${FX_JARS:+--module-path "$FX_JARS" --add-modules javafx.controls} -cp build GameSetup
