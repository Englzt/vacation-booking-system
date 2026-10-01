package vacationrental.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import vacationrental.messages.MessageManagement;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Order(23) // 20 Haus, Event,Booking,User
public class UserControllerTests {

	@Autowired
	private UserController userController;

	@Autowired
	private MessageManagement messageManagement;

	@Autowired
	private UserManagement userManagement;

	private UserAccount adminUserAccount;
	private UserAccount customerUserAccount;
	private Model model;

	@BeforeEach
	void setUp() {
		Optional<User> adminUserOptional = userManagement.findByUsername("MaxMeinzelmännchen");
		if (adminUserOptional.isEmpty()) {
			adminUserAccount = userManagement.create("MaxMeinzelmännchen", "Slay", UserManagement.ADMIN_ROLE).getUserAccount();
			adminUserAccount.setEnabled(true);
			userManagement.getUserAccountManagement().save(adminUserAccount);
		} else {
			adminUserAccount = adminUserOptional.get().getUserAccount();
		}

		Optional<User> customerUserOptional = userManagement.findByUsername("testKäufer");
		if (customerUserOptional.isEmpty()) {
			customerUserAccount = userManagement.create("testKäufer", "LidlLidlLidl", UserManagement.CUSTOMER_ROLE).getUserAccount();
			customerUserAccount.setEnabled(true);
			userManagement.getUserAccountManagement().save(customerUserAccount);
		} else {
			customerUserAccount = customerUserOptional.get().getUserAccount();
	
		}
		
		this.model = new ExtendedModelMap();
	}

	@Test
	public void testRegisterNewValidUser() {
		RegistrationForm form = new RegistrationForm(
			"Bernd",
			"Customer",
			"BRENDKAters",
			"BRENDKAters",
			"bernd@gmail.com"
		);
		Errors errors = new DirectFieldBindingResult(form, "registrationForm");

		String viewName = userController.registerNew(form, errors, model);

		assertEquals("redirect:/login", viewName);
		assertFalse(errors.hasErrors());
		assertTrue(userManagement.findByUsername("Bernd").isPresent());
	}

	@Test
	public void testRegisterNewValidUserAlreadyTaken() {
		RegistrationForm form = new RegistrationForm(
			"Anthony",
			"Customer",
			"strongestpasswordEver!maginable",
			"strongestpasswordEver!maginable",
			"bernd@gmail.com"
		);
		Errors errors = new DirectFieldBindingResult(form, "registrationForm");

		String viewName = userController.registerNew(form, errors, model);

		assertNotEquals("redirect:/login", viewName);
	}

