package vacationrental.messages;

import org.salespointframework.useraccount.UserAccount;
import vacationrental.booking.Booking;

/**
 * Event to notify a user about a refund of a booking.
 */
public class BookingRefundEvent {
	private final UserAccount.UserAccountIdentifier userId;
	private final Booking booking;

	public BookingRefundEvent(UserAccount.UserAccountIdentifier userId, Booking booking) {
		this.userId = userId;
		this.booking = booking;
	}

	public Booking getBooking(){
		return booking;
	}

	public UserAccount.UserAccountIdentifier getUserId(){return userId;	}

}