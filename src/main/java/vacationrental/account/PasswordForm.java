package vacationrental.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;



import java.util.ResourceBundle;

public class PasswordForm {
	ResourceBundle messages = ResourceBundle.getBundle("messages");


	private  String oldPassword;

	@Pattern(regexp = "^(?=.*[A-Z]).{4,}$", message = "{profile.notMatchingPassword}")
	private  String newPassword;

	@Pattern(regexp = "^(?=.*[A-Z]).{4,}$", message = "{profile.notMatchingPassword}")
	private  String confirmPassword;

	@Email
	private String email;


	/**
	 * constructor to set everything up
	 * @param oldPassword
	 * @param newPassword
	 * @param confirmPassword
	 * @param email
	 */
	public PasswordForm(String oldPassword, String newPassword, String confirmPassword, String email) {
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.confirmPassword = confirmPassword;
		this.email = email;
	}

	/**
	 * common constructor but the email is already filled in every time
	 * @param email
	 */
	public PasswordForm(String email) {
		this.oldPassword = null;
		this.newPassword = null;
		this.confirmPassword = null;
		this.email = email;
	}

	/**
	 * default constructor
	 */
	public PasswordForm() {
		this.oldPassword = null;
		this.newPassword = null;
		this.confirmPassword = null;
		this.email = null;
	}

	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}


	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
