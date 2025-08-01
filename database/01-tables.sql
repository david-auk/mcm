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
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name              VARCHAR(255) UNIQUE NOT NULL,
    description       VARCHAR(255),
    minecraft_version VARCHAR(100)        NOT NULL,
    jar_url           TEXT                NOT NULL,
    eula_accepted     BOOLEAN          DEFAULT FALSE,
    created_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    allocated_ram_mb  INTEGER          DEFAULT 1024,
    port              INTEGER             NOT NULL CHECK (
        port BETWEEN 1023 AND 65535 AND port % 2 = 0
        )
);

-- Server instance sessions
CREATE TABLE server_instance_sessions
(
    id                 UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    server_instance_id UUID      NOT NULL,
    started_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_by_user_id UUID      NOT NULL,
    stopped_at         TIMESTAMP,
    stopped_by_user_id UUID,

    CONSTRAINT fk_started_by_user FOREIGN KEY (started_by_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_stopped_by_user FOREIGN KEY (stopped_by_user_id) REFERENCES users (id) ON DELETE CASCADE
);


CREATE TABLE server_instance_properties
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    server_instance_id UUID REFERENCES server_instances (id) ON DELETE CASCADE,
    key                TEXT NOT NULL,
    value              TEXT NOT NULL,
    type               TEXT NOT NULL CHECK (type IN ('string', 'integer', 'boolean')),
    hidden             BOOLEAN          DEFAULT FALSE, -- whether to hide from API/UI etc.

    CONSTRAINT unique_key_per_instance UNIQUE (server_instance_id, key)
);

-- Roles
CREATE TABLE roles
(
    name        VARCHAR(100) PRIMARY KEY,
    description TEXT
);

CREATE TABLE role_inheritance
(
    role_name          VARCHAR(100) NOT NULL
        REFERENCES roles (name) ON DELETE CASCADE,
    inherits_role_name VARCHAR(100) NOT NULL
        REFERENCES roles (name) ON DELETE CASCADE,
    PRIMARY KEY (role_name, inherits_role_name)
);
-- User Role Assignments (many-to-many per instance)
CREATE TABLE user_role_assignments
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    instance_id UUID         NOT NULL,
    role        VARCHAR(100) NOT NULL,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_instance FOREIGN KEY (instance_id) REFERENCES server_instances (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role) REFERENCES roles (name) ON DELETE CASCADE,

    UNIQUE (user_id, instance_id) -- Make constraint rule: only 1 role assignment per instance.
    --UNIQUE (user_id, instance_id, role) -- Prevent duplicate assignments
);

-- Types of actions (for the user_action_logs tableOld)
CREATE TABLE action_types
(
    name             VARCHAR(100) PRIMARY KEY UNIQUE,
    message_template TEXT,
    severity         VARCHAR(20) DEFAULT 'info' CHECK (severity IN ('info', 'warning', 'severe', 'critical'))
);

-- User actions logs
CREATE TABLE user_action_logs
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action_type      VARCHAR(100) NOT NULL,
    user_id          UUID         NOT NULL,
    affected_user_id UUID,
    instance_id      UUID,
    timestamp        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    metadata         JSONB, -- e.g., { "from": 2048, "to": 4096 }

    -- TODO Find better way to preserve logs when items get deleted
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_log_affected_user FOREIGN KEY (affected_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_log_instance FOREIGN KEY (instance_id) REFERENCES server_instances (id) ON DELETE CASCADE,
    CONSTRAINT fk_log_action_type FOREIGN KEY (action_type) REFERENCES action_types (name) ON DELETE CASCADE
);

-- Backup table
CREATE TABLE backups
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    server_instance_id UUID NOT NULL REFERENCES server_instances (id) ON DELETE CASCADE,
-- TODO implement    extends_backup     UUID REFERENCES backups (id) ON DELETE CASCADE, -- IF extended backup gets deleted make the current backup inherit parent and make changes of deleted backup to the current backup
    created_by         UUID REFERENCES users (id) ON DELETE SET NULL,
    timestamp          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
