INSERT INTO action_types (name, message, severity)
VALUES ('change_allocated_ram', '${user} changed the server RAM allocation from ${from} to ${new}', 'warning'),
       ('edit_config_file', '${user} edited a server config file', 'info'),
       ('upload_file', '${user} uploaded a file to the server', 'info'),
       ('execute_command', '${user} executed a console command: ${command} on ${server_instance}', 'info'),
       ('assign_role', '${user} assigned user ${affected_user} as a ${role} on ${server_instance}', 'warning'),
       ('promote_admin', '${user} promoted  ${affected_user} to admin', 'critical'),
       ('demote_admin', '${user} demoted admin ${affected_user} to user', 'critical'),
       ('user_changed_username', '${user} changed their username from ${old_username} to ${new username}', 'info')
