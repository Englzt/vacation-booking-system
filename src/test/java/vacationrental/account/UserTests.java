package vacationrental.account;

import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import java.util.UUID;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserTests {
	private User user;
	private User.UserIdentifier id;
	private UserAccount userAccount;

	@Autowired
	private UserAccountManagement userAccountManagement;

	@BeforeEach
	void setUp() {
		String uniqueUsername = "username" + UUID.randomUUID();
		Password.UnencryptedPassword password = Password.UnencryptedPassword.of("password");
		userAccount = userAccountManagement.create(uniqueUsername, password, Role.of("ROLE_CUSTOMER"));
		user = new User(userAccount);
		id = user.getId();
	}

	@Test
	public void testGetId() {
		assertNotNull(user.getId());
	}

	@Test
	public void testGetUserAccount() {
		assertEquals(userAccount, user.getUserAccount());
	}

	@Test
	public void testSetUserAccount() {
		String uniqueUsername = "newUsername" + UUID.randomUUID();
		Password.UnencryptedPassword newPassword = Password.UnencryptedPassword.of("newPassword");
		UserAccount newUserAccount = userAccountManagement.create(uniqueUsername, newPassword, Role.of("ROLE_CUSTOMER"));
		user.setUserAccount(newUserAccount);
		assertEquals(newUserAccount, user.getUserAccount());
	}

	@Test
	public void testUserIdentifierHashCode() {
		User.UserIdentifier identifier1 = new User.UserIdentifier(UUID.randomUUID());
		User.UserIdentifier identifier2 = new User.UserIdentifier(UUID.randomUUID());
		assertNotEquals(identifier1.hashCode(), identifier2.hashCode());
	}

	@Test
	public void testUserIdentifierEquals() {
		User.UserIdentifier identifier1 = new User.UserIdentifier(UUID.randomUUID());
		User.UserIdentifier identifier2 = new User.UserIdentifier(identifier1.getId());
		assertEquals(identifier1, identifier2);
		assertNotEquals(identifier1, new User.UserIdentifier(UUID.randomUUID()));
	}
}