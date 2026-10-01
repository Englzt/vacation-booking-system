package vacationrental.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GeocodingTests {
    Geocoding testGeocoding;

    @BeforeEach
    void setup(){
        this.testGeocoding = new Geocoding();
    }

    @Test
    void testSetFormat() {
        String testFormat = "Client-Server";
        testGeocoding.setFormat(testFormat);
        assertEquals(testFormat, testGeocoding.getFormat());

    }

    @Test
    void testSetUrl() {
        String testUerl = "neueTestUrl.de/https/www";
        testGeocoding.setUrl(testUerl);
        assertEquals(testUerl, testGeocoding.getUrl());

    }
}
