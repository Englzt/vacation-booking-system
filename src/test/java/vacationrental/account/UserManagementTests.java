package vacationrental.account;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Streamable;
import org.springframework.core.annotation.Order;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(4)
public class UserManagementTests {

	@Autowired
	private UserManagement userManagement;


	@Test
	public void testCreateUser() {
		RegistrationForm form = new RegistrationForm(
			"OttoNormalUser",
			"Customer",
			"OttosKatze",
			"OttosKatze",
			"OttoNormalUser@otto.de"
		);

		userManagement.createUser(form);

		assertTrue(userManagement.findByUsername("OttoNormalUser").isPresent());
	}

	@Test
	public void testValidate() {
		RegistrationForm form = new RegistrationForm(
			"SabrinaNormalUser",
			"Customer",
			"Bibi",
			"Bibi",
			"SabrinaNormalUser@bibi.ag"
		);

		assertTrue(userManagement.validate(form));

		userManagement.createUser(form);

		assertFalse(userManagement.validate(form));
	}

	@Test
	public void testCreate() {
		User user = userManagement.create("hexhex", "BIBI und Tina GmbH", Role.of("Customer"));

		assertNotNull(user);
		assertEquals("hexhex", user.getUserAccount().getUsername());
	}

	@Test
	public void testFindAll() {
		User user1 = userManagement.create("alex", "benten", Role.of("Customer"));
		User user2 = userManagement.create("bend", "bigben", Role.of("Landlord"));

		Streamable<User> users = userManagement.findAll();

		List<User> userList = users.toList();

		assertTrue(userList.contains(user1));
		assertTrue(userList.contains(user2));
	}

	@Test
	public void testFindById() {
		User user = userManagement.create("myIdUser", "Userrrrrr", Role.of("Customer"));
		User.UserIdentifier id = user.getId();

		Optional<User> foundUser = userManagement.findById(id);

		assertTrue(foundUser.isPresent());
		assertEquals(user, foundUser.get());
	}

	@Test
	public void testFindByUsername() {
		User user = userManagement.create("nanana", "nananabatmann", Role.of("Customer"));

		Optional<User> foundUser = userManagement.findByUsername("nanana");

		assertTrue(foundUser.isPresent());
		assertEquals(user, foundUser.get());
	}

	@Test
	public void testGetUserAccountManagement() {
		assertNotNull(userManagement.getUserAccountManagement());
	}

	@Test
	public void testFindByUserAccount() {
		User user = userManagement.create("testUserAccount", "password", Role.of("Customer"));
		UserAccount userAccount = user.getUserAccount();

		Optional<User> foundUser = userManagement.findByUserAccount(userAccount);

		assertTrue(foundUser.isPresent());
		assertEquals(user, foundUser.get());
	}

	@Test
	public void testFindAllDisabledUsers() {
		RegistrationForm form1 = new RegistrationForm(
			"disabledUser:(",
			"Landlord",
			"FuckWheelChairs",
			"FuckWheelChairs",
			"fck@whlchrs.org"
		);
		RegistrationForm form2 = new RegistrationForm(
			"deadUser",
			"EventStaff",
			"driveDiesel",
			"driveDiesel",
			"dead@hell_climatechange.org"
		);

		userManagement.createUser(form1);
		userManagement.createUser(form2);

		List<User> disabledUsers = userManagement.findAllDisabledUsers();

		assertTrue(disabledUsers.stream().anyMatch(u -> u.getUserAccount().getUsername().equals("disabledUser:(")));
		assertTrue(disabledUsers.stream().anyMatch(u -> u.getUserAccount().getUsername().equals("deadUser")));
	}
}