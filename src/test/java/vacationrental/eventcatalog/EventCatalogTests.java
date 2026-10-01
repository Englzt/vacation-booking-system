package vacationrental.eventcatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.test.annotation.DirtiesContext;

import io.micrometer.common.lang.NonNull;
import net.bytebuddy.implementation.bind.annotation.IgnoreForBinding;
import org.springframework.util.Assert;
import vacationrental.location.Location;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import vacationrental.account.User;
import vacationrental.account.UserManagement;
import vacationrental.housecatalog.HouseCatalog;
import vacationrental.housecatalog.House;


import static org.salespointframework.core.Currencies.EURO;

import org.javamoney.moneta.Money;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventCatalogTests {
	@Autowired
	private EventManagement eventManagement;

	private EventCatalog eventCatalog;
	private HouseCatalog houseCatalog;

	private Location location, eventLocation;

	private User userLandlord, userEventStaff;

	private UserAccountIdentifier landlordId, eventStaffId;
	private Event bigEvent, smallEvent;
	private House myHouse;

	@Autowired
	EventCatalogTests(EventCatalog eventCatalog, HouseCatalog houseCatalog) {

		this.eventCatalog = eventCatalog;
		this.houseCatalog = houseCatalog;
		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		this.eventLocation = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
	}

	@BeforeAll
	void setUpOnce(@Autowired UserManagement userManagement) {

		this.userEventStaff = userManagement.create("MyNewTestEventStaff2", "0987", Role.of("EventStaff"));
		this.eventStaffId = userEventStaff.getUserAccount().getId();

		this.userLandlord = userManagement.create("MyNewTestEventLandlord2", "32100", Role.of("Landlord"));
		this.landlordId = userLandlord.getUserAccount().getId();
	}


	@BeforeEach
	void setUp(@Autowired EventCatalog eventCatalog, @Autowired HouseCatalog houseCatalog) {

		this.eventCatalog = eventCatalog;
		this.houseCatalog = houseCatalog;
		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		this.landlordId = userLandlord.getUserAccount().getId();
		this.eventStaffId = userEventStaff.getUserAccount().getId();

		House testHouse = new House(
			"TestHouse",
			Money.of(100, EURO),
			"Test Beschreibung",
			location,
			3, 2, 1, 1,
			true, false,
			landlordId
		);
		this.myHouse = houseCatalog.save(testHouse);


		double ticketprice = 15;
		EventForm bigForm = new EventForm(
			"BigTestEvent",
			"<hier kann eine (große) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().plusDays(5).toString(),
			LocalTime.parse("15:30"),
			ticketprice,
			100,
			eventStaffId
		);
		this.bigEvent = eventManagement.createBigEvent(bigForm);

		EventForm smallForm = new EventForm(
			"SmallTestEvent",
			"<hier kann eine (kleine) wunderbar schöne Beschreibung Stehen >",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(1).toString(),
			LocalDate.now().plusDays(3).toString(),
			LocalTime.parse("18:00", DateTimeFormatter.ofPattern("HH:mm")),
			eventStaffId,
			RecurrencePattern.NONE,
			LocalDate.now().toString()
		);
		this.smallEvent = eventManagement.createSmallEvent(smallForm);

	}

	@Test
	public void testFindById() {
		Optional<Event> event = eventCatalog.findById(bigEvent.getId());
		assertEquals(bigEvent, event.get());
		assertEquals(bigEvent.getId(), event.get().getId());
	}

	@Test
	public void testFindByName() {
		Streamable<Event> newBigEvent = eventCatalog.findByName(bigEvent.getName());
		assertNotNull(newBigEvent, "newBigEvent darf nicht null sein.");
		boolean found = newBigEvent.stream().anyMatch(event -> event.getName().equals("BigTestEvent"));
		assertTrue(found, "Ein Event mit dem Namen 'BigTestEvent' wurde gefunden.");
	}

	@Test
	public void testFindByLocation() {
		Streamable<Event> newBigEvent = eventCatalog.findByLocation(bigEvent.getLocation());
		assertNotNull(newBigEvent, "newBigEvent darf nicht null sein.");

		boolean found = newBigEvent.stream().anyMatch(event -> event.getLocation().getAddressString().equals(eventLocation.getAddressString()));
		assertTrue(found, "Ein Event mit der Adresse wurde gefunden.");
	}
}

