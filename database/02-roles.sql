INSERT INTO roles (name, description)
VALUES ('User', 'Default user, no special rights'),
       ('Viewer', 'Can view console output and player count'),
       ('Operator', 'Viewer + can type commands'),
       ('Editor', 'Operator + can edit config files'),
       ('Maintainer', 'Editor + can manage files and allocate RAM');
