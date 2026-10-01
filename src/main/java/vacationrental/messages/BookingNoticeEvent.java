package vacationrental.messages;

import org.salespointframework.useraccount.UserAccount;
import vacationrental.booking.Booking;

/**
 * Event to notify a user about a late payment for a booking. Send by request of the landlord.
 */
public class BookingNoticeEvent {
	private final UserAccount.UserAccountIdentifier userId;
	private final Booking booking;

	public BookingNoticeEvent(UserAccount.UserAccountIdentifier userId, Booking booking) {
		this.userId = userId;
		this.booking = booking;
	}

	public Booking getBooking(){
		return booking;
	}

	public UserAccount.UserAccountIdentifier getUserId(){return userId;	}

}