-- KEYS:
-- [1] userIdKey
-- [2] cellIdExpiriesKey
-- [3] cellIdUsersKey

-- ARGV:
-- [1] newCellId
-- [2] encodedUserId
-- [3] TTL_SECONDS
-- [4] expireAt

local newCellId = ARGV[1]
local encodedUserId = ARGV[2]
local ttl_seconds = tonumber(ARGV[3])
local expireAt = tonumber(ARGV[4])
local cellIdNotInTargetAreaFlag = tonumber(ARGV[5])

local prevCellId = redis.call('GET', KEYS[1])

if prevCellId then
    if prevCellId == newCellId then
        redis.call('EXPIRE', KEYS[2], ttl_seconds)
        return
    end

    local oldUsersKey = "cell:" .. prevCellId .. ":users"
    redis.call('SREM', oldUsersKey, encodedUserId)
end

if cellIdNotInTargetAreaFlag == 1 then
    return
end

redis.call('SET', KEYS[1], newCellId, 'EX', ttl_seconds)

redis.call('SADD', KEYS[3], encodedUserId)

redis.call('ZADD', KEYS[2], expireAt, encodedUserId)