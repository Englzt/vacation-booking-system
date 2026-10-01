package vacationrental.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vacationrental.booking.BookingManagement;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.messages.MessageManagement;
import vacationrental.messages.MessageStatus;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * A Spring MVC controller to manage user-related actions.
 *
 * @author Anthony, Sokar
 */
@Controller
public class UserController {
	private final UserManagement userManagement;
	private final BookingManagement bookingManagement;
	private final HouseManagement houseManagement;
	private final EventManagement eventManagement;
	private final MessageManagement messageManagement;
	private final ResourceBundle properties;

	/**
	 * Constructor for {@link UserController}.
	 *
	 * @param userManagement    The {@link UserManagement} instance to manage users.
	 * @param messageManagement The {@link MessageManagement} instance to manage messages.
	 * @param bookingManagement The {@link BookingManagement} instance to manage bookings.
	 * @param houseManagement   The {@link HouseManagement} instance to manage houses.
	 * @param eventManagement   The {@link EventManagement} instance to manage events.
	 */
	public UserController(UserManagement userManagement, MessageManagement messageManagement,
						  BookingManagement bookingManagement, HouseManagement houseManagement, EventManagement eventManagement) {
		this.userManagement = userManagement;
		this.messageManagement = messageManagement;
		this.bookingManagement = bookingManagement;
		this.houseManagement = houseManagement;
		this.eventManagement = eventManagement;
		this.properties = ResourceBundle.getBundle("messages");
	}

	/**
	 * Displays the login page.
	 *
	 * @param error   An optional error message.
	 * @param model   The {@link Model} to pass data to the view.
	 * @param request The {@link HttpServletRequest} to access the session.
	 * @return The login page view name.
	 */
	@GetMapping("/login")
	public String login(
		@RequestParam(value = "error", required = false) String error,
		Model model,
		HttpServletRequest request) {
		if (error != null) {
			Exception exception = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
			if (exception instanceof DisabledException) {
				model.addAttribute("loginError", properties.getString("login.error.inactive"));
			} else {
				model.addAttribute("loginError", properties.getString("login.error.invalid"));
			}
		}
		return "login";
	}

	/**
	 * Handles new user registration.
	 *
	 * @param form   The {@link RegistrationForm} containing registration data.
	 * @param result The {@link Errors} object for validation errors.
	 * @param model  The {@link Model} to pass data to the view.
	 * @return The registration page view name or redirect to the login page.
	 */
	@PostMapping("/register")
	String registerNew(@Valid RegistrationForm form, Errors result, Model model) {
		if (!userManagement.validate(form)){
			result.rejectValue("name", "username.already.registered", "username.already.registered");
		}
		if (!form.getPassword().equals(form.getRepeatpassword())){
			result.rejectValue("repeatPassword", "passwords.haveToMatch", "passwords.haveToMatch");
		}
		if (!form.isRole()){
			result.rejectValue("role", "passwords.role", "Error");
		}
		if (result.hasErrors()){
			return "account/register";
		}
		try {
			userManagement.createUser(form);
		} catch (IllegalArgumentException e) {
			model.addAttribute("registrationError", e.getMessage());
			return "account/register";
		}
		return "redirect:/login";
	}

	/**
	 * Displays the registration page.
	 *
	 * @param model The {@link Model} to pass data to the view.
	 * @return The registration page view name.
	 */
	@GetMapping("/register")
	String register(Model model) {
		model.addAttribute("registrationForm", new RegistrationForm(null, "Customer", null, null, null));
		return "account/register";
	}

	/**
	 * Displays the user's profile.
	 *
	 * @param model       The {@link Model} to pass data to the view.
	 * @param userAccount The optional {@link UserAccount} of the logged-in user.
	 * @return The profile page view name or redirect to the home page if not logged in.
	 */
	@GetMapping("/profile")
	public String profile(Model model, @LoggedIn Optional<UserAccount> userAccount) {
		if (userAccount.isEmpty()) {
			return "redirect:/";
		}
		model.addAttribute("username", userAccount.get().getUsername());
		model.addAttribute("email", userAccount.get().getEmail());
		model.addAttribute("password", userAccount.get().getPassword());
		model.addAttribute("passwordForm", new PasswordForm(userAccount.get().getEmail()));
		model.addAttribute("currentPath", "/profile");
		return "account/profile";
	}


