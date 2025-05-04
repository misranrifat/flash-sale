#!/bin/bash

echo "Starting Flash Sale Simulator"

# Ensure Redis is running
echo "Checking if Redis is running..."
if command -v redis-cli &> /dev/null; then
    if redis-cli ping > /dev/null; then
        echo "Redis is running."
    else
        echo "Redis is not running. Please start Redis before running the simulator."
        echo "You can start Redis with: redis-server"
        exit 1
    fi
else
    echo "Warning: redis-cli not found. Unable to check if Redis is running."
    echo "Make sure Redis is running before proceeding."
fi

# Build and run the application with the simulator profile
./gradlew bootRun --args="--spring.profiles.active=simulator" 