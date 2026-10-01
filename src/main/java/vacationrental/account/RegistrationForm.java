package vacationrental.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.salespointframework.useraccount.Role;

import java.util.Objects;


/**
 * A form backing class to collect data for the creation of {@link User}s.
 *
 * @author Anthony, Sokar
 */
public record RegistrationForm(
	@NotEmpty
	String name,
	String role,

	@NotEmpty
	@Pattern(
		regexp = "^(?=.*[A-Z]).{4,}$",
		message = "{profile.notMatchingPassword}"
	)
	String password,

	String repeatPassword,

	@NotEmpty
	@Email
	String email) {

	public RegistrationForm(String name, String role, String password, String repeatPassword, String email) {
		this.name = name;
		this.password = password;
		this.repeatPassword = repeatPassword;
		this.email = email;
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getRepeatpassword() {
		return repeatPassword;
	}

	public String getEmail() {
		return email;
	}

	public Role getRole() {
		return Role.of(role);
	}
	public boolean isRole() {
		return Objects.equals(this.role, "Customer") ||
			Objects.equals(this.role, "Landlord") ||
			Objects.equals(this.role, "Admin") ||
			Objects.equals(this.role, "EventStaff");
	}

}
