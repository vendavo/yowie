FROM vtajzich/mesos-java8-docker:0.22.1
MAINTAINER Vitek Tajzich <v.tajzich@vendavo.com>
RUN mkdir /home/yowie
ADD build/distributions/yowie-framework-0.1.0-SNAPSHOT.tar /home/yowie/

EXPOSE 8080

CMD /home/yowie/yowie-framework-0.1.0-SNAPSHOT/bin/yowie-framework
