package vacationrental.comments;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;


public class CommentForm {

	private Product.ProductIdentifier productId;

	private String username;

	private String text;

	@Min(1)
	@Max(5)
	private int rating;

	private CommentType type;

	private UserAccount.UserAccountIdentifier userId;


	/**
	 * default constructor
	 */
	public CommentForm() {
		this.productId = null;
		this.username = null;
		this.text = null;
		this.rating = 0;
		this.type = null;
		this.userId = null;
	}

	public CommentForm(Product.ProductIdentifier productId, String username, String text, int rating,
					   CommentType type, UserAccount.UserAccountIdentifier userId) {
		this.productId = productId;
		this.username = username;
		this.text = text;
		this.rating = rating;
		this.type = type;
		this.userId = userId;
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

}
