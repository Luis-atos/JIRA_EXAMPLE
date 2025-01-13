import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.atlassian.jira.component.ComponentAccessor

// Obtener información de la issue
def jiraIssue = ComponentAccessor.issueManager.getIssueObject(issue.id)
def issueKey = jiraIssue.key
def issueSummary = jiraIssue.summary
def reporter = jiraIssue.reporter?.displayName ?: "Unknown"
def status = jiraIssue.status.name
def jobName = "Desconocido"


// Supongamos que el resumen contiene algo como "Deploy: <JobName>"
def matcher = (issueSummary =~ /#(.*?)#/)
if (matcher.find()) {
    jobName = matcher.group(1) // Extrae el primer grupo capturado
    log.info "El nombre del job extraído es: ${jobName}"
} else {
    log.error "No se pudo extraer el nombre del job del resumen: ${issueSummary}"
    throw new RuntimeException("No se pudo determinar el nombre del job de Jenkins.")
}


// Configuración de Jenkins
def jenkinsUrl = "http://srvcceacml77:8080/"        // URL de Jenkins
def jenkinsPath = "job/${jobName}/job/"             // Nombre de la tarea en Jenkins
def branch = "develop"                              // Nombre de la rama
def user = "jfajram"                                // Usuario de Jenkins
def token = "118cc81dfb0b44f8be4b448cbc47c2de1c"    // Token API de Jenkins



// Parámetros para pasar a Jenkins
// def params = [
//     ISSUE_KEY: issueKey,
//     ISSUE_SUMMARY: issueSummary,
//     REPORTER: reporter,
//     STATUS: status
// ]

println(jiraIssue)

def params = [
    issue: issue
]

// Crear la URL con los parámetros
def buildUrl = "${jenkinsUrl}/${jenkinsPath}/${branch}/buildWithParameters"
def queryParams = params.collect { k, v -> "${k}=${URLEncoder.encode(v.toString(), 'UTF-8')}" }.join("&")
def fullUrl = "${buildUrl}?${queryParams}"

try {
    // Abrir la conexión y convertirla a HttpURLConnection
    def connection = new URL(fullUrl).openConnection() as HttpURLConnection
    connection.setRequestProperty("Authorization", "Basic " + "${user}:${token}".bytes.encodeBase64().toString())
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)

    // Verificar la respuesta
    def responseCode = connection.responseCode
    def responseMessage = connection.responseMessage

    if (responseCode == 201 || responseCode == 200) {
        log.info "Jenkins job triggered successfully for issue ${issueKey}."
    } else {
        log.error "Failed to trigger Jenkins job. Response code: ${responseCode}. Message: ${responseMessage}"
    }
} catch (Exception e) {
    log.error "Error triggering Jenkins job: ${e.message}"
    throw e
}

