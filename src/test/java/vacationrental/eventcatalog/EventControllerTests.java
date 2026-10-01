package vacationrental.eventcatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vacationrental.account.UserManagement;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Order(21) // 20 Haus, Event,Booking,User 
public class EventControllerTests {
	
    @Autowired MockMvc mvc;
	@Autowired EventController controller;

	@Autowired
	private EventManagement eventManagement;

	@Autowired
	private HouseManagement houseManagement;
	
	@Autowired 
	private UserManagement userManagement;

	private final Location eventLocation;

	private final UserAccountIdentifier eventStaffId;
	private final Event cr_bigEvent;
	private final Event cr_smallEvent;
	private final EventForm cr_SmallForm;
	private final EventForm cr_BigForm;
	private Event bigEvent,smallEvent;
	private Model model;

	ProductIdentifier fake = new Event().getId();
	private final EventForm wrongBigForm;
	private final EventForm wrongSmallForm;

	@Autowired
	EventControllerTests(@Autowired HouseManagement houseManagement,
						 @Autowired  UserManagement userManagement,
						@Autowired EventManagement eventManagement,
						 @Autowired EventController controller) {

		this.controller = controller;

		userManagement.findByUsername("myEventStaff").get().getUserAccount().setEnabled(true);
		this.eventStaffId = userManagement.findByUsername("myEventStaff").get().getUserAccount().getId();
		this.eventLocation = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");
		//this.myHouse = houseManagement.findByName("Historic Vineyard House").toList().getFirst();

		this.cr_bigEvent = eventManagement.findByName("Yoga Flow").toList().getFirst();

		this.cr_smallEvent = eventManagement.findByName("Winter Spaziergang").toList().getFirst();


		this.cr_SmallForm = new EventForm(
			"Winter Spaziergang",
			"<hier kann eine (kleines) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().plusDays(5).toString(),
			LocalTime.parse("15:30"),
			eventStaffId,
			RecurrencePattern.NONE,
			LocalDate.now().plusMonths(5).toString());

		this.cr_BigForm = new EventForm(
			"Yoga Flow",
			"<hier kann eine (große) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(8).toString(),
			LocalDate.now().plusDays(10).toString(),
			LocalTime.parse("15:30"),
			15,
			100,
			eventStaffId
		);
		this.wrongBigForm = new EventForm(
			"BigControlerTestEvent",
			"<hier kann eine (große) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(2).toString(),
			LocalDate.now().minusYears(1).toString(),
			LocalTime.parse("15:30"),
			15,
			100,
			eventStaffId
		);
		
		this.wrongSmallForm = new EventForm("SmallControlerTestEvent",
			"<hier kann eine (kleines) wunderbar schöne Beschreibung Stehen>",
			eventLocation.getAddressString(),
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().minusYears(1).toString(),
			LocalTime.parse("15:30"),
			eventStaffId,
			RecurrencePattern.NONE,
			LocalDate.now().plusMonths(5).toString());

	}

	@BeforeEach
	void setUp(@Autowired EventManagement eventManagement, 
	@Autowired UserManagement userManagement) {
		// Setze den Testzustand zurück oder initialisiere ihn neu.

		this.bigEvent = cr_bigEvent;
		this.smallEvent = cr_smallEvent;
		this.model = new ExtendedModelMap();

	
	}
	 
