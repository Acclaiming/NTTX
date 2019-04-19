docker run -ti --name cqhttp-ntt \
             -v /usr/local/ntt/coolq:/home/user/coolq \
             -p 11452:9000 \
             -p 11212:11212 \
             -e COOLQ_ACCOUNT=198197419 \
             -e COOLQ_USE_HTTP=false \
             -e COOLQ_USE_WS=true \
             -e COOLQ_WS_HOST="127.0.0.1" \
             -e COOLQ_WS_PORT=11212 \
             -e CQHTTP_SERVE_DATA_FILES=yes \
             richardchien/cqhttp:latest