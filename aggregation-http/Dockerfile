FROM anapsix/alpine-java:jre8
MAINTAINER David Aparicio "david.aparicio@free.fr"

ARG MODULE
ARG MOD
ARG VERSION=1.2
ARG PORT=8080

ENV USERNAME duser
ENV HOME /home/$USERNAME
ENV SHELL /bin/sh

ENV JAR aggregation-http-${MOD}-${VERSION}-SNAPSHOT-fat.jar

# Don't be root.
RUN adduser -h ${HOME} -D -s ${SHELL} -S ${USERNAME}
USER ${USERNAME}
WORKDIR ${HOME}

# Expose remote access ports.
EXPOSE ${PORT}

COPY ./${MODULE}/src/main/config/config.json  .
COPY ./etc .
COPY ./${MODULE}/target/${JAR} .

ENTRYPOINT ["sh", "-c"]
CMD ["java -Djava.net.preferIPv4Stack=true -jar ${JAR} -cluster -cp ./etc -conf config.json"]
