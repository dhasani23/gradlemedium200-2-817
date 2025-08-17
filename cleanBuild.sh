#!/bin/bash

# Remove build directories
find . -name "build" -type d -exec rm -rf {} +
find . -name ".gradle" -type d -exec rm -rf {} +

# Run gradle build with clean
./gradlew clean build --no-daemon