package vacationrental.booking;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Order;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends CrudRepository<Booking, Order.OrderIdentifier> {
	@NotNull
	@Override
	Optional<Booking> findById(@NotNull Order.OrderIdentifier id);

	@NotNull
	@Override
	Streamable<Booking> findAll();

	Streamable<Booking> findByStatus(BookingStatus status);

	Streamable<Booking> findByRentalInformation_House_Id(Product.ProductIdentifier id);

	Streamable<Booking> findByRentalInformation_House_IdAndRentalType(Product.ProductIdentifier id,
																	  RentalType rentalType);

	Streamable<Booking> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountId);

	Optional<Booking> findByOrderIdentifier(Order.OrderIdentifier orderIdentifier);

	Streamable<Booking> findByRentalInformation_House_LandlordIdAndStatus(UserAccount.UserAccountIdentifier landlordId,
																		  BookingStatus status);

	Streamable<Booking> findByUserAccountIdentifierAndStatus(UserAccount.UserAccountIdentifier userAccountIdentifier,
															 BookingStatus status);

	List<Booking> findByEventIdAndRentalInformation_House_Id(Product.ProductIdentifier eventId,
															 Product.ProductIdentifier id);

	Streamable<Booking> findByEventId(Product.ProductIdentifier eventId);

	Streamable<Booking> findByUserAccountIdentifierAndRentalType(UserAccount.UserAccountIdentifier userAccountIdentifier,
																 RentalType rentalType);

	/**
	 * Find a booking by user account identifier, landlord id, interval, rental type and with any but the provided status.
	 * @param userAccountIdentifier {@link org.salespointframework.useraccount.UserAccount.UserAccountIdentifier}
	 * @param landlordId {@link org.salespointframework.useraccount.UserAccount.UserAccountIdentifier}
	 * @param interval {@link org.salespointframework.time.Interval}
	 * @param rentalType {@link RentalType}
	 * @param status {@link BookingStatus}
	 * @return Streamable&lt;Booking&gt;
	 */
	@Query("""
		select b from Booking b
		where b.userAccountIdentifier = ?1 and b.rentalInformation.house.landlordId = ?2
				and b.rentalInformation.interval = ?3 and b.rentalType = ?4 and b.status <> ?5""")
	Streamable<Booking> findBy_UserId_LandlordId_Interval_Type_NotStatus(
		UserAccount.UserAccountIdentifier userAccountIdentifier,
		UserAccount.UserAccountIdentifier landlordId,
		Interval interval, RentalType rentalType,
		BookingStatus status);

	/**
	 * Find bookings by house id, rental type and status.
	 * @param id {@link Product.ProductIdentifier}
	 * @param rentalType {@link RentalType}
	 * @param status {@link BookingStatus}
	 * @return {@link Streamable<Booking>}
	 */
	@Query("select b from Booking b where b.rentalInformation.house.id = ?1 and b.rentalType = ?2 and b.status = ?3")
	Streamable<Booking> findBy_HouseId_Type_Status(Product.ProductIdentifier id,
												   RentalType rentalType,
												   BookingStatus status);

	/**
	 * Find bookings by house id, landlord id, rental type and status.
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param landlordId {@link UserAccount.UserAccountIdentifier}
	 * @param rentalType {@link RentalType}
	 * @param status {@link BookingStatus}
	 * @return {@link Streamable<Booking>}
	 */
	@Query("""
		select b from Booking b
		where b.rentalInformation.house.id = ?1 and b.rentalInformation.house.landlordId = ?2
				and b.rentalType = ?3 and b.status = ?4""")
	Streamable<Booking> findBy_HouseId_LandlordId_Type_Status(Product.ProductIdentifier houseId,
															  UserAccount.UserAccountIdentifier landlordId,
															  RentalType rentalType, BookingStatus status);

	/**
	 * Find bookings by house id, landlord id and rental type.
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param landlordId {@link UserAccount.UserAccountIdentifier}
	 * @param rentalType {@link RentalType}
	 * @return {@link Streamable<Booking>}
	 */
	@Query("""
		select b from Booking b
		where b.rentalInformation.house.id = ?1 and b.rentalInformation.house.landlordId = ?2 and b.rentalType = ?3""")
	Streamable<Booking> findBy_HouseId_LandlordId_Type(Product.ProductIdentifier houseId,
													   UserAccount.UserAccountIdentifier landlordId,
													   RentalType rentalType);

	/**
	 * Find bookings by house id and landlord id.
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param landlordId {@link UserAccount.UserAccountIdentifier}
	 * @return {@link Streamable<Booking>}
	 */
	@Query("""
		select b from Booking b
		where b.rentalInformation.house.id = ?1 and b.rentalInformation.house.landlordId = ?2""")
	Streamable<Booking> findBy_houseId_LandlordId(Product.ProductIdentifier houseId,
												  UserAccount.UserAccountIdentifier landlordId);

	List<Booking> findByRentalType(RentalType rentalType);

	/**
	 * Find bookings by house id, event id and by any but the two provided statuses.
	 * @param id {@link Product.ProductIdentifier} house id
	 * @param eventId {@link Product.ProductIdentifier} event id
	 * @param status {@link BookingStatus} status
	 * @param status1 {@link BookingStatus} status1
	 * @return {@link List<Booking>}
	 */
	@Query("""
		select b from Booking b
		where b.rentalInformation.house.id = ?1 and b.eventId = ?2 and b.status <> ?3 and b.status <> ?4""")
	List<Booking> findBy_HouseId_EventId_NotStatus_NotStatus1(Product.ProductIdentifier id,
															  Product.ProductIdentifier eventId,
															  BookingStatus status,
															  BookingStatus status1);

	/**
	 * Find bookings by event id, rental type and by any but the provided three statuses.
	 * @param eventId {@link Product.ProductIdentifier}
	 * @param rentalType {@link RentalType}
	 * @param status {@link BookingStatus}
	 * @param status1 {@link BookingStatus}
	 * @param status2 {@link BookingStatus}
	 * @return {@link List<Booking>}
	 */
	@Query("""
		select b from Booking b
		where b.eventId = ?1 and b.rentalType = ?2 and b.status <> ?3 and b.status <> ?4 and b.status <> ?5""")
	List<Booking> findBy_EventId_Type_NotStatus_NotStatus1_NotStatus2(Product.ProductIdentifier eventId,
																	  RentalType rentalType,
																	  BookingStatus status,
																	  BookingStatus status1,
																	  BookingStatus status2);

	/**
	 * Find bookings by house id, user account identifier and status.
	 * Used to check if Customer has a completed booking for a house and is allowed to leave a comment.
	 *
	 * @param id house id
	 * @param userAccountIdentifier user account identifier
	 * @param status BookingStatus
	 * @return list of bookings
	 */
	@Query("""
		select b from Booking b
		where b.rentalInformation.house.id = ?1 and b.userAccountIdentifier = ?2 and b.status = ?3""")
	List<Booking> findBy_CustomerId_HouseId_Status(Product.ProductIdentifier id,
												   UserAccount.UserAccountIdentifier userAccountIdentifier,
												   BookingStatus status);

	/**
	 * Find bookings by event id, user account identifier and status.
	 * Used to check if Customer has a completed booking for an event and is allowed to leave a comment.
	 * @param eventId event id
	 * @param userAccountIdentifier user account identifier
	 * @param status BookingStatus
	 * @return list of bookings
	 */
	@Query("select b from Booking b where b.eventId = ?1 and b.userAccountIdentifier = ?2 and b.status = ?3")
	List<Booking> findBy_CustomerId_EventId_Status(Product.ProductIdentifier eventId,
												   UserAccount.UserAccountIdentifier userAccountIdentifier,
												   BookingStatus status);


}
