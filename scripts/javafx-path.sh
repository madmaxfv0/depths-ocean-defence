#!/usr/bin/env bash
# Sourced by the other scripts. Works out how to reach JavaFX.
#
#   JAVAFX_LIB=/path/to/javafx-sdk/lib   an OpenJFX SDK "lib" folder, or any
#                                        folder holding the javafx-*.jar files
#                                        (BlueJ ships them in Contents/Java)
#
# Leave JAVAFX_LIB unset when the JDK already bundles JavaFX
# (Azul Zulu "JDK FX", BellSoft Liberica "Full JDK").
FX_JARS=""
if [ -n "${JAVAFX_LIB:-}" ]; then
  if [ ! -d "$JAVAFX_LIB" ]; then
    echo "JAVAFX_LIB is set to '$JAVAFX_LIB', which is not a folder." >&2
    exit 1
  fi
  for jar in "$JAVAFX_LIB"/javafx*.jar; do
    [ -e "$jar" ] && FX_JARS="${FX_JARS:+$FX_JARS:}$jar"
  done
  if [ -z "$FX_JARS" ]; then
    echo "No javafx*.jar files found in '$JAVAFX_LIB'." >&2
    exit 1
  fi
fi
