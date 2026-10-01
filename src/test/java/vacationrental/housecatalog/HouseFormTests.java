package vacationrental.housecatalog;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vacationrental.account.UserManagement;
import vacationrental.location.Location;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;


import static org.salespointframework.core.Currencies.EURO;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HouseFormTests {

	@Autowired
	private UserManagement userManagement;

	private HouseForm mytestForm;

	private Location location;
	private UserAccountIdentifier landlordId;

	@BeforeAll
	void groundwork(@Autowired UserManagement userManagement){

		this.landlordId = userManagement.findByUsername("myLandlord1").get().getUserAccount().getId();
		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
	}

	@BeforeEach
	void setUp(@Autowired HouseCatalog catalog,@Autowired UserManagement userManagement ) {

		this.mytestForm = new HouseForm(
			"newHouse1234",
			"Test Beschreibung",
			location.getAddressString(),
			Money.of(23,EURO),
			5,
			4,
			3,
			2,
			true,true);
	}


	@Test
	void testCreatFormWithHouse(){

		House testHouse = new House(
			"TestHouse",
			Money.of(0, EURO),
			"Test Beschreibung",
			location,
			5,
			4,
			3,
			2,
			true, false,landlordId
		);
		HouseForm createHouse = new HouseForm(testHouse);
		assertEquals("TestHouse",createHouse.getName());
		assertEquals("Test Beschreibung", createHouse.getDescription());
		assertEquals(location.getAddressString(), createHouse.getLocation());
		assertEquals(2, createHouse.getBathrooms());
		assertEquals(4, createHouse.getBeds());
		assertEquals(3, createHouse.getKitchen());
		assertEquals(5, createHouse.getMaxPerson());
		assertFalse(createHouse.getHandicappedAccessible());
		assertTrue(createHouse.getParkingSpot());
	}

	@Test
	void testStandartKonstruktor(){
		HouseForm hF = new HouseForm();
		assertNull(hF.getName());
		assertNull(hF.getDescription());
		assertNull(hF.getLocation());
		assertEquals(0, hF.getBathrooms());
		assertEquals(0, hF.getBeds());
		assertEquals(0, hF.getKitchen());
		assertEquals(0, hF.getMaxPerson());
		assertFalse(hF.getHandicappedAccessible());
		assertFalse(hF.getParkingSpot());
	}


	@Test
	void testGetBathrooms() {
		assertEquals(2, mytestForm.getBathrooms());
	}

	@Test
	void testGetBeds() {
		assertEquals(4, mytestForm.getBeds());
	}

	@Test
	void testGetDescription() {

		assertEquals("Test Beschreibung", mytestForm.getDescription());
	}

	@Test
	void testGetHandicappedAccessible() {
		assertTrue(mytestForm.getHandicappedAccessible());
	}

	@Test
	void testGetKitchen() {
		assertEquals(3, mytestForm.getKitchen());
	}

	@Test
	void testGetLocation() {
		assertEquals(location.getAddressString(), mytestForm.getLocation());

	}

	@Test
	void testGetMaxPerson() {
		assertEquals(5,mytestForm.getMaxPerson());

	}

	@Test
	void testGetName() {
		assertEquals("newHouse1234", mytestForm.getName());

	}

	@Test
	void testGetParkingSpot() {

		assertTrue(mytestForm.getParkingSpot());
	}


	@Test
	void testGetPrice_money() {
		assertEquals(Money.of(23, EURO), mytestForm.getPriceMoney());
	}

	@Test
	void testSetBathrooms() {
		mytestForm.setBathrooms(10);
		assertEquals(10, mytestForm.getBathrooms());

	}

	@Test
	void testSetBeds() {

		mytestForm.setBeds(10);
		assertEquals(10, mytestForm.getBeds());

	}

	@Test
	void testSetDescription() {
		mytestForm.setDescription("SuperCoolNewTestDescription");
		assertEquals("SuperCoolNewTestDescription", mytestForm.getDescription());

	}

	@Test
	void testSetHandicappedAccessible() {
		mytestForm.setHandicappedAccessible(false);
		assertFalse( mytestForm.getHandicappedAccessible());
	}

	@Test
	void testSetKitchen() {
		mytestForm.setKitchen(10);
		assertEquals(10, mytestForm.getKitchen());

	}

	@Test
	void testSetLocation() {
		Location testLocatioon = new Location("Bergstraße", "64", "01069", "Dresden", "Germany");
		mytestForm.setLocation(testLocatioon.getAddressString());
		assertEquals(testLocatioon.getAddressString(), mytestForm.getLocation());

	}

	@Test
	void testSetMaxPerson() {
		mytestForm.setMaxPerson(10);
		assertEquals(10, mytestForm.getMaxPerson());

	}

	@Test
	void testSetName() {
		mytestForm.setName("SuperNewTestFormName");
		assertEquals("SuperNewTestFormName", mytestForm.getName());

	}

	@Test
	void testSetParkingSpot() {
		mytestForm.setParkingSpot(true);
		assertTrue( mytestForm.getParkingSpot());
	}

	@Test
	void testSetPrice_money() {
		mytestForm.setPriceMoney(Money.of(10,EURO));
		assertEquals(Money.of(10,EURO), mytestForm.getPriceMoney());
	}
}
