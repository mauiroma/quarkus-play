# Quarkus play

```
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