./mvnw clean package -Dquarkus.package.type=uber-jar
mkdir target/ocp   
cp -R target/*-runner.jar target/ocp
oc start-build quarkus-play --from-dir=./target/ocp -n anas --follow

