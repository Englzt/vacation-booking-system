package vacationrental.account;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Extends {@link CrudRepository} to provide CRUD operations.
 *
 * @author Anthony, Sokar
 */
public interface UserRepository extends CrudRepository<vacationrental.account.User,
	vacationrental.account.User.UserIdentifier> {

	/**
	 * Retrieves all {@link User} entities.
	 *
	 * @return A {@link Streamable} containing all users.
	 */
	@NotNull
	@Override
	Streamable<User> findAll();

	/**
	 * Finds a {@link User} by their identifier.
	 *
	 * @param id The {@link User.UserIdentifier} of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	@NotNull
	@Override
	Optional<User> findById(@NotNull User.UserIdentifier id);

	/**
	 * Finds a {@link User} by their username.
	 *
	 * @param username The username of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	Optional<User> findByUserAccountUsername(@NotNull String username);

	/**
	 * Finds a {@link User} by their associated {@link UserAccount}.
	 *
	 * @param userAccount The {@link UserAccount} of the user.
	 * @return An {@link Optional} containing the user if found.
	 */
	Optional<User> findByUserAccount(UserAccount userAccount);

	/**
	 * Finds all {@link User} entities with disabled accounts.
	 *
	 * @return A list of {@link User} entities with disabled accounts.
	 */
	List<User> findByUserAccountEnabledFalse();
}