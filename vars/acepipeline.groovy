def step_build(){
  node('master') {
      try {
          println (">> before checkout")
          checkout scm
          println (">> after checkout")

          wrap([$class: 'Xvfb', additionalOptions: '', assignedLabels: '', autoDisplayName: true, installationName: 'Xvfb', screen: '']) {
          
            sh ''' echo "BUILD_FOLDER *********" + ${BUILD_FOLDER}
                  . ${ACE_INSTALL_DIR}/server/bin/mqsiprofile
                  pwd
                  echo "App name"+${APP_NAME}
                  mqsicreatebar -data . -b ${APP_NAME}.bar -a ${APP_NAME} -skipWSErrorCheck
                  cp ${APP_NAME}.bar ${BUILD_FOLDER}'''
              
          }
      }
      catch(error) {
        println ">> build failed"
        throw error
      }
    }
}

def step_run_ta() {
    node() {
      try {
        //unstash 'barFileComponent'
        println (">> running TA  ${BUILD_NO} <<")
        
        sh '''
            . ${ACE_INSTALL_DIR}/server/bin/mqsiprofile
            mkdir -p /home/ucp4i/play/ta-dir/${APP_NAME}
            export TADataCollectorDirectory=/home/ucp4i/play/ta-dir/${APP_NAME}
            ${ACE_INSTALL_DIR}/server/bin/TADataCollector.sh ace run /home/ucp4i/play/one-click-builds/${APP_NAME}.bar
            '''
         
        println (">> custom image pushed to registry <<")
        
      }
      catch(error) {
        println " failure to push "
        throw error
      }
    
    }
}

/*def step_createDockerImage() {
    node() {
      try {
        unstash 'barFileComponent'
        println (">> creating custom image  ${BUILD_NO} <<")
        
        sh ''' 
            docker login ${DOCKER_REGISTRY_URL} --username serviceaccount --password eyJhbGciOiJSUzI1NiIsImtpZCI6ImpxYThnRmc1anhWMC16ZnJIYjJ5dTVueW83Q2twekRoTURlLWItZm81cE0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJjcDRpIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImplbmtpbnMtdG9rZW4tN213NTYiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiamVua2lucyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjdiMTM1ZTY4LTExNDktNDViOS05YzM4LTlhODUxZDg3ZGMwNCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpjcDRpOmplbmtpbnMifQ.i6eq-8gvyS21pe_BHlQ6I5aZ6s0oj5TI4wVt22wQ6PhyIjF4KA8RceWRVlMocq9fkimawIKVdG3wziBymDdWJFLKgajILNa28cCci69OnoTszIS7HLitVQI-lsKjwoeR8AalGuatC9eH_vQV4dorQZSBrbhYsl9cFTL9Xpj_GJ2MaPC1PuHDeNxJKCXODveCfoTneyIhENgrrsGyqX-0fo42sKKWL95Xt4O8olnN0DiHqEUwV5FI721u1_s0SeSRe5rn5CFJHZBFIRco9kwuBpOW7azqCU-5sx-DdPQnU_G09JSUWbnLZMn-trWegkCJBXpXzcIc2qOJhpwAC6589g
            docker build -t barimage:${BUILD_NO} -f- . <<EOF
FROM ${DOCKER_REGISTRY_URL}/ace/ibm-ace-server-prod:11.0.0.7-r1-amd64
COPY *.bar /home/aceuser/bar/*
RUN ace_compile_bars.sh 
EOF
            docker tag barimage:${BUILD_NO} ${DOCKER_REGISTRY_URL}/ace/barimage:${BUILD_NO}-amd64
            docker push ${DOCKER_REGISTRY_URL}/ace/barimage:${BUILD_NO}-amd64

            docker rmi barimage:${BUILD_NO}
            '''
         
        println (">> custom image pushed to registry <<")
        
      }
      catch(error) {
        println " failure to push "
        throw error
      }
    
    }
}

def step_deployImage() {
    node() {
      try {
        
      sh '''
      oc login ${OPEN_SHIFT_URL} --token=eyJhbGciOiJSUzI1NiIsImtpZCI6ImpxYThnRmc1anhWMC16ZnJIYjJ5dTVueW83Q2twekRoTURlLWItZm81cE0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJjcDRpIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImplbmtpbnMtdG9rZW4tOWNnbXMiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiamVua2lucyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjdiMTM1ZTY4LTExNDktNDViOS05YzM4LTlhODUxZDg3ZGMwNCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpjcDRpOmplbmtpbnMifQ.Cvkz927rT_Iux_SSEBr30uomT_wN9z1jb5JFakuw0VsQR35OjP3h7_EV06xSjxyfAIo7zrChNi_DvucEHci2OcxDjk9KtcgKdiTZbVYKkPdkVvFQR_r2Tl-EdcMic4zQ9yfrJSZRLKzDHtsbO4Feji78W9_tb6TTloHU9rmNAYWM9Iy7iiCuA1zX6gEr4ypL2pANphrQTBEm2PXCmbUrXOtorfKiw0WmjIQtTTX2PcKvpcuMa3UiNEiWqCcKeMadn7vXK_rP8k40dLtEdqT9WehjaD3T0-yojoFNNcwUPbUQ281hbBxeU79YmYBaEKnTubkOBQkG1giTfZZe511gpQ
      cd /opt/certs
      helm init --client-only
      helm repo add ibm-entitled-charts https://raw.githubusercontent.com/IBM/charts/master/repo/entitled/
      helm install --name ${RELEASE_NAME} ibm-entitled-charts/ibm-ace-server-icp4i-prod --namespace cp4i --set imageType=ace  --set image.aceonly=docker-registry.default.svc:5000/cp4i/barimage:${BUILD_NO} --set productionDeployment=false --set image.pullSecret=ibm-entitlement-key --set service.iP=icp-console.tcs-cp4i-202121-6fb0b86391cd68c8282858623a1dddff-0000.che01.containers.appdomain.cloud --set aceonly.replicaCount=1 --set dataPVC.storageClassName=ibmc-file-bronze --set integrationServer.name=intserverkafka --set license=accept --tls
      #oc expose svc ${RELEASE_NAME}-ibm-ace-server-icp4i-prod --port=7800 --name=${RELEASE_NAME}-http
      oc delete all --selector release=${RELEASE_NAME_TO_DEL}
      '''
        
      }
      catch(error) {
        println ">> image deployment failed <<"
        throw error
      }
    
    }
}   */ 
   
