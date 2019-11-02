FROM alpine

RUN apk add bash git && \
    git clone https://github.com/sstephenson/bats.git && \
    cd bats && \
    ./install.sh /usr/local && \
    cd .. && \
    rm -rf bats && \
    git clone https://github.com/capitalone/bash_shell_mock.git && \
    cd bash_shell_mock && \
    ./install.sh /usr/local && \
    cd .. && \
    rm -rf bash_shell_mock

COPY tests.bats /opt/
COPY tools /opt/tools/

WORKDIR /opt

CMD ["bats", "tests.bats"]

