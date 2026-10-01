package vacationrental.booking;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;
import vacationrental.eventcatalog.Event;
import vacationrental.eventcatalog.EventType;

import javax.money.MonetaryAmount;
import java.time.format.DateTimeFormatter;

import static org.salespointframework.core.Currencies.EURO;


@Entity
public class Booking extends Order {

	private BookingStatus status;
	private Product.ProductIdentifier eventId;
	private RentalType rentalType;

	private RentalInformation rentalInformation;
	private int tickets;
	private MonetaryAmount bookingPrice;

	/**
	 * Constructor for event bookings.
	 * @param account the user account
	 * @param eventId the event id
	 * @param rentalInformation the rental information
	 */
	public Booking(UserAccount.UserAccountIdentifier account,
				   Product.ProductIdentifier eventId,
				   RentalInformation rentalInformation) {
		super(account);
		this.eventId = eventId;
		status = BookingStatus.OPEN;
		rentalType = RentalType.EVENT;
		this.rentalInformation = rentalInformation;
		this.tickets = 0;
		this.bookingPrice = rentalInformation.getHouse().getPrice();
	}

	/**
	 * Constructor for personal bookings.
	 * @param account the user account
	 * @param rentalInformation the rental information
	 */
	public Booking(UserAccount.UserAccountIdentifier account, RentalInformation rentalInformation) {
		super(account);
		status = BookingStatus.OPEN;
		rentalType = RentalType.PERSONAL;
		this.rentalInformation = rentalInformation;
		this.tickets = 0;
		this.bookingPrice = rentalInformation.getHouse().getPrice();
	}

	/**
	 * Constructor for Ticket bookings
	 * @param account {@link UserAccount.UserAccountIdentifier} the user account
	 * @param event {@link Event} the event
	 * @param tickets {@link Integer} number of tickets
	 */
	public Booking(UserAccount.UserAccountIdentifier account,
				   Event event,
				   int tickets,
				   RentalInformation rentalInformation) {
		super(account);
		this.eventId = event.getId();
		this.tickets = tickets;
		status = BookingStatus.PAID;
		rentalType = RentalType.TICKET;
		this.rentalInformation = rentalInformation;
		this.bookingPrice = event.getPrice();
	}

	/**
	 * constructor for Advertising bookings
	 * @param eventstaffId {@link UserAccount.UserAccountIdentifier}
	 * @param eventId {@link Product.ProductIdentifier}
	 * @param rentalInformation {@link RentalInformation}
	 * @param eventType {@link EventType}
	 */
	public Booking(UserAccount.UserAccountIdentifier eventstaffId,
				   Product.ProductIdentifier eventId,
				   RentalInformation rentalInformation,
				   EventType eventType) {
		super(eventstaffId);
		if(eventType.equals(EventType.SMALL_EVENT)){
			this.eventId = eventId;
			status = BookingStatus.OPEN;
			rentalType = RentalType.ADVERTISING;
			this.rentalInformation = rentalInformation;
			this.bookingPrice = Money.of(0, EURO);
			this.tickets = 0;
		}

	}

	/**
	 * Public default constructor for JPA.
	 */
	public Booking() {
		status = BookingStatus.OPEN;
	}

	/**
	 * @return true if the booking is an event booking
	 */
	public boolean isEventBooking(){
		return rentalType == RentalType.EVENT;
	}

	/**
	 * @return true if the booking is a personal booking
	 */
	public boolean isPersonalBooking(){
		return rentalType == RentalType.PERSONAL;
	}

	/**
	 * @return true if the booking is a ticket booking
	 */
	public boolean isTicketBooking(){
		return rentalType == RentalType.TICKET;
	}

	/**
	 *
	 * @return true if booking is an advertising booking
	 */
	public boolean isAdvertisingBooking(){
		return rentalType == RentalType.ADVERTISING;
	}

	/**
	 * @return the rental type
	 */
	public RentalType getRentalType(){
		return rentalType;
	}

	/**
	 * @return the event id of the booking
	 */
	public Product.ProductIdentifier getEvent() {
		return eventId;
	}

	/**
	 * @return the status of the booking
	 */
	public BookingStatus getStatus() {
		return status;
	}

	/**
	 * @return number of tickets
	 */
	public int getTickets(){
		return tickets;
	}

	/**
	 * @return price of the house/Event at the time of creation
	 */
	public @NotNull MonetaryAmount getBookingPrice() {
		return bookingPrice;
	}

	/**
	 * set's the status of the booking to open
	 */
	public void setOpen(){
		status = BookingStatus.OPEN;
	}

	/**
	 * set's the status of the booking to reserved
	 */
	public void setReserved(){
		status = BookingStatus.RESERVED;
	}

	/**
	 * @return the date the booking was created as formatted string. Used in rental details page
	 */
	public String getDateCreatedFormatted() {
		return this.getDateCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	/**
	 * set's the status of the booking to paid
	 */
	public void setPaid(){
		status = BookingStatus.PAID;
	}

	/**
	 * set's the status of the booking to completed
	 */
	public void setCompleted(){
		status = BookingStatus.COMPLETED;
	}

	/**
	 * set's the status of the booking to canceled
	 */
	public void setCanceled(){
		status = BookingStatus.CANCELED;
	}

	/**
	 * set's the number of tickets for a ticket booking
	 * @param tickets {@link Integer}
	 * @return {@link Boolean}
	 */
	public boolean setTickets(int tickets){
		if(this.rentalType == RentalType.TICKET){
			this.tickets = tickets;
			return true;
		}
		return false;
	}

	/**
	 * increases the number of tickets for a ticket booking by the given amount
	 * @param tickets {@link Integer}
	 * @return {@link Boolean}
	 */
	public boolean increaseTickets(int tickets){
		if(this.rentalType == RentalType.TICKET){
			this.tickets += tickets;
			return true;
		}
		return false;
	}

	/**
	 * decreases the number of tickets for a ticket booking by the given amount
	 *
	 * @param tickets {@link Integer}
	 */
	public void decreaseTickets(int tickets){
		if(this.rentalType == RentalType.TICKET){
			this.tickets -= tickets;
		}
	}

	/**
	 * @return the rental information of the booking
	 */
	public RentalInformation getRentalInformation() {
		return rentalInformation;
	}
}
