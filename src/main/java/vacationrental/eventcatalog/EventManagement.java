package vacationrental.eventcatalog;

import jakarta.annotation.PostConstruct;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;
import vacationrental.util.TimeManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.salespointframework.core.Currencies.EURO;


/**
 * Implementation of business logic related to {@link Event}s.
 *
 * @author Erik Schneider
 */
@Service
public class EventManagement {
	private final EventCatalog eventCatalog;
	private final HouseManagement houseManagement;


	@Value("${upload.directory}")
	private Path uploadDirectory;

	/**
	 * Constructor for {@link EventManagement}.
	 *
	 * @param eventCatalog {@link EventCatalog} to access {@link Event}s saved in the Catalog.
	 * @param houseManagement {@link HouseManagement} to use business logic related to {@link vacationrental.housecatalog.House}s.
	 */
	public EventManagement(EventCatalog eventCatalog, HouseManagement houseManagement) {
		Assert.notNull(eventCatalog, "EventCatalog must not be null!");
		Assert.notNull(houseManagement, "HouseManagement must not be null!");

		this.eventCatalog = eventCatalog;
		this.houseManagement = houseManagement;
	}

	/**
	 * Initialize the upload directory.
	 *
	 * @throws IOException if the directory could not be created.
	 */
	@PostConstruct
	public void init() throws IOException {
		uploadDirectory = Paths.get(System.getProperty("user.home"), "uploads");
		Files.createDirectories(uploadDirectory);
	}

	/**
	 * Save a {@link Event} in the {@link EventCatalog}.
	 *
	 * @param event {@link Event} to save.
	 * @return the saved {@link Event} in the {@link EventCatalog}.
	 */
	public Event save(Event event){
		return eventCatalog.save(event);
	}

	/**
	 * Add image path to an existing {@link Event}.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @param path String that contains the path where the image is stored.
	 */
	private void addImageToEvent(ProductIdentifier eventId, String path) {
		Optional<Event> event = eventCatalog.findById(eventId);
		if (event.isPresent()) {
			event.get().addImagePath(path);
			eventCatalog.save(event.get());
		}
	}

	/**
	 * Create a {@link Event} of type BIG_EVENT.
	 *
	 * @param bigEventForm {@link EventForm} with collected data needed by {@link Event}s constructor.
	 * @return the saved {@link Event} in the {@link EventCatalog}.
	 */
	public Event createBigEvent(EventForm bigEventForm) {
		Assert.notNull(bigEventForm, "EventForm form must not be null!");

		String name = bigEventForm.getName();
		String description = bigEventForm.getDescription();
		Location location = new Location(bigEventForm.getLocation());

		LocalDateTime start_date = LocalDate.parse(bigEventForm.getStartDate()).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(bigEventForm.getEndDate()).atTime(LocalTime.parse("23:59"));
		if (!checkInvalidInterval(start_date, end_date)) {
			return null;
		}
		Interval interval = Interval.from(start_date).to(end_date);

		LocalTime time = bigEventForm.getTime();
		double ticketprice = bigEventForm.getTicketprice();
		int tickets = bigEventForm.getTickets();

		return eventCatalog.save(new Event(name, description, location, interval, time, ticketprice,
			tickets, EventType.BIG_EVENT, bigEventForm.getEventStaffId()));
	}

	/**
	 * Check if the start date of an Interval is before its end date.
	 *
	 * @param start_date LocalDateTime that contains the start date.
	 * @param end_date LocalDateTime that contains the end date.
	 * @return if Interval is valid.
	 */
	public boolean checkInvalidInterval(LocalDateTime start_date, LocalDateTime end_date) {
		return !start_date.isAfter(end_date) && !start_date.isBefore(TimeManager.getTime());
	}

