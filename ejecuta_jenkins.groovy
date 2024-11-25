import groovyx.net.http.RESTClient

def jenkinsUrl = "http://srvcceacml77:8080"
def jenkinsJob = "/job/SIACUDEF_AP/build"
def jenkinsUser = "lmunma1"
def jenkinsToken = "Minisdef4321088"

// Obtener el crumb
def crumbClient = new RESTClient(jenkinsUrl)
crumbClient.auth.basic(jenkinsUser, jenkinsToken)
def crumbResponse = crumbClient.get(path: "/crumbIssuer/api/json")
def crumb = crumbResponse
def crumbField = crumbResponse

if (!crumb) {
    throw new RuntimeException("No se pudo obtener el crumb para la autenticación.")
}

// Crear cliente para lanzar el job
def client = new RESTClient("${jenkinsUrl}${jenkinsJob}")
client.auth.basic(jenkinsUser, jenkinsToken)

try {
    def response = client.post(
        path: '',
        requestContentType: 'application/x-www-form-urlencoded',
        headers: [(crumbField): crumb]
    )

    if (response == 201) {
        log.info("Tarea en Jenkins lanzada exitosamente.")
    } else {
        log.error("Error al lanzar la tarea en Jenkins. Código de estado: ${response}")
    }
} catch (Exception e) {
    log.error("Se produjo un error al intentar lanzar la tarea en Jenkins: ${e.message}")
}


===============================================
import groovyx.net.http.RESTClient

def jenkinsUrl = "http://srvcceacml77:8080"
def jenkinsJob = "/job/SIACUDEF_AP/build"
def jenkinsUser = "lmunma1"
def jenkinsToken = "Minisdef4321088"

// Obtener el crumb
def crumbClient = new RESTClient(jenkinsUrl)
crumbClient.auth.basic(jenkinsUser, jenkinsToken)
def crumbResponse = crumbClient.get(path: "/crumbIssuer/api/json")
log.info("Tarea en Jenkins ${crumbResponse}")
def crumb = crumbResponse
def crumbField = crumbResponse

if (!crumb) {
    throw new RuntimeException("No se pudo obtener el crumb para la autenticación.")
}

// Crear cliente para lanzar el job
def client = new RESTClient("${jenkinsUrl}${jenkinsJob}")
client.auth.basic(jenkinsUser, jenkinsToken)

try {
    def response = client.post(
        path: '',
        requestContentType: 'application/x-www-form-urlencoded',
        headers: [(crumbField): crumb]
    )

    if (response == 201) {
        log.info("Tarea en Jenkins lanzada exitosamente.")
    } else {
        log.error("Error al lanzar la tarea en Jenkins. Código de estado: ${response}")
    }
} catch (Exception e) {
    log.error("Se produjo un error al intentar lanzar la tarea en Jenkins: ${e.message}")
}
