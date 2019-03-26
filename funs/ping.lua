functions["ping"] =function (user,msg)

msg:sendTyping()

local start = System:currentTimeMillis()

local sended = msg:send("pong！"):send()

local stop = System:currentTimeMillis()

sended:edit("pong！","time : " .. (stop - start) .. "ms"):exec()

end