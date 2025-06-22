INSERT INTO action_types (name, description, severity)
VALUES ('change_allocated_ram', 'User changed the server RAM allocation', 'warning'),
       ('edit_config_file', 'User edited a server config file', 'info'),
       ('upload_file', 'User uploaded a file to the server', 'info'),
       ('execute_command', 'User executed a console command', 'info'),
       ('assign_role', 'User assigned a role to another user', 'warning'),
       ('promote_admin', 'User promoted another user to admin', 'critical'),
       ('demote_admin', 'User demoted admin to user', 'critical')
