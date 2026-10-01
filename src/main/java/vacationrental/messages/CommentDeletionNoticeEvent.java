package vacationrental.messages;

import org.salespointframework.useraccount.UserAccount;
import vacationrental.comments.Comment;

/**
 * Event to notify the user that a comment has been deleted.
 */
public class CommentDeletionNoticeEvent {

	private final UserAccount.UserAccountIdentifier userId;
	private final Comment comment;

	public CommentDeletionNoticeEvent(UserAccount.UserAccountIdentifier userId, Comment comment) {
		this.userId = userId;
		this.comment = comment;
	}

	public Comment getComment(){
		return comment;
	}

	public UserAccount.UserAccountIdentifier getUserId(){return userId;	}
}
