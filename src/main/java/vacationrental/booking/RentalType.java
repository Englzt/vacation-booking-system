package vacationrental.booking;

public enum RentalType {
	EVENT,
	PERSONAL,
	TICKET,
	VACANT,/* this is only used in isHouseAvailable method if both EVENT and PERSONAL bookings are possible */
	ADVERTISING //used for small Event requests on a house (called so because the house is "advertising" the small Event)
}