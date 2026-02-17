#!/bin/bash
set -e

# Navigate to the a2a-java root directory
cd "$(dirname "$0")/.."

echo "Starting database container..."
docker-compose up -d

echo "Waiting for database to be ready..."
sleep 5

echo "Database started successfully."
