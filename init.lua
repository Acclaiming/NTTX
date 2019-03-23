bind {
"io.kurumi.ntt.*",
"io.kurumi.ntt.model.*",
"io.kurumi.ntt.model.request.*",
"io.kurumi.ntt.twitter.*",
}

function send(chat_id,msg)

return Send.new(Launcher.INSTANCE,chat_id,msg)

end

function print(msg)

send("530055491",msg):exec()

end

require "funs/chatid"