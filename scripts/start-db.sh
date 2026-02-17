#!/bin/bash
set -e

# Navigate to the a2a-java root directory
cd "$(dirname "$0")/.."

echo "Starting database container..."
docker-compose up -d

echo "Waiting for database to be ready..."
sleep 5

echo "Database started successfully."

echo "To initialize the database schema and insert sample data, run:"
echo "  mvn exec:java -pl extras/task-store-database-jpa"
