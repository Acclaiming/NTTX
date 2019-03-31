local function _get_id(user,msg)

msg:sendTyping()

if msg:isReply() then

if msg:command() == "msgid" then

msg:send(msg:replyTo():messageId()):exec()

else 

msg:send(msg:replyTo():from().id):exec()

end

else

msg:send(msg:chatId()):exec()

end

end

functions["id"] = _get_id
functions["msgid"] = _get_id
