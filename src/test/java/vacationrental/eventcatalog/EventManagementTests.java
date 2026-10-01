
package vacationrental.eventcatalog;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Streamable;
import org.springframework.test.annotation.DirtiesContext;
import vacationrental.account.UserManagement;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(2)
public class EventManagementTests {

	@Autowired
	private EventManagement eventManagement;

	private EventCatalog eventCatalog;
	private final Location eventLocation;

	private final UserAccountIdentifier eventStaffId;
	private final Event cr_bigEvent;
	private final Event cr_smallEvent;
	private Event bigEvent,smallEvent;
	
	@Autowired
	EventManagementTests(EventCatalog eventCatalog,@Autowired HouseManagement houseManagement,
						 @Autowired UserManagement userManagement, @Autowired EventManagement eventManagement) {

		this.eventCatalog = eventCatalog;

		this.eventStaffId = userManagement.findByUsername("myEventStaff").get().getUserAccount().getId();
		this.eventLocation = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");
		//this.myHouse = houseManagement.findByName("Historic Vineyard House").toList().getFirst();

		this.cr_bigEvent = eventManagement.findByName("Yoga Flow").toList().getFirst();

		this.cr_smallEvent = eventManagement.findByName("Winter Spaziergang").toList().getFirst();
		
	}

	@BeforeEach
	void setUp(@Autowired EventCatalog eventCatalog, @Autowired EventManagement eventManagement, 
	@Autowired UserManagement userManagement) {
		// Setze den Testzustand zurück oder initialisiere ihn neu.
		this.eventCatalog = eventCatalog;

		this.bigEvent = cr_bigEvent;
		this.smallEvent = cr_smallEvent;
	}

	// kein landlordID dabei
	@Test
	public void testCreateEvents() {

		assertNotNull(bigEvent);
		assertNotNull(smallEvent);
		assertEquals("Yoga Flow", bigEvent.getName());
		assertEquals("Winter Spaziergang", smallEvent.getName());

		// Überprüfen, ob das Event im EventCatalog vorhanden ist
		Streamable <Event> event_list = eventCatalog.findAll();
		boolean bigEventFound = false;
		boolean smallEventFound = false;
		for (Event e : event_list) {
			if (e.getName().equals(bigEvent.getName())) {
				bigEventFound = true;
			}
			if (e.getName().equals(bigEvent.getName())) {
				smallEventFound = true;
			}

			eventManagement.cancelEvent(e.getId());
		}

		assertTrue(bigEventFound, "Das große Event sollte im Katalog gespeichert sein.");
		assertTrue(smallEventFound, "Das kleine Event sollte im Katalog gespeichert sein.");

	}

	@Test
	public void testFindById(){
		Optional<Event> event = eventManagement.findById(eventManagement.findByName("Winter Spaziergang").toList().getFirst().getId());
		assertNotNull(event);
		assertEquals(smallEvent.getName(), event.get().getName());

	}
	@Test
	public void testFindAll() {
		assertNotNull(eventCatalog.findAll());
		assertEquals(eventManagement.findAll().stream().count(),eventCatalog.count());
	}

	@Test
	public void testEditBigEvent() {
		EventForm bigForm = new EventForm(
			"newEditedBigEvent",
			"<hier kann eine (große) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(4).toString(),
			LocalDate.now().plusDays(6).toString(),
			LocalTime.parse("14:00"),
			15,
			100,
			eventStaffId
		);
		Event bigEvent = eventManagement.createBigEvent(bigForm);

		EventForm updatedForm = new EventForm(
			"UpdatedBigEvent",
			"Neue Beschreibung",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(7).toString(),
			LocalDate.now().plusDays(8).toString(),
			LocalTime.parse("15:00"),
			20,
			100,
			eventStaffId
		);
		boolean result = eventManagement.editBigEvent(bigEvent.getId(), updatedForm);
		assertTrue(result, "Die Bearbeitung sollte funktionieren.");

		Optional<Event> updatedEvent = eventCatalog.findById(bigEvent.getId());
		assertTrue(updatedEvent.isPresent(), "Das bearbeitete Event sollte existieren.");
		assertEquals("UpdatedBigEvent", updatedEvent.get().getName(), "Der Name sollte aktualisiert sein.");
		assertEquals(20, updatedEvent.get().getTicketprice(), "Der Preis sollte aktualisiert sein.");

	}

	@Test
	public void testEditSmallEvent() {
		EventForm smallForm = new EventForm(
			"newEditedSmallEvent",
			"<hier kann eine (große) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().plusDays(5).toString(),
			LocalTime.parse("15:30"),
			eventStaffId,
			RecurrencePattern.NONE,
			LocalDate.now().plusMonths(5).toString()
		);
		
		Event smallEvent = eventManagement.createSmallEvent(smallForm);
		ProductIdentifier eventId = smallEvent.getId();

		EventForm updatedForm = new EventForm(
			"UpdatedSmallEvent",
			"Neue Beschreibung",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(8).toString(),
			LocalDate.now().plusDays(12).toString(),
			LocalTime.parse("16:00"),
			eventStaffId,
			RecurrencePattern.NONE,
			LocalDate.now().plusMonths(5).toString());
		boolean result = eventManagement.editSmallEvent(eventId, updatedForm);
		assertTrue(result, "Die Bearbeitung sollte funktionieren.");

		Optional<Event> updatedEvent = eventCatalog.findById(eventId);
		assertTrue(updatedEvent.isPresent(), "Das bearbeitete Event sollte existieren.");
		assertEquals("UpdatedSmallEvent", updatedEvent.get().getName(), "Der Name sollte aktualisiert sein.");
		assertEquals("16:00", updatedEvent.get().getTime().toString());

	}

	@Test
	public void testGetHouses() {
		Streamable<House> houses = eventManagement.getHouses();
		assertNotNull(houses, "Houses sollten nicht Null sein.");
	}


}

