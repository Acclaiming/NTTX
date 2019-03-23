function _encode(user,msg)

msg:sendUpdatingPhoto()

local content = ""

for i,v in ipairs(msg:commandParams()) do

content = content .. tostring(v)

end

local png = QrCodeUtil:generatePng(content,500,500)

Launcher.INSTANCE:bot():execute(SendPhoto.new(tostring(msg:chatId()),png))

end

function _decode(user,msg)

if not msg:isReply() then

msg:send("用法  : 对单张图片回复 /qrdec"):exec()

return

end

local size = msg:replyTo():photoSize()

if size == 0 then

msg:send("请对图片回复 (ﾟ⊿ﾟ)ﾂ"):exec()

return

else

local content = QrCodeUtil:decode(msg:replyTo():photo(0))

if content then

msg:reply("解析成功 *٩(๑´∀`๑)ง*  : " .. tostring(content)):exec()

else

msg:reply("解析失败 Σ( ° △ °|||)︴"):exec()

end

end

end

end

functions["qr"] = _encode
functions["qrdec"] = _decode