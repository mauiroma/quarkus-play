# Quarkus play

# Run Application
```
./mvnw compile quarkus:dev
```

# Jagger Integration
The integration with Jagger has been devined into [application.properties](src/main/resources/application.properties)
https://quarkus.io/guides/opentracing
```
docker run -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 jaegertracing/all-in-one:latest
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 14250:14250 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.21
```

# Deploy on OCP
## Deploy Manually
```
oc new-project quarkus-play
mvn clean package
mkdir target/ocp   
cp -R target/lib target/ocp
cp -R target/*-runner.jar target/ocp

oc new-build --name=quarkus-play --binary=true -i=java:openjdk-11-ubi8
oc start-build quarkus-play --from-dir=./target/ocp --follow
oc new-app quarkus-play
oc expose svc/quarkus-play

export URL="http://$(oc get route quarkus-play -o jsonpath='{.spec.host}')"
echo "Application URL: $URL"

curl $URL/hello
curl $URL/hello/greeting/mauiroma
```
## Deploy With Quarkus Openshift Extension
Refer to [application.properties](src/main/resources/application.properties) for default configuration
https://quarkus.io/guides/all-config
```
mvn clean package -Dquarkus.profile=dev -DskipTests
```
## Deploy as a Serverless
```
oc appy -f knative/service.yaml
```
## Deploy with Helm
```
cd helm
helm create quarkus-play

```



oc create configmap app-variable --from-file=configmap/app-variable.properties
