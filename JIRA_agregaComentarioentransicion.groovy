// Importa las clases necesarias
import com.atlassian.jira.component.ComponentAccessor

// Obtén el objeto del issue
def issueManager = ComponentAccessor.getIssueManager()
def currentIssue = issueManager.getIssueObject(issue.key)

// Obtén el gestor de comentarios
def commentManager = ComponentAccessor.getCommentManager()
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

// Mensaje personalizado
def message = "¡Felicidades! Este ticket ha sido marcado como 'Done'. Recuerda verificar que todo esté completo."

// Agrega un comentario al ticket
commentManager.create(currentIssue, currentUser, message, false)
