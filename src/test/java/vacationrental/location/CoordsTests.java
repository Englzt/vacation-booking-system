package vacationrental.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CoordsTests {
    Coordinates myCoordinates;
    
    @BeforeAll
    void setup(){
        this.myCoordinates = new Coordinates(1,2);
        myCoordinates.setAddressLabel("null");
        Coordinates testCoordinates = new Coordinates();
        assertNotNull(testCoordinates);
    }

    @Test
    void testGetAddressLabel() {
        assertEquals("null", myCoordinates.getAddressLabel());
    }

    @Test
    void testGetLatitude() {
        assertEquals(1, myCoordinates.getLatitude());
    }

    @Test
    void testGetLongitude() {
        assertEquals(2, myCoordinates.getLongitude());
    }

    @Test
    void testToJson() {
        assertNotNull(myCoordinates.toJson());
    }

    @Test
    void testToString() {
        assertEquals("1.0-2.0", myCoordinates.toString());
    }
}
