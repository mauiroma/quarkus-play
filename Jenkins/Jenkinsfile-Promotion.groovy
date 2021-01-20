  def target_cluster_flags = ""
  def docker_registry = "image-registry.openshift-image-registry.svc:5000"
  def runApplicationStages=true
  def runOcpStages=true
  //oc delete all -l app=quarkus-app
  //oc delete all -l build=quarkus-app
  pipeline {
    agent any
    environment { 
      OCP_API_SERVER='https://api.lab01.gpslab.club:6443'
      MVN_HOME='/usr/local/bin'
    }  
    parameters {
      string(name: 'PROJECT_NAME', description: 'Application Name', defaultValue: "quarkus-app")
      string(name: 'PROJECT_TAG', description: 'Application Tag', defaultValue: "1.0")
      string(name: 'OCP_NAMESPACE', description: 'OCP Project Name', defaultValue: "esselunga-uat")
      string(name: 'OCP_NAMESPACE_SOURCE', description: 'OCP Project Name', defaultValue: "esselunga")
      string(name: 'OCP_CREDENTIAL', description: 'ID OCP salvate in Jenkins', defaultValue: "gpslab-lab01")
    }  
    stages {
      stage('Init') {
        steps {
          script {
            target_cluster_flags = "--server=${OCP_API_SERVER} --namespace=${OCP_NAMESPACE} --insecure-skip-tls-verify"
            withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
              def currentDeployedImage =
                  sh(
                      script: "oc get dc ${PROJECT_NAME} -o jsonpath='{.spec.template.spec.containers[0].image}' --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                      returnStdout: true
                  )
              if (currentDeployedImage.equalsIgnoreCase("$docker_registry/${OCP_NAMESPACE}/${PROJECT_NAME}:${PROJECT_TAG}")) {
                  currentBuild.result = 'ABORTED'
                  echo "DeploymentConfit ${PROJECT_NAME} whit image tag ${PROJECT_TAG} already active, skip application and OCP stages"
                  runApplicationStages = false
                  runOcpStages = false
              }else{
                def imageStream =
                sh(
                  script: "oc get is ${PROJECT_NAME} -o yaml --ignore-not-found=true --token=${OCP_SERVICE_TOKEN}  $target_cluster_flags",
                  returnStdout: true
                )
                if (imageStream.size()>0 && imageStream.contains("tag: \"${PROJECT_TAG}\"")) {            
                  echo "ImageStream ${PROJECT_NAME} with Tag ${PROJECT_TAG} already present, skip application's stages"
                  runApplicationStages = false
                }
              }
            }           
          }
        }
      }  
      stage('OCP'){
        when{
          expression {runOcpStages == true}
        }         
        stages{
          stage('Tag Image') {
            steps{
              script{
                withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
                  def imageStream =
                  sh(
                    script: "oc get is ${PROJECT_NAME} -o yaml --ignore-not-found=true --token=${OCP_SERVICE_TOKEN}  $target_cluster_flags",
                    returnStdout: true
                  )
                  if (imageStream.size()>0 && imageStream.contains("tag: \"${PROJECT_TAG}\"")) {                    
                    echo "Image with version ${PROJECT_TAG} already present, will be restored"
                  }else{
                    sh "oc tag ${OCP_NAMESPACE_SOURCE}/${PROJECT_NAME}:${PROJECT_TAG} ${OCP_NAMESPACE}/${PROJECT_NAME}:${PROJECT_TAG} --token=${OCP_SERVICE_TOKEN}  $target_cluster_flags"
                  }
                }
              }
            }
          }
          stage('Deploy') {
            steps{
              script{          
                withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
                  def isDCExists =
                  sh( 
                    script: "oc get dc/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                    returnStatus:true
                  )
                  if (isDCExists == 1) {
                    sh"""
                      oc new-app ${PROJECT_NAME} --as-deployment-config -l app=${PROJECT_NAME} --allow-missing-images --token=${OCP_SERVICE_TOKEN} $target_cluster_flags
                      oc set image dc/${PROJECT_NAME} ${PROJECT_NAME}=$docker_registry/${OCP_NAMESPACE}/${PROJECT_NAME}:${PROJECT_TAG} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags
                    """
                  }else{
                    sh("oc set image dc/${PROJECT_NAME} ${PROJECT_NAME}=$docker_registry/${OCP_NAMESPACE}/${PROJECT_NAME}:${PROJECT_TAG} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags")
                  }
                }
              }
            }      
          }
          stage('Create Service') {
            steps{
              script{
                withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
                  def isServiceExists =
                  sh(
                    script: "oc get svc/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                    returnStatus:true
                  )
                  if (isServiceExists == 1) {
                    sh("oc create service clusterip ${PROJECT_NAME} --tcp=8080:8080 --token=${OCP_SERVICE_TOKEN} $target_cluster_flags")
                  }
                }
              }
            }      
          }
          stage('Create Route') {
            steps{
              script{
                withCredentials([string(credentialsId: "${OCP_CREDENTIAL}", variable: 'OCP_SERVICE_TOKEN')]) {
                  def isRouteExists =
                  sh(
                    script: "oc get route/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags",
                    returnStatus:true
                  )
                  if (isRouteExists == 1) {
                    sh("oc expose svc/${PROJECT_NAME} --token=${OCP_SERVICE_TOKEN} $target_cluster_flags")
                  }
                }
              }
            }      
          }    
        }
      }
    }    
  }