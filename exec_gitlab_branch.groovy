import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.net.HttpURLConnection
import com.atlassian.jira.component.ComponentAccessor

// Obtener información de la issue
def jiraIssue = ComponentAccessor.issueManager.getIssueObject(issue.id)
def issueKey = jiraIssue.key
def issueSummary = jiraIssue.summary
def reporter = jiraIssue.reporter?.displayName ?: "Unknown"
def status = jiraIssue.status.name
def repositoryName = "Desconocido"

// Supongamos que el resumen contiene algo como "Deploy: <JobName>"
// Supongamos que el resumen contiene algo como "Deploy: <JobName>"
def matcher = (issueSummary =~ /#(.*?)#/)
if (matcher.find()) {
    repositoryName = matcher.group(1) // Extrae el primer grupo capturado
    log.info "El nombre de la rama extraída es: ${repositoryName}"
} else {
    log.error "No se pudo extraer el nombre la rama del resumen: ${issueSummary}"
    throw new RuntimeException("No se pudo determinar el nombre de la rama de Gitlab.")
}

// Configuración
def gitlabToken = "glpat-KB1LpGiB9LEqcVjhzx6-" // Reemplaza con tu token de acceso personal
def gitlabApiUrl = "https://git.servdev.mdef.es/api/v4" // URL base de la API de GitLab
def baseBranch = "develop" // Rama base desde la cual crear la nueva rama
def newBranchName = "feature/${issue}" // Nombre de la nueva rama

// Función para buscar el ID del repositorio por su nombre
def findRepositoryIdByName(repositoryName) {
    def gitlabApiUrl = "https://git.servdev.mdef.es/api/v4" // URL base de la API de GitLab
    def gitlabToken = "glpat-KB1LpGiB9LEqcVjhzx6-" // Reemplaza con tu token de acceso personal
    if (!repositoryName) {
        throw new RuntimeException("El nombre del repositorio no puede ser nulo.")
    }
    def searchUrl = "${gitlabApiUrl}/projects?search=${URLEncoder.encode(repositoryName.toString(), 'UTF-8')}"

    def connection = new URL(searchUrl).openConnection() as HttpURLConnection
    connection.setRequestMethod("GET")
    connection.setRequestProperty("Authorization", "Bearer ${gitlabToken}")

   if (connection.responseCode == 200) {
        def responseText = connection.inputStream.text
        def response = new JsonSlurper().parseText(responseText)
        log.info "Contenido de la respuesta de la API: ${responseText}"


        // Validar que la respuesta sea una lista
        if (response instanceof List) {
            def project = response.find { it instanceof Map && it.name == repositoryName }
            def projectId = project instanceof Map ? project.id : null
            if (project) {
                log.info "Repositorio '${repositoryName}' encontrado. ID: ${projectId}"
                return projectId
            } else {
                log.error "Repositorio '${repositoryName}' no encontrado en los resultados. Respuesta completa: ${responseText}"
                throw new RuntimeException("No se pudo encontrar el repositorio con nombre: ${repositoryName}")
            }
        } else {
            log.error "La respuesta de la API de GitLab no es una lista. Respuesta: ${responseText}"
            throw new RuntimeException("La API de GitLab devolvió un formato inesperado.")
        }
    } else {
        def errorResponse = connection.errorStream.text
        log.error "Error buscando el repositorio. Código: ${connection.responseCode}. Respuesta: ${errorResponse}"
        throw new RuntimeException("Error buscando el repositorio.")
    }


}

// Buscar el ID del repositorio
def repositoryId = findRepositoryIdByName(repositoryName)

// Crear la rama en el repositorio
def createBranchUrl = "${gitlabApiUrl}/projects/${repositoryId}/repository/branches"

// Payload para la creación de la rama
def payload = JsonOutput.toJson([
    branch: newBranchName,
    ref: baseBranch
])

try {
    // Abrir la conexión HTTP
    def connection = new URL(createBranchUrl).openConnection() as HttpURLConnection
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Authorization", "Bearer ${gitlabToken}")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setDoOutput(true)

    // Enviar el payload
    connection.outputStream.withWriter("UTF-8") { writer ->
        writer.write(payload)
    }

    // Leer la respuesta
    def responseCode = connection.responseCode
    def response = connection.inputStream.withReader("UTF-8") { reader ->
        reader.text
    }

    if (responseCode == 201) {
        log.info "Rama '${newBranchName}' creada exitosamente desde '${baseBranch}' en el repositorio '${repositoryName}'."
        log.info "Respuesta: ${response}"
    } else {
        log.error "Error creando la rama. Código de respuesta: ${responseCode}. Respuesta: ${response}"
        throw new RuntimeException("Error creando la rama.")
    }
} catch (Exception e) {
    log.error "Error al crear la rama: ${e.message}"
    throw e
}
