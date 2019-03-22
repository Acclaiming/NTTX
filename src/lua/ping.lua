local System = luajava.bindClass "java.lng.System"

local function _ping(user,msg)

local start = System:currentTimeMillis()

local pingMsg = msg:send("pong！").sync()

local stop = System:currentTimeMillis()

pingMsg:edit("pong！","time : " .. stop - start .. "ms").exec()

end

functions["ping"] = _ping