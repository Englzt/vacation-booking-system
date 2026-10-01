package vacationrental.housecatalog;

import org.javamoney.moneta.Money;
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
import vacationrental.account.UserManagement;
import vacationrental.location.Location;

import java.time.LocalDateTime;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.salespointframework.core.Currencies.EURO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;;

/**
 * Integration test for the CatalogController on the web layer, i.e. simulating HTTP requests.
 *
 * 
 */
@SpringBootTest
@AutoConfigureMockMvc
@Order(20) // 20 Haus, Event,Booking,User
public class HouseControllerTests {


    @Autowired MockMvc mvc;
	@Autowired HouseController controller;

	@Autowired
	private HouseManagement houseManagement;


	private HouseForm form;

	private final Location location;

	private House myTestHouse;

	private static UserManagement userManagement;


	@Autowired
	HouseControllerTests(
        @Autowired HouseController controller,@Autowired UserManagement userManagement,
     @Autowired HouseManagement houseManagement) {
		this.controller = controller;
        HouseControllerTests.userManagement = userManagement;
		this.location = new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
	}


	@BeforeEach
	void setUp( @Autowired HouseManagement houseManagement, @Autowired HouseController controller, @Autowired UserManagement userManagement) {
		
		this.myTestHouse = houseManagement.findByName("Mountain View Lodge").toList().getFirst();
	}
    // Test: Anzeige des Hauskatalogs
    @Test
    void testShowHouseCatalog() {
        Model model = new ExtendedModelMap();
        String viewName = controller.showHouseCatalog(model);

        // Überprüfen, dass die richtige View zurückgegeben wird
        assertEquals("house/houses", viewName);

        // Überprüfen, dass die erwarteten Attribute gesetzt sind
        assertTrue(model.containsAttribute("allHouses"));
        assertNotNull(model.getAttribute("allHouses"));
    }

    // Test: Einzelnes Haus anzeigen

    @Test
    void testShowHouse() {
        Model model = new ExtendedModelMap();
			model.addAttribute("eventStartDate",null);
			model.addAttribute("eventEndDate", null);

            assertNull(model.getAttribute("eventStartDate"));
            assertNull(model.getAttribute("eventEndDate"));

            LocalDateTime start,end;
            start = LocalDateTime.parse("2024-12-18T10:15:01");
            end = LocalDateTime.parse("2024-12-30T20:50:01");
        String viewName = controller.showHouse(
                model,
                myTestHouse.getId(),
                start,
                end,
                Optional.of(userManagement.findAll().toList().getFirst().getUserAccount())
        );

        // Überprüfen, dass die richtige View zurückgegeben wird
        assertEquals("house/houseDetails", viewName);

        // Überprüfen, dass das Haus korrekt ins Model geladen wurde
        assertTrue(model.containsAttribute("house"));
        House house = (House) model.getAttribute("house");
        assertNotNull(house);
        assertEquals("Mountain View Lodge", house.getName());
    }

    // Test: Neues Haus hinzufügen
    @Test    
	@WithMockUser(roles={"Landlord"})
    void testAddHouse() {
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        MultipartFile[] files = new MultipartFile[0]; // Keine Dateien

		form = new HouseForm(
			"myTestHouse4",
			"new_up Beschreibung",
			location.toString(),
			Money.of(100,EURO),
			10,10,30,20,
			true,false);
        
        String viewName = controller.addHouse(form, bindingResult, model, 
        Optional.of(userManagement.findByUsername("myLandlord1").get().getUserAccount()), files, form.getPriceMoney().toString());

        // Überprüfen, dass eine Weiterleitung auf das neue Haus erfolgt
        assertTrue(viewName.startsWith("redirect:/house/"));

        // Überprüfen, ob das Haus hinzugefügt wurde
        Optional<House> addedHouse = houseManagement.findByName("myTestHouse4").stream().findFirst();
        assertTrue(addedHouse.isPresent());
        assertEquals("myTestHouse4", addedHouse.get().getName());

        //Test 
        houseManagement.deleteHouse(addedHouse.get().getId());
    }
    // Test: Haus bearbeiten
	
    @Test
    @WithMockUser(roles={"Landlord"})
    void testEditHouse() {
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        MultipartFile[] files = new MultipartFile[0]; // Keine neuen Dateien
        
		this.myTestHouse = houseManagement.findByName("Riverfront Cabin").toList().getFirst();
        String name = myTestHouse.getName();

        HouseForm updatedForm = new HouseForm(myTestHouse);
        updatedForm.setName("Updated House");
	
        String viewName = controller.editHouse(model, updatedForm, bindingResult,myTestHouse.getId(), files,
        200);

        // Überprüfen, dass die Weiterleitung auf die Seite "myHouses" erfolgt
        assertEquals("redirect:/myHouses", viewName);

        // Überprüfen, ob das Haus aktualisiert wurde
        House updatedHouse = houseManagement.findById(myTestHouse.getId()).get();
        assertEquals("Updated House", updatedHouse.getName());
        
        //revert update for Testt
        myTestHouse.setName(name);
    }

    @Test
    @WithMockUser(roles={"Landlord"})
    public void testShowEditForm(){
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ProductIdentifier id= myTestHouse.getId();

        String viewName = controller.showEditForm(model, null);
        assertEquals("redirect:/myHouses", viewName);

        viewName = controller.showEditForm(model,id);
        assertEquals("house/editHouse", viewName);

    }

    @Test
    @WithMockUser(roles={"Landlord"})
    public void testShowMyHouses(){
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = controller.showMyHouses(model, 
        Optional.of(userManagement.findByUsername("myLandlord").get().getUserAccount()));
        assertEquals("house/myHouses", viewName);
    }

    @Test
    @WithMockUser(roles={"Landlord"})  
    public void testDeleteImage(){

        String id,typ;
        id ="";
        typ ="house";
        String viewName = controller.deleteImage(id,typ,myTestHouse.getId());

        assertEquals("redirect:/myHouses/edit/" + myTestHouse.getId(), viewName);

        typ ="bigevent";
        viewName = controller.deleteImage(id,typ,myTestHouse.getId());

        assertEquals("redirect:/editBigEvent/" + myTestHouse.getId(), viewName);

        typ ="smallevent";
        viewName = controller.deleteImage(id,typ,myTestHouse.getId());

        assertEquals("redirect:/editSmallEvent/" + myTestHouse.getId(), viewName);

    }

    @Test
    @WithMockUser(roles={"Landlord"})   
    public void testShowDeleteHouse(){
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        assertEquals("redirect:/myHouses",controller.showDeleteHouse(model,null));
        assertEquals("house/deleteHouse",controller.showDeleteHouse(model,myTestHouse.getId()));
    }

	/*
    *
    Mvc Test
	* 
    */

    @Test
    void testShowHouseCatalogMvc() throws Exception {
        mvc.perform(get("/houses"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("allHouses"))
            .andExpect(view().name("house/houses"));
        }


    @Test
    void testShowHouseDetails_InvalidIdMvc() throws Exception {

        mvc.perform(get("/house/invalid"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/houses"));
    }

@Test
void testIndexMvc() throws Exception {
    mvc.perform(get("/"))
        .andExpect(redirectedUrl("/houses"));
}

}

