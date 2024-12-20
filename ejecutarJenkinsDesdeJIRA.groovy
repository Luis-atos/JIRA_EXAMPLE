import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def jenkinsJobTrigger() {
    def jenkinsUrl = "http://srvcceacml77:8080/job/SIACUDEF_AP_LUIS/job/develop/build"
    def jenkinsUser = "lmunma1"
    def jenkinsToken = "11253d0293d61aa417fef153683d06e99e"

    def command = [
        "curl", "-X", "POST",
        "-u", "${jenkinsUser}:${jenkinsToken}",
        jenkinsUrl
    ]

    def process = command.execute()
    def output = process.text
    def exitCode = process.waitFor()

    if (exitCode == 0) {
        log.info("Jenkins job triggered successfully!")
        log.info("Response: ${output}")
    } else {
        log.error("Failed to trigger Jenkins job.")
        log.error("Response: ${output}")
    }
}

jenkinsJobTrigger()
