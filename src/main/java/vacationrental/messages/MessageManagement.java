package vacationrental.messages;

import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import vacationrental.account.User;
import vacationrental.account.UserManagement;
import vacationrental.booking.BookingManagement;
import vacationrental.booking.RentalType;
import vacationrental.comments.Comment;
import vacationrental.comments.CommentType;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.HouseManagement;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;


@Service
public class MessageManagement {
	@Autowired
	private final MessageRepository messages;
	private final UserManagement userManagement;
	private final BookingManagement bookingManagement;
	private final MessageSource messageSource;

	private final ResourceBundle properties;
	@Autowired
	private HouseManagement houseManagement;
	@Autowired
	private EventManagement eventManagement;

	public MessageManagement(UserManagement userManagement,
							 MessageRepository messages,
							 BookingManagement bookingManagement, MessageSource messageSource) {
		this.userManagement = userManagement;
		this.messages = messages;
		this.bookingManagement = bookingManagement;
		this.messageSource = messageSource;
		this.properties = ResourceBundle.getBundle("messages");
	}

	/**
	 * Creates a new message and saves it;
	 * @param user_id
	 * @param title
	 * @param body
	 * @return Message
	 */
	public Message createMessage(UserAccount.UserAccountIdentifier user_id, String title, String body) {
		return messages.save(new Message(user_id, title, body));
	}

	public Streamable<Message> findByUserAccountIdentifier(String user_id) {
		return messages.findByUserId_UserAccountId(user_id);
	}

	public Streamable<Message> findByUser_UserAccountIdAndStatus(String userAccountId, MessageStatus status) {
		return messages.findByUserId_UserAccountIdAndStatus(userAccountId, status, Sort.by(Sort.Direction.DESC, "timestamp"));
	}

	public Optional<Message> findById(Long id) {
		return messages.findById(id);
	}

	public void deleteMessage(Message message) {
		messages.delete(message);
	}

	public Message setMessageStatusRead(Message message) {
		message.setRead();
		return messages.save(message);
	}

	/**
	 * Sends a message to the landlord when a new booking for his house was created.
	 * @param event
	 */
	@EventListener(condition = "#event.changedBooking.status == T(vacationrental.booking.BookingStatus).OPEN")
	public void bookingCreated(BookingStatusChangedEvent event) {
		createMessage(event.getUserId(), event.getChangedBooking().getId().toString(), properties.getString(
			"message.new_booking") + event.getChangedBooking().getRentalInformation().getHouse().getName());
	}

	/**
	 * Send a message to the customer/eventstaff when the status changes to RESERVED
	 * @param event
	 */
	@EventListener(condition = "#event.changedBooking.status == T(vacationrental.booking.BookingStatus).RESERVED")
	public void bookingStatusChangedReserved(BookingStatusChangedEvent event) {

		if (event.getChangedBooking().getRentalType() == RentalType.PERSONAL) {
			createMessage(event.getUserId(), event.getChangedBooking().getId().toString(),
				properties.getString("message.status_change_1")
					+ event.getChangedBooking().getStatus()
					+ properties.getString("message.status_change_2") + "\n"
					+ properties.getString("message.amount_open_1")
					+ bookingManagement.calculateDeposit(event.getChangedBooking())
					+ properties.getString("message.amount_open_2"));

		} else if (event.getChangedBooking().getRentalType() == RentalType.EVENT) {
			createMessage(event.getUserId(), event.getChangedBooking().getId().toString(),
				properties.getString("message.status_change_1")
					+ event.getChangedBooking().getStatus()
					+ properties.getString("message.status_change_2") + "\n"
					+ properties.getString("message.event_approved")
					+ event.getChangedBooking().getRentalInformation().getHouse().getName());
		}
	}

	/**
	 * Send a message when a Booking is canceled.
	 * @param event
	 */
	@EventListener(condition = "#event.changedBooking.status == T(vacationrental.booking.BookingStatus).CANCELED")
	public void bookingStatusChangedCanceled(BookingStatusChangedEvent event) {

		createMessage(event.getUserId(), event.getChangedBooking().getId().toString(),
			properties.getString("message.status_change_1")
				+ event.getChangedBooking().getStatus()
				+ properties.getString("message.status_change_2"));
	}

