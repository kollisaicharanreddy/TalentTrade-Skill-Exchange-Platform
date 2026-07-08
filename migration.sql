-- TalentTrade - Migration Script to add RBAC and Google OAuth2 columns to users table

-- 1. Add email_verified, enabled, role, and provider columns if they don't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'USER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'LOCAL';

-- 2. Backfill existing records to have default values
UPDATE users SET email_verified = TRUE WHERE email_verified IS NULL;
UPDATE users SET enabled = TRUE WHERE enabled IS NULL;
UPDATE users SET role = 'USER' WHERE role IS NULL;
UPDATE users SET provider = 'LOCAL' WHERE provider IS NULL;

-- 3. Set constraints
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN provider SET NOT NULL;
