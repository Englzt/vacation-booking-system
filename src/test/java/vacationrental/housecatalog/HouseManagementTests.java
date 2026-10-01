package vacationrental.housecatalog;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import vacationrental.account.UserManagement;
import vacationrental.location.Location;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.salespointframework.core.Currencies.EURO;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(1)
public class HouseManagementTests {

	@Autowired
	private HouseManagement houseManagement;

	private HouseCatalog houseCatalog;


	private HouseForm form;

	private final Location location;

	private House myTestHouse;

	private static UserManagement userManagement;

	private final UserAccountIdentifier landlordId;
	private final UserAccountIdentifier llId1;
	private final UserAccountIdentifier llId2;



	@Autowired
	HouseManagementTests(HouseCatalog catalog, @Autowired UserManagement userManagement, @Autowired HouseManagement houseManagement) {
		this.houseCatalog = catalog;
		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");

		this.landlordId = userManagement.findByUsername("myLandlord").get().getUserAccount().getId();
		this.llId1 = userManagement.findByUsername("myLandlord1").get().getUserAccount().getId();
		this.llId2 = userManagement.findByUsername("myLandlord2").get().getUserAccount().getId();


	}


	@BeforeEach
	void setUp(@Autowired HouseCatalog catalog, @Autowired HouseManagement houseManagement ) {
		// Setze den Testzustand zurück oder initialisiere ihn neu.
		this.houseCatalog = catalog;

		this.myTestHouse = houseManagement.findByName("Mountain View Lodge").toList().getFirst();
	}




	// kein landlordID dabei
	@Test
	public void testCreateHouse() {

		House haus1 = new House(
			"Riverfront Cabin",  Money.of(125,EURO),landlordId);

		House house_create = houseManagement.addHouse(haus1);

		// Überprüfe, ob das Haus erfolgreich erstellt wurde
		assertNotNull(house_create);
		assertEquals(house_create, haus1);
		assertEquals(house_create.getPrice(), haus1.getPrice());

		//cleanup Houses
		testDeleteHouse(haus1);
	}

	//Aus Hausform erstellt
	@Test
	public void testAddHouse() {

		// Erstelle ein Testformular
		form = new HouseForm(
			"newHouse1234",
			"Test Beschreibung",
			location.toString(),
			Money.of(23,EURO),
			5,4,3,2,
			true,true);


		House house = houseManagement.addHouse(form, landlordId);

		assertNotNull(house);
		assertEquals("newHouse1234", house.getName());

		// Überprüfen, ob das House im HouseCatalog vorhanden
		Iterable<House> house_list = houseCatalog.findAll();
		boolean houseFound = false;
		for (House h : house_list) {
			if (h.getName().equals("newHouse1234")) {
				houseFound = true;
				break;
			}
		}

		assertTrue(houseFound, "Das Haus sollte im Katalog gespeichert sein.");
		//cleanup House
		houseCatalog.delete(house);
	}

	//Del
	public void testDeleteHouse(House h) {

		assertNotNull(h);

		//löschen

		houseManagement.deleteHouse(h.getId());
		List<House> bookingList = houseCatalog.findAll().toList();
		assertFalse(bookingList.contains(h), "Haus ist aus dem Catalog gelöscht");

	}



	//update Test
	@Test
	public void testUpdateHouse() {
		House modHouse = houseManagement.findByName("Historic Vineyard House").toList().getFirst();


		form = new HouseForm(
			"UPDATED_HOUSE",
			"new_up Beschreibung",
			location.toString(),
			Money.of(100,EURO),
			10,10,30,20,
			true,false);

		House up_House = houseManagement.updateHouse(modHouse.getId(), form);

		assertEquals("UPDATED_HOUSE", up_House.getName());
		assertEquals(up_House.getId(), modHouse.getId());
	}

	@Test
	public void testToString(){
		String result = "House:  " +
			myTestHouse.getDescription() + ", " +
			myTestHouse.getMaxPerson() + ", " +
			myTestHouse.getBeds() + ", " +
			myTestHouse.getKitchen() + ", " +
			myTestHouse.getBathrooms() + ", " +
			myTestHouse.getParkingSpot() + ", " +
			myTestHouse.getHandicappedAccessible();
		assertEquals(result,myTestHouse.toString());
	}


	//findById Test
	@Test
	public void testFindById(){

		Optional<House> h = houseCatalog.findById(myTestHouse.getId());

		assertEquals(myTestHouse, h.get());
		assertEquals(myTestHouse.getId(), h.get().getId());

	}


	// Testen ob Haus die Richtige LAndlordid hat
	@Test
	public void testGetAllHousesWithLandlordNames(){


		UserAccountIdentifier userid = llId1;
		UserAccountIdentifier userid2  = llId2 ;
		// für x=5 häuser 
		float housecreations = 5;


		float i = housecreations;
		while (i>0) {
			UserAccountIdentifier tempId = userid2;
			if(i%2==1)tempId = userid;

			House houseNew= new House(
				"TestHouse" + i,  Money.of(125,EURO),
				"Hallo das ist "+i, location,
				7, 6 , 1 ,1,
				true, false,tempId);

			houseCatalog.save(houseNew);
			i--;
		}
		//Checke ob der Name aller Landlord zum zugehöigen haus stimmt

		Streamable<Pair<House, String>> allNames = houseManagement.getAllHousesWithLandlordNames();


		for (Pair<House, String> checkName : allNames) {

			//LandlorndnameByHouseId() , name der mit zu dem Haus gespeichert ist
			assertEquals(houseManagement.getLandlordName(checkName.getFirst().getId()),checkName.getSecond());
		}


		//ceil weil evtl 1 Haus mehr als "landlord2" bei ungerade
		testFindByLandlordId(userid,Math.ceil( housecreations/2));
		testFindByLandlordId(userid2,Math.floor(housecreations/2));

	}
	// wird in ^ getest, cleancode
	void testFindByLandlordId(@Autowired UserAccountIdentifier id, double assertCount){
		Streamable <House> h = houseManagement.findByLandlordId(id);

		assertNotNull(h);
		assertFalse(h.isEmpty());
		int count=0;
		for (House test_h : h) {
			count++;
			// cleanup tests
			houseManagement.deleteHouse(test_h.getId());
		}
		assertEquals(assertCount, count);
	}

	@Test
	void testFindAll() {
		assertNotNull(houseManagement.findAll());
		assertEquals(houseManagement.findAll().stream().count(), houseCatalog.count());
	}

	@Test
	void testFindByID() {
		assertNotNull(houseManagement.findById(myTestHouse.getId()));
		assertEquals(myTestHouse, houseManagement.findById(myTestHouse.getId()).get());
	}
	@Test
	void testFindByName() {
		List<House> h = houseManagement.findByName(myTestHouse.getName()).stream().toList();

		assertNotNull(h);
		assertFalse(h.isEmpty());
		assertTrue(h.contains(myTestHouse));
	}

	@Test
	void testGetLandlordName() {
		assertEquals("myLandlord", houseManagement.getLandlordName(myTestHouse.getId()));

	}

	@Test
	void testFindByDescription()
	{
		assertNotNull(houseCatalog.findByDescription("TestBeschreibung"));
		assertNotNull(houseCatalog.findByDescription(myTestHouse.getDescription()));
	}

}