	/**
	 * Create a {@link Event} of type SMALL_EVENT.
	 *
	 * @param smallEventForm {@link EventForm} with collected data needed by {@link Event}s constructor.
	 * @return the saved {@link Event} in the {@link EventCatalog}.
	 */
	public Event createSmallEvent(EventForm smallEventForm) {
		Assert.notNull(smallEventForm, "EventForm form must not be null!");

		String name = smallEventForm.getName();
		String description = smallEventForm.getDescription();
		Location location = new Location(smallEventForm.getLocation());

		LocalDateTime start_date = LocalDate.parse(smallEventForm.getStartDate()).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(smallEventForm.getEndDate()).atTime(LocalTime.parse("23:59"));
		if (!checkInvalidInterval(start_date, end_date)) {
			return null;
		}
		Interval interval = Interval.from(start_date).to(end_date);

		LocalTime time = smallEventForm.getTime();

		LocalDate endOfRecurrence = LocalDate.parse(smallEventForm.getEndOfRecurrence());
		return eventCatalog.save(new Event(name, description, location, interval, time, EventType.SMALL_EVENT,
			smallEventForm.getEventStaffId(), smallEventForm.getRecurrencePattern(), endOfRecurrence));
	}

	/**
	 * Cancel aan existing {@link Event}.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @return if the {@link Event} was cancelled successfully.
	 */
	public boolean cancelEvent(ProductIdentifier eventId) {
		Assert.notNull(eventId, "Event Id must not be null");
		Optional<Event> event = eventCatalog.findById(eventId);
		if (event.isPresent()) {
			event.get().cancel();
			eventCatalog.save(event.get());
			return true;
		}
		return false;
	}

	/**
	 * Get a Stream of all {@link House}s. Used to get houses in the EditEventForm.
	 * @return Stream of all {@link House}s.
	 */
	public Streamable<House> getHouses() {
		return houseManagement.findAll();
	}

	/**
	 * Edit an existing {@link Event} of type BIG_EVENT.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @param bigEventForm {@link EventForm} with collected data needed by {@link Event}s constructor.
	 * @return boolean if the {@link Event} was edited successfully.
	 */
	public boolean editBigEvent(ProductIdentifier eventId, EventForm bigEventForm) {
		Optional<Event> existingEvent = eventCatalog.findById(eventId);

		if (existingEvent.isEmpty()) {
			return false;
		}

		Event bigEvent = existingEvent.get();

		bigEvent.setName(bigEventForm.getName());
		bigEvent.setDescription(bigEventForm.getDescription());
		bigEvent.setLocation(new Location(bigEventForm.getLocation()));

		LocalDateTime start_date = LocalDate.parse(bigEventForm.getStartDate()).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(bigEventForm.getEndDate()).atTime(LocalTime.parse("23:59"));
		if (!checkInvalidInterval(start_date, end_date)) {
			return false;
		}
		bigEvent.setInterval(Interval.from(start_date).to(end_date));

		bigEvent.setTime(bigEventForm.getTime());
		bigEvent.setPrice(Money.of(bigEventForm.getTicketprice(), EURO));
		bigEvent.increaseTickets(bigEventForm.getTickets());
		eventCatalog.save(bigEvent);
		return true;
	}

	/**
	 * Edit an existing {@link Event} of type SMALL_EVENT.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @param smallEventForm {@link EventForm} with collected data needed by {@link Event}s constructor.
	 * @return boolean if the {@link Event} was edited successfully.
	 */
	public boolean editSmallEvent(ProductIdentifier eventId, EventForm smallEventForm) {
		Optional<Event> existingEvent = eventCatalog.findById(eventId);

		if (existingEvent.isEmpty()) {
			return false;
		}

		Event smallEvent = existingEvent.get();

		smallEvent.setName(smallEventForm.getName());
		smallEvent.setDescription(smallEventForm.getDescription());
		smallEvent.setLocation(new Location(smallEventForm.getLocation()));

		LocalDateTime start_date = LocalDate.parse(smallEventForm.getStartDate()).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(smallEventForm.getEndDate()).atTime(LocalTime.parse("23:59"));
		if (!checkInvalidInterval(start_date, end_date)) {
			return false;
		}
		smallEvent.setInterval(Interval.from(start_date).to(end_date));

		smallEvent.setTime(smallEventForm.getTime());

		smallEvent.setRecurrencePattern(smallEventForm.getRecurrencePattern());

		smallEvent.setEndOfRecurrence(LocalDate.parse(smallEventForm.getEndOfRecurrence()));

		eventCatalog.save(smallEvent);
		return true;
	}

