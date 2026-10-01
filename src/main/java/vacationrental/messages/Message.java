package vacationrental.messages;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.salespointframework.useraccount.UserAccount;
import vacationrental.util.TimeManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class which represents the message to a user
 */
@Entity
public class Message {

	@Id
	@GeneratedValue
	private Long id;

	private UserAccount.UserAccountIdentifier userId;

	private String title;
	private String body;
	private MessageStatus status;
	private LocalDateTime timestamp;

	public Message(UserAccount.UserAccountIdentifier user, String title, String body){
		this.userId = user;
		this.title = title;
		this.body = body;
		this.status = MessageStatus.UNREAD;
		this.timestamp = TimeManager.getTime();
	}

	public Message(){}

	public String getMessage() {
		return title + "\n" + body;
	}

	public String getTitle(){
		return title;
	}

	public Long getId(){
		return id;
	}

	public LocalDateTime getTimestamp(){
		return timestamp;
	}

	public String getTimestampString(){
		return timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
	}

	public String getBody(){
		return body;
	}

	public void setRead(){
		this.status = MessageStatus.READ;
	}

	public UserAccount.UserAccountIdentifier getUserId(){
		return userId;
	}

}
