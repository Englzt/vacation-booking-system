package vacationrental.booking;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vacationrental.account.User;
import vacationrental.account.UserManagement;
import vacationrental.eventcatalog.Event;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.eventcatalog.EventType;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;
import vacationrental.util.TimeManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;


@SpringBootTest
@AutoConfigureMockMvc
@Order(22) // 20 Haus, Event,Booking,User
public class BookingControllerTests {

	
	
    @Autowired MockMvc mvc;
	@Autowired BookingController controller;


	@Autowired
	private  BookingManagement bookingManagement;
	@Autowired
	private  BookingRepository bookings;
	@Autowired
	private  UserManagement userManagement;
	@Autowired
	private  HouseManagement houseManagement;
	@Autowired
	private  EventManagement eventManagement;
	private final Location location;
	private final UserAccount.UserAccountIdentifier landlordId;
	private final UserAccount.UserAccountIdentifier userId;
	private final House house;
	private RentalInformation rental;
	private final Booking personalBooking;
	private final Interval interval;
	private final Event bigEvent;
	private final User user;
	private final Event event2;

	
	private Model model;


	BookingControllerTests(@Autowired EventManagement eventManagement, @Autowired BookingRepository bookings,
	 @Autowired UserManagement userManagement, @Autowired BookingManagement bookingManagement,
	 @Autowired HouseManagement houseManagement){
		this.bookings = bookings;
		this.userManagement = userManagement;
		this.bookingManagement = bookingManagement;
		this.houseManagement = houseManagement;
		this.eventManagement = eventManagement;
		this.landlordId = userManagement.findByUsername("myLandlord").get().getUserAccount().getId();

		this.user = userManagement.findByUsername("User").get();
		this.userId = user.getUserAccount().getId();
		this.house = houseManagement.findByName("Riverfront Cabin").toList().getFirst();

		
		bookings.deleteAll();

		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		Interval.IntervalBuilder intervalBuilder = Interval.from(TimeManager.getTime().plusDays(25));
		this.interval = intervalBuilder.to(TimeManager.getTime().plusDays(28));

		this.rental = new RentalInformation(house, interval);
		this.personalBooking = new Booking(userId, rental);
		
		this.bigEvent = eventManagement.findByName("Yoga Flow").toList().getFirst();
		this.event2 = eventManagement.findByName("Park Fußball").toList().getFirst();


		
	}

	@BeforeEach
	void setUp(@Autowired EventManagement eventManagement, @Autowired BookingRepository bookings, 
	@Autowired UserManagement userManagement, @Autowired BookingManagement bookingManagement, 
	@Autowired HouseManagement houseManagement){
		bookingManagement.addBooking(personalBooking);
		this.rental = new RentalInformation(house, interval);
	
		this.model = new ExtendedModelMap();

	}

 	@AfterEach
	void cleanup(){
		bookings.deleteAll();
		personalBooking.setOpen();
	}
    @Test	
	@WithMockUser(roles={"Landlord"})
    void testApproveRental() {      
		RedirectAttributes  attributes =  mock(RedirectAttributes.class); 
		assertEquals("redirect:/landlordrentals",
		controller.approveRental(attributes, personalBooking.getId()));
    }

	@Test
	@WithMockUser(roles={"Landlord"})
    void testConfirmRentalPayment() {
		assertEquals("redirect:/landlordrentals",
		controller.confirmRentalPayment(personalBooking.getId()));
	}

	@Test
	@WithMockUser(roles={"Customer","Landlord","EventStaff"})
	void testDeclineRental() {
        RedirectAttributes  attributes =  mock(RedirectAttributes.class);
		
		assertEquals("redirect:/null",
		controller.declineRental(personalBooking.getId(), "null",attributes));
    }

	@Test
	@WithMockUser(roles={"Customer"})
    void testGetCustomerRentals() {
		assertEquals("booking/customerrentals",
		controller.getCustomerRentals(model, Optional.of(user.getUserAccount())));
	
	}

    @Test
	@WithMockUser(roles={"EventStaff"})
    void testGetEventStaffRentals() {
	
	
		assertEquals("booking/eventstaffrentals",
		controller.getEventStaffRentals(model, Optional.of(user.getUserAccount())));
	}
    @Test
	@WithMockUser(roles={"Landlord"})
    void testGetLandlordRentals() {
		assertEquals("booking/landlordrentals",

		controller.getLandlordRentals(model, Optional.of(user.getUserAccount())));
     
    }

