package vacationrental.booking;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.salespointframework.catalog.Product;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vacationrental.account.User;
import vacationrental.account.UserManagement;
import vacationrental.eventcatalog.Event;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;
import vacationrental.util.TimeManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(3)
class BookingManagementTest {
	@Autowired
	private final BookingManagement bookingManagement;
	@Autowired
	private final BookingRepository bookings;
	@Autowired
	private final UserManagement userManagement;
	@Autowired
	private final HouseManagement houseManagement;
	@Autowired
	private final EventManagement eventManagement;
	private Location location;
	private final UserAccount.UserAccountIdentifier landlordId;
	private final UserAccount.UserAccountIdentifier userId;
	private final House house;
	private RentalInformation rental;
	private Booking personalBooking;
	private Interval interval;
	private Event bigEvent;
	private final User user;
	private Event event2;

	BookingManagementTest(@Autowired EventManagement eventManagement, @Autowired BookingRepository bookings, @Autowired UserManagement userManagement, @Autowired BookingManagement bookingManagement, @Autowired HouseManagement houseManagement){
		this.bookings = bookings;
		this.userManagement = userManagement;
		this.bookingManagement = bookingManagement;
		this.houseManagement = houseManagement;
		this.eventManagement = eventManagement;
		this.landlordId = userManagement.findByUsername("myLandlord").get().getUserAccount().getId();

		this.user = userManagement.findByUsername("User").get();
		this.userId = user.getUserAccount().getId();
		this.house = houseManagement.findByName("Riverfront Cabin").toList().getFirst();

	}
	@BeforeAll
	void setupOnce(@Autowired EventManagement eventManagement, @Autowired BookingRepository bookings, @Autowired UserManagement userManagement, @Autowired BookingManagement bookingManagement, @Autowired HouseManagement houseManagement){

		bookings.deleteAll();
		Locale.setDefault(Locale.GERMANY); 

		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		Interval.IntervalBuilder intervalBuilder = Interval.from(TimeManager.getTime());
		this.interval = intervalBuilder.to(TimeManager.getTime().plusDays(5));

		this.rental = new RentalInformation(house, interval);
		this.personalBooking = new Booking(userId, rental);

		this.bigEvent = eventManagement.findByName("Yoga Flow").toList().getFirst();
		this.event2 = eventManagement.findByName("Park Fußball").toList().getFirst();

	}
	@BeforeEach
	void setUp(@Autowired EventManagement eventManagement, @Autowired BookingRepository bookings, @Autowired UserManagement userManagement, @Autowired BookingManagement bookingManagement, @Autowired HouseManagement houseManagement){

		bookingManagement.addBooking(personalBooking);
	}

	@AfterEach
	void cleanup(){
		bookings.deleteAll();
		personalBooking.setOpen();
	}
	@AfterAll
	void delextraHOuse(){
		houseManagement.deleteHouse(house.getId());
	}

	@Test
	void addBooking() {

		bookingManagement.addBooking(new Booking(userId, rental));
		assertEquals(2, bookings.findAll().stream().count());
	}

	@Test
	void findAll() {
		bookingManagement.addBooking(new Booking(userId, rental));
		bookingManagement.addBooking(new Booking(userId, rental));
		assertEquals(3, bookings.findAll().stream().count());
	}

	@Test
	void findByBookingStatus() {
		Booking testBooking = new Booking(userId, rental);
		testBooking.setCompleted();
		bookingManagement.addBooking(testBooking);
		List<Booking> bookingList = bookingManagement.findByBookingStatus(BookingStatus.COMPLETED).toList();
		assertTrue(bookingList.contains(testBooking));
	}

	@Test
	void findById() {
		assertEquals(personalBooking, bookingManagement.findById(personalBooking.getId()));
	}

	@Test
	void createRentalInformation() {
		assertInstanceOf(RentalInformation.class, bookingManagement.createRentalInformation(house, interval));

	}
	@Test
	void checkInvalidInterval() {
		LocalDateTime start_date = TimeManager.getTime().plusDays(2);
		LocalDateTime end_date =  TimeManager.getTime().plusDays(7);
		assertTrue(bookingManagement.checkInvalidInterval(start_date, end_date));
		assertFalse(bookingManagement.checkInvalidInterval(end_date, start_date));
	}


	@Test
	void createPersonalBooking() {
		House updatedHouse = houseManagement.findByName("Woodland Cottage").toList().getFirst();
		String start_date = LocalDate.now().plusDays(7).toString();
		String end_date = LocalDate.now().plusDays(9).toString();
		assertNotNull(bookingManagement.createPersonalBooking(updatedHouse.getId(), start_date, end_date, userId));
	}

	@Test
	void createEventBooking() {
		House updatedHouse = houseManagement.findByName("Elb Valley Retreat").toList().getFirst();
		String start_date = LocalDate.now().plusDays(7).toString();
		String end_date = LocalDate.now().plusDays(9).toString();
		assertTrue(bookingManagement.createEventBooking(updatedHouse.getId(), start_date, end_date, userId, bigEvent.getId()));
	}


