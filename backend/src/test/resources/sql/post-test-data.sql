-- Integration-test fixtures for devclimb_nexus_test.
-- The script resets only the tables used by the Create Post feature.
-- Cascade is required because other schema tables reference users/community.

TRUNCATE TABLE attachment, post, user_community, community, users
    RESTART IDENTITY CASCADE;

INSERT INTO users (user_email, user_name, user_firebaseuid, user_isadmin, user_isactive, user_createdat)
VALUES
    ('dev1@test.local', 'Dev User 1', 'dev-user-1', false, true, '2026-01-01T00:00:00Z'),
    ('dev2@test.local', 'Dev User 2', 'dev-user-2', false, true, '2026-01-01T00:00:00Z'),
    ('inactive@test.local', 'Inactive User', 'dev-user-inactive', false, false, '2026-01-01T00:00:00Z');

INSERT INTO community (comm_name, comm_shoutout, comm_ispublic, comm_isactive, comm_createdat)
VALUES
    ('Test Public Community', 'Public test community', true, true, '2026-01-01T00:00:00Z'),
    ('Test Private Community', 'Private test community', false, true, '2026-01-01T00:00:00Z'),
    ('Test Inactive Community', 'Inactive test community', true, false, '2026-01-01T00:00:00Z');

INSERT INTO user_community (user_id, comm_id, ucom_joinedat)
SELECT u.user_id, c.comm_id, '2026-01-01T00:00:00Z'
FROM users u, community c
WHERE u.user_firebaseuid = 'dev-user-1'
  AND c.comm_name = 'Test Public Community';

INSERT INTO user_community (user_id, comm_id, ucom_joinedat)
SELECT u.user_id, c.comm_id, '2026-01-01T00:00:00Z'
FROM users u, community c
WHERE u.user_firebaseuid = 'dev-user-2'
  AND c.comm_name = 'Test Private Community';

-- Two top-level posts in the public community, deliberately out of insert order.
INSERT INTO post (user_id, comm_id, post_content, post_createdat)
SELECT user_id, 1, 'Older public post', '2026-01-01T00:00:00Z'
FROM users
WHERE user_firebaseuid = 'dev-user-1';

INSERT INTO post (user_id, comm_id, post_content, post_createdat)
SELECT user_id, 1, 'Newest public post', '2026-01-03T00:00:00Z'
FROM users
WHERE user_firebaseuid = 'dev-user-1';

-- A reply must never appear in the top-level feed.
INSERT INTO post (user_id, comm_id, post_parentid, post_content, post_createdat)
SELECT u.user_id, 1, p.post_id, 'Reply to older post', '2026-01-04T00:00:00Z'
FROM users u, post p
WHERE u.user_firebaseuid = 'dev-user-1'
  AND p.post_content = 'Older public post';

-- One attachment proves the inherited entity/enum mapping serialises correctly.
INSERT INTO attachment (post_id, user_id, atta_url, atta_type, atta_createdat)
SELECT p.post_id, p.user_id, 'https://example.com/attachment.png', 'IMAGE', '2026-01-03T00:05:00Z'
FROM post p
WHERE p.post_content = 'Newest public post';

-- One top-level post in the private community for the member-only GET test.
INSERT INTO post (user_id, comm_id, post_content, post_createdat)
SELECT user_id, 2, 'Private member post', '2026-01-02T00:00:00Z'
FROM users
WHERE user_firebaseuid = 'dev-user-2';
