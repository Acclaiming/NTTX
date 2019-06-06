/bin/cp -rf ntt.service /etc/systemd/system/ntt.service && systemctl daemon-reload

bash ./rebuild.sh && bash ./start.sh