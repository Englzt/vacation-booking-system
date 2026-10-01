package vacationrental.comments;

import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.HouseManagement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class CommentController {

	private final CommentManagement commentManagement;
	private final EventManagement eventManagement;
	private final HouseManagement houseManagement;


	public CommentController(CommentManagement commentManagement,
							 EventManagement eventManagement,
							 HouseManagement houseManagement) {
		this.commentManagement = commentManagement;
		this.eventManagement = eventManagement;
		this.houseManagement = houseManagement;
	}


	/**
	 * Post mapping which is called by posting a comment, which saves teh new comment and updates the average rating
	 * @param userAccount {@link UserAccount}
	 * @param id {@link Product.ProductIdentifier}
	 * @param commentForm {@link CommentForm}
	 * @param typeString {@link String}
	 * @return redirects to the product page
	 */
	@PostMapping("/comment/{id}")
	public String comment(@LoggedIn Optional<UserAccount> userAccount, @PathVariable Product.ProductIdentifier id,
						  @ModelAttribute CommentForm commentForm, @RequestParam String typeString) {

		UserAccount account = userAccount.get();
		commentForm.setProductId(id);
		commentForm.setUsername(account.getUsername());
		commentForm.setUserId(account.getId());
		if(typeString.equals("House")){
			commentForm.setType(CommentType.HOUSECOMMENT);
			commentManagement.addComment(commentForm);
			return "redirect:/house/" + id;
		}else {
			commentForm.setType(CommentType.EVENTCOMMENT);
			commentManagement.addComment(commentForm);
			return "redirect:/event/" + id;
		}
	}


	/**
	 * get mapping to display all comments to a specific product; is called in myHouses and myEvents
	 * @param id {@link Product.ProductIdentifier}
	 * @param model {@link Model}
	 * @return the commentOverview page
	 */
	@GetMapping("/comments/{id}")
	public String showComments(@PathVariable Product.ProductIdentifier id, Model model) {
		String typeString = null;
		List<Comment> comments = commentManagement.findByProductId(id);
		if(comments.isEmpty()){
			model.addAttribute("comments", comments);
			return "comments/commentOverview";
		}

		if(comments.getFirst().getType() == CommentType.HOUSECOMMENT){
			typeString = "House";
			model.addAttribute("house", houseManagement.findById(id).get());
		}else{
			typeString = "Event";
			model.addAttribute("event", eventManagement.findById(id).get());
		}
		model.addAttribute("userIsOwner", true);
		model.addAttribute("comments", comments);
		model.addAttribute("typeString", typeString);
		return "comments/commentOverview";
	}

	/**
	 * get mapping which is called by clicking the delete button in the comments of a product page belonging to the owner
	 * (landlord or eventstaff) to request the deletion of a comment.
	 * @param id {@link UUID}
	 * @return redirects to the product page
	 */
	@GetMapping("requestCommentDeletion/{id}")
	public String requestCommentDeletion(@PathVariable UUID id){
		Comment comment = commentManagement.findCommentById(id).get();
		commentManagement.requestDeletion(comment);

		String returnAddress = "redirect:/";
		if (comment.getType() == CommentType.HOUSECOMMENT) {
			returnAddress += "house/" + comment.getProductId();
		} else {
			returnAddress += "event/" + comment.getProductId();
		}
		return returnAddress;
	}

	/**
	 * get mapping which is called by clicking the delete button on a marked comment to approve the deletion of the
	 * comment by an administator
	 * @param id {@link UUID}
	 * @return redirects to the product page
	 */
	@GetMapping("approveCommentDeletion/{id}")
	public String approveCommentDeletion(@PathVariable UUID id){
		Comment comment = commentManagement.findCommentById(id).get();
		commentManagement.deleteComment(comment);

		String returnAddress = "redirect:/";
		if (comment.getType() == CommentType.HOUSECOMMENT) {
			returnAddress += "house/" + comment.getProductId();
		} else {
			returnAddress += "event/" + comment.getProductId();
		}
		return returnAddress;
	}

	/**
	 * get mapping which is called by clicking the delete button on a marked comment to decline the deletion of the
	 * comment by an administator
	 * @param id {@link UUID}
	 * @return redirects to the product page
	 */
	@GetMapping("declineCommentDeletion/{id}")
	public String declineCommentDeletion(@PathVariable UUID id){
		Comment comment = commentManagement.findCommentById(id).get();
		commentManagement.declineDeletionRequest(comment);

		String returnAddress = "redirect:/";
		if (comment.getType() == CommentType.HOUSECOMMENT) {
			returnAddress += "house/" + comment.getProductId();
		} else {
			returnAddress += "event/" + comment.getProductId();
		}
		return returnAddress;
	}
}
