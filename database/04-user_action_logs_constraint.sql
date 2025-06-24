-- 1. If you’re re-running migrations, you may want to drop any old versions first
DROP TRIGGER IF EXISTS trg_validate_user_action_log
    ON user_action_logs;

DROP FUNCTION IF EXISTS validate_user_action_log();

-- 2. Create the PL/pgSQL validation function
CREATE OR REPLACE FUNCTION validate_user_action_log()
    RETURNS trigger
  LANGUAGE plpgsql
AS $$
DECLARE
    tmpl    text;
    key     text;
    vars    text[];
BEGIN
    SELECT message_template
    INTO tmpl
    FROM action_types
    WHERE name = NEW.action_type;

    SELECT ARRAY(
               SELECT DISTINCT (regexp_matches(tmpl, '\$\{([^}]+)\}', 'g'))[1]
  )
    INTO vars;

    FOREACH key IN ARRAY vars
    LOOP
        CASE key
        WHEN 'user' THEN
        NULL;  -- user_id is already NOT NULL

      WHEN 'affected_user' THEN
        IF NEW.affected_user_id IS NULL THEN
            RAISE EXCEPTION
            'action_type "%": requires non-null affected_user_id for ${affected_user}',
            NEW.action_type;
        END IF;

        WHEN 'server_instance' THEN
        IF NEW.instance_id IS NULL THEN
            RAISE EXCEPTION
            'action_type "%": requires non-null instance_id for ${server_instance}',
            NEW.action_type;
        END IF;

        ELSE
        IF NEW.metadata IS NULL
            OR NOT (NEW.metadata ? key) THEN
            RAISE EXCEPTION
            'action_type "%": metadata must contain key "%" for ${%}',
            NEW.action_type, key, key;
        END IF;
    END CASE;
END LOOP;

RETURN NEW;
END;
$$;

-- 3. Attach it as a trigger on your table
CREATE TRIGGER trg_validate_user_action_log
    BEFORE INSERT OR UPDATE
    ON user_action_logs
    FOR EACH ROW
EXECUTE FUNCTION validate_user_action_log();
