local System = luajava.bindClass "java.lang.System"

local function _ping(user,msg)

local start = System:currentTimeMillis()

local pingMsg = msg:send({"pong！"}):send()

local stop = System:currentTimeMillis()

pingMsg:edit({"pong！","time : " .. stop - start .. "ms"}):exec()

end

functions["ping"] = _ping