#!/bin/bash
echo "Removing all build and .gradle directories..."
find /home/hcchavan/workplace/segsynth/output/gradlemedium200 -name "build" -type d -exec rm -rf {} \; 2>/dev/null
find /home/hcchavan/workplace/segsynth/output/gradlemedium200 -name ".gradle" -type d -exec rm -rf {} \; 2>/dev/null
find /home/hcchavan/workplace/segsynth/output/gradlemedium200 -name "*.class" -delete
echo "Clean completed"