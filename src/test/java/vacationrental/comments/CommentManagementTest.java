package vacationrental.comments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;

import org.springframework.beans.factory.annotation.Autowired;



@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentManagementTest {



	@Autowired
	private CommentRepository commentRepository;
	@Autowired
    private CommentManagement commentManagement;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
	@Autowired
	private UserAccountManagement userAccountManagement;
	@Autowired
	private  HouseManagement houseManagement;


	private CommentForm form_h;
    private final UserAccount user;

    private Comment com_h;

	private House myTestHouse;

	CommentManagementTest(
    @Autowired CommentManagement commentManagement,    
    @Autowired CommentRepository commentRepository,
    @Autowired ApplicationEventPublisher eventPublisher,
    @Autowired UserAccountManagement userAccountManagement, 
    @Autowired HouseManagement houseManagement) {
        this.commentManagement = commentManagement;
        this.commentRepository = commentRepository;
		this.eventPublisher = eventPublisher;
		this.userAccountManagement = userAccountManagement;
		this.houseManagement = houseManagement;
        commentRepository.deleteAll();
        
        
        user = userAccountManagement.findAll().toList().getFirst();

        this.myTestHouse =houseManagement.findByName("Historic Vineyard House").toList().getFirst();
        

        this.form_h = new CommentForm(
        this.myTestHouse.getId(),
        user.getUsername(),
        "Super toller Komentar für ein Haus",
        5,
        CommentType.HOUSECOMMENT,
        user.getId());


	}
    @BeforeEach
    void setup(){
       
        commentRepository.deleteAll();
        assertEquals(0, commentRepository.count());

        com_h = commentManagement.addComment(form_h);
    
    }




    @Test
    void testAddComment() {
        
        Comment com =  commentManagement.addComment(form_h);

        assertNotNull(com);

    }

    @Test
    void testCalcAverageRating() {

        double rating =  commentManagement.calcAverageRating(form_h.getProductId());
        assertEquals(form_h.getRating(), rating);
        form_h.setUserId(userAccountManagement.findAll().toList().getLast().getId());
        form_h.setRating(3);
        commentManagement.addComment(form_h);
        rating = commentManagement.calcAverageRating(form_h.getProductId());
        assertEquals(4, rating);

        //Cleanup
        form_h.setUserId(user.getId());
        form_h.setRating(5);
        
    }

    @Test
    void testDeclineDeletionRequest() {      
        assertFalse(commentManagement.findByAccountIdAndProductId(user.getId(),
        form_h.getProductId()).getFirst().isDeletionRequested());

        commentManagement.requestDeletion(com_h);
        assertTrue(commentManagement.findByAccountIdAndProductId(user.getId(),
        form_h.getProductId()).getFirst().isDeletionRequested());

        commentManagement.declineDeletionRequest(com_h);  

        assertFalse(commentManagement.findByAccountIdAndProductId(user.getId(),
        form_h.getProductId()).getFirst().isDeletionRequested());

    }

    @Test
    void testDeleteComment() {
        assertEquals(1,commentRepository.count());
        commentManagement.deleteComment(com_h);
        assertEquals(0,commentRepository.count());

    }

    @Test
    void testFindByAccountIdAndProductId() {
        assertEquals(form_h.getProductId(),
        commentRepository.findByUserIdAndProductId(com_h.getUserId(),com_h.getProductId()).getFirst().getProductId());
    
    }

    @Test
    void testFindByProductId() {
        assertEquals(form_h.getProductId(),
        commentRepository.findByProductId(com_h.getProductId()).getFirst().getProductId());
     
    }

    @Test
    void testFindCommentById() {
        assertEquals(com_h.getId(),
        commentManagement.findCommentById(com_h.getId()).get().getId());
    
    }

    @Test
    void testRequestDeletion() {

        commentManagement.requestDeletion(com_h);
        assertTrue(commentManagement.findByAccountIdAndProductId(user.getId(),
        form_h.getProductId()).getFirst().isDeletionRequested());


    }

    @Test
    void testSendDeletionNotice() {
        commentManagement.sendDeletionNotice(com_h);
        assertTrue(true);
    }

    @Test
    void testSendDeletionRequest() {
        commentManagement.sendDeletionNotice(com_h);
        assertTrue(true);
    
    }
}
