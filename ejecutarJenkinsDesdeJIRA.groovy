
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.mail.Email
import com.atlassian.mail.queue.SingleMailQueueItem
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// Obtén el objeto del issue
def issueManager = ComponentAccessor.getIssueManager()
def currentIssue = issueManager.getIssueObject(issue.key)

// Mensaje personalizado
def issueSummary = currentIssue.getSummary()
def proy = issueSummary.split("/")[0]
def bra = issueSummary.split("/")[1]

def jenkinsUrl = "http://srvcceacml120D:8080/job/${proy}/job/${bra}/build"
    def jenkinsUser = "lmunma1"
    //TOKEN_Prueba maquina 120D
	def jenkinsToken = "11b03b8d9a991932deaddc8387c2d52a36"
   // def jenkinsToken = "11253d0293d61aa417fef153683d06e99e"

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



def email = new Email('lmunma1@ext.mde.es')
email.setSubject("Lanzada Tarea Jenkins")
email.setBody("""
    Issue ${proy} - ${bra} transitioned: Este ticket ha sido marcado como 'In Jenkins' 
""")

SingleMailQueueItem item = new SingleMailQueueItem(email)
ComponentAccessor.getMailQueue().addItem(item)

=====================================================
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
