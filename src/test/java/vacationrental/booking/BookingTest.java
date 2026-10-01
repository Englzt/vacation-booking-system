package vacationrental.booking;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vacationrental.account.User;
import vacationrental.account.UserManagement;
import vacationrental.eventcatalog.EventForm;
import vacationrental.eventcatalog.Event;
import vacationrental.housecatalog.House;
import vacationrental.location.Location;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.salespointframework.core.Currencies.EURO;
import static vacationrental.booking.RentalType.EVENT;
import static vacationrental.booking.RentalType.PERSONAL;
import static vacationrental.eventcatalog.EventType.BIG_EVENT;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingTest {

	@Autowired
	private final UserManagement userManagement;

	private Booking personalBooking;
	private Booking eventBooking;
	private UserAccount.UserAccountIdentifier userId;
	private Event bigEvent;
	private RentalInformation rental;
	private Booking emptyBooking;

	BookingTest(@Autowired UserManagement userManagement) {
		this.userManagement = userManagement;
	}

	@BeforeAll
	void setUpOnce(@Autowired UserManagement userManagement) {
		User user = userManagement.create("landlord_test1", "321", Role.of("Landlord"));
		this.userId = user.getUserAccount().getId();

		LocalDateTime start_date = LocalDate.parse("2024-11-30").atStartOfDay();
		LocalDateTime end_date = LocalDate.parse("2024-12-01").atStartOfDay();
		Interval interval = Interval.from(start_date).to(end_date);

		User landlord = userManagement.create("user_test", "321", Role.of("Customer"));
		UserAccount.UserAccountIdentifier landlordId = landlord.getUserAccount().getId();
		Location location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		House house = new House(
			"TestHouse",
			Money.of(100, EURO),
			"Test Beschreibung",
			location,
			3, 2, 1, 1,
			true, false,
			landlordId
		);

		this.rental = new RentalInformation(house, interval);
		this.bigEvent = new Event("Yoga Kurs", "Dieser Kurs bietet eine Einführung in Yoga.", location,
			interval, LocalTime.parse("15:30"),  10.0, 66, BIG_EVENT, userId);
		this.eventBooking = new Booking(userId, bigEvent.getId(), rental);

		this.emptyBooking = new Booking();

	}

	@BeforeEach
	void setUp() {
		this.personalBooking = new Booking(userId, rental);
	}

	@Test
	void emptyConstructor(){
		assertEquals(BookingStatus.OPEN, emptyBooking.getStatus());
	}

	@Test
	void isEventBooking() {
		assertTrue(eventBooking.isEventBooking());
	}

	@Test
	void isPersonalBooking() {
		assertTrue(personalBooking.isPersonalBooking());
	}

	@Test
	void getRentalType() {
		assertEquals(EVENT, eventBooking.getRentalType());
		assertEquals(PERSONAL, personalBooking.getRentalType());
	}

	@Test
	void getEvent() {
		assertEquals(bigEvent.getId(), eventBooking.getEvent());
	}

	@Test
	void getStatus() {
		assertEquals(BookingStatus.OPEN, personalBooking.getStatus());
	}

	@Test
	void setOpen() {
		personalBooking.setPaid(); //test chane bec paid is "default"
		assertEquals(BookingStatus.PAID, personalBooking.getStatus());
		personalBooking.setOpen();
		assertEquals(BookingStatus.OPEN, personalBooking.getStatus());
	}

	@Test
	void setReserved() {
		personalBooking.setReserved();
		assertEquals(BookingStatus.RESERVED, personalBooking.getStatus());
	}

	@Test
	void setPaid() {
		personalBooking.setPaid();
		assertEquals(BookingStatus.PAID, personalBooking.getStatus());
	}

	@Test
	void setCompleted() {
		personalBooking.setCompleted();
		assertEquals(BookingStatus.COMPLETED, personalBooking.getStatus());
	}

	@Test
	void setCanceled() {
		personalBooking.setCanceled();
		assertEquals(BookingStatus.CANCELED, personalBooking.getStatus());
	}

	@Test
	void getRentalInformation() {
		assertEquals(this.rental, personalBooking.getRentalInformation());
	}
}