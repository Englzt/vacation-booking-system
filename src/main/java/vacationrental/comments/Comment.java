package vacationrental.comments;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;

import java.util.UUID;

@Entity
public class Comment{
	@Id
	@GeneratedValue
	private UUID id;

	private Product.ProductIdentifier productId;
	private String username;
	private String text;
	private int rating;
	private CommentType type;
	private UserAccount.UserAccountIdentifier userId;
	private boolean deletionRequested = false;

	/**
	 * default constructor
	 */
	public Comment() {
		this.id = UUID.randomUUID();
		this.productId = null;
		this.username = null;
		this.text = null;
		this.rating = 0;
		this.type = null;
		this.userId = null;
	}


	/**
	 * constructor to create a new comment with a given comment form
	 * @param commentForm CommentForm
	 */
	public Comment(CommentForm commentForm) {
		this.id = UUID.randomUUID();
		this.productId = commentForm.getProductId();
		this.username = commentForm.getUsername();
		this.text = commentForm.getText();
		this.rating = commentForm.getRating();
		this.type = commentForm.getType();
		this.userId = commentForm.getUserId();
	}


	/**
	 * one Constructor for both types depending on the given CommenType
	 * @param productId Product.ProductIdentifier
	 * @param username String
	 * @param text String
	 * @param rating int
	 * @param type CommentType
	 */
	public Comment(Product.ProductIdentifier productId, String username, String text, int rating, CommentType type) {
		this.productId = productId;
		this.username = username;
		this.text = text;
		this.rating = rating;
		this.type = type;
	}


	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}


	public Product.ProductIdentifier getProductId() {
		return productId;
	}
	public void setProductId(Product.ProductIdentifier productId) {
		this.productId = productId;
	}


	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}


	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}


	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}


	public CommentType getType() {
		return type;
	}
	public void setType(CommentType type) {
		this.type = type;
	}


	public UserAccount.UserAccountIdentifier getUserId() {
		return userId;
	}
	public void setUserId(UserAccount.UserAccountIdentifier userId) {
		this.userId = userId;
	}

	/**
	 * checks if the comment is requested to be deleted
	 * @return {@link Boolean}
	 */
	public boolean isDeletionRequested() {
		return deletionRequested;
	}

	/**
	 * requests the deletion of the comment
	 */
	public void requestDeletion() {
		this.deletionRequested = true;
	}

	/**
	 * declines the deletion of the comment
	 */
	public void declineDeletion() {
		this.deletionRequested = false;
	}
}
