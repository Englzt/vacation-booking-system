package vacationrental.eventcatalog;

import org.salespointframework.catalog.Catalog;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Streamable;
import vacationrental.location.Location;

import java.util.List;

/**
 * An extension of {@link Catalog} to add Event Shop specific query methods
 *
 * @author Erik Schneider
 */

public interface EventCatalog extends Catalog<Event>{
	/**
	 * Returns all {@link Event}s by location.
	 *
	 * @param location must not be {@literal null}.
	 * @return the events with the given location, never {@literal null}.
	 */
	Streamable<Event> findByLocation(Location location);

	/**
	 * Returns all {@link Event}s by name.
	 *
	 * @param name must not be {@literal null}.
	 * @return the events with the given name, never {@literal null}.
	 */
	@Override
	Streamable<Event> findByName(String name);

	/**
	 * Returns a List of all {@link Event}s of type BIG_EVENT with a ticket number higher or equal to the given one.
	 *
	 * @param tickets int of ticket number to search by.
	 * @return List of {@link Event}s of type BIG_EVENT.
	 */
	List<Event> findByTicketsGreaterThan(int tickets);

	/**
	 * Returns a List of all {@link Event}s of given type.
	 *
	 * @param type {@link EventType} to search by.
	 * @return List of {@link Event}s of given type.
	 */
	List<Event> findByType(EventType type);


	/**
	 * Returns a Streamable {@link Event} by {@link UserAccount} with given ID
	 *
	 * @param eventStaffId {@link org.salespointframework.useraccount.UserAccount.UserAccountIdentifier} to search by.
	 * @return Streamable {@link Event}.
	 */
	Streamable<Event> findByEventStaffId(UserAccount.UserAccountIdentifier eventStaffId);
}
