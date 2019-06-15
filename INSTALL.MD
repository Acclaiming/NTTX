以 Centos7 为例部署NTT :

实际上是没用过其他系统 (小声

### 下载

```
git clone https://github.com/HiedaNaKan/NTTools /usr/local/ntt

cd /usr/local/ntt
```

### 配置Nginx

```
yum install -y nginx

cp ntt.conf /etc/nginx/conf.d/ntt.conf

# 此处需要配置好SSL证书 可使用 acme.sh 自动生成/更新

service nginx restart

```

### 安装 epel 源

```
yum install -y epel-release
```

### 安装 Git 与 OpenJDK8

```
yum install -y git java java-devel
```

### 安装 FFMpeg

```
rpm -v --import http://li.nux.ro/download/nux/RPM-GPG-KEY-nux.ro
rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm

yum install -y ffmpeg ffmpeg-devel
```

### 安装 MongoDB

```
vim /etc/yum.repos.d/mongodb-org-4.0.repo
```

```
[mongodb-org-4.0]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/4.0/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-4.0.asc
```

```
yum install -y mongodb-org
```

### 从备份还原

```
# 将NTT导出的备份ZIP文件放置于数据目录下

cd /etc/ntt

unzip data.zip

mongodump -d NTTools -o /db/NTTools

rm -rf /db

```

### 安装启动

```
bash rebuild.sh && bash start.sh
```

输入 本地HTTP服务器端口 需与nginx配置中反向代理的端口保持一致
输入 Nginx配置中的反向代理的域名
输入 BotToken 完成初始化
完成后结束程序 配置系统服务

### 配置服务

```
/bin/cp -rf ntt.service /etc/systemd/system/ntt.service

#重载系统服务

systemctl daemon-reload

# 开机启动

systemctl enable ntt

# 更新并立即滚动日志

service ntt restart && journalctl -u ntt -f
```