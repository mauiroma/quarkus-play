def target_cluster_flags = ""
def docker_registry = "image-registry.openshift-image-registry.svc:5000"


pipeline {
  agent any
  environment { 
    OCP_API_SERVER='https://api.lab01.gpslab.club:6443'
    MVN_HOME='/usr/local/bin'
  }  
  parameters {
    string(name: 'GIT_URL', description: 'Repository BitBucket', defaultValue: "https://github.com/mauiroma/quarkus-play.git")
    string(name: 'PROJECT_NAME', description: 'Application Name', defaultValue: "quarkus-app")
    string(name: 'PROJECT_TAG', description: 'Application Tag', defaultValue: "1.0")
    string(name: 'OCP_NAMESPACE', description: 'OCP Project Name', defaultValue: "esselunga")
    string(name: 'OCP_CREDENTIAL', description: 'ID OCP salvate in Jenkins', defaultValue: "gpslab-lab01")
  }  
  stages {
    stage('Init') {
      steps {
        script {
          target_cluster_flags = "--server=${OCP_API_SERVER} --namespace=${OCP_NAMESPACE} --insecure-skip-tls-verify"
        }
      }
    }
    stage('Source checkout') {
      steps {
        checkout(
          [$class                           : 'GitSCM', 
          branches: [],
          doGenerateSubmoduleConfigurations: false,
          extensions                       : [],
          submoduleCfg                     : [],
          userRemoteConfigs                : [[url: "${GIT_URL}"]]]
          )
      }
    }
//    stage('Test'){
//      steps{
//        script{
//          sh(
//            script: "${MVN_HOME}/mvn test",
//            returnStdout: true
//            )
//        }
//      }
//    }
    stage('Package') {
      steps {
        script{
          sh(
            script: "${MVN_HOME}/mvn clean package -DskipTests",
            returnStdout: true
            )
        }
      }
    }
    stage('Prepare') {
      steps {
        script{
          sh """
            rm -rf ${WORKSPACE}/target/ocp
            mkdir ${WORKSPACE}/target/ocp   
            cp -R ${WORKSPACE}/target/lib ${WORKSPACE}/target/ocp
            cp -R ${WORKSPACE}/target/*-runner.jar ${WORKSPACE}/target/ocp
          """
        }
      }
    }
    stage('Create Build') {
      steps{
        script{
          withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
            def checkBCExists =
            sh(
              script: "oc get bc/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
              returnStatus:true
            )
            if (checkBCExists == 1) {
              sh(
                script: "oc new-build --name=${PROJECT_NAME} --binary=true -i=java:openjdk-11-ubi8 --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                returnStdout:true
              )
            }
          }
        }
      }      
    }
    stage('Build') {
      steps {
        script {
          withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
            def tagExist =
            sh(
              script: "oc get is ${PROJECT_NAME} -o jsonpath='{.status.tags}' --token=${OCP_SERVICE_TOKEN}  $target_cluster_flags |grep tag:${PROJECT_TAG}",
              returnStatus: true
            )
            if (tagExist == 1) {
              sh """
                oc patch bc ${PROJECT_NAME} --type=json -p='[{"op": "replace", "path": "/spec/output/to/name", "value":"${PROJECT_NAME}:${PROJECT_TAG}"}]' --token=${OCP_SERVICE_TOKEN}  $target_cluster_flags
                oc start-build ${PROJECT_NAME} --from-dir=${WORKSPACE}/target/ocp --follow --token=${OCP_SERVICE_TOKEN} $target_cluster_flags
              """
            }else{
              echo "Image Tag is the same, nothing to do"
            }
          }
        }

      }
    }    
    stage('Deploy') {
      steps{
        script{          
          withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
            def checkDCExists =
            sh(
              script: "oc get dc/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
              returnStatus:true
            )
            if (checkDCExists == 1) {
              sh(
                script: "oc new-app -l app=${PROJECT_NAME} --image-stream=${OCP_NAMESPACE}/${PROJECT_NAME}:${PROJECT_TAG} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                returnStdout:true
              )
            }else{
              sh"""
                oc set image dc/${PROJECT_NAME} ${PROJECT_NAME}=$docker_registry/${OCP_NAMESPACE}/${PROJECT_NAME}:${PROJECT_TAG} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags
              """
            }
          }
        }
      }      
    }
    stage('Expose') {
      steps{
        script{
          withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
            def checkRouteExists =
            sh(
              script: "oc get route/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
              returnStatus:true
            )
            if (checkRouteExists == 1) {
              sh(
                script: "oc expose svc/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                returnStdout:true
              )
            }
          }
        }
      }      
    }
  }    
}