	/**
	 * Changes the email address of the currently logged-in user.
	 *
	 * @param model        The {@link Model} used to pass data to the view.
	 * @param userAccount  The optional {@link UserAccount} of the logged-in user.
	 * @param passwordForm The {@link PasswordForm} containing the old password and the new email address.
	 * @param result       The {@link Errors} object for handling validation errors.
	 * @return The view name "account/profile" or a redirect to the home page if the user is not logged in.
	 *
	 * @throws IllegalArgumentException If the old password is incorrect or the new email is empty.
	 */
	@PostMapping("/profile/change-email")
	public String changeEmail(Model model, @LoggedIn Optional<UserAccount> userAccount, @Valid PasswordForm passwordForm,
							  Errors result) {
		if (userAccount.isEmpty()) {
			return "redirect:/";
		}
		if(result.hasErrors()){
			return "account/profile";
		}

		UserAccount account = userAccount.get();

		if (userManagement.checkPassword(
			Password.UnencryptedPassword.of(passwordForm.getOldPassword()), account.getPassword())) {
			if (passwordForm.getEmail() != null && !passwordForm.getEmail().isBlank()) {
				userManagement.changeMail(account, passwordForm.getEmail());
			} else {
				result.rejectValue("email", "profile.notEmpty", "profile.notEmpty");
			}

		} else {
			result.rejectValue("password", "profile.wrongPassword", "profile.wrongPassword");
		}

		model.addAttribute("username", account.getUsername());
		model.addAttribute("email", userAccount.get().getEmail());
		return "account/profile";
	}

	/**
	 * Changes the password of the currently logged-in user.
	 *
	 * @param model        The {@link Model} used to pass data to the view.
	 * @param userAccount  The optional {@link UserAccount} of the logged-in user.
	 * @param passwordForm The {@link PasswordForm} containing the old and new passwords.
	 * @param result       The {@link Errors} object for handling validation errors.
	 * @return The view name "account/profile" or a redirect to the home page if the user is not logged in.
	 * @throws IllegalArgumentException If the old password is incorrect or the new passwords do not match.
	 */
	@PostMapping("/profile/change-password")
	public String changePassword(Model model, @LoggedIn Optional<UserAccount> userAccount,
								 @Valid PasswordForm passwordForm, Errors result) {
		if (userAccount.isEmpty()) {
			return "redirect:/";
		}
		if(result.hasErrors()){
			return "account/profile";
		}

		UserAccount account = userAccount.get();

		if (userManagement.checkPassword(
			Password.UnencryptedPassword.of(passwordForm.getOldPassword()), account.getPassword())) {
			if (passwordForm.getNewPassword() != null && !passwordForm.getNewPassword().isBlank()
				&& passwordForm.getConfirmPassword() != null && !passwordForm.getConfirmPassword().isBlank()) {

				if (passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
					userManagement.changePassword(account, Password.UnencryptedPassword.of(passwordForm.getNewPassword()));
				} else {
					result.rejectValue("confirmPassword", "profile.unequalPasswords", "profile.unequalPasswords");
				}

			} else {
				result.rejectValue("newPassword", "profile.notEmpty", "profile.notEmpty");
				result.rejectValue("confirmPassword", "profile.notEmpty", "profile.notEmpty");
			}
		} else {
			result.rejectValue("oldPassword", "profile.wrongPassword", "profile.wrongPassword");
		}

		model.addAttribute("username", account.getUsername());
		model.addAttribute("email", userAccount.get().getEmail());
		return "account/profile";
	}


	/**
	 * Displays the user's messages.
	 *
	 * @param model       The {@link Model} to pass data to the view.
	 * @param userAccount The optional {@link UserAccount} of the logged-in user.
	 * @return The messages page view name or redirect to the home page if not logged in.
	 */
	@GetMapping("/messages")
	public String messages(Model model, @LoggedIn Optional<UserAccount> userAccount) {
		if (userAccount.isEmpty()) {
			return "redirect:/";
		}
		UserAccount.UserAccountIdentifier userId = userAccount.get().getId();
		model.addAttribute("unreadMessages",
			messageManagement.findByUser_UserAccountIdAndStatus(userId.toString(), MessageStatus.UNREAD));
		model.addAttribute("readMessages",
			messageManagement.findByUser_UserAccountIdAndStatus(userId.toString(), MessageStatus.READ));
		return "account/messages";
	}

	/**
	 * Marks a message as read.
	 *
	 * @param message_id  The ID of the message to mark as read.
	 * @param userAccount The optional {@link UserAccount} of the logged-in user.
	 * @return Redirects to the messages page.
	 */
	@PostMapping("/read")
	public String setMessageRead(@RequestParam Long message_id, @LoggedIn Optional<UserAccount> userAccount) {
		messageManagement.setMessageStatusRead(messageManagement.findById(message_id).get());
		return "redirect:/messages";
	}

