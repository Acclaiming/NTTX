scl enable devtoolset-3 bash

cmake3 -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON ..

cmake3 --build . --target install

cmake3 -DCMAKE_BUILD_TYPE=Release -DTd_DIR=/root/td/jnibuild/example/java/td/lib/cmake/Td -DCMAKE_INSTALL_PREFIX:PATH=.. ..