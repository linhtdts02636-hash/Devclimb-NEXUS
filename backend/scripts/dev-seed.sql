-- DevClimb Nexus - local development seed
-- This file is for local development only. It does not create posts or attachments.

INSERT INTO users (user_email, user_name, user_firebaseuid, user_isadmin, user_isactive)
VALUES ('dev1@devclimb.local', 'Dev User 1', 'dev-user-1', false, true)
ON CONFLICT (user_firebaseuid) DO UPDATE
SET user_name = EXCLUDED.user_name,
    user_isactive = true;

INSERT INTO users (user_email, user_name, user_firebaseuid, user_isadmin, user_isactive)
VALUES ('dev2@devclimb.local', 'Dev User 2', 'dev-user-2', false, true)
ON CONFLICT (user_firebaseuid) DO UPDATE
SET user_name = EXCLUDED.user_name,
    user_isactive = true;

INSERT INTO community (comm_name, comm_shoutout, comm_ispublic, comm_isactive)
VALUES ('Dev Public Community', 'Community for local development', true, true)
ON CONFLICT (comm_name) DO UPDATE
SET comm_ispublic = true,
    comm_isactive = true;

INSERT INTO community (comm_name, comm_shoutout, comm_ispublic, comm_isactive)
VALUES ('Dev Private Community', 'Private community for local development', false, true)
ON CONFLICT (comm_name) DO UPDATE
SET comm_ispublic = false,
    comm_isactive = true;

INSERT INTO user_community (user_id, comm_id)
SELECT u.user_id, c.comm_id
FROM users u, community c
WHERE u.user_firebaseuid = 'dev-user-1'
  AND c.comm_name = 'Dev Public Community'
ON CONFLICT DO NOTHING;

INSERT INTO user_community (user_id, comm_id)
SELECT u.user_id, c.comm_id
FROM users u, community c
WHERE u.user_firebaseuid = 'dev-user-2'
  AND c.comm_name = 'Dev Private Community'
ON CONFLICT DO NOTHING;
