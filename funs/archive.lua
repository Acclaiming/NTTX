-- Twitter 存档

function _get_status_archive(user,msg)

if not #msg:commandParams() == 1 then

msg:send("用法 /archs 推文链接/ID"):exec()

return

end

-- 解析推文ID

local input = msg:commandParams()[1]

if (not isnumber(input) and input:find("twitter.com/") then

input = StrUtil:subAfter(input,"status/",true)

if input:find("?") then -- url后的参数

input = StrUtil:subBefore(input,"?",false)

end

if not isnumber(input) then

msg:send("格式错误...？"):exec()

return

end

-- 查找推文

if StatusArchive.INSTANCE:exists(input) then


end

end

end