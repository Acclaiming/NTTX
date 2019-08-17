docker run -ti --rm --name ntt \
             -v /root/coolq:/home/user/coolq \
             -p 9000:9000 \
             -p 5700:5700 \
             -p 5701:5701 \
             -e CQHTTP_HOST=127.0.0.1 \
             -e CQHTTP_PORT=5700 \
             -e CQHTTP_USE_HTTP=true \
             -e CQHTTP_WS_HOST=127.0.0.1 \
             -e CQHTTP_WS_PORT=5701 \
             -e CQHTTP_USE_WS=true \
             -e CQHTTP_CONVERT_UNICODE_EMOJI=true
             richardchien/cqhttp:latest