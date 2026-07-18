-- ===== SEED PERMISSIONS =====
INSERT INTO permissions (name, description) VALUES
                                                ('USER_READ', 'View user list and user details'),
                                                ('USER_CREATE', 'Create user accounts'),
                                                ('USER_UPDATE', 'Update user information'),
                                                ('USER_ROLE_UPDATE', 'Update roles assigned to users'),
                                                ('USER_DELETE', 'Delete user accounts'),
                                                ('USER_PASSWORD_RESET', 'Reset another user password'),
                                                ('USER_LOCK_UPDATE', 'Lock or unlock user accounts');

-- ===== SEED ROLES =====
INSERT INTO roles (name, description) VALUES
                                          ('ADMIN', 'System administrator'),
                                          ('USER', 'Regular user');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';