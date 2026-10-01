package vacationrental.account;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import vacationrental.eventcatalog.EventInitializer;

import java.util.List;
import java.util.Optional;


/**
 * A {@link DataInitializer} implementation that will create dummy users on application startup.
 *
 * @author Anthony, Sokar
 * @see DataInitializer
 */
@Component
@Order(1)
public class UserDataInitializer implements DataInitializer{
	private static final Logger LOG = LoggerFactory.getLogger(UserDataInitializer.class);

	private final UserAccountManagement userAccountManagement;
	private final UserManagement userManagement;


	/**
	 * Constructor for {@link EventInitializer}.
	 *
	 * @param userAccountManagement {@link UserAccountManagement} to use business logic related to {@link User}s.
	 * @param userManagement        {@link UserManagement} to use business logic related to {@link UserAccount}s.
	 */
	UserDataInitializer(UserAccountManagement userAccountManagement, UserManagement userManagement) {
		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
		Assert.notNull(userManagement, "CustomerRepository must not be null!");

		this.userAccountManagement = userAccountManagement;
		this.userManagement = userManagement;
	}

	/**
	 * Create dummy {@link User}s.
	 */
	@Override
	public void initialize() {
		if (userAccountManagement.findByUsername("Test").isPresent()) {
			return; // skip if already populated
		}

		LOG.info("Creating default users and customers.");

		List.of(
			// would be better to have 2 constructors -> threw errors
			// DO NOT CHANGE ANY OF THE First 4  - Used for most Tests & WebInit
			new RegistrationForm("User", "Customer", "123", "123", "user@customer.com"),
			new RegistrationForm("Landlord", "Landlord", "123", "123", "dr@landlord.com"),
			new RegistrationForm("EventStaff", "EventStaff","123","123", "jonny@eventstaff.dj"),
			new RegistrationForm("Admin", "Admin","123","123", "admin@sudo.vr"),

			//Test only User
			// ReadOnly
			new RegistrationForm("myLandlord", "Landlord", "123", "123", "mydr@landlord.com"),
			new RegistrationForm("myEventStaff", "EventStaff", "123", "123", "myjonny@eventstaff.dj"),
			//can del / change
			new RegistrationForm("myLandlord1", "Landlord", "123", "123", "my1dr@landlord.com"),
			new RegistrationForm("myLandlord2", "Landlord", "123", "123", "my2dr@landlord.com"),


			// andere User
			new RegistrationForm("Hans", "Customer","123","123", "hans@gmail.com"),
			new RegistrationForm("Franz", "Customer", "123", "123", "franz@gmail.com"),
			new RegistrationForm("Christoph", "Customer","123", "123", "christoph@gmail.com"),
			new RegistrationForm("Anthony", "Customer","Password","Password", "anthony@icloud.com"),
			new RegistrationForm("Customer", "Customer", "123", "123", "customer@gmail.com")

		).forEach(userManagement::createUser);


		userManagement.findAll().forEach(user -> {
			switch (user.getUserAccount().getUsername()) {
				case "Sokar" -> user.addImagePath("/img/product/yoga.jpg");
				case "Christoph" -> user.addImagePath("/img/product/culture.jpg");
				case "Francis" -> user.addImagePath("/img/profilePics/wandering.jpg");
				case "Anthony" -> user.addImagePath("/img/profilePics/anthony.jpeg");
				case "angryChickenW" -> user.addImagePath("/img/product/yoga_evening.jpg");

				case "Customer" -> user.addImagePath("/img/profilePics/customerpb.png");
				case "Landlord" -> user.addImagePath("/img/profilePics/landlord.png");
				case "EventStaff" -> user.addImagePath("/img/profilePics/eventstaff.png");
				case "Admin" -> user.addImagePath("/img/profilePics/admin.png");
				default -> user.addImagePath("/img/profilePics/anthony.jpeg");
			}
		});

		Optional<UserAccount> userAccountOptional = userManagement.getUserAccountManagement().findByUsername("Landlord");
		if (userAccountOptional.isPresent()) {
			UserAccount userAccount = userAccountOptional.get();
			userAccount.setEnabled(true);
			userManagement.getUserAccountManagement().save(userAccount);
		}

		userAccountOptional = userManagement.getUserAccountManagement().findByUsername("EventStaff");
		if (userAccountOptional.isPresent()) {
			UserAccount userAccount = userAccountOptional.get();
			userAccount.setEnabled(true);
			userManagement.getUserAccountManagement().save(userAccount);
		}
	}
}
