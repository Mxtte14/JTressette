#!/bin/bash
# Build script for JTressette
# This script compiles all Java source files and copies resources

echo "Building JTressette..."

# Navigate to the JTressette directory
cd JTressette

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
javac -d bin -sourcepath src src/main/JTressette.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Copy resources to bin directory
    echo "Copying resources..."
    cp -r src/res bin/
    
    echo "Build successful! Compiled files are in JTressette/bin/"
    echo "To run the application, use: ./run.sh"
else
    echo "Build failed!"
    exit 1
fi
