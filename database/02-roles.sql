INSERT INTO roles (name, description)
VALUES ('user', 'Default user, no special rights'),
       ('viewer', 'Can view console output and player count'),
       ('operator', 'Viewer + can type commands'),
       ('editor', 'Operator + can edit config files'),
       ('maintainer', 'Editor + can manage files and allocate RAM');

-- Create the inheritance links
INSERT INTO role_inheritance (role_name, inherits_role_name)
VALUES
  -- Viewer builds on User
  ('viewer',     'user'),
  -- Operator builds on Viewer
  ('operator',   'viewer'),
  -- Editor builds on Operator
  ('editor',     'operator'),
  -- Maintainer builds on Editor
  ('maintainer', 'editor');