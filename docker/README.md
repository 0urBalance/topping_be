# Docker PostgreSQL Setup

This directory contains the Docker configuration for running PostgreSQL and pgAdmin locally for development.

## Quick Start

```bash
# Start database containers
./start-db.sh

# Stop database containers  
./stop-db.sh

# Reset database (delete all data)
./reset-db.sh
```

## Access Information

- **PostgreSQL**: `localhost:5432`
- **pgAdmin**: `http://localhost:5050`
- **Database**: `topping`
- **Username**: `topping_user`
- **Password**: `topping_pass`

## Configuration Files

- `docker-compose.yml` - Container orchestration
- `init/init.sql` - Database initialization script
- `pgadmin/servers.json` - pgAdmin server configuration
- `.env` - Environment variables (not tracked in git)

## Switching Between Local and Production Database

### For Local Development (Docker PostgreSQL)
Update `.env` file:
```env
# Uncomment these lines
DB_NAME=topping
DB_USER=topping_user
DB_PASSWORD=topping_pass
DB_URL=jdbc:postgresql://localhost:5432/topping

# Comment out production database settings
# DB_URL=jdbc:postgresql://...
```

### For Production (Neon PostgreSQL)
Update `.env` file:
```env
# Comment out Docker PostgreSQL settings
# DB_NAME=topping
# DB_USER=topping_user

# Uncomment production database settings
DB_URL=jdbc:postgresql://ep-floral-glade-a8gb92q4-pooler.eastus2.azure.neon.tech/...
```

## Profiles

- **Default**: Uses `.env` configuration
- **Docker**: `application-docker.properties` for containerized deployment
- **Production**: `application-prod.properties` for production deployment

## Data Persistence

Database data is stored in Docker volumes and persists across container restarts:
- `topping_postgres_data` - PostgreSQL data
- `topping_pgadmin_data` - pgAdmin configuration

## Troubleshooting

1. **Connection refused**: Ensure containers are running with `docker-compose ps`
2. **Authentication failed**: Check credentials in `.env` file
3. **Port conflicts**: Update ports in `docker-compose.yml` if needed
4. **Reset data**: Use `./reset-db.sh` to start fresh