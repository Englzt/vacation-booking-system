		
package vacationrental.location; 

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationTests {


    private Location mylocation;

    @BeforeAll
    void setup()
    {
        this.mylocation =  new Location("Nöthnitzer Straße", "46", "01187", "Dresden", "Germany");
    }

    @Test
    @Order(1)
    void testGetAddressString() {
        assertEquals(mylocation.getAddressString(),"Nöthnitzer Straße" + " " + 46 + ", " + "01187" + " " + "Dresden" + ", " + "Germany"); 
    }

    @Test
    @Order(2)
    public void testLocation(){
        String street="Flughafenstraße";
        String houseNumber="";
        String postalCode="01109";
        String city="Dresden";
        String country="Germany";

        Location testLocation = 
                new Location(
                    street,
                    houseNumber,
                    postalCode,
                    city,
                    country);
        
        assertNotEquals(testLocation,mylocation);

        assertEquals(testLocation.getAddressString(), street + " " + houseNumber + ", " + postalCode + " " + city + ", " + country);
    }


    // zu sich selebst
    @Test
    void testDistanceTo() {
        double distance = 0;  
        assertEquals(distance,mylocation.distanceTo(mylocation));
    }


    @Test
    void testGetUrl(){
    assertEquals(mylocation.getUrl(), mylocation.getUrl());

    }




    
}
