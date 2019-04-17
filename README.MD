# NTTools

一个Twitter盒子

### 环境

JDK >= 7 、 Git 、MongoDB

### 运行

git clone https://github.com/HiedaNaKan/NTTools /usr/local/ntt/bin

cd /usr/local/ntt/bin

bash start.sh

注 :  每次都会自动更新编译启动

### 系统服务

注 :  使用service 必须将项目clone在 /usr/local/ntt/bin 或者请手动修改ntt.service

另 :  必须手动启动 start.sh 并配置之后才可以使用service启动

cp /usr/local/ntt/bin/ntt.service /etc/systemd/system/ntt.service # 创建服务

systemctl daemon-reload # 重载服务

systemctl start ntt # 启动服务

systemctl enable ntt # 开机启动

journalctl -u ntt # 查看日志

journalctl -u ntt -f # 实时日志

# service ntt restart && journalctl -u ntt -f # 更新并立即滚动日志
