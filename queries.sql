-- addUser
INSERT INTO Users (
    emailAddress,
    nickname,
    passwordHash
) VALUES (
    [email],
    [username],
    [passwordHash]
);
SELECT LAST_INSERT_ID();

-- checkAuth
SELECT (
    SELECT u.passwordHash FROM Users u WHERE u.emailAddress = [email]
) = [passwordHash] passwordMatches;

-- getPublicGroups
SELECT c.id, c.type, c.name FROM Channels c WHERE c.type = "publicGroup";

-- joinGroup
SELECT (
    SELECT c.type FROM Channels c WHERE c.id = [channelId]
) = "publicGroup" isPublicGroup;

INSERT INTO ChannelMembers(
    user,
    channel
) VALUES (
    [userId],
    [channelId]
);
SELECT LAST_INSERT_ID();

-- getChannels
SELECT c.id, c.type, c.name FROM Channels c
INNER JOIN channelMembers cm ON cm.channel = c.id
INNER JOIN Users u ON u.id = cm.user
WHERE u.id = [userId];

-- getChannelMembers
SELECT u.id, u.nickname, ur.note, ur.type, cm.isAdmin FROM Users u
INNER JOIN channelMembers cm ON cm.user = u.id
INNER JOIN userRelationships ur ON ur.userB = u.id
WHERE c.id = [channelId] AND ur.userA = [userId];

-- getUser
SELECT u.id, u.nickname, ur.note, ur.type FROM Users u
INNER JOIN  userRelationships ur ON ur.userB = u.id
WHERE ur.userA = [userA] AND u.id = [userB];

-- addFriend
INSERT INTO userRelationships (
    userA,
    userB,
    type
) VALUES (
    [userA],
    [userB],
    "friend"
) ON DUPLICATE KEY UPDATE type = "friend";

-- getFriends
SELECT u.id, u.nickname, ur.note, ur.type FROM Users u
INNER JOIN userRelationships ur ON ur.userB = u.id
WHERE ur.userA = [userId];

-- sendMessage
INSERT INTO Messages (
    channel,
    author,
    timestamp,
    data,
    dataType
) VALUES (
    [channelId],
    [userId],
    [timestamp],
    [data],
    [dataType]
);

-- createDm
INSERT INTO Channels (
    type
) VALUES (
    "dm"
);
SELECT LAST_INSERT_ID();
INSERT INTO channelMembers (
    user,
    channel
) VALUES (
    [userA],
    [channelId]
), (
    [userB],
    [channelId]
)

-- receiveMessage
SELECT m.channel, m.author, m.timestamp, m.data, m.dataType FROM Messages m
INNER JOIN Channels c ON c.id = m.channel
INNER JOIN channelMembers cm ON cm.channel = c.id
WHERE cm.user = [userId];