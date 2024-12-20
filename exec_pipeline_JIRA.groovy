// Fichero que contiene el pipeline común a todos los proyectos
import groovy.transform.Field
import java.text.SimpleDateFormat
import java.util.zip.ZipFile

def call(Map params) {
pipeline {
environment {
        JIRA_SITE = "JENKINS_JIRA"
        JIRA_PROJECT_KEY = "IMPEXP"  
        JIRA_ISSUE_TYPE = "Error"
    }
def pathWS=''
def userID =""
def versionInput=""
def environment_deploy=""
def deployTarget=""
def versionOK=""
def taskUrl =""
def statusSonar="PASSED"
def libsfunctionsArtefactos
def libsfunctionsZipConsoleOut
def checkQG=""
def agentLabels='linux'
if (currentBuild.getBuildCauses().toString().contains('BranchIndexingCause')) { currentBuild.result = 'ABORTED'; return; }
def name_job = env.JOB_NAME.split("/")[0]
if (name_job.matches("SINFRADEF_(.*)")){
   agentLabels='linux'
}else if ((env.BRANCH_NAME.matches("feature(.*)"))){
   agentLabels='Jenkins_CI'
}
pipeline{
   agent { label agentLabels}
   
   options {
    timeout(time: 2, unit: 'HOURS')   // timeout on whole pipeline job
   }
   tools{
	    maven "apache-maven-3.8.6"
	   jdk "openjdk1.8"
	 }
         
   stages{  
         stage('Download params'){
         steps{
         //   logstash {
            script{
                      echo """*************** Parametros de entrada *************************
                                Tecnologia: ${params.Tecnologia}
                                Proyecto: ${params.Proyecto}
                                Rama: ${params.Rama}                     
                                """

                     echo "*************************************" 
                     echo "**Download params ***"
                     echo "*************************************" 
                     def usu = sh "whoami"
                     try{
                          def birdArr = ["Parrot", "Cockatiel", "Pigeon"] as String[]
                          System.out.println(birdArr[1]);
                     }catch(ArrayIndexOutOfBoundsException e){
                     echo "******************************" 
                     echo "**pasamos al catch ***"
                     echo "******************************" 
def testIssue = [fields: [ project: [key: "IMPEXP"],
                                 summary: "${params.Proyecto}/${params.Rama}",
                                 description: "${params.Rama}/Error",
                                 priority: [id: "3"], // Ejemplo: Prioridad Media (id puede variar según tu configuración)
                                 labels: ["jenkins", "automated", "issue"], // Etiquetas para el issue
                                 duedate: "2024-12-31", // Fecha de vencimiento en formato YYYY-MM-DD
                                 reporter: [name: "lmunma1"], // Reportero del issue
                                 assignee: [name: "amerbu1"], // Persona asignada al issue
                                 issuetype: [id: '10102']]]

      response = jiraNewIssue issue: testIssue, site: "JENKINS_JIRA"

      echo response.successful.toString()
      echo response.data.toString()

                     /*
                        def FieldsIssue = [
                        fields: [
                            summary     : "Resumen del problema",
                            description : "Detalles a consierar",
                            project: [
                                key: "IMPEXP"
                            ],
                            issuetype: [
                                name: "Error"
                            ]
                        ]
                    ]

                    def issue = jiraNewIssue(issue: FieldsIssue)
                    echo "Nueva issue creada: ${issue.key}"
                    */
                        sleep(10)
                        throw new Exception("ERROR : Revisar errores Nuevos")
                     }
            }
         //   }
          }
       }
       stage('\u2705 Obtener Version'){
            steps{
            //   logstash {
               script{
                   echo "*************************************" 
                   echo "**Download params ***"
                   echo "*************************************" 
               }
            //   }
            }
         }
         stage('\u2705 Entorno'){
            steps{
              // logstash {
               script{
                   echo "*************************************" 
                     echo "**Entorno ***"
                     echo "*************************************" 
                   }
               }
               //}
         }
         stage('build & Sonarqube'){
            steps{
              // logstash {
               script{
                         echo "***********************************************"
                         echo "Construccion : Proceso de Construccion. ********"
                         echo "************************************************"      
                        
               }
              // }
            }
         }
          stage('Quality Gates Sonarqube'){
            steps{
            //   logstash {
               script{
             
         echo "***********************************************"
         echo "Construccion : Proceso de Construccion. ********"
         echo "************************************************"    
         //   }
         }
         }
         }
         stage('nexus - Tag'){
            when{
             anyOf {
                expression { (deployTarget == 'Validacion') && (env.BRANCH_NAME == 'develop') }
                expression { (deployTarget == 'Validacion') && (env.BRANCH_NAME.matches("RELEASE(.*)")) }
             }
            }
            steps{
           //    logstash {
               script{
                         echo "***********************************************"
                         echo "Nexus : Proceso de Subida y almacenamiento."
                         echo "***********************************************"

               }
             //  }
            }
         }

        stage('entorno Deploy'){
           stages {
                stage("Desarrollo") {
                   when {
                        anyOf {
                              expression { (deployTarget == 'Desarrollo') && (env.BRANCH_NAME == 'develop') }
                              expression { (deployTarget == 'Desarrollo') && (env.BRANCH_NAME.matches("feature(.*)")) }
                              expression { (deployTarget == 'Desarrollo') && (env.BRANCH_NAME.matches("RELEASE(.*)")) }
                          }
                   }
                   steps {
                   script{
                      echo "Despliegue Desarrollo"
                 
                         echo "*************************************************"
                         echo "Despliegue : Proceso de Despliegue en Desarrollo."
                         echo "*************************************************"
                         
                     
                   }
                   }
                 }
                 stage("Validacion") {
                   when {
                     anyOf {
                      expression { (deployTarget == 'Validacion') && (env.BRANCH_NAME == 'develop') }
                      expression { (deployTarget == 'Validacion') && (env.BRANCH_NAME.matches("feature(.*)")) }
                      expression { (deployTarget == 'Validacion') && (env.BRANCH_NAME.matches("RELEASE(.*)")) }
                     }
                   }
                   steps {
                   script{
                      echo "Despliegue Validacion "
                    
                  //   }
                  
                   }
                   }
                 }
                 stage("Preproduccion") {
                    when {
                     anyOf {
                      expression { (deployTarget == 'Pre-produccion') && (env.BRANCH_NAME == 'develop') }
                      expression { (deployTarget == 'Pre-produccion') && (env.BRANCH_NAME.matches("RELEASE(.*)")) }
                     }
                   }
                   steps {
                   //  logstash { 
                     script{
                       echo "Proceso de Pre-Produccion "
                       
                        echo "***********************************************************"
                        echo "Nexus-PreProduccion : Proceso de Bajada Artefacto desde Nexus."
                        echo "***********************************************************"
                         libsfunctionsArtefactos = new divindes.libs.repo.functionsDownloadArtefacto()
                         libsfunctionsArtefactos.downloadArtefactoTotal_raiz(env.BRANCH_NAME, deployTarget, pathWS,versionInput)
                         sleep(10)
                         libsfunctionsunzipArtefactos = new divindes.libs.repo.functionsUnzipArtefacto()
                         libsfunctionsunzipArtefactos.unzipArtefacto_raiz(env.BRANCH_NAME, deployTarget, pathWS,versionInput)
                         sleep(10)
                      
                         echo "*************************************************"
                         echo "Despliegue : Proceso en Pre-Produccion.          "
                         echo "*************************************************"
                      
                       
                       
                     }
                  //   }
                   }
                 }
                 stage("Produccion") {
                   when {
                     anyOf {
                      expression { (deployTarget == 'Produccion') && (env.BRANCH_NAME.matches("RELEASE(.*)")) }
                     }
                   }
                   steps {
               //  logstash { 
                     script{ 
                      echo "*************************************************"
                         echo "Despliegue : Proceso en Produccion.          "
                         echo "*************************************************"
                   }
             //   }
                  }
                 }
                 stage("Hotfix") {
                   when {
                     anyOf {
                      expression {(env.BRANCH_NAME.matches("HOTFIX(.*)")) }
                     }
                   }
                   steps {
               //  logstash { 
                     script{
                      echo "*********************************************************"
                      echo "HOTFIX : Proceso en HOTFIX. *****************************"
                      echo "*********************************************************" 
                   }
             //   }
                  }
                 }
            }
            
         }

    } 
    post{
        always {
         //  steps{
           //    logstash {
               script{
                  echo "***** Borrando Workspace ************ "
                  cleanWs()
               }
             //  }
          //  }

        }
        success{
           //  steps{
           //    logstash {
               script{
                
                     echo "*************************************************"
                     echo "Jenkins : Proceso finaliza correctamente.        "
                     echo "*************************************************"
               }
             //  }
           // }
        }
        failure {
                     echo "*************************************************"
                     echo "Jenkins : Proceso finaliza con Errores.          "
                     echo "*************************************************"
        }
    }         
   }
  }
}

return this
