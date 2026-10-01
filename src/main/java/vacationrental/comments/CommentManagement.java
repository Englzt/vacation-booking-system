package vacationrental.comments;

import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.messages.CommentDeletionNoticeEvent;
import vacationrental.messages.CommentDeletionRequestEvent;
import vacationrental.housecatalog.HouseManagement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentManagement {
	@Autowired
	private final CommentRepository commentRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final UserAccountManagement userAccountManagement;
	private final HouseManagement houseManagement;
	private final EventManagement eventManagement;

	/**
	 * constructor
	 * @param commentRepository
	 */
	public CommentManagement(CommentRepository commentRepository,
							 ApplicationEventPublisher eventPublisher,
							 UserAccountManagement userAccountManagement,
							 HouseManagement houseManagement,
							 EventManagement eventManagement) {
		this.commentRepository = commentRepository;
		this.eventPublisher = eventPublisher;
		this.userAccountManagement = userAccountManagement;
		this.houseManagement = houseManagement;
		this.eventManagement = eventManagement;
	}


	/**
	 * creates a new comment by the given comment form, adds it to the repository and returns the new object
	 * @param commentForm {@link CommentForm}
	 * @return {@link Comment}
	 */
	public Comment addComment(CommentForm commentForm) {
		Comment comment = commentRepository.save(new Comment(commentForm));

		if (commentForm.getType() == CommentType.HOUSECOMMENT) {
			houseManagement.updateAverageRating(commentForm.getProductId(), calcAverageRating(commentForm.getProductId()));
		} else {
			eventManagement.updateAverageRating(commentForm.getProductId(), calcAverageRating(commentForm.getProductId()));
		}

		return comment;
	}


	/**
	 * deletes a given comment and calls the updateAverageRating method of the corresponding management class.
	 *
	 * @param comment {@link Comment}
	 */
	public void deleteComment(Comment comment) {
		commentRepository.delete(comment);
		if (comment.getType() == CommentType.HOUSECOMMENT) {
			houseManagement.updateAverageRating(comment.getProductId(), calcAverageRating(comment.getProductId()));
		} else {
			eventManagement.updateAverageRating(comment.getProductId(), calcAverageRating(comment.getProductId()));
		}
		sendDeletionNotice(comment);
	}


	/**
	 * returns the comment with the given id if present
	 * @param id {@link UUID}
	 * @return Optional&lt;Comment&gt;
	 */
	public Optional<Comment> findCommentById(UUID id){
		return commentRepository.findById(id);
	}


	/**
	 * returning a list of all comments to a specific product
	 * @param productId {@link Product.ProductIdentifier}
	 * @return List&lt;Comment&gt;
	 */
	public List<Comment> findByProductId(Product.ProductIdentifier productId){
		return commentRepository.findByProductId(productId);
	}


	/**
	 * returning a list of all comments made by a customer about the specific product
	 * @param userId {@link UserAccount.UserAccountIdentifier}
	 * @param productId {@link Product.ProductIdentifier}
	 * @return List&lt;Comment&gt;
	 */
	public List<Comment> findByAccountIdAndProductId(UserAccount.UserAccountIdentifier userId,
													 Product.ProductIdentifier productId){
		return commentRepository.findByUserIdAndProductId(userId, productId);
	}


	/**
	 * calculates the average rating of a product
	 * @param productId {@link Product.ProductIdentifier}
	 * @return {@link Double}
	 */
	public double calcAverageRating(Product.ProductIdentifier productId){
		List<Comment> comments = commentRepository.findByProductId(productId);
		double sumOfRatings = 0;
		for (Comment comment : comments) {
			sumOfRatings += comment.getRating();
		}
		return Math.round((sumOfRatings / comments.size()) * 100.0) / 100.0;
	}


	/**
	 * handles the deletion request of a comment
	 * @param comment {@link Comment}
	 */
	public void sendDeletionRequest(Comment comment) {

		if (comment.isDeletionRequested()) {
			return;
		}

		comment.requestDeletion();
		commentRepository.save(comment);

		UserAccount.UserAccountIdentifier adminId = userAccountManagement.findByUsername("Admin").get().getId();
		eventPublisher.publishEvent(new CommentDeletionRequestEvent(adminId, comment));
	}

	/**
	 * sends a deletion notice to the administator
	 * @param comment {@link Comment}
	 */
	public void sendDeletionNotice(Comment comment) {
		UserAccount.UserAccountIdentifier userId = comment.getUserId();
		eventPublisher.publishEvent(new CommentDeletionNoticeEvent(userId, comment));
	}

	/**
	 * handles the decline of a deletion request
	 * @param comment {@link Comment}
	 */
	public void declineDeletionRequest(Comment comment) {
		if (!comment.isDeletionRequested()) {
			return;
		}

		comment.declineDeletion();
		commentRepository.save(comment);
	}

	/**
	 * handles the deletion request of a comment
	 * @param comment {@link Comment}
	 */
	public void requestDeletion(Comment comment) {
		sendDeletionRequest(comment);
	}
}
