Fragment {

onGroupMsg = function (user,msg,superGroup)

if msg:chatId() == "-1001400255262" and user.id == 107550100 then

-- 你群收到Github更新通知啦

msg:send("收到更新 ( - _ - )"):send()

end

end

}:install()