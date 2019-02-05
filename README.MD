# NTTools

一个Twitter盒子

### 环境

jdk >= 7

yum install java // java 安装  
yum install java-devel // jdk安装

git

yum install git

### 下载并编译

git clone https://github.com/HiedaNaKan/NTTools

cd ./NTTools

chmod 777 *.sh

./build.sh

### 运行

./start.sh

### 更新并重新编译

./update.sh

### 注意

因为不会powershell win的脚本暂不可用


### Run on Android

app_process -Djava.class.path=<DEX绝对路径> /data/local/tmp io.kurumi.nttools.Launcher
