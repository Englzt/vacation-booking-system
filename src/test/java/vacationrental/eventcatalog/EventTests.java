
package vacationrental.eventcatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vacationrental.account.UserManagement;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventTests {

	@Autowired
	private EventManagement eventManagement;

	private EventCatalog eventCatalog;
	private HouseManagement houseManagement;

	private final Location eventLocation;


	private final Event bigEvent;
	private Event event;

	@Autowired
	EventTests(EventCatalog eventCatalog,@Autowired HouseManagement houseManagement,
			   @Autowired UserManagement userManagement, @Autowired EventManagement eventManagement) {

		this.eventCatalog = eventCatalog;

		this.eventLocation = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");

		this.bigEvent = eventManagement.findByName("Abend Yoga").toList().getFirst();

	}

	@BeforeEach
	void setUp(@Autowired EventCatalog eventCatalog, @Autowired EventManagement eventManagement, @Autowired UserManagement userManagement) {
		// Setze den Testzustand zurück oder initialisiere ihn neu.
		this.eventCatalog = eventCatalog;

		this.event = this.bigEvent;
	}

	@Test
	void testUnusedConstructor() {
		Event event = new Event();
		assertNotNull(event, "EventForm sollte nicht null sein.");
	}


	@Test
	void testGetName(){
		assertEquals("Abend Yoga", event.getName());
	}

	@Test
	void testGetDescription(){
		assertEquals(bigEvent.getDescription(), event.getDescription());
	}

	@Test
	void testGetLocation() {
		assertEquals(eventLocation.getAddressString(), event.getLocation().getAddressString());
	}

	@Test
	void testGetStartDate(){
		LocalDateTime start_date = LocalDate.parse(LocalDate.now().plusDays(2).toString()).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(LocalDate.now().plusDays(4).toString()).atTime(LocalTime.parse("23:59"));
		Interval interval = Interval.from(start_date).to(end_date);
		assertEquals(interval, event.getInterval());
	}

	@Test
	void testGetTime(){
		assertEquals("15:30", event.getTime().toString());
	}


	@Test
	void testSetName() {
		event.setName("newName");
		assertEquals("newName", event.getName());
	}

	@Test
	void testSetDescription() {
		event.setDescription("newBeschreibung");
		assertEquals("newBeschreibung", event.getDescription());
	}

	@Test
	void testSetLocation() {
		Location testLocatioon = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");
		event.setLocation(testLocatioon);
		assertEquals(testLocatioon.getAddressString(), event.getLocation().getAddressString());
	}

	@Test
	void testSetDate() {
		LocalDateTime start_date = LocalDate.parse("2024-11-30").atStartOfDay();
		LocalDateTime end_date = LocalDate.parse("2024-12-01").atStartOfDay();
		Interval interval = Interval.from(start_date).to(end_date);
		event.setInterval(interval);
		assertEquals(interval, event.getInterval());
	}

	@Test
	void testSetTime() {
		event.setTime(LocalTime.parse("18:00"));
		assertEquals("18:00", event.getTime().toString());
	}

	@Test
	public void testGetTicketprice(){
		assertEquals(20, event.getTicketprice());
	}

	@Test
	public void testGetTickets(){
		assertEquals(20, event.getTickets());
	}

	@Test
	public void testSetTickets(){
		event.setTickets(150);
		assertEquals(150, event.getTickets());
	}

}