	/**
	 * Sends a message to the customer when the landlord confirms the full payment.
	 * @param event
	 */
	@EventListener(condition = "#event.changedBooking.status == T(vacationrental.booking.BookingStatus).PAID")
	public void bookingStatusChangedPaid(BookingStatusChangedEvent event) {

		if (event.getChangedBooking().getRentalType() == RentalType.PERSONAL) {
			createMessage(event.getUserId(), event.getChangedBooking().getId().toString(),
				properties.getString("message.status_change_1")
					+ event.getChangedBooking().getStatus()
					+ properties.getString("message.status_change_2") + " " + properties.getString("message.fully_paid"));
		}
	}

	/**
	 * Sends a message when a Booking is completed, if the recipient is a customer it also asks him to comment
	 * @param event
	 */
	@EventListener(condition = "#event.changedBooking.status == T(vacationrental.booking.BookingStatus).COMPLETED")
	public void bookingStatusChangedCompleted(BookingStatusChangedEvent event) {
		User user = userManagement.findByUserAccountIdentifier(event.getUserId()).get();

		String commentInfo = "";

		/* if user is customer add a link to the product page and add call to action for leaving a comment */
		Boolean isCustomer = user.getUserAccount().getRoles().stream().findFirst().toString().contains("Customer");

		if (isCustomer) {
			String redirectAddress = "";
			if (event.getChangedBooking().getRentalType() == RentalType.PERSONAL) {
				redirectAddress += "house/" + event.getChangedBooking().getRentalInformation().getHouse().getId();
			} else if (event.getChangedBooking().getRentalType() == RentalType.TICKET) {
				redirectAddress += "event/" + event.getChangedBooking().getEvent();
			}

			String[] params = {
				"<a href=\"" + redirectAddress + "\" class='external_link'>"
					+ properties.getString("message.comment-cta-2")
					+ "</a>"
			};

			commentInfo = "<br/>" + messageSource.getMessage("message.comment-cta", params, Locale.getDefault());
		}

		createMessage(event.getUserId(), event.getChangedBooking().getId().toString(),
			properties.getString("message.status_change_1")
				+ event.getChangedBooking().getStatus()
				+ properties.getString("message.status_change_2") + commentInfo);
	}

	/**
	 * message to the user send by the landlord if payment is late
	 * @param event
	 */
	@EventListener
	public void bookingLateNotice(BookingNoticeEvent event) {
		createMessage(event.getUserId(), event.getBooking().getId().toString(),
			properties.getString("message.late_notice"));
	}

	/**
	 * message that's send when a booking is canceled and refunded
	 * @param event
	 */
	@EventListener
	public void bookingRefund(BookingRefundEvent event) {
		createMessage(event.getUserId(), event.getBooking().getId().toString(),
			properties.getString("message.refund"));
	}

	/**
	 * message send to the admin when a landlord requests the deletion of a comment
	 * @param event
	 */
	@EventListener
	public void commentDeletionRequest(CommentDeletionRequestEvent event) {
		Comment comment = event.getComment();
		String redirectAddress = "";
		if (event.getComment().getType() == CommentType.HOUSECOMMENT) {
			redirectAddress += "house/" + comment.getProductId();
		} else if (event.getComment().getType() == CommentType.EVENTCOMMENT) {
			redirectAddress += "event/" + comment.getProductId();
		}
		String html = "<a href=\"" + redirectAddress + "\" class='external_link'>"
				+ properties.getString("message.comment_deletion_link")
			+ "</a>";

		createMessage(event.getUserId(), "",
			properties.getString("message.comment_deletion_request") + ": " + html);
	}

	/**
	 * message to the user that his comment was deleted
	 * @param event
	 */
	@EventListener
	public void commentDeletionNotice(CommentDeletionNoticeEvent event) {
		Comment comment = event.getComment();

		String productName = "";
		if (event.getComment().getType() == CommentType.HOUSECOMMENT) {
			productName = houseManagement.findById(comment.getProductId()).get().getName();
		} else if (event.getComment().getType() == CommentType.EVENTCOMMENT) {
			productName = eventManagement.findById(comment.getProductId()).get().getName();
		}

		String[] params = {
			productName,
			String.valueOf(comment.getRating()),
			comment.getText()
		};

		String commentInfo = messageSource.getMessage("message.comment_deletion_notice", params, Locale.getDefault());
		createMessage(event.getUserId(), "", commentInfo);
	}
}
