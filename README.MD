# NTTools

一个Twitter盒子

### 环境

jdk >= 7 、 git

yum install java java-devel git

### 运行

git clone https://github.com/HiedaNaKan/NTTools

chmod 777 start.sh && ./start.sh

注 :  每次都会自动更新编译启动

### 系统服务

注 :  使用service 必须将项目clone在 /bot 或者请手动修改ntt.service

另 :  必须手动启动 ./start.sh 并输入BotToken之后才可以使用service启动

cp ntt.service /etc/systemd/system/ntt.service # 创建服务

systemctl daemon-reload # 重载服务

systemctl start ntt # 启动服务

systemctl enable ntt # 开机启动

syatemctl status ntt -l # 查看日志