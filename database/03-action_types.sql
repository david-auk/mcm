INSERT INTO action_types (name, message_template, severity)
VALUES
    -- User operations
    ('user_changed_username', '${user} changed their username from ${old_username} to ${new_username}', 'info'),
    ('user_changed_password', '${user} changed their password', 'info'),

    -- Operator operations
    ('execute_command', '${user} executed a console command: ${command} on ${server_instance}', 'info'),
    ('start_server', '${user} started server: ${server_instance}', 'info'),
    ('stop_server', '${user} executed a console command: ${command} on ${server_instance}', 'info'),

    -- Editor operations
    ('edit_property', '${user} edited a property: ${property_name} ${old_value} -> ${new_value} for ${server_instance}', 'info'),
    ('upload_file', '${user} uploaded file ${filename} on ${server_instance}', 'info'),
    ('change_file', '${user} changed file ${filename} on ${server_instance}', 'info'),
    ('delete_file', '${user} deleted file ${filename} on ${server_instance}', 'warning'),

    -- Maintainer operations
    ('change_allocated_ram', '${user} changed the server RAM allocation from ${from} to ${new}', 'warning'),

    -- Admin operations
    ('assign_role', '${user} assigned user ${affected_user} as a ${role} on ${server_instance}', 'warning'),
    ('user_promote', '${user} promoted  ${affected_user} to admin', 'critical'),
    ('admin_demote', '${user} demoted admin ${affected_user} to user', 'critical'),

        -- User management
        ('user_create', '${user} created a new user: ${affected_user}', 'info'),
        ('user_update', '${user} updated ${updated_field}: ${old_value} -> ${new_value} of user ${affected_user}', 'warning'),
        ('user_delete', '${user} deleted user with username ${deleted_user_username}', 'critical')
