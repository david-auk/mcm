-- Enable the gen_random_uuid function
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users
CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL
);

-- Admins (normalized 1:1 with users)
CREATE TABLE admins
(
    id UUID PRIMARY KEY,
    CONSTRAINT fk_admin_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

-- Server Instances
CREATE TABLE server_instances
(
    id                UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name              VARCHAR(255) NOT NULL,
    description       VARCHAR(255),
    minecraft_version VARCHAR(100) NOT NULL,
    jar_url           TEXT         NOT NULL,
    eula_accepted     BOOLEAN                  DEFAULT FALSE,
    created_at        TIMESTAMP                DEFAULT CURRENT_TIMESTAMP,
    allocated_ram_mb  INTEGER                  DEFAULT 1024,
    port              INTEGER                  NOT NULL CHECK (port BETWEEN 1024 AND 65535)
);


CREATE TABLE server_instance_properties (
    id               UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    server_instance_id UUID REFERENCES server_instances(id) ON DELETE CASCADE,
    key TEXT NOT NULL,
    value TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('string', 'integer', 'boolean')),
    hidden BOOLEAN DEFAULT FALSE,     -- whether to hide from API/UI etc.

    CONSTRAINT unique_key_per_instance UNIQUE (server_instance_id, key)
);

-- Roles
CREATE TABLE roles
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- User Role Assignments (many-to-many per instance)
CREATE TABLE user_role_assignments
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    instance_id UUID NOT NULL,
    role_id     UUID NOT NULL,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_instance FOREIGN KEY (instance_id) REFERENCES server_instances (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,

    UNIQUE (user_id, instance_id, role_id) -- Prevent duplicate assignments
);

-- Types of actions (for the user_action_logs tableOld)
CREATE TABLE action_types
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    severity    VARCHAR(20)      DEFAULT 'info' CHECK (severity IN ('info', 'warning', 'severe', 'critical'))
);

-- User actions logs
CREATE TABLE user_action_logs
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL,
    instance_id    UUID NOT NULL,
    action_type_id UUID NOT NULL,
    timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata       JSONB, -- e.g., { "from": 2048, "to": 4096 }

    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_log_instance FOREIGN KEY (instance_id) REFERENCES server_instances (id) ON DELETE CASCADE,
    CONSTRAINT fk_log_action_type FOREIGN KEY (action_type_id) REFERENCES action_types (id) ON DELETE SET NULL
);