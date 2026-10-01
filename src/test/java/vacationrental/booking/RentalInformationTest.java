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
import vacationrental.housecatalog.House;
import vacationrental.location.Location;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.salespointframework.core.Currencies.EURO;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RentalInformationTest {
	@Autowired
	private final UserManagement userManagement;
	private Interval interval;
	private House house;
	private RentalInformation rental;
	private UserAccount.UserAccountIdentifier landlordId;

	RentalInformationTest(@Autowired UserManagement userManagement) {
		this.userManagement = userManagement;
	}


	@BeforeAll
	void setUpOnce(@Autowired UserManagement userManagement) {
		LocalDateTime start_date = LocalDate.parse("2024-11-30").atStartOfDay();
		LocalDateTime end_date = LocalDate.parse("2024-12-01").atStartOfDay();
		this.interval = Interval.from(start_date).to(end_date);

		User user = userManagement.create("MyNewTestLandlord1", "321", Role.of("Landlord"));
		this.landlordId = user.getUserAccount().getId();
		Location location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		this.house = new House(
			"TestHouse",
			Money.of(100, EURO),
			"Test Beschreibung",
			location,
			3, 2, 1, 1,
			true, false,
			landlordId
		);

	}

	@BeforeEach
	void setUp() {
		this.rental = new RentalInformation(house, interval);
	}

	@Test
	void getHouse() {
		assertNotNull(rental.getHouse());
		assertEquals(house, rental.getHouse());
	}

	@Test
	void setHouse() {

		Location location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
		House house2 = new House(
			"TestHouse",
			Money.of(100, EURO),
			"Test Beschreibung",
			location,
			3, 2, 1, 1,
			true, false,
			landlordId
		);

		rental.setHouse(house2);
		assertEquals(house2, rental.getHouse());
	}

	@Test
	void getInterval() {
		assertEquals(interval, rental.getInterval());
	}

	@Test
	void getStartDateString() {
		assertEquals("30.11.2024", rental.getStartDateString());
	}

	@Test
	void getEndDateString() {
		assertEquals("01.12.2024", rental.getEndDateString());
	}
}