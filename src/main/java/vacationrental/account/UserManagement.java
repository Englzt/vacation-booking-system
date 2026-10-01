package vacationrental.account;

import org.salespointframework.useraccount.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users in the application.
 * Provides methods for user creation, validation, and retrieval.
 *
 * @author Anthony, Sokar
 */
@Service
@Transactional
public class UserManagement {
	public static final Role CUSTOMER_ROLE = Role.of("Customer");
	public static final Role LANDLORD_ROLE = Role.of("Landlord");
	public static final Role STAFF_ROLE = Role.of("EventStaff");
	public static final Role ADMIN_ROLE = Role.of("Admin");

	private final UserRepository users;
	private final UserAccountManagement userAccounts;
	private final AuthenticationManagement authenticationManagement;

	/**
	 * Constructor for {@link UserManagement}.
	 *
	 * @param users        The {@link UserRepository} for accessing user data.
	 * @param userAccounts The {@link UserAccountManagement} for managing user accounts.
	 */
	UserManagement(UserRepository users, @Qualifier("persistentUserAccountManagement") UserAccountManagement userAccounts,
				   AuthenticationManagement authenticationManagement) {
		this.authenticationManagement = authenticationManagement;
		Assert.notNull(users, "CustomerRepository must not be null!");
		Assert.notNull(userAccounts, "UserAccountManagement must not be null!");

		this.users = users;
		this.userAccounts = userAccounts;
	}

	/**
	 * Creates a new user based on the registration form data.
	 *
	 * @param form The {@link RegistrationForm} containing user details.
	 */
	public void createUser(RegistrationForm form) {
		Assert.notNull(form, "RegistrationForm must not be null!");

		var password = Password.UnencryptedPassword.of(form.password());
		var role = form.getRole();
		var userAccount = userAccounts.create(form.name(), password, form.getEmail(), role);

		String roleName = role.getName();
		userAccount.setEnabled(roleName.equals("Customer") || roleName.equals("Admin"));

		userAccounts.save(userAccount);
		users.save(new User(userAccount));
	}

	/**
	 * Validates if a username is already registered.
	 *
	 * @param form The {@link RegistrationForm} with the username to validate.
	 * @return True if the username is not registered, false otherwise.
	 */
	public boolean validate(RegistrationForm form) {
		return userAccounts.findByUsername(form.name()).isEmpty();
	}

	/**
	 * Creates a new user with the specified name, password, and role.
	 *
	 * @param name     The username for the new user.
	 * @param password The password for the new user.
	 * @param role     The {@link Role} of the new user.
	 * @return The created {@link User}.
	 */
	public User create(String name, String password, Role role) {
		UserAccount userAccount = userAccounts.create(name, Password.UnencryptedPassword.of(password), role);
		return users.save(new User(userAccount));
	}

	/**
	 * Saves a user entity to the database.
	 *
	 * @param user The {@link User} entity to save.
	 * @return The saved {@link User}.
	 */
	public User save(User user) {
		return users.save(user);
	}

	/**
	 * Retrieves all users from the repository.
	 *
	 * @return A {@link Streamable} containing all {@link User} entities.
	 */
	public Streamable<User> findAll() {
		return users.findAll();
	}

	/**
	 * Finds a user by their identifier.
	 *
	 * @param id The {@link User.UserIdentifier} of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	public Optional<User> findById(User.UserIdentifier id) {
		return users.findById(id);
	}

	/**
	 * Finds a user by their username.
	 *
	 * @param username The username of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	public Optional<User> findByUsername(String username) {
		return users.findByUserAccountUsername(username);
	}

	/**
	 * Retrieves the {@link UserAccountManagement} instance.
	 *
	 * @return The {@link UserAccountManagement} instance.
	 */
	public UserAccountManagement getUserAccountManagement() {
		return userAccounts;
	}

	/**
	 * Finds a user by their associated {@link UserAccount}.
	 *
	 * @param userAccount The {@link UserAccount} of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	public Optional<User> findByUserAccount(UserAccount userAccount) {
		return users.findByUserAccount(userAccount);
	}

	/**
	 * Finds all users with disabled accounts.
	 *
	 * @return A list of {@link User} entities with disabled accounts.
	 */
	public List<User> findAllDisabledUsers() {
		return users.findByUserAccountEnabledFalse();
	}

	/**
	 * Finds a user by their {@link UserAccount.UserAccountIdentifier}.
	 *
	 * @param id The {@link UserAccount.UserAccountIdentifier} of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	public Optional<User> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier id) {
		return users.findByUserAccount(userAccounts.get(id).get());
	}


	/**
	 * checks if the password which was typed in (UnencryptedPassword)
	 * is equal to the original password (EncryptedPassword)
	 * @param newPassword
	 * @param oldPassword
	 * @return
	 */
	public boolean checkPassword(Password.UnencryptedPassword newPassword, Password.EncryptedPassword oldPassword){
		boolean match = authenticationManagement.matches(newPassword, oldPassword);
		return match;
	}


	/**
	 * changes the password of the given account to the given password
	 * @param account
	 * @param newPassword
	 */
	public void changePassword(UserAccount account, Password.UnencryptedPassword newPassword){
		userAccounts.changePassword(account, newPassword);
	}


	/**
	 * changing teh mail address of a user and saves the changes
	 * @param account
	 * @param newMail
	 */
	public void changeMail(UserAccount account, String newMail){
		account.setEmail(newMail);
		userAccounts.save(account);
	}

}