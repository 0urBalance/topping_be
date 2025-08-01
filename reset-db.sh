#!/bin/bash

# ==============================================
# Topping Database Reset Script
# ==============================================
# Completely resets the database by removing containers and volumes

echo "⚠️  WARNING: This will completely delete all database data!"
echo "Are you sure you want to continue? (y/N)"
read -r response

if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo "🗑️  Stopping and removing containers..."
    docker-compose down
    
    echo "🗑️  Removing database volumes..."
    docker volume rm topping_postgres_data 2>/dev/null || true
    docker volume rm topping_pgadmin_data 2>/dev/null || true
    
    echo "🚀 Starting fresh containers..."
    docker-compose up -d
    
    echo "⏳ Waiting for PostgreSQL to initialize..."
    sleep 15
    
    echo ""
    echo "✅ Database has been completely reset!"
    echo ""
    echo "🔗 Access Information:"
    echo "   PostgreSQL: localhost:5432"
    echo "   pgAdmin:    http://localhost:5050"
    echo "   Database:   topping"
    echo "   Username:   topping_user"
else
    echo "❌ Database reset cancelled."
fi