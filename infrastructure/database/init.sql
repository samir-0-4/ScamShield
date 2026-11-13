-- ==============================================================================
-- PhantomTrace Database Initialization Script
-- Used by Docker Compose to ensure required extensions and base schema exist
-- ==============================================================================

-- Enable UUID extension for cryptographic UUIDv4 identifiers
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pg_trgm for fuzzy text and indicator search capabilities
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

