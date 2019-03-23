function _encode(user,msg)

msg:sendUpdatingPhoto()

local content = ""

if #msg:commandParams() == 0 then

msg:send("用法 : /qr 内容"):exec()

return

end

for i,v in ipairs(msg:commandParams()) do

content = content .. tostring(v)

end

local png = QrCodeUtil:generatePng(content,500,500)

Launcher.INSTANCE:bot():execute(SendPhoto.new(tostring(msg:chatId()),png))

end

function _decode(user,msg)

msg:sendTyping()

if not msg:isReply() then

msg:send("用法  : 对单张图片回复 /qrdec"):exec()

return

end

local size = msg:replyTo():photoSize()

if size == 0 then

msg:send("请对图片回复 (ﾟ⊿ﾟ)ﾂ"):exec()

return

else

local status,content = pcall(function ()

return QrCodeUtil:decode(msg:replyTo():photo(0))

end)

if status then

msg:reply("解析成功 *٩(๑´∀`๑)ง*  : \n" , tostring(content)):exec()

else

msg:reply("解析失败 Σ( ° △ °|||)︴"):exec()

end

end

end

functions["qr"] = _encode
functions["qrdec"] = _decode