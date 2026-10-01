package vacationrental.booking;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import vacationrental.account.UserManagement;
import vacationrental.comments.CommentForm;
import vacationrental.comments.CommentManagement;
import vacationrental.comments.CommentType;
import vacationrental.eventcatalog.Event;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.util.TimeManager;

import java.util.List;

@Component
@Order(4)
public class RentalDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(RentalDataInitializer.class);

	private final BookingManagement bookingManagement;
	private final HouseManagement houseManagement;
	private final UserManagement userManagement;
	private final CommentManagement commentManagement;

	private final EventManagement eventManagement;

	RentalDataInitializer(BookingManagement bookingManagement,
						  HouseManagement houseManagement,
						  UserManagement userManagement,
						  CommentManagement commentManagement,
						  EventManagement eventManagement) {
		this.commentManagement = commentManagement;
		this.eventManagement = eventManagement;

		Assert.notNull(bookingManagement, "RentalManagement must not be null!");

		this.bookingManagement = bookingManagement;
		this.houseManagement = houseManagement;
		this.userManagement = userManagement;
	}

	@Override
	public void initialize() {
		LOG.info("Creating default bookings.");

		Interval.IntervalBuilder intervalBuilder = Interval.from(TimeManager.getTime());
		Interval interval = intervalBuilder.to(TimeManager.getTime().plusDays(5));

        UserAccount.UserAccountIdentifier userId = userManagement.findByUsername("User").get().getUserAccount().getId();

		House h1 = houseManagement.findByName("Woodland Cottage").toList().getFirst();
		House h2 = houseManagement.findByName("Elb Valley Retreat").toList().getFirst();

		Event event1 = eventManagement.findByName("Yoga Flow").toList().getFirst();
		Event event2 = eventManagement.findByName("Kultur Abend").toList().getFirst();

		Interval.IntervalBuilder intervalBuilder2 = Interval.from(TimeManager.getTime().plusDays(10));
		Interval interval2 = intervalBuilder2.to(TimeManager.getTime().plusDays(15));

		Interval.IntervalBuilder intervalBuilder3 = Interval.from(TimeManager.getTime().plusDays(15));
		Interval interval3 = intervalBuilder3.to(TimeManager.getTime().plusDays(20));

		List.of(
			new Booking(userId , new RentalInformation(h2, interval2)),
			new Booking(userId , new RentalInformation(h1, interval3)),
			new Booking(event1.getEventStaffId() , event1.getId() , new RentalInformation(h2, event1.getInterval())),
			new Booking(event2.getEventStaffId() , event2.getId(), new RentalInformation(h1, event2.getInterval())),
			new Booking(userId , new RentalInformation(h1, interval))

		).forEach(bookingManagement::addBooking);

		bookingManagement.findAll().forEach(booking -> {
			booking.setReserved();
			bookingManagement.save(booking);
		});

		UserAccount.UserAccountIdentifier uid1 = userManagement.findByUsername("Hans").get().getUserAccount().getId();
		UserAccount.UserAccountIdentifier uid2 = userManagement.findByUsername("Franz").get().getUserAccount().getId();

		// add default comments to house
		List.of(
			new CommentForm(h1.getId(), "Hans", "Unfreundlicher Vermieter!", 1, CommentType.HOUSECOMMENT, uid1),
			new CommentForm(h1.getId(), "Franz", "Schönes Haus, nette Lage.", 4, CommentType.HOUSECOMMENT, uid2)
		).forEach(commentManagement::addComment);

		bookingManagement.findByRentalType(RentalType.ADVERTISING).forEach(bookingManagement::setBookingStatusPaid);

		LOG.info("Initialization done - VacationRental is ready to use.");
	}
}
