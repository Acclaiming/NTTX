function _ping(user,msg)

local start = System:currentTimeMillis()

local sended = msg:send("pong！"):send()

local stop = System:currentTimeMillis()

sended:edit("pong！","time : " .. (stop - start) .. "ms"):exec()

end

functions["ping"] = _ping