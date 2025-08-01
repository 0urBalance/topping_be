-- ==============================================
-- Topping Database Initialization Script
-- ==============================================
-- This script runs automatically when PostgreSQL container starts for the first time

-- Create additional extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Set timezone
SET timezone = 'Asia/Seoul';

-- Create indexes for better performance (these will be created by JPA, but kept for reference)
-- Note: Let JPA handle table creation via Hibernate DDL

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'Topping database initialization completed successfully';
END $$;