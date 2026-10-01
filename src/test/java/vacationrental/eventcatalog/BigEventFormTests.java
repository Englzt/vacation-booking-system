package vacationrental.eventcatalog;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vacationrental.account.UserManagement;
import vacationrental.location.Location;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BigEventFormTests {


	private EventForm cr_bigEvent, bigForm;
	private Location location;

	@Autowired
	BigEventFormTests(@Autowired UserManagement userManagement) {

		UserAccountIdentifier eventStaffId = userManagement.findByUsername("myEventStaff").get().getUserAccount().getId();
		this.location = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");

		this.cr_bigEvent =new EventForm(
			"BigTestEvent",
			"<hier kann eine (große) wunderbar schöne Beschreibung Stehen>",
			location.getAddressString(),
		
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().plusDays(5).toString(),
			LocalTime.parse("14:00"),
			15,
			100,
			eventStaffId
		);
	}

	@BeforeEach
	void setUp() {
		// Setze den Testzustand zurück oder initialisiere ihn neu.
		this.bigForm = this.cr_bigEvent;
	}

	@Test
	void testUnusedConstructor() {
		EventForm bigEventForm = new EventForm();
		assertNotNull(bigEventForm, "EventForm sollte nicht null sein.");
	}


	@Test
	void testGetDescription(){
		assertEquals(cr_bigEvent.getDescription(), bigForm.getDescription());
		//assertEquals("<hier kann eine (große) wunderbar schöne Beschreibung Stehen>", bigForm.getDescription());
	}

	@Test
	void testGetName(){
		assertEquals("BigTestEvent", bigForm.getName());
	}

	@Test
	void testGetLocation() {
		assertEquals(location.getAddressString(), bigForm.getLocation());
	}

	
	
	@Test
	void testGetStartDate(){
		assertEquals(LocalDate.now().plusDays(3).toString(), bigForm.getStartDate());
	}
	@Test
	void testGetEndDate(){
		assertEquals(LocalDate.now().plusDays(5).toString(), bigForm.getEndDate());
	}

	@Test
	void testGetTime(){
		assertEquals("14:00", bigForm.getTime().toString());
	}

	@Test
	void testSetName() {
		bigForm.setName("newName");
		assertEquals("newName", bigForm.getName());
	}

	@Test
	void testSetDescription() {
		bigForm.setDescription("newBeschreibung");
		assertEquals("newBeschreibung", bigForm.getDescription());
	}

	@Test
	void testSetLocation() {
		Location testLocatioon = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");
		bigForm.setLocation(testLocatioon.getAddressString());
		assertEquals(testLocatioon.getAddressString(), bigForm.getLocation());
	}

	@Test
	void testSetStartDate() {
		bigForm.setStartDate(LocalDate.now().plusDays(2).toString());
		assertEquals(LocalDate.now().plusDays(2).toString(), bigForm.getStartDate().toString());
	}
	@Test
	void testSetEndDate() {
		bigForm.setEndDate(LocalDate.now().plusDays(9).toString());
		assertEquals(LocalDate.now().plusDays(9).toString(), bigForm.getEndDate().toString());
	}

	@Test
	void testSetTime() {
		bigForm.setTime(LocalTime.parse("18:00"));
		assertEquals("18:00", bigForm.getTime().toString());
	}

	@Test
	void testGetAndSetEventId(){
		EventForm bigEventForm = new EventForm();
		Product.ProductIdentifier mockId = mock(Product.ProductIdentifier.class);

		bigEventForm.setEventId(mockId);
		Product.ProductIdentifier eventId = bigEventForm.getEventId();
		assertNotNull(eventId, "EventId sollte nicht null sein.");
		assertEquals(mockId, eventId, "EventId sollte der MockId entsprechen.");
	}
	@Test
	void testGetAndSetEventStaffId(){
		EventForm bigEventForm = new EventForm();
		UserAccount.UserAccountIdentifier mockId = mock(UserAccount.UserAccountIdentifier.class);

		bigEventForm.setEventStaffId(mockId);
		UserAccount.UserAccountIdentifier eventStaffId = bigEventForm.getEventStaffId();
		assertNotNull(eventStaffId, "EventStaffId sollte nicht null sein.");
		assertEquals(mockId, eventStaffId, "EventStaffId sollte der MockId entsprechen.");
	}
	@Test
	void testSetTicketprice(){
		bigForm.setTicketprice(10);
		assertEquals(10, bigForm.getTicketprice());
	}
}


