#!/bin/bash
# Run script for JTressette
# This script runs the compiled application

echo "Running JTressette..."

# Navigate to the JTressette directory
cd JTressette

# Check if bin directory exists
if [ ! -d "bin" ]; then
    echo "Error: bin directory not found. Please run ./build.sh first."
    exit 1
fi

# Run the application
java -cp bin:src main.JTressette

# Check if run was successful
if [ $? -ne 0 ]; then
    echo "Failed to run JTressette!"
    exit 1
fi
