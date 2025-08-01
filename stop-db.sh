#!/bin/bash

# ==============================================
# Topping Database Stop Script
# ==============================================
# Stops PostgreSQL and pgAdmin containers

echo "🛑 Stopping Topping database containers..."

docker-compose down

echo ""
echo "✅ Database containers stopped successfully!"
echo ""
echo "💡 Data is preserved in Docker volumes."
echo "   To start again: ./start-db.sh"
echo "   To reset data: ./reset-db.sh"