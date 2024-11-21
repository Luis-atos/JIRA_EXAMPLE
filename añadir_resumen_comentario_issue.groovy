// modifica el resumen de la incidencia y agrega comentario
// Importa las clases necesarias
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.MutableIssue

// Obtén el objeto del issue
def issueManager = ComponentAccessor.getIssueManager()
def currentIssue = issue as MutableIssue // Asegúrate de que sea un MutableIssue

// Obtén el gestor de comentarios
def commentManager = ComponentAccessor.getCommentManager()
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

// Nuevo resumen
def newSummary = "Agregado Completado: ${currentIssue.summary}"

// Actualiza el resumen
currentIssue.setSummary(newSummary)
issueManager.updateIssue(currentUser, currentIssue, com.atlassian.jira.event.type.EventDispatchOption.ISSUE_UPDATED, false)

// Mensaje personalizado
def message = "¡Felicidades! Este ticket ha sido marcado como 'Done'. El resumen se actualizó a: '${newSummary}'"

// Agrega un comentario al ticket
commentManager.create(currentIssue, currentUser, message, false)
