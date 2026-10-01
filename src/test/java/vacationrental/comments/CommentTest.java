package vacationrental.comments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;

import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;

@SpringBootTest
public class CommentTest {
    
    @Autowired
	private UserAccountManagement userAccountManagement;
	@Autowired
	private  HouseManagement houseManagement;

	private House myTestHouse;
    
	private CommentForm form_h;
    private final UserAccount user;

    Comment myCom;
    
    CommentTest(
    @Autowired UserAccountManagement userAccountManagement, 
    @Autowired HouseManagement houseManagement) {
       
        
		this.userAccountManagement = userAccountManagement;
		this.houseManagement = houseManagement;
        
        
        user = userAccountManagement.findAll().toList().getFirst();

        this.myTestHouse =houseManagement.findByName("Historic Vineyard House").toList().getFirst();
        

        this.form_h = new CommentForm(
        this.myTestHouse.getId(),
        user.getUsername(),
        "Super toller Komentar für ein Haus",
        5,
        CommentType.HOUSECOMMENT,
        user.getId());

        this.myCom = new Comment(form_h);

    }

    @Test 
    void testGetter(){
        
        assertEquals(this.myTestHouse.getId(), myCom.getProductId());
        assertEquals(user.getUsername(), myCom.getUsername());
        assertEquals("Super toller Komentar für ein Haus", myCom.getText());
        assertEquals(5, myCom.getRating());
        assertEquals(CommentType.HOUSECOMMENT, myCom.getType());
        assertEquals(user.getId(), myCom.getUserId());
    }

    @Test
    void testSetter(){

        //zweiter Construktor testen
        Comment com2 = new Comment(
        myTestHouse.getId(),user.getUsername(),
        "Testtext",3,CommentType.HOUSECOMMENT);

        com2.setProductId(null);
        com2.setRating(4);
        com2.setId(null);
        com2.setText("null");
        com2.setType(CommentType.EVENTCOMMENT);
        com2.setUsername("new");
        com2.setUserId(null);


        assertEquals(null, com2.getProductId());
        assertEquals("new", com2.getUsername());
        assertEquals("null", com2.getText());
        assertEquals(4, com2.getRating());
        assertEquals(CommentType.EVENTCOMMENT, com2.getType());
        assertEquals(null, com2.getUserId());
    }

    @Test
    void testDeclineDeletion() {

        myCom.declineDeletion();
        assertFalse(myCom.isDeletionRequested());
    }

    @Test
    void testRequestDeletion() {
        myCom.requestDeletion();
        assertTrue(myCom.isDeletionRequested());

    }

}