	/**
	 * Returns a Stream of all {@link Event}s.
	 * @return Stream of all {@link Event}s.
	 */
	public Streamable<Event> findAll() {
		return eventCatalog.findAll();
	}

	/**
	 * Find an {@link Event} by its ID.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @return Optional of the {@link Event} with the given ID.
	 */
	public Optional<Event> findById(ProductIdentifier eventId) {
		Assert.notNull(eventId, "Event Id must not be null");
		return eventCatalog.findById(eventId);
	}

	/**
	 * Find an {@link Event} by its name.
	 *
	 * @param name Name of the {@link Event}.
	 * @return Stream of all {@link Event}s with the given name.
	 */
	public Streamable<Event> findByName(String name) {
		return eventCatalog.findByName(name);
	}

	/**
	 * Find all {@link Event}s of a given {@link EventType}.
	 *
	 * @param type {@link EventType} of the {@link Event}s.
	 * @return List of all {@link Event}s with the given {@link EventType}.
	 */
	public List<Event> findByEventType(EventType type) {
		List<Event> list = eventCatalog.findByType(type);

		/* remove all passed events */
		ChronoLocalDate now = ChronoLocalDate.from(TimeManager.getTime());
		list.removeIf(event -> event.getEndOfRecurrence().isBefore(now));
		return list;
	}

	/**
	 * Find all {@link Event}s of a given {@link EventType}.
	 *
	 * @param type {@link EventType} of the {@link Event}s.
	 * @return List of all {@link Event}s with the given {@link EventType}.
	 */
	public List<Event> findByEventTypeInPast(UserAccount.UserAccountIdentifier eventStaffId, EventType type) {
		List<Event> list = eventCatalog.findByType(type);

		/* remove all passed events and events where the eventStaffId differs */
		ChronoLocalDate now = ChronoLocalDate.from(TimeManager.getTime());
		list.removeIf(event -> event.getEndOfRecurrence().isAfter(now) && event.getEventStaffId().equals(eventStaffId));
		return list;
	}


	/**
	 * Delete an image from an {@link Event}.
	 *
	 * @param id ID of the {@link Event}.
	 * @param imageUrl String that contains the path where the image is stored.
	 */
	public void deleteImageFromEvent(ProductIdentifier id, String imageUrl) {
		Optional<Event> event = eventCatalog.findById(id);
		if (event.isPresent()) {
			event.get().removeImagePath(imageUrl);
			eventCatalog.save(event.get());
		}
	}

	/**
	 * Add images to an {@link Event}.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @param files Array of MultipartFiles that contain the images to add.
	 */
	public void addImages(Product.ProductIdentifier eventId, MultipartFile[] files) {
		Event event = eventCatalog.findById(eventId).get();
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}

			if (!event.getImagePaths().isEmpty()) {
				break;
			}

			String fileName = UUID.randomUUID().toString();

			if ("image/jpeg".equals(file.getContentType()) || "image/jpg".equals(file.getContentType())) {
				fileName += ".jpg";
			} else if ("image/png".equals(file.getContentType())) {
				fileName += ".png";
			} else if ("image/webp".equals(file.getContentType())) {
				fileName += ".webp";
			} else {
				continue;
			}

			Path fileNameAndPath = uploadDirectory.resolve(fileName);

