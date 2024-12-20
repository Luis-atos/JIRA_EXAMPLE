import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.mail.Email
import com.atlassian.mail.queue.SingleMailQueueItem

def email = new Email('lmunma1@ext.mde.es')
email.setSubject("Issue has been updated")
email.setBody("""
    Issue has been transitioned to 
""")

SingleMailQueueItem item = new SingleMailQueueItem(email)
ComponentAccessor.getMailQueue().addItem(item)
