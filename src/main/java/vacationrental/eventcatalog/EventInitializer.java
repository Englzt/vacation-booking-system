package vacationrental.eventcatalog;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import vacationrental.account.UserManagement;
import vacationrental.booking.BookingManagement;
import vacationrental.location.Location;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


/**
 * A {@link DataInitializer} implementation that will create dummy data for the application on application startup.
 *
 * @author Erik Schneider
 * @see DataInitializer
 */
@Component
@Order(3)
public class EventInitializer implements DataInitializer{

    private static final Logger LOG = LoggerFactory.getLogger(EventInitializer.class);

    private final EventManagement eventManagement;
	private final UserManagement userManagement;
	private final BookingManagement bookingManagement;

	/**
	 * Constructor for {@link EventInitializer}.
	 *
	 * @param eventManagement {@link EventManagement} to use business logic related to {@link Event}s.
	 * @param userManagement {@link UserManagement} to use business logic related to {@link UserAccount}s.
	 */
    EventInitializer(EventManagement eventManagement,
					 UserManagement userManagement,
					 BookingManagement bookingManagement) {
		this.bookingManagement = bookingManagement;

		Assert.notNull(eventManagement, "EventManagement must not be null!");
		Assert.notNull(userManagement, "UserManagement must not be null!");

		this.eventManagement = eventManagement;
		this.userManagement = userManagement;
	}

	/**
	 * Create dummy {@link Event}s of type BIG_EVENT and SMALL_EVENT.
	 */
	@Override
	public void initialize() {

		LOG.info("Creating default events.");

		Location location1 = new Location("Semperstraße", "3", "01067", "Dresden", "Germany");
		Location location2 = new Location("Prager Straße", "2", "01069", "Dresden", "Germany");
		Location location3 = new Location("Großer Garten", "12", "01219", "Dresden", "Germany");
		Location location4 = new Location("An d. Richtermühle", "1", "01855", "Sebnitz", "Germany");

		UserAccount.UserAccountIdentifier userId = userManagement.findByUsername("EventStaff").get().getUserAccount().getId();
		UserAccount.UserAccountIdentifier testuserId = userManagement.
			findByUsername("myEventStaff").get().getUserAccount().getId();

		List.of(
			new EventForm("Yoga Flow",
				"Erleben Sie einen entspannenden Yoga-Kurs im Herzen der Stadt. Ideal für alle," +
					"die Körper und Geist in Einklang bringen möchten.",
				location1.getAddressString(), LocalDate.now().plusDays(1).toString(), LocalDate.now().plusDays(3).toString(),
				LocalTime.parse("17:00"), 25.0, 40, userId),
			new EventForm("Kultur Abend",
				"Ein geführter Rundgang mit spannenden Geschichten und kulturellen Highlights aus Dresdens Altstadt.",
				location2.getAddressString(), LocalDate.now().plusDays(4).toString(), LocalDate.now().plusDays(5).toString(),
				LocalTime.parse("19:30"), 18.0, 25, userId)
		).forEach(eventManagement::createBigEvent);

		List.of(
			new EventForm("Park Fußball",
				"Freizeitfußball für alle Altersgruppen im Grünen. Keine Anmeldung erforderlich.",
				location3.getAddressString(), LocalDate.now().plusDays(5).toString(), LocalDate.now().plusDays(7).toString(),
				LocalTime.parse("16:00"), userId, RecurrencePattern.NONE, LocalDate.now().toString()),
			new EventForm("Wander Ausflug",
				"Eine geführte Wanderung durch die idyllische Natur der Dresdner Heide mit atemberaubenden Aussichten.",
				location4.getAddressString(), LocalDate.now().plusDays(8).toString(), LocalDate.now().plusDays(9).toString(),
				LocalTime.parse("10:00"), userId, RecurrencePattern.WEEKLY, LocalDate.now().plusMonths(2).toString())
		).forEach(eventManagement::createSmallEvent);

		Event event1 = eventManagement.findByName("Park Fußball").toList().getFirst();
		Event event2 = eventManagement.findByName("Wander Ausflug").toList().getFirst();
		bookingManagement.bookAdsForSmallEvent(event1, 20000, event1.getEventStaffId());
		bookingManagement.bookAdsForSmallEvent(event2, 20000, event2.getEventStaffId());

		// Tests
		eventManagement.createBigEvent(new EventForm("Abend Yoga",
			"Ein sanfter Yoga-Kurs bei Sonnenuntergang. Perfekt für eine beruhigende Auszeit vom Alltag.",
			location1.getAddressString(),
			LocalDate.now().plusDays(2).toString(),
			LocalDate.now().plusDays(4).toString(),
			LocalTime.parse("15:30"),
			20.0,
			20,
			testuserId));
		eventManagement.createSmallEvent(new EventForm(
			"Winter Spaziergang",
			"Ein gemütlicher Spaziergang entlang der Elbe. Ideal, um die Winterstimmung zu genießen.",
			location3.getAddressString(),
			LocalDate.now().plusDays(3).toString(),
			LocalDate.now().plusDays(5).toString(),
			LocalTime.parse("15:30"),
			testuserId,
			RecurrencePattern.NONE,
			LocalDate.now().toString()));



		// Bilder hinzufügen
		eventManagement.findAll().forEach(event -> {
			switch (event.getName()) {
				case "Yoga Flow" -> event.addImagePath("/img/product/yoga.jpg");
				case "Kultur Abend" -> event.addImagePath("/img/product/culture.jpg");
				case "Park Fußball" -> event.addImagePath("/img/product/football.jpg");
				case "Wander Ausflug" -> event.addImagePath("/img/product/wandering.jpeg");
				case "Abend Yoga" -> event.addImagePath("/img/product/yoga_evening.jpg");
				case "Winter Spaziergang" -> event.addImagePath("/img/product/winter_wonderland.jpg");
				default -> event.addImagePath("/img/product/culture.jpg");
			}
		});
	}
}
