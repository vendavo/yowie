FROM vtajzich/mesos-java8-ubuntu:0.21.1
MAINTAINER Vitek Tajzich <v.tajzich@vendavo.com>
RUN mkdir /home/yowie
ADD build/distributions/yowie-framework-0.1.0-SNAPSHOT.tar /home/yowie/

ENV MESOS_NATIVE_JAVA_LIBRARY=$MESOS_NATIVE_LIBRARY

EXPOSE 8080

CMD /home/yowie/yowie-framework-0.1.0-SNAPSHOT/bin/yowie-framework
