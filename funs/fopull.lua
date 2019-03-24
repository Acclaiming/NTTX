local function _fopull(user,msg)

if not #msg:commandParams() == 1 then

msg:send("用法 /fopull 用户名"):exec()

return

end

if not TAuth:contains(user) then

msg:send("你没有认证Twitter账号"):exec()

return

end

local auth = TAuth:get(user)

msg:send(tostring(auth.accToken)):exec()

local api = auth:createApi()

local allfo = TApi:getAllFo(api,msg:commandParams()[1])

local content = ""
local index = 0

for index,fo in ipairs(allfo) do

content = content .. TApi:formatUserNameMarkdown(fo) .. "\n"

if index < 20 then

index = index + 1

else 

msg:send(content):markdown():exec()

content = ""

index = 0

end

end

end

functions["fopull"] = _fopull