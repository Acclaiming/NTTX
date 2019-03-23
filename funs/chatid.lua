local function _get_id(user,msg)

if msg:isReply() then

msg:send(msg:replyTo():msgId()):exec()

else

msg:send(msg:chatId()):exec()

end

end

functions["id"] = _get_id