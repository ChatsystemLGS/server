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

-- isPublicChannel
SELECT c.type type FROM Channels c WHERE c.id = [channelId];

-- isChannelMember
SELECT EXISTS (SELECT * FROM channelMembers cm WHERE cm.user = [userId] AND cm.channel = [chnnalId]) isMember;

-- checkAuth
SELECT (
    SELECT u.passwordHash FROM Users u WHERE u.emailAddress = [email]
) = [passwordHash] passwordMatches;


-- login
SELECT u.id id, u.emailAddress emailAddress, u.nickname nickname, ur.note note FROM Users u
LEFT JOIN userRelationships ur ON ur.userA=ur.userB
WHERE u.emailAddress=[emailAddress];

-- getPublicGroups
SELECT c.id id, c.type type, c.name name FROM Channels c WHERE c.type = 'PUBLIC_GROUP';

-- joinGroup
SELECT (
    SELECT c.type FROM Channels c WHERE c.id = [channelId]
) = 'PUBLIC_GROUP' isPublicGroup;

INSERT INTO channelMembers(
    user,
    channel
) VALUES (
    [userId],
    [channelId]
);

-- getChannels
SELECT c.id id, c.type type, c.name name FROM Channels c
INNER JOIN channelMembers cm ON cm.channel = c.id
INNER JOIN Users u ON u.id = cm.user
WHERE u.id = [userId];

-- getChannelMembers
SELECT u.id id, u.nickname nickname, ur.note note, ur.type type, cm.isAdmin isAdmin FROM Users u
INNER JOIN channelMembers cm ON cm.user = u.id
LEFT JOIN userRelationships ur ON ur.userB = u.id AND userA = [userId]
WHERE cm.channel = [channelId];

-- getUserById
SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u
INNER JOIN  userRelationships ur ON ur.userB = u.id
WHERE ur.userA = [userA] AND u.id = [userB];

-- getUserByEmail
SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u
INNER JOIN  userRelationships ur ON ur.userB = u.id
WHERE ur.userA = [userA] AND u.emailAddress = [userB];

-- addFriend
INSERT INTO userRelationships (
    userA,
    userB,
    type
) VALUES (
    [userA],
    [userB],
    'FRIEND'
) ON DUPLICATE KEY UPDATE type = 'FRIEND';

-- getFriends
SELECT u.id id, u.nickname nickname, ur.note note, ur.type type FROM Users u
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
    'DM'
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

-- receiveMessages
SELECT m.author author, m.timestamp timestamp, m.data data, m.dataType dataType FROM Messages m
INNER JOIN Channels c ON c.id = m.channel
INNER JOIN channelMembers cm ON cm.channel = c.id
WHERE cm.user = [userId] AND c.id = [channelId]
LIMIT [limit];