package vacationrental.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import vacationrental.account.UserManagement;
import vacationrental.eventcatalog.EventController;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.HouseManagement;
import org.springframework.core.annotation.Order;

@SpringBootTest
@AutoConfigureMockMvc
@Order(50)
public class TimeControllerTest {

    
    @Autowired MockMvc mvc;
	@Autowired TimeController controller;


	@Autowired
	TimeControllerTest(@Autowired TimeController controller) {

		this.controller = controller;

    }
    @Test
    public void testforward(){
        Model model = new ExtendedModelMap();
            assertEquals("redirect:/"+null,
            controller.forward(model,"5",null,null));
            assertEquals("redirect:/"+null,
            controller.forward(model,"5",null,"null"));


            assertEquals("redirect:/notnull/jo",
            controller.forward(model,"5","notnull","jo"));
    
            //cleanup Time -15 = 3x +5 
            controller.forward(model,"-15","notnull","jo");
    }

}
