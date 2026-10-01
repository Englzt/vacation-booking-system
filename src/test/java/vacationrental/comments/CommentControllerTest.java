package vacationrental.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import vacationrental.account.User;

import vacationrental.account.UserManagement;

import vacationrental.eventcatalog.EventManagement;
import vacationrental.eventcatalog.Event;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccount;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    CommentController controller;


	@Autowired  private 
    CommentManagement commentManagement;
    @Autowired 
	private EventManagement eventManagement;
    @Autowired 
	private HouseManagement houseManagement;
    @Autowired
	private  UserManagement userManagement;
	
	private CommentForm form_h,form_e;
    private final UserAccount user;

    private Comment com_h,com_e;

	private House myTestHouse;
	private Event myTestEvent;


    CommentControllerTest( 
    @Autowired CommentManagement commentManagement,
    @Autowired HouseManagement houseManagement,
    @Autowired EventManagement eventManagement,
    @Autowired UserManagement userManagement){

        this.commentManagement = commentManagement;
        this.eventManagement = eventManagement;
        this.houseManagement = houseManagement;
        this.userManagement = userManagement;
        user = userManagement.findAll().toList().getFirst().getUserAccount();

        this.myTestHouse =houseManagement.findByName("Historic Vineyard House").toList().getFirst();
        
        this.myTestEvent = eventManagement.findByName("Yoga Flow").toList().getFirst();
        
        this.form_h = new CommentForm(
        this.myTestHouse.getId(),
        user.getUsername(),
        "Super toller Komentar für ein Haus",
        5,
        CommentType.HOUSECOMMENT,
        user.getId());

        this.form_e = new CommentForm(myTestEvent.getId(),
        user.getUsername(),
        "Super toller Komentar für en Event",
        4,
        CommentType.EVENTCOMMENT,
        user.getId());

        assertEquals(myTestHouse.getId(),form_h.getProductId());
    }

    @BeforeEach
    void setup (){
        this.com_h= commentManagement.addComment(form_h);   
    }

    @AfterEach
    void cleanup(){
        if(com_h != null)
        {
        commentManagement.deleteComment(com_h);
        }
    }

    @Test
    void testApproveCommentDeletion() {
    
        assertEquals("redirect:/house/"+form_h.getProductId(),
        controller.approveCommentDeletion(com_h.getId()));

    }

    @Test
    void testComment() {

        assertEquals("redirect:/house/"+myTestHouse.getId(),
        controller.comment(Optional.of(user),
        myTestHouse.getId(),
        form_h, 
        "House"));

    }

    @Test
    void testDeclineCommentDeletion() {


        assertEquals("redirect:/house/"+form_h.getProductId(),
        controller.declineCommentDeletion(com_h.getId()));

    }

    @Test
    void testRequestCommentDeletion() {

        
        assertEquals("redirect:/house/"+form_h.getProductId(),
        controller.requestCommentDeletion(com_h.getId()));

    }

    @Test
    void testShowComments() {
        Model model = new ExtendedModelMap();


        assertEquals("comments/commentOverview",
        controller.showComments(com_h.getProductId(),model));
        
        commentManagement.deleteComment(com_h);
    
        assertEquals("comments/commentOverview",
        controller.showComments(com_h.getProductId(),model));
        
    }
}
