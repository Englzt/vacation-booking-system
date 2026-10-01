package vacationrental.messages;

import org.salespointframework.useraccount.UserAccount;
import vacationrental.comments.Comment;

/**
 * Event to notify the admin about a comment deletion request.
 */
public class CommentDeletionRequestEvent {

	private final UserAccount.UserAccountIdentifier userId;
	private final Comment comment;

	public CommentDeletionRequestEvent(UserAccount.UserAccountIdentifier userId, Comment comment) {
		this.userId = userId;
		this.comment = comment;
	}

	public Comment getComment(){
		return comment;
	}

	public UserAccount.UserAccountIdentifier getUserId(){return userId;	}
}
