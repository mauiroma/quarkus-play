FROM registry.redhat.io/openjdk/openjdk-8-rhel8
WORKDIR /work/
COPY target/getting-started-1.0-SNAPSHOT-runner.jar /work/
COPY target/lib/* /work/lib/
USER root
RUN chmod 775 /work
EXPOSE 8080
CMD ["java", "-Dquarkus.http.host=0.0.0.0","-jar","/work/getting-started-1.0-SNAPSHOT-runner.jar"]