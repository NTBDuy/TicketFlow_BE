-- ===== USERS =====
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR(150) NOT NULL,
                       email VARCHAR(254) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       must_change_password BOOLEAN NOT NULL,
                       enabled BOOLEAN NOT NULL,
                       locked BOOLEAN NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP,
                       created_by VARCHAR(255) NOT NULL,
                       updated_by VARCHAR(255)
);

CREATE INDEX idx_user_email ON users(email);

CREATE UNIQUE INDEX uq_user_email_active ON users(email) WHERE deleted_at IS NULL;

-- ===== ROLES =====
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE,
                       description VARCHAR(255)
);

-- ===== PERMISSIONS =====
CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL UNIQUE,
                             description VARCHAR(255)
);

-- ===== USER_ROLES (many-to-many) =====
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ===== ROLE_PERMISSIONS (many-to-many) =====
CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
                                  CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);