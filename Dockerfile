FROM ubuntu:20.04

LABEL name="kih-anvendelse" \
    description="KIH Anvendelse" \
    maintainer="Kvalitets IT"

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
ENV LC_ALL en_US.UTF-8

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y locales \
    && sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen \
    && dpkg-reconfigure --frontend=noninteractive locales \
    && update-locale LANG=en_US.UTF-8

RUN apt update
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y maven git-all

RUN mkdir /kih-anvendelse
# Copy source code here
COPY . /kih-anvendelse

CMD ["bash"]
# Use this file with docker
# >>> docker build -t kih-anvendelse .
# >>> docker run -it kih-anvendelse
# >>> cd /kih-anvendelse
# >>> mvn clean install