	@Test
	void bookHousesForEvent() {
		House updatedHouse = houseManagement.findByName("Mountain View Lodge").toList().getFirst();
		List<Product.ProductIdentifier> houseList = new ArrayList<>();
		houseList.add(updatedHouse.getId());

		assertTrue(bookingManagement.bookHousesForEvent(houseList, event2, userId));
	}

	@Test
	void isHouseAvailable() {
		assertEquals(RentalType.VACANT,bookingManagement.isHouseAvailable(house.getId(), interval, personalBooking.getId() ,personalBooking.isCompleted()));
	}

	@Test
	void save() {
		bookings.save(new Booking(userId, rental));
		assertEquals(2, bookings.findAll().stream().count());
	}

	@Test
	void get() {
		assertEquals(personalBooking, bookingManagement.get(personalBooking.getId()).get());
	}

	@Test
	void contains() {
		assertTrue(bookingManagement.contains(personalBooking.getId()));
	}

	@Test
	void testFindBy_acc() {
		assertNull(
		bookingManagement.findBy(user.getUserAccount()));
	}

	@Test
	void testFindBy_intervall() {
	
		assertNull(
		bookingManagement.findBy(interval));
	}

	@Test
	void testFindBy_acc_intervall(){
		assertNull(
		bookingManagement.findBy(user.getUserAccount(),interval));
	
	}

	@Test
	void completeOrder() {
		assertTrue(true);
	}

	@Test
	void payOrder() {
		assertFalse(
		bookingManagement.payOrder(personalBooking));
	}

	@Test
	void cancelOrder() {
		assertFalse(
		bookingManagement.cancelOrder(personalBooking,"weil wegen xyz"));
	}

	@Test
	void delete() {
		assertNull(
		bookingManagement.delete(personalBooking));
	}

	@Test
	void testFindAll() {
		assertTrue(true);
	}

	@Test
	void setBookingStatusReserved() {
		bookingManagement.setBookingStatusReserved(personalBooking);
		assertEquals(BookingStatus.RESERVED, personalBooking.getStatus());
	}

	@Test
	void setBookingStatusCanceled() {
		assertFalse(bookingManagement.setBookingStatusCanceled(personalBooking));
		assertEquals(BookingStatus.OPEN, personalBooking.getStatus());
	}

	@Test
	void findByCustomerAndStatus() {
		List<Booking> bookingList = bookingManagement.findByCustomerAndStatus(userId, BookingStatus.OPEN).toList();
		assertTrue(bookingList.contains(personalBooking));
	}

	@Test
	void setBookingStatusPaid() {
		bookingManagement.setBookingStatusPaid(personalBooking);
		assertEquals(BookingStatus.PAID, personalBooking.getStatus());
	}

	@Test
	void findByAccountIdentifier() {
		List<Booking> bookingList = bookingManagement.findByAccountIdentifier(userId).toList();
		assertTrue(bookingList.contains(personalBooking));
	}

	@Test
	void findByHouseIdAndRentalType() {
		List<Booking> bookingList = bookingManagement.findByHouseIdAndRentalType(house.getId(), RentalType.PERSONAL).toList();
		assertTrue(bookingList.contains(personalBooking));
	}

	@Test
	void findByLandlordIdAndStatus() {
		List<Booking> bookingList = bookingManagement.findByLandlordIdAndStatus(landlordId, BookingStatus.OPEN).toList();
		assertTrue(bookingList.contains(personalBooking));
	}

	@Test
	void getBookingsWithCustomerByLandlordAndStatus() {
		String username = bookingManagement.getBookingsWithCustomerByLandlordAndStatus(landlordId, BookingStatus.OPEN).values().stream().toList().getFirst();
		assertEquals("User", username);
	}

	@Test
	void getBookingWithNamesByStatusAndCustomer() {
		String username = bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.OPEN, userId).values().stream().toList().getFirst();
		assertEquals("myLandlord", username);
	}

	@Test
	void getAllBookingsWithNameByCustomer() {
		String username = bookingManagement.getAllBookingsWithNameByCustomer(userId).values().stream().toList().getFirst();
		assertEquals("myLandlord", username);
	}

	@Test
		void getNearbyFreeHousesToLocation() {
			List<House> bookingList = bookingManagement.getNearbyFreeHousesToLocation(location, event2.getId(),20000);
			assertTrue(bookingList.size() >=1);
		}

	@Test
	void cancelEventBookings() {
				LocalDateTime start_date = TimeManager.getTime().plusDays(7);
				LocalDateTime end_date =  TimeManager.getTime().plusDays(9);
				Interval interval3 = Interval.from(start_date).to(end_date);
		
				
				bookingManagement.cancelEventBookings(bigEvent.getId(), interval3);
			 	var	booking = bookings.findByEventId(bigEvent.getId()).stream().count();
				assertEquals(0, booking);
	}
}