			try {
				Files.write(fileNameAndPath, file.getBytes());
				String imageUrl = "/images/" + fileName;
				addImageToEvent(eventId, imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* add default image if no image is set */
		if (event.getImagePaths().isEmpty()) {
			addImageToEvent(eventId, "/img/product/culture.jpg");
		}
	}

	/**
	 * returning a list of events which have a >= tickets left than the given number + all Small Events
	 * @param min_tickets
	 * @return List of events
	 */
	public List<Event> getAllEventsWithTicketsLeftAndSmallEvents(int min_tickets){
		ChronoLocalDate now = ChronoLocalDate.from(TimeManager.getTime());
		List<Event> allEvents = eventCatalog.findByType(EventType.SMALL_EVENT);
		allEvents.addAll(eventCatalog.findByTicketsGreaterThan(min_tickets));

		/* remove all passed events */
		allEvents.removeIf(event -> event.getEndOfRecurrence().isBefore(now));
		return allEvents;
	}

	/**
	 * Find all {@link Event}s of a given {@link UserAccount.UserAccountIdentifier}.
	 *
	 * @param id {@link UserAccount.UserAccountIdentifier} of the {@link Event}s.
	 * @return Stream of all {@link Event}s with the given {@link UserAccount.UserAccountIdentifier}.
	 */
	public Streamable<Event> findByEventStaffId(UserAccount.UserAccountIdentifier id) {
		return eventCatalog.findByEventStaffId(id);
	}

	/**
	 * Get a list of intervals for recurring events.
	 * @param event {@link Event}
	 * @return List of {@link Interval}
	 */
	public List<Interval> getRecurringDates(Event event) {
		List<Interval> recurringDates = new ArrayList<>();

		if (event.getType() != EventType.SMALL_EVENT || event.getRecurrencePattern() == RecurrencePattern.NONE) {
			return recurringDates;
		}

		LocalDateTime startDate = event.getInterval().getStart();
		LocalDateTime endDate = event.getInterval().getEnd();
		LocalDateTime endOfRecurrence = event.getEndOfRecurrence().atTime(LocalTime.MAX);

		ChronoUnit unit;
		switch (event.getRecurrencePattern()) {
			case WEEKLY:
				unit = ChronoUnit.WEEKS;
				break;
			case MONTHLY:
				unit = ChronoUnit.MONTHS;
				break;
			case YEARLY:
				unit = ChronoUnit.YEARS;
				break;
			default:
				return recurringDates;
		}

		while (endDate.plus(1, unit).isBefore(endOfRecurrence) || endDate.plus(1, unit).isEqual(endOfRecurrence)) {
			startDate = startDate.plus(1, unit);
			endDate = endDate.plus(1, unit);
			Interval interval = Interval.from(startDate).to(endDate);
			Assert.notNull(interval, "Interval must not be null");
			recurringDates.add(interval);
		}

		return recurringDates;
	}

	/**
	 * Checks if the interval of a event has passed.
	 * @param event {@link Event}
	 * @return boolean
	 */
	public boolean isIntervalReached(Event event) {
		LocalDateTime now = TimeManager.getTime();
		Interval currentInterval = event.getInterval();
		return currentInterval.getEnd().isBefore(now) || currentInterval.getEnd().isEqual(now);
	}

	/**
	 * Advances the interval of a small event to the next occurrence.
	 * @param event {@link Event}
	 */
	public void setNextInterval(Event event) {
		if (event.getRecurrencePattern() == RecurrencePattern.NONE) {
			return;
		}

		LocalDateTime nextStart = event.getInterval().getStart();
		LocalDateTime nextEnd = event.getInterval().getEnd();
		LocalDateTime endOfRecurrence = LocalDate.parse(event.getEndOfRecurrence().toString())
			.atTime(LocalTime.parse("23:59"));

		switch (event.getRecurrencePattern()) {
			case WEEKLY:
				nextStart = nextStart.plusWeeks(1);
				nextEnd = nextEnd.plusWeeks(1);
				break;
			case MONTHLY:
				nextStart = nextStart.plusMonths(1);
				nextEnd = nextEnd.plusMonths(1);
				break;
			case YEARLY:
				nextStart = nextStart.plusYears(1);
				nextEnd = nextEnd.plusYears(1);
				break;
			default:
				return;
		}

		if (nextEnd.isAfter(endOfRecurrence) || nextEnd.isEqual(endOfRecurrence)) {
			return;
		}

		Interval newInterval = Interval.from(nextStart).to(nextEnd);
		event.setInterval(newInterval);
		eventCatalog.save(event);
	}


	/**
	 * Returns a list of dates (String), used to display recurring events in the calendar in event details page.
	 * @param event {@link Event}
	 * @return List&lt;String&gt;
	 */
	public List<String> getCalendarData(Event event) {
		List<Interval> recurrences = getRecurringDates(event);
		recurrences.add(event.getInterval());
		List<String> list = new ArrayList<>();

		for (Interval interval : recurrences) {
			LocalDateTime start = interval.getStart();
			LocalDateTime end = interval.getEnd();

			LocalDateTime current = start;

			while (current.isBefore(end) || current.isEqual(end)) {
				String date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				list.add(date);
				current = current.plusDays(1);
			}
		}

		return list;
	}

	/**
	 * Check if a recurrence pattern is valid.
	 * @param startDate String
	 * @param endOfRecurrence String
	 * @param recurrencePattern {@link RecurrencePattern}
	 * @return boolean
	 */
	public boolean checkValidEndOfRecurrence(String startDate, String endOfRecurrence,
											 RecurrencePattern recurrencePattern){

		LocalDate startDateLocalDate = LocalDate.parse(startDate);
		LocalDate endOfRecurrenceLocalDate = LocalDate.parse(endOfRecurrence);

		boolean validRecurrence = switch (recurrencePattern) {
            case NONE -> true;
            case WEEKLY -> endOfRecurrenceLocalDate.isAfter(startDateLocalDate.plusWeeks(1));
            case MONTHLY -> endOfRecurrenceLocalDate.isAfter(startDateLocalDate.plusMonths(1));
            case YEARLY -> endOfRecurrenceLocalDate.isAfter(startDateLocalDate.plusYears(1));
        };

        return validRecurrence;
	}

	/**
	 * EventListener for the dayHasPassed event, checks if the interval
	 * of an small event has passed and updates it if necessary.
	 * @param event {@link BusinessTime.DayHasPassed}
	 */
	@EventListener
	void dayPassed(BusinessTime.DayHasPassed event) {
		eventCatalog.findAll().forEach(eventInst -> {
			if (eventInst.getType() == EventType.SMALL_EVENT && isIntervalReached(eventInst)) {
				setNextInterval(eventInst);
			}
		});
	}

	/**
	 * Calculates the total Price of a Map with type Event, Integer
	 * @param eventMap
	 * @return
	 */
	public Money calculateTotalEventPrice(Map<Event, Integer> eventMap){
		Money totalPrice = Money.of(0, EURO);

		for (Event event: eventMap.keySet()){
			totalPrice = totalPrice.add(Money.from(event.getPrice()).multiply(eventMap.get(event)));
		}
		return totalPrice;
	}


	/**
	 * updating the average Rating for the event and save the update
	 * @param eventId
	 * @param averageRating
	 * @return
	 */
	public Event updateAverageRating(ProductIdentifier eventId, double averageRating) {
		Event event = findById(eventId).get();
		event.setAverageRating(averageRating);
		return eventCatalog.save(event);
	}

	public void addDefaultImageIfNone(ProductIdentifier eventId) {
		Optional<Event> event = eventCatalog.findById(eventId);
		if (event.isPresent() && event.get().getImagePaths().isEmpty()) {
			event.get().addImagePath("/img/product/culture.jpg");
			eventCatalog.save(event.get());
		}
	}
}