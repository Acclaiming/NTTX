bind {
"io.kurumi.ntt.*",
"io.kurumi.ntt.model.*",
"io.kurumi.ntt.model.request.*",
"io.kurumi.ntt.twitter.*",
}

function print(msg)

Send.new(530055491,msg):exec()

end