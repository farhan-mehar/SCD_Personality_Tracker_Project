#!/bin/sh
# Maven Wrapper Script
# Requires Maven to be installed. If not installed, run: sudo apt install maven
# Or download from: https://maven.apache.org/download.cgi

MAVEN_OPTS="${MAVEN_OPTS:--Xmx512m}"

# Try to find mvn in PATH
if command -v mvn > /dev/null 2>&1; then
  exec mvn "$@"
fi

# Try common installation paths
for MVN_PATH in /usr/bin/mvn /usr/local/bin/mvn "$MAVEN_HOME/bin/mvn" "$M2_HOME/bin/mvn"; do
  if [ -f "$MVN_PATH" ]; then
    exec "$MVN_PATH" "$@"
  fi
done

echo "Maven not found. Please install Maven 3.8+ and ensure 'mvn' is in your PATH."
echo "Download: https://maven.apache.org/download.cgi"
exit 1