    @Test
	@WithMockUser(roles={"Customer"})
    void testViewRentalDetails() {
			//invalid OrderID
			assertEquals("redirect:/",
			controller.viewRentalDetails(null, model));

			//Success customer
			assertEquals("booking/rentaldetails",
			controller.viewRentalDetails(personalBooking.getId(), model));

			
			Booking eventBooking = new Booking(userManagement.findByUsername("myEventStaff").get().getUserAccount().getId(),
			event2,50, rental);
			bookingManagement.addBooking(eventBooking);

			assertEquals("booking/ticketDetails",
			controller.viewRentalDetails(eventBooking.getId(), model));

			bookings.delete(eventBooking);
	}
	
    @Test
	@WithMockUser(roles={"Customer"})
    void testBookingConfirmation() {
		String start_date = LocalDate.now().plusDays(2).toString();
		String end_date =  LocalDate.now().plusDays(5).toString();

		assertEquals("booking/bookingConfirmation",
		controller.bookingConfirmation(
			house.getId(),
			start_date,
			end_date,
			null, 
			null, 
			Optional.of(user.getUserAccount()), 
			model));
		
	}

	@Test
	void testCancelBooking() {

		
        RedirectAttributes  attributes =  mock(RedirectAttributes.class);
		assertEquals("booking/bookingCancellation", 
		controller.cancelBooking(model, personalBooking.getId(), "", null));
		
	}

	@Test
	@WithMockUser(roles={"Customer"})
	void testGetCustomerTickets() {
	
		assertEquals("booking/tickets", 
		controller.getCustomerTickets(model, Optional.of(user.getUserAccount())));
	}

	@Test
	@WithMockUser(roles={"Landlord"})
	void testGetHouseBookingHistory() {

		assertEquals("house/rentalHistory",
		controller.getHouseBookingHistory(house.getId(), model, Optional.of(user.getUserAccount())));
	}

	@Test
	@WithMockUser(roles={"Landlord"})
	void testGetHouseStatistics() {
		assertEquals("house/openBookings",
		controller.getHouseStatistics(house.getId(), model, Optional.of(user.getUserAccount())));
	}
 
	@Test
	@WithMockUser(roles={"Customer"})
	void testRentHouse() {
		
        RedirectAttributes  attributes =  mock(RedirectAttributes.class);

		String start_date = LocalDate.now().plusDays(2).toString();
		String end_date =  LocalDate.now().plusDays(5).toString();
		String url = "redirect:/rent/bookingConfirmation?houseId="+house.getId()
		+"&intervalStart="+start_date +"&intervalEnd=" +end_date;

		//Success
		assertEquals(url,		
		controller.rentHouse(
			house.getId(),
			start_date,
			end_date,
			model,
			attributes,
			Optional.of(user.getUserAccount())
			));	

		url = "redirect:/house/" + house.getId();
		
		//fail interval
		start_date = LocalDate.now().minusYears(1).toString();
		end_date = LocalDate.now().minusYears(1).toString();
		assertEquals(url,
		controller.rentHouse(
			house.getId(),
			start_date,
			end_date,
			model,
			attributes,
			Optional.of(user.getUserAccount())
			));	

	}


	@Test
	@WithMockUser(roles={"Landlord"})
	void testSendLateNotice() {
		assertEquals("redirect:/landlordrentals",
		controller.sendLateNotice(personalBooking.getId()));	
	}


	@Test
	@WithMockUser(roles={"Customer"})
	void testShowTicketRefundPage(@Autowired BookingManagement bookingManagement) {
		
		Booking eventBooking = new Booking(userManagement.findByUsername("myEventStaff").get().getUserAccount().getId(),
		event2.getId(), rental, EventType.SMALL_EVENT);
		bookingManagement.addBooking(eventBooking);
		assertEquals("booking/cancelSingleTickets",
		controller.showTicketRefundPage(eventBooking.getId(), model));
		
		//cleanup
		bookings.delete(eventBooking);
		
	}

	@Test
	@WithMockUser(roles={"Customer"})
    void testRentHouseWithEvents() {



		String start_date = LocalDate.now().plusDays(2).toString();
		String end_date =  LocalDate.now().plusDays(5).toString();


		List<ProductIdentifier> eventIdlist = new ArrayList<>();
		eventIdlist.add(bigEvent.getId()); // Beispiel für eine ID
		eventIdlist.add(event2.getId());

		List<Integer> bookedTicketslist = new ArrayList<> ();
		bookedTicketslist.add(2);
		bookedTicketslist.add(4);
		String viewName = controller.rentHouseWithEvents(
		house.getId(),
		start_date,
		end_date,
		eventIdlist,
		bookedTicketslist,
		Optional.of(user.getUserAccount()),
		model);

		assertEquals("booking/bookingSuccess", viewName);

    }

	
}