	@Test
	@WithMockUser(roles={"Customer","EventStaff"})
	void testCreateBigEvent() {


		BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

		MultipartFile[] files = new MultipartFile[0]; // Keine neuen Dateien

		assertEquals("event/createBigEvent",
		controller.createBigEvent(wrongBigForm ,bindingResult,model, files,
		Optional.of(userManagement.findByUsername("myEventStaff").get().getUserAccount())));

		String viewName =controller.createBigEvent(cr_BigForm ,bindingResult,model,files,
		Optional.of(userManagement.findByUsername("myEventStaff").get().getUserAccount()));
		assertEquals("event/bookHouseForEvent", viewName);
	}
@Test
	@WithMockUser(roles={"EventStaff"})
	void testCreateSmallEvent() {

		MultipartFile[] files = new MultipartFile[0]; // Keine neuen Dateien

		assertEquals("event/createSmallEvent",
		controller.createSmallEvent(wrongSmallForm ,model, files, 20000,
			Optional.of(userManagement.findByUsername("myEventStaff").get().getUserAccount())));

			//"event/createSmallEvent"
		String viewName =controller.createSmallEvent(cr_SmallForm ,model,files, 20000,
			Optional.of(userManagement.findByUsername("myEventStaff").get().getUserAccount()));
		assertEquals("event/bookAdvertisments",viewName);
	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testDeleteEvent() {
		assertEquals("redirect:/myEvents",
		controller.deleteEvent( smallEvent.getId()));
	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testEditBigEvent() {
		
		BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

		MultipartFile[] files = new MultipartFile[0]; // Keine neuen Dateien
        		
		assertEquals("event/editBigEvent",
		controller.editBigEvent(wrongBigForm ,bindingResult,fake,model, files,100));
		
		cr_bigEvent.setName("Yoga Flow");
		String viewName =controller.editBigEvent(cr_BigForm ,bindingResult,
		bigEvent.getId(),model,files,100); 
		assertEquals("redirect:/myEvents", viewName);
		


	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testEditSmallEvent() {
		BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

		MultipartFile[] files = new MultipartFile[0]; // Keine neuen Dateien
		RedirectAttributes  attributes =  mock(RedirectAttributes.class); // Korrektes Objekt für RedirectAttributes

		assertEquals("event/editSmallEvent",
		controller.editSmallEvent(model, wrongSmallForm ,bindingResult,fake,files,attributes));

		String viewName =controller.editSmallEvent(model,cr_SmallForm ,bindingResult,
		bigEvent.getId(),files, attributes);
		assertEquals("redirect:/myEvents/chooseAdvertismentRadius/"+bigEvent.getId(), viewName);

	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testShowCreateBigEvent() {
		assertEquals("event/createBigEvent",
		controller.showCreateBigEvent(model,
		Optional.of(userManagement.findByUsername("myEventStaff").get().getUserAccount())));

	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testShowCreateSmallEvent() {
		assertEquals("event/createSmallEvent",
		controller.showCreateSmallEvent(model,
	Optional.of(userManagement.findByUsername("myEventStaff").get().getUserAccount())));
	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testShowDeleteEvent() {
		
		assertEquals("event/deleteEvent",
		controller.showDeleteEvent(bigEvent.getId(),model));
		 
	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testShowEditBigEvent() {
		assertEquals("redirect:/myEvents",
		controller.showEditBigEvent(model, fake));
		assertEquals("event/editBigEvent",
		controller.showEditBigEvent(model, bigEvent.getId()));
		
	}

	@Test
	@WithMockUser(roles={"EventStaff"})
	void testShowEditSmallEvent() {
		assertEquals("redirect:/myEvents",
		controller.showEditSmallEvent(model, fake));
		assertEquals("event/editSmallEvent",
		controller.showEditSmallEvent(model, smallEvent.getId()));	
	}

	@Test
	void testShowEvent() {

		UserAccount user = userManagement.findAll().toList().getFirst().getUserAccount();
		assertEquals("redirect:/events",
		controller.showEvent(fake,model,Optional.of(user)));


		assertEquals("event/event",
		 controller.showEvent(bigEvent.getId(),model,Optional.of(user)));

	}

	@Test
	void testShowEvents() {
    	assertEquals("event/events", controller.showEvents(model));

	}


	@Test
	@WithMockUser(roles={"EventStaff"})
	void testShowbookableHouses() {
		
		assertEquals("event/bookHouseForEvent", 
		controller.showBookableHouses(model, bigEvent.getId()));
		
	}

		
}

