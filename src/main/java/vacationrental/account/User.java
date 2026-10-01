package vacationrental.account;

import jakarta.persistence.*;
import org.jmolecules.ddd.types.Identifier;
import org.salespointframework.core.AbstractAggregateRoot;
import org.salespointframework.useraccount.UserAccount;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * An extension of {@link AbstractAggregateRoot} to add User specific Parameters and Methods.
 *
 * @author Anthony, Sokar
 */
@Entity
public class User extends AbstractAggregateRoot<User.UserIdentifier> {
	private final @EmbeddedId UserIdentifier id = new UserIdentifier();

	@OneToOne
	@JoinColumn
	private UserAccount userAccount;

	@ElementCollection
	private final List<String> imagePath = new ArrayList<>();

	/**
	 * Default constructor for the {@link User} entity.
	 */
	public User() {
	}

	/**
	 * Constructor for creating a {@link User} with a {@link UserAccount}.
	 *
	 * @param userAccount The {@link UserAccount} associated with the user.
	 */
	public User(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	/**
	 * Retrieves the identifier of the {@link User}.
	 *
	 * @return The {@link UserIdentifier} of the user.
	 */
	public UserIdentifier getId() {
		return id;
	}

	/**
	 * Retrieves the {@link UserAccount} associated with the {@link User}.
	 *
	 * @return The {@link UserAccount} of the user.
	 */
	public UserAccount getUserAccount() {
		return userAccount;
	}

	/**
	 * Updates the {@link UserAccount} associated with the {@link User}.
	 *
	 * @param userAccount The new {@link UserAccount} to associate with the user.
	 */
	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	/**
	 * Retrieves the list of image paths associated with the {@link User}.
	 *
	 * @return A {@link List} of strings representing image paths.
	 */
	public List<String> getImagePath() {
		return imagePath;
	}

	/**
	 * Adds a path of where an image is saved to the {@link User}.
	 *
	 * @param imagePath String of path where image is saved.
	 */
	public void addImagePath(String imagePath) {
		this.imagePath.add(imagePath);
	}

	/**
	 * Removes a path of where an image is saved from the {@link User}.
	 *
	 * @param imagePath String of path where image is saved.
	 */
	public void removeImagePath(String imagePath) {
		for (String path : this.imagePath) {
			if (path.equals(imagePath) || path.contains(imagePath)) {
				this.imagePath.remove(path);
				break;
			}
		}
	}

	@Embeddable
	public static final class UserIdentifier implements Identifier, Serializable {
		@Serial
		private static final long serialVersionUID = 7740660930809051850L;

		private final UUID identifier;

		/**
		 * Default constructor for {@link UserIdentifier}.
		 * Generates a new UUID for identification.
		 */
		public UserIdentifier() {
			this(UUID.randomUUID());
		}

		/**
		 * Constructor for {@link UserIdentifier} with a specific UUID.
		 *
		 * @param identifier The {@link UUID} for the identifier.
		 */
		public UserIdentifier(UUID identifier) {
			this.identifier = identifier;
		}

		/**
		 * Retrieves the UUID of the {@link UserIdentifier}.
		 *
		 * @return The {@link UUID} of the identifier.
		 */
		public UUID getId() {
			return identifier;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;

			result = prime * result + (identifier == null ? 0 : identifier.hashCode());

			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof UserIdentifier)){
				return false;
			}

			return this.identifier.equals(((UserIdentifier) obj).identifier);
		}
	}


	/**
	 * Retrieves the profilePicPath of the {@link User}.
	 *
	 * @return The imagePath of the User or default.
	 */
	public String getProfilePicturePath() {
		return imagePath.isEmpty() ? "/img/profilePics/ted.webp" : imagePath.getFirst();
	}
}