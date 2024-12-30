def call(Map params) {
    pipeline {
        agent any
        parameters {
            string(name: 'issue', description: 'Jira issue')
        }
        stages {
            stage('Checkout') {
                steps {
                    echo "Imprimiendo parámetro issue... ${params.issue}"
                    script {
                        try {
                            script.checkout([
                                $class: 'GitSCM',
                                branches: [[name: "*/develop"]],
                                userRemoteConfigs: [[url: repoUrl]]
                            ])
                            NotifyJira(params.issue, "La rama se ha clonado correctamente ...", "SUCCESS")
                        } catch(error) {
                            revertToPreviousState(params.issue)
                            NotifyJira(params.issue, "Hubo un error clonando el repositorio de codigo ...", "FAIL")
                            error "Error en etapa checkout: ${error.message}"
                        }
                    }
                }
            }
            stage('Build') {
                steps {
                    echo 'Contruyendo...'
                    script {
                        def buildStatus = currentBuild.currentResult
                        NotifyJira(params.issue, "El paquete se ha construido correctamente ...", buildStatus)
                    }
                }
            }
            stage('Test') {
                steps {
                    echo 'Ejecutando pruebas unitarias...'
                    script {
                        def buildStatus = currentBuild.currentResult
                        NotifyJira(params.issue, "Las pruebas unitarias se han ejecutado correctamente ...", buildStatus)
                    }
                }
            }
            stage('Deploy') {
                steps {
                    echo 'Desplegando aplicación...'
                    script {
                        def buildStatus = currentBuild.currentResult
                        NotifyJira(params.issue, "La aplicación se ha desplegado correctamente ...", buildStatus)
                    }
                }
            }
        }
        post {
            always {
                echo 'Pipeline finalizado, limpiando workspace...'
            }
            success {
                echo 'El pipeline se completó con éxito.'
            }
            failure {
                echo 'Hubo un fallo en el pipeline.'
            }
        }
    }
}

def NotifyJira(String issue, String message, String status) {
    def url = "https://jiradivindes.mdef.es/rest/api/latest/issue/${issue}/comment"
    def statusColor = status == 'SUCCESS' ? 'green' : 'red'
    def payload = [
        body: """
        h4. Notificación Jenkins
        ----
        {color:${statusColor}}*${message}*{color}

        * Rama: *develop*
        * Build URL: [Ver build|${env.BUILD_URL}]

        ----
        _Generado automáticamente por Jenkins_
        """
    ]
    
    try {
        def response = httpRequest(
            url: url,
            httpMode: 'POST',
            contentType: 'APPLICATION_JSON',
            customHeaders: [
                [name: 'Authorization', value: 'Bearer OTQzNjgwNTAzMzQ0OhhbXdLyY+I5qKPlXGZGe8juL80m']
            ],
            requestBody: groovy.json.JsonOutput.toJson(payload),
            validResponseCodes: '200:299'
        )
        echo "Estado de la respuesta: ${response.status}"
        echo "Contenido de la respuesta: ${response.content}"
    } catch (Exception e) {
        echo "Error al notificar a Jira: ${e.message}"
        throw e
    }
}

def revertToPreviousState(String issueKey) {
    def jiraBaseUrl = "https://jiradivindes.mdef.es"
    def jiraToken = "Bearer OTQzNjgwNTAzMzQ0OhhbXdLyY+I5qKPlXGZGe8juL80m"
    
    try {
        // Obtener el historial de la issue
        def historyResponse = httpRequest(
            url: "${jiraBaseUrl}/rest/api/latest/issue/${issueKey}?expand=changelog",
            httpMode: 'GET',
            contentType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Authorization', value: jiraToken]]
        )
        
        def history = readJSON text: historyResponse.content
        
        // Encontrar el estado anterior buscando por ID
        def statusHistories = history.changelog.histories.findAll { 
            it.items.any { item -> item.field == "status" }
        }
        
        if (statusHistories.isEmpty()) {
            error "No se encontró historial de estados para la issue."
        }
        
        // Obtener el ID del estado anterior
        def previousStatusId = statusHistories[-1].items.find { it.field == "status" }?.from
        echo "ID del estado anterior: ${previousStatusId}"
        
        if (!previousStatusId) {
            error "No se pudo determinar el ID del estado anterior de la issue."
        }
        
        // Obtener transiciones disponibles
        def transitionsResponse = httpRequest(
            url: "${jiraBaseUrl}/rest/api/latest/issue/${issueKey}/transitions",
            httpMode: 'GET',
            contentType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Authorization', value: jiraToken]]
        )
        
        def transitions = readJSON text: transitionsResponse.content
        def transitionId = transitions.transitions.find { 
            it.to.id == previousStatusId 
        }?.id
        
        if (!transitionId) {
            echo "Transiciones disponibles: ${transitions.transitions.collect { "ID: ${it.to.id}, Nombre: ${it.to.name}" }.join(', ')}"
            error "No se encontró una transición válida para regresar al estado anterior (ID: ${previousStatusId})"
        }
        
        // Ejecutar la transición usando el ID
        httpRequest(
            url: "${jiraBaseUrl}/rest/api/latest/issue/${issueKey}/transitions",
            httpMode: 'POST',
            contentType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Authorization', value: jiraToken]],
            requestBody: groovy.json.JsonOutput.toJson([
                transition: [id: transitionId]
            ])
        )
        
        echo "Issue revertida exitosamente al estado anterior con ID: ${previousStatusId}"
        
    } catch (Exception e) {
        echo "Error al revertir el estado de la issue: ${e.message}"
        throw e
    }
}

return this
