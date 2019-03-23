function _encode(user,msg)

msg:sendUpdatingPhoto()

local png = QrCodeUtil:generatePng(StrUtil:subAfter(msg:text(),"/",true),500,500)

Launcher.INSTANCE:bot():execute(SendPhoto.new(tostring(msg:chatId()),png))

end

function _decode(user,msg)

if not msg:isReply() then

msg:send("用法  : 对单张过多张图片回复 /qrdec"):exec()

return

end

local size = msg:photoSize()

if size == 0 then

msg:send("请对图片回复 (ﾟ⊿ﾟ)ﾂ"):exec()

return

else if size == 1 then

local content = QrCodeUtil:decode(msg:photo(0))

if content then

msg:reply("解析成功 *٩(๑´∀`๑)ง*  : ",content):exec()

else

msg:reply("解析失败 Σ( ° △ °|||)︴"):exec()

end

else

-- 多图片

local result = ""

for index=0,size,1 do

local content = QrCodeUtil:decode(msg:photo(index))

result = result .. "图片" .. (index + 1) .. " 解析"

if content then

result = result .. "成功 *٩(๑´∀`๑)ง*  : " .. contnent

else

result = result .. "失败 Σ( ° △ °|||)︴"

end

result = result .. "\n"

end

msg:reply(result):exec()

end

end

end

functions["qr"] = _encode
functions["qrdec"] = _decode