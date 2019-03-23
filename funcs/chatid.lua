local function _get_chat_id(user,msg)

msg:send(msg:chatId()):exec()

end

functions["chatid"] = _get_chat_id