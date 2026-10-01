package vacationrental.account;

import org.salespointframework.useraccount.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegistrationFormTests {

	private RegistrationForm form;

	@BeforeEach
	void setUp() {
		form = new RegistrationForm(
			"OttoNormalUser",
			"Customer",
			"OttosKatze",
			"OttosKatze",
			"otto@example.com"
		);
	}

	@Test
	public void testGetName() {
		assertEquals("OttoNormalUser", form.getName());
	}

	@Test
	public void testGetPassword() {
		assertEquals("OttosKatze", form.getPassword());
	}

	@Test
	public void testGetRepeatpassword() {
		assertEquals("OttosKatze", form.getRepeatpassword());
	}

	@Test
	public void testGetEmail() {
		assertEquals("otto@example.com", form.getEmail());
	}

	@Test
	public void testGetRole() {
		assertEquals(Role.of("Customer"), form.getRole());
	}

	@Test
	public void testIsRole() {
		assertTrue(form.isRole());
	}
}
