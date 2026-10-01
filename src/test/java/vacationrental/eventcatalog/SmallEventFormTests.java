package vacationrental.eventcatalog;


import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vacationrental.account.User;
import vacationrental.account.UserManagement;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseCatalog;
import vacationrental.location.Location;

import java.time.LocalDate;
import java.time.LocalTime;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmallEventFormTests {

	private EventForm cr_smallEvent, smallForm;
	private Location location;

	@Autowired
	SmallEventFormTests(@Autowired UserManagement userManagement) {

		UserAccountIdentifier eventStaffId = userManagement.findByUsername("myEventStaff").get().getUserAccount().getId();
		this.location = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");

		this.cr_smallEvent = new EventForm(
			"Winter Spaziergang",
			"<hier kann eine (kleines) wunderbar schöne Beschreibung Stehen>",
			location.getAddressString(),
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().plusDays(5).toString(),
			LocalTime.parse("15:30"),
			eventStaffId,
			RecurrencePattern.NONE,
			LocalDate.now().plusMonths(5).toString());


	}

	@BeforeEach
	void setUp() {
		// Setze den Testzustand zurück oder initialisiere ihn neu.
		this.smallForm = this.cr_smallEvent;
	}

	@Test
	void testUnusedConstructor() {
		EventForm smallEventForm = new EventForm();
		assertNotNull(smallEventForm, "EventForm sollte nicht null sein.");
	}

	@Test
	void testGetName(){
		assertEquals("Winter Spaziergang", smallForm.getName());
	}

	@Test
	void testGetDescription(){
		assertEquals(cr_smallEvent.getDescription(), smallForm.getDescription());
	}

	@Test
	void testGetLocation() {
		assertEquals(location.getAddressString(), smallForm.getLocation());
	}
	@Test
	void testGetStartDate(){
		assertEquals(
			LocalDate.now().plusDays(3).toString(), smallForm.getStartDate());
	}

	@Test
	void testGetEndDate(){
		assertEquals(
			LocalDate.now().plusDays(5).toString(), smallForm.getEndDate());
	}

	@Test
	void testGetTime(){
		assertEquals("15:30", smallForm.getTime().toString());
	}


	@Test
	void testSetName() {
		smallForm.setName("newName");
		assertEquals("newName", smallForm.getName());
	}

	@Test
	void testSetDescription() {
		smallForm.setDescription("newBeschreibung");
		assertEquals("newBeschreibung", smallForm.getDescription());
	}

	@Test
	void testSetLocation() {
		Location testLocatioon = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");
		smallForm.setLocation(testLocatioon.getAddressString());
		assertEquals(testLocatioon.getAddressString(), smallForm.getLocation());
	}

	@Test
	void testSetStartDate() {
		smallForm.setStartDate(
			LocalDate.now().plusDays(5).toString());
		assertEquals(LocalDate.now().plusDays(5).toString(), smallForm.getStartDate());
	}
	@Test
	void testSetEndDate() {
		smallForm.setEndDate(LocalDate.now().plusDays(8).toString());
		assertEquals(LocalDate.now().plusDays(8).toString(), smallForm.getEndDate());
	}

	@Test
	void testSetTime() {
		smallForm.setTime(LocalTime.parse("14:00"));
		assertEquals("14:00", smallForm.getTime().toString());
	}

	@Test
	void testGetAndSetEventId(){
		EventForm smallEventForm = new EventForm();
		Product.ProductIdentifier mockId = mock(Product.ProductIdentifier.class);

		smallEventForm.setEventId(mockId);
		Product.ProductIdentifier eventId = smallEventForm.getEventId();
		assertNotNull(eventId, "EventId sollte nicht null sein.");
		assertEquals(mockId, eventId, "EventId sollte der MockId entsprechen.");
	}

	@Test
	void testGetAndSetEventStaffId(){
		EventForm smallEventForm = new EventForm();
		UserAccount.UserAccountIdentifier mockId = mock(UserAccount.UserAccountIdentifier.class);

		smallEventForm.setEventStaffId(mockId);
		UserAccount.UserAccountIdentifier eventStaffId = smallEventForm.getEventStaffId();
		assertNotNull(eventStaffId, "EventStaffId sollte nicht null sein.");
		assertEquals(mockId, eventStaffId, "EventStaffId sollte der MockId entsprechen.");
	}

}

