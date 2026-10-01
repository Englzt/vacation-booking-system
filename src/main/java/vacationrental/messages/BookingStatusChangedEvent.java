package vacationrental.messages;

import org.salespointframework.useraccount.UserAccount;
import vacationrental.booking.Booking;

/**
 * Event to inform the user that the status of a booking has changed.
 */
public class BookingStatusChangedEvent {

	private final UserAccount.UserAccountIdentifier userId;
	private final Booking changedBooking;

	public BookingStatusChangedEvent(Booking changedBooking, UserAccount.UserAccountIdentifier userId){
		this.changedBooking = changedBooking;
		this.userId = userId;
	}

	public Booking getChangedBooking(){
		return changedBooking;
	}

	public UserAccount.UserAccountIdentifier getUserId(){return userId;	}

}