	@Test
	public void testRegisterNewInvalidUser() {
		RegistrationForm form = new RegistrationForm(
			"Anthony",
			"InvalidRole",
			"Password1",
			"penis_hihi",
			"anthony@gmail.com"
		);
		Errors errors = new DirectFieldBindingResult(form, "registrationForm");

		String viewName = userController.registerNew(form, errors, model);

		assertEquals("account/register", viewName);
		assertTrue(errors.hasErrors());
	}

//	@Test
//	public void testLoginWithDisabledException() {
//		Model model = new org.springframework.ui.ConcurrentModel();
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new DisabledException("User is disabled"));
//
//		String viewName = userController.login("error", model, request);
//
//		assertEquals("login", viewName);
//		
//		assertEquals("The account is inactive", model.getAttribute("loginError"));
//	}

	@Test
	public void testProfile() {
		SecurityContextHolder.getContext().setAuthentication(
			new TestingAuthenticationToken(customerUserAccount, null, "ROLE_Customer"));

		Model model = new org.springframework.ui.ConcurrentModel();
		Optional<UserAccount> userAccountOptional = Optional.of(customerUserAccount);

		String viewName = userController.profile(model, userAccountOptional);

		assertEquals("account/profile", viewName);
		assertEquals("testKäufer", model.getAttribute("username"));
		assertEquals(customerUserAccount.getEmail(), model.getAttribute("email"));
		assertEquals(customerUserAccount.getPassword(), model.getAttribute("password"));
		assertEquals("/profile", model.getAttribute("currentPath"));

		SecurityContextHolder.clearContext();
	}

	@Test
	public void testActivateUser() {
		// Create an inactive user
		RegistrationForm form = new RegistrationForm(
			"timeless",
			"Landlord",
			"cutUp",
			"cutUp",
			"forest@uni.com"
		);
		userManagement.createUser(form);

		//make sure timeless is inactive
		Optional<UserAccount> userAccountOptional = userManagement.getUserAccountManagement().findByUsername("timeless");
		assertTrue(userAccountOptional.isPresent());
		assertFalse(userAccountOptional.get().isEnabled());

		// admin who tests the activation
		SecurityContextHolder.getContext().setAuthentication(
			new TestingAuthenticationToken(adminUserAccount, null, "ROLE_Admin"));

		String viewName = userController.activateUser("timeless");

		Optional<UserAccount> userAccount = userManagement.getUserAccountManagement().findByUsername("timeless");
		assertTrue(userAccount.isPresent());
		assertTrue(userAccount.get().isEnabled());
		assertEquals("redirect:/users", viewName);

		SecurityContextHolder.clearContext();
	}

	@Test
	public void testShowPendingUsers() {
		// same here
		SecurityContextHolder.getContext().setAuthentication(
			new TestingAuthenticationToken(adminUserAccount, null, "ROLE_Admin"));

		Model model = new org.springframework.ui.ConcurrentModel();

		String viewName = userController.showPendingUsers(model);

		assertEquals("account/pendingUsers", viewName);
		assertEquals("/pendingUsers", model.getAttribute("currentPath"));
		assertNotNull(model.getAttribute("pendingUsers"));

		// clearing admin
		SecurityContextHolder.clearContext();
	}


	@Test
	public void testRegister() {
		assertEquals("account/register",
		userController.register(model));
	}

 
    @Test
    void testDeleteMessage() {

	Long msg = messageManagement.createMessage(customerUserAccount.getId(), "newTestMessage", "I can get Info about...").getId();
	assertEquals("redirect:/messages",
	userController.deleteMessage(msg, Optional.of(customerUserAccount)));
	assertTrue(messageManagement.findById(msg).isEmpty());	
	}


    @Test
	@WithMockUser(roles = {"Admin"})
    void testManageUsers() {
		assertEquals("account/manageUsers",
    	userController.manageUsers(model, Optional.of(customerUserAccount)));
    }

    @Test
    void testMessages() {
		assertEquals("account/messages",
		userController.messages(model, Optional.of(customerUserAccount)));
	}


    @Test
    void testSetMessageRead() {
    
	Long msg = messageManagement.createMessage(customerUserAccount.getId(), "newTestMessage", "I can get Info about...").getId();
	assertEquals("redirect:/messages",
	userController.setMessageRead(msg, Optional.of(customerUserAccount)));
	}

	@Test
    void testRegisterNew() {
        
    }

	@Test
	@WithMockUser(roles = {"Admin"})
    void testDeactivateUser() {
   
		assertEquals("redirect:/users",
		userController.deactivateUser("testKäufer"));

		//Cleanup			
		customerUserAccount.setEnabled(true);
		userManagement.getUserAccountManagement().save(customerUserAccount);
    }

    @Test
	@WithMockUser(roles = {"Admin"})
    void testShowDeactivateUser() {
        

		//vorbedingung			
		customerUserAccount.setEnabled(false);
		userManagement.getUserAccountManagement().save(customerUserAccount);

		assertEquals( "account/deactivateUser",
		userController.showDeactivateUser(model,"testKäufer"));

		//Cleanup			
		customerUserAccount.setEnabled(true);
		userManagement.getUserAccountManagement().save(customerUserAccount);
    }

    @Test
    void testLogin() {
    }
	@Test
	void testChangeEmail(){
		PasswordForm passwordForm = new PasswordForm("LidlLidlLidl",
		"newPasswd",
		"newPasswd"
		,"null@mail.de");
		Errors errors = new DirectFieldBindingResult(passwordForm, "password");
		assertEquals("account/profile",
		userController.changeEmail(model,  Optional.of(customerUserAccount), passwordForm, errors));

		//cleanup
		userManagement.changeMail(customerUserAccount, "testKäufer@mail.de");
		
	}

	@Test
	void testChangePassword(){
		PasswordForm passwordForm = new PasswordForm("LidlLidlLidl",
		"newPasswd",
		"newPasswd"
		,"null@mail.de");
		Errors errors = new DirectFieldBindingResult(passwordForm, "passwordForm");

		assertEquals("account/profile",
		userController.changePassword(model,  Optional.of(customerUserAccount), passwordForm, errors));

		//Cleanup
		userManagement.changePassword(customerUserAccount,Password.UnencryptedPassword.of("LidlLidlLidl") );
	}
   
}