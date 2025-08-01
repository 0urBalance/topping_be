#!/bin/bash

# ==============================================
# Topping Database Startup Script
# ==============================================
# Starts PostgreSQL and pgAdmin containers for local development

echo "🚀 Starting Topping PostgreSQL + pgAdmin containers..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found. Please create one based on .env.example"
    exit 1
fi

# Start containers
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 10

# Check container status
echo "📊 Container Status:"
docker-compose ps

echo ""
echo "✅ Database containers started successfully!"
echo ""
echo "🔗 Access Information:"
echo "   PostgreSQL: localhost:5432"
echo "   pgAdmin:    http://localhost:5050"
echo "   Database:   topping"
echo "   Username:   topping_user"
echo ""
echo "📝 To use Docker PostgreSQL in your application:"
echo "   1. Update your .env file to use Docker PostgreSQL settings"
echo "   2. Comment out production DB settings"
echo "   3. Uncomment Docker PostgreSQL settings"
echo ""
echo "🛑 To stop containers: ./stop-db.sh"