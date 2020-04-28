FROM openjdk:8-jdk-slim

# Build odo
COPY . /odo-source
WORKDIR /odo-source
RUN ./gradlew bootWar && \
	mkdir ../odo && \
	mv /odo-source/proxyui/build/libs/* ../odo &&\
	rm -r /odo-source

# Prepare for running
EXPOSE 8090 8082 8012 9090 9092
WORKDIR /odo
ENTRYPOINT java -Xmx1024m -jar `ls | head -n 1`