	/**
	 * Deletes a message.
	 *
	 * @param message_id  The ID of the message to delete.
	 * @param userAccount The optional {@link UserAccount} of the logged-in user.
	 * @return Redirects to the messages page.
	 */
	@PostMapping("/delete")
	public String deleteMessage(@RequestParam Long message_id, @LoggedIn Optional<UserAccount> userAccount) {
		messageManagement.deleteMessage(messageManagement.findById(message_id).get());
		return "redirect:/messages";
	}

	/**
	 * Activates a user account.
	 *
	 * @param username The username of the account to activate.
	 * @return Redirects to the user management page.
	 */
	@PostMapping("/activateUser")
	@PreAuthorize("hasRole('Admin')")
	public String activateUser(@RequestParam("username") String username) {
		Optional<UserAccount> userAccountOptional = userManagement.getUserAccountManagement().findByUsername(username);
		userAccountOptional.ifPresent(userAccount -> {
			userAccount.setEnabled(true);
			userManagement.getUserAccountManagement().save(userAccount);
		});
		return "redirect:/users";
	}

	/**
	 * Displays the deactivate user page.
	 *
	 * @param model    The {@link Model} to pass data to the view.
	 * @param username The username of the account to deactivate.
	 * @return The deactivate user page view name.
	 */
	@GetMapping("/deactivateUser")
	@PreAuthorize("hasRole('Admin')")
	public String showDeactivateUser(Model model, @RequestParam("username") String username) {
		model.addAttribute("user", userManagement.findByUsername(username).get());
		return "account/deactivateUser";
	}

	/**
	 * Deactivates a user account.
	 *
	 * @param username The username of the account to deactivate.
	 * @return Redirects to the user management page.
	 */
	@PostMapping("/deactivateUser")
	@PreAuthorize("hasRole('Admin')")
	public String deactivateUser(@RequestParam("username") String username) {
		Optional<UserAccount> userAccountOptional = userManagement.getUserAccountManagement().findByUsername(username);
		userAccountOptional.ifPresent(userAccount -> {
			userAccount.setEnabled(false);
			userManagement.getUserAccountManagement().save(userAccount);
			List<Role> roles = userAccount.getRoles().stream().toList();
			if (roles.contains(Role.of("Landlord"))) {
				houseManagement.getHousesByLandlordId(userAccount).forEach(house -> {
					bookingManagement.deleteBookingsByHouseId(house.getId());
					houseManagement.deleteHouse(house.getId());
				});
			}
			if (roles.contains(Role.of("Customer"))) {
				bookingManagement.cancelAllByCustomerId(userAccount.getId());
			}
			if (roles.contains(Role.of("EventStaff"))) {
				eventManagement.findByEventStaffId(userAccount.getId()).forEach(event -> {
					eventManagement.cancelEvent(event.getId());
					bookingManagement.cancelEventBookings(event.getId(), null);
				});
			}
		});
		return "redirect:/users";
	}

	/**
	 * Displays the user management page.
	 *
	 * @param model       The {@link Model} to pass data to the view.
	 * @param userAccount The optional {@link UserAccount} of the logged-in user.
	 * @return The user management page view name.
	 */
	@GetMapping("/users")
	@PreAuthorize("hasRole('Admin')")
	public String manageUsers(Model model, @LoggedIn Optional<UserAccount> userAccount) {
		model.addAttribute("users", userManagement.findAll());
		model.addAttribute("currentUsername", userAccount.get().getUsername());
		model.addAttribute("currentPath", "/users");
		return "account/manageUsers";
	}

	/**
	 * Displays the pending users page.
	 *
	 * @param model The {@link Model} to pass data to the view.
	 * @return The pending users page view name.
	 */
	@PreAuthorize("hasRole('Admin')")
	@GetMapping("/pendingUsers")
	public String showPendingUsers(Model model) {
		List<User> pendingUsers = userManagement.findAllDisabledUsers();
		model.addAttribute("pendingUsers", pendingUsers);
		model.addAttribute("currentPath", "/pendingUsers");
		return "account/pendingUsers";
	}


	/**
	 * Provides the current user's profile picture to all Thymeleaf templates.
	 *
	 * @return The profile picture path or a default image if not set.
	 */
	@ModelAttribute("currentUserProfilePicture")
	public String getCurrentUserProfilePicture() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return "/img/profilePics/ted.webp";
		}


		String username = authentication.getName();
		Optional<UserAccount> userAccount = userManagement.getUserAccountManagement().findByUsername(username);
		return userAccount.flatMap(userManagement::findByUserAccount)
			.map(User::getProfilePicturePath)
			.orElse("/img/profilePics/ted.webp");
	}

}