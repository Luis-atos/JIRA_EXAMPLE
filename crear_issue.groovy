pipeline {
    agent any
    environment {
        JIRA_SITE = "JENKINS_JIRA" 
        JIRA_PROJECT_KEY = "SOF"  
        JIRA_ISSUE_TYPE = "Error"
    }
    stages {
        stage('Create Jira Issue') {
            steps {
                script {
                    def issueFields = [
                        fields: [
                            summary: "Prueba",
                            description: "Jenkins actualiza JIRA",
                            project: [
                                key: env.JIRA_PROJECT_KEY
                            ],
                            issuetype: [
                                name: env.JIRA_ISSUE_TYPE
                            ]
                        ]
                    ]

                    def newIssue = jiraNewIssue issue: issueFields
                    echo "Created new Jira Issue: ${newIssue.data.key}"
                }
            }
        }
    }
}
