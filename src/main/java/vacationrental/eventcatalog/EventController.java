package vacationrental.eventcatalog;

import com.mysema.commons.lang.Assert;
import jakarta.validation.Valid;
import org.salespointframework.catalog.Product;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Streamable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vacationrental.account.User;
import vacationrental.booking.BookingManagement;
import vacationrental.comments.CommentForm;
import vacationrental.comments.CommentManagement;
import vacationrental.comments.CommentType;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.util.TimeManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A Spring MVC controller to manage the {@link Event}s.
 *
 * @author Erik Schneider
 */
@Controller
public class EventController {
	private final EventManagement eventManagement;
	private final BookingManagement bookingManagement;
	private final HouseManagement houseManagement;
	private final CommentManagement commentManagement;

	@Value("${vacationrental.nearby-houses-radius}")
	private int radius;

	/**
	 * Constructor for {@link EventController}.
	 *
	 * @param eventManagement {@link EventManagement} to use business logic related to {@link Event}s.
	 * @param bookingManagement {@link BookingManagement} for booking of Events.
	 * @param houseManagement {@link HouseManagement} to use business logic related to {@link vacationrental.housecatalog.House}s.
	 */
	public EventController(EventManagement eventManagement, BookingManagement bookingManagement,
						   HouseManagement houseManagement, CommentManagement commentManagement) {
		this.bookingManagement = bookingManagement;
		this.houseManagement = houseManagement;
		this.commentManagement = commentManagement;
		Assert.notNull(eventManagement, "Eventmanagement must not be null");

		this.eventManagement = eventManagement;

	}

	/**
	 * Displays all {@link Event}s in the system.
	 *
	 * @param model will never be {@literal null}.
	 * @return the view name.
	 */
	@GetMapping("/events")
	public String showEvents(Model model) {
		List<Event> allEvents = eventManagement.getAllEventsWithTicketsLeftAndSmallEvents(0);
		model.addAttribute("eventList", bookingManagement.removeEventsWithoutHouse(allEvents));

		model.addAttribute("currentPath", "/events");
		return "event/events";
	}

	/**
	 * Displays all {@link Event}s by currently logged-in {@link vacationrental.account.User} with Role EventStaff in the system.
	 *
	 * @param model will never be {@literal null}.
	 * @return the view name.
	 */
	@GetMapping("/myEvents")
	@PreAuthorize("hasRole('EventStaff')")
	public String showMyEvents(Model model, @LoggedIn Optional<UserAccount> user) {

		UserAccount.UserAccountIdentifier eventStaffId = user.get().getId();

		Streamable<Event> events = eventManagement.findByEventStaffId(eventStaffId);

		model.addAttribute("allSmallEvents", events.filter(event -> event.getType().equals(EventType.SMALL_EVENT)));
		model.addAttribute("allBigEvents", events.filter(event -> event.getType().equals(EventType.BIG_EVENT)));

		model.addAttribute("currentPath", "/myEvents");
		return "event/myEvents";
	}

	/**
	 * Displays all {@link Event}s by currently logged-in {@link vacationrental.account.User} with Role EventStaff in the system.
	 *
	 * @param model will never be {@literal null}.
	 * @return the view name.
	 */
	@GetMapping("/myEvents/pastEvents")
	@PreAuthorize("hasRole('EventStaff')")
	public String showPastEvents(Model model, @LoggedIn Optional<UserAccount> user) {
		UserAccount.UserAccountIdentifier eventStaffId = user.get().getId();
		model.addAttribute("allSmallEvents", eventManagement.findByEventTypeInPast(eventStaffId, EventType.SMALL_EVENT));
		model.addAttribute("allBigEvents", eventManagement.findByEventTypeInPast(eventStaffId, EventType.BIG_EVENT));

		model.addAttribute("currentPath", "/myEvents/pastEvents");
		return "event/pastEvents";
	}

	/**
	 * Displays the site to collect Data for {@link EventForm}.
	 *
	 * @param model will never be {@literal null}.
	 * @param user {@link UserAccount}
	 * @return the view name.
	 */
	@GetMapping("/createSmallEvent")
	@PreAuthorize("hasRole('EventStaff')")
	public String showCreateSmallEvent(Model model, @LoggedIn Optional<UserAccount> user) {

		UserAccount.UserAccountIdentifier eventStaffId = user.get().getId();

		model.addAttribute("smallEventForm",
			new EventForm("", "", "",
				LocalDate.now().plusDays(1).toString(),
				LocalDate.now().plusDays(2).toString(),
				LocalTime.parse("18:00"), eventStaffId,
				RecurrencePattern.NONE, LocalDate.now().toString()));
		model.addAttribute("dateError", null);
		model.addAttribute("recurrencePatternEnum", RecurrencePattern.values());
		return "event/createSmallEvent";
	}

	/**
	 * Displays the site to collect Data for {@link EventForm}.
	 *
	 * @param model will never be {@literal null}.
	 * @param user {@link UserAccount}
	 * @return the view name.
	 */
	@GetMapping("/createBigEvent")
	@PreAuthorize("hasRole('EventStaff')")
	public String showCreateBigEvent(Model model, @LoggedIn Optional<UserAccount> user) {

		UserAccount.UserAccountIdentifier eventStaffId = user.get().getId();

		// solange kalender kaputt sind wird datum automatisch eingetragen
		model.addAttribute("bigEventForm",
			new EventForm("", "", "",
				LocalDate.now().plusDays(1).toString(),
				LocalDate.now().plusDays(2).toString(),
				LocalTime.parse("18:00"),
				0.0, 0, eventStaffId));
		model.addAttribute("dateError", null);

		return "event/createBigEvent";
	}

	/**
	 * Creates a {@link Event} of type SMALL_EVENT.
	 *
	 * @param smallEventForm the {@link EventForm} with the collected Data to create a {@link Event} of type SMALL_EVENT.
	 * @param model will never be {@literal null}.
	 * @param files multiple files of type MultipartFile to add Images to the {@link Event} of type SMALL_EVENT.
	 * @return the view name and ID of created {@link Event} of type SMALL_EVENT.
	 */
	@PostMapping("/createSmallEvent")
	@PreAuthorize("hasRole('EventStaff')")
	public String createSmallEvent(@Valid @ModelAttribute("smallEventForm") EventForm smallEventForm, Model model,
								   @RequestParam("files") MultipartFile[] files,
								   @RequestParam int advertisingRadius,
								   @LoggedIn Optional<UserAccount> userAccount) {

		/* for some reason the eventStaffId is null in the form, even though it is correctly passed from showCreateSmallEvent.
		 * So we get the currently logged in user here and overwrite the null value */
		UserAccount.UserAccountIdentifier eventStaffId = userAccount.get().getId();
		smallEventForm.setEventStaffId(eventStaffId);

		if (!eventManagement.checkInvalidInterval(LocalDate.parse(smallEventForm.getStartDate())
				.atTime(LocalTime.parse("00:01")),
			LocalDate.parse(smallEventForm.getEndDate()).atTime(LocalTime.parse("23:59")))) {
			model.addAttribute("dateError", "Der Zeitraum muss gültig sein und in der Zukunft liegen!");
			model.addAttribute("recurrencePatternEnum", RecurrencePattern.values());
			model.addAttribute("smallEventForm", smallEventForm);
			return "event/createSmallEvent";
		}

		if(!eventManagement.checkValidEndOfRecurrence(smallEventForm.getStartDate(), 
		smallEventForm.getEndOfRecurrence(), smallEventForm.getRecurrencePattern())){
			String howMuchOfWhat = switch (smallEventForm.getRecurrencePattern()) {
				case NONE -> "Wenn das angezeigt wird läuft irgendwas gewaltig falsch";
				case WEEKLY -> "eine Woche";
				case MONTHLY -> "einen Monat";
				case YEARLY -> "ein Jahr";
			};

			model.addAttribute("recurrenceError",
				"Das Datum muss gültig sein und mindestens " + howMuchOfWhat + " in der Zukunft liegen!");
			model.addAttribute("recurrencePatternEnum", RecurrencePattern.values());
			model.addAttribute("smallEventForm", smallEventForm);
			return "event/createSmallEvent";
		}

		Event event = eventManagement.createSmallEvent(smallEventForm);
		model.addAttribute("event", event);
		model.addAttribute("housesInRadius",
			houseManagement.getAllNearbyHousesToLocation(advertisingRadius, event.getLocation()));
		model.addAttribute("advertisingRadius", advertisingRadius);
		if (files.length !=0){
		eventManagement.addImages(event.getId(), files);
		}
		return "event/bookAdvertisments";
	}

	/**
	 * Creates a {@link Event} of type BIG_EVENT.
	 *
	 * @param bigEventForm the {@link EventForm} with the collected Data to create a {@link Event} of type BIG_EVENT.
	 * @param result the BindingResult to catch errors.
	 * @param model will never be {@literal null}.
	 * @param files multiple files of type MultipartFile to add Images to the {@link Event} of type BIG_EVENT.
	 * @return the view name
	 */
	@PostMapping("/createBigEvent")
	@PreAuthorize("hasRole('EventStaff')")
	public String createBigEvent(@Valid @ModelAttribute("bigEventForm") EventForm bigEventForm, BindingResult result,
								 Model model, @RequestParam("files") MultipartFile[] files, @LoggedIn Optional<UserAccount> userAccount) {

		/* for some reason the eventStaffId is null in the form, even though it is correctly passed from showCreateBigEvent.
		* So we get the currently logged in user here and overwrite the null value */
		UserAccount.UserAccountIdentifier eventStaffId = userAccount.get().getId();
		bigEventForm.setEventStaffId(eventStaffId);

		if (!eventManagement.checkInvalidInterval(LocalDate.parse(
			bigEventForm.getStartDate()).atTime(LocalTime.parse("00:01")),
			LocalDate.parse(bigEventForm.getEndDate()).atTime(LocalTime.parse("23:59")))) {
			model.addAttribute("dateError", "Der Zeitraum muss gültig sein und in der Zukunft liegen!");
			model.addAttribute("bigEventForm", bigEventForm);
			return "event/createBigEvent";
		}

		Event event = eventManagement.createBigEvent(bigEventForm);
		if (files.length !=0){
		eventManagement.addImages(event.getId(), files);
		}
		model.addAttribute("nearbyHouses",
			bookingManagement.getNearbyFreeHousesToLocation(event.getLocation(), event.getId(), radius));
		model.addAttribute("eventId", event.getId());

		return "event/bookHouseForEvent";
	}

	/**
	 * Shows the bookable Houses for a specific {@link Event}.
	 *
	 * @param model will never be {@literal null}.
	 * @param eventId ID of the {@link Event} for which the bookable {@link vacationrental.housecatalog.House}s should be displayed.
	 * @return the view name
	 */
	@GetMapping("/myEvents/bookHouses")
	@PreAuthorize("hasRole('EventStaff')")
	public String showBookableHouses(Model model, @RequestParam Product.ProductIdentifier eventId) {
		Event event = eventManagement.findById(eventId).get();
		model.addAttribute("radius", radius);
		model.addAttribute("nearbyHouses",
			bookingManagement.getNearbyFreeHousesToLocation(event.getLocation(), eventId, radius));
		model.addAttribute("eventId", eventId);
		return "event/bookHouseForEvent";
	}

	/**
	 * Books the selected Houses for a specific {@link Event}.
	 *
	 * @param houseIds IDs of the {@link vacationrental.housecatalog.House}s to book.
	 * @param eventId ID of the {@link Event}.
	 * @param userAccount Account of a {@link vacationrental.account.User} who books the {@link vacationrental.housecatalog.House}s.
	 * @return the view name
	 */
	@PostMapping("/myEvents/bookHouses")
	@PreAuthorize("hasRole('EventStaff')")
	public String bookHouseToEvent(@RequestParam List<String> houseIds,
								   @RequestParam Product.ProductIdentifier eventId,
								   @LoggedIn Optional<UserAccount> userAccount){
		UserAccount.UserAccountIdentifier userId = userAccount.get().getId();
		if(houseIds.isEmpty()){
			return "redirect:/myEvents";
		}
		houseIds.removeLast();
		Event event = eventManagement.findById(eventId).get();
		List<Product.ProductIdentifier> ids = houseIds.stream().map(Product.ProductIdentifier::of).toList();
		bookingManagement.bookHousesForEvent(ids, event, userId);
		return "redirect:/myEvents";
	}


	/**
	 * Shows page to choose Radius.
	 *
	 * @param model will never be {@literal null}.
	 * @param id ID of the {@link Event}.
	 * @return the view name
	 */
	@GetMapping("/myEvents/chooseAdvertismentRadius/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	String chooseRadiusPage(Model model, @PathVariable Product.ProductIdentifier id) {
		model.addAttribute("event", eventManagement.findById(id).get());
		return "event/chooseAdvertismentRadius";
	}


	/**
	 * Shows Houses in Radius.
	 *
	 * @param model will never be {@literal null}.
	 * @param id ID of the {@link Event}.
	 * @param advertisingRadius int of Radius to advertise in.
	 * @return the view name.
	 */
	@PostMapping("/myEvents/chooseAdvertismentRadius/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	String showHousesInRadius(Model model, @PathVariable Product.ProductIdentifier id,
							  @RequestParam int advertisingRadius) {
		Event smallEvent = eventManagement.findById(id).get();
		List<House> housesInRadius = houseManagement.getAllNearbyHousesToLocation(advertisingRadius,
			smallEvent.getLocation());
		model.addAttribute("event", smallEvent);
		model.addAttribute("housesInRadius", bookingManagement.removeHousesWithActiveAdvertising(housesInRadius, id));
		model.addAttribute("advertisingRadius", advertisingRadius);
		return "event/bookAdvertisments";
	}


	/**
	 * Books Advertisements for a {@link Event} of type SMALL_EVENT.
	 *
	 * @param id ID of the {@link Event}.
	 * @param advertisingRadius int of Radius to advertise in.
	 * @param userAccount Optional of {@link UserAccount}.
	 * @return the view name and id.
	 */
	@PostMapping("/myEvents/bookAdvertisments/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	String bookAdvertisementsForSmallEvent(@PathVariable Product.ProductIdentifier id,
										   @RequestParam int advertisingRadius,
										   @LoggedIn Optional<UserAccount> userAccount) {
		bookingManagement.bookAdsForSmallEvent(eventManagement.findById(id).get(),
			advertisingRadius, userAccount.get().getId());
		return "redirect:/event/" + id;
	}


	/**
	 * Shows a {@link Event} that is to be deleted.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @param model will never be {@literal null}.
	 * @return the view name
	 */
	@GetMapping("/events/deleteEvent/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	public String showDeleteEvent(@PathVariable("id") Product.ProductIdentifier eventId, Model model) {
		Event event = eventManagement.findById(eventId).get();
		model.addAttribute("event", event);
		return "event/deleteEvent";
	}

	/**
	 * Deletes a {@link Event}.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @return the view name
	 */
	@PostMapping("/events/deleteEvent/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	public String deleteEvent(@PathVariable("id") Product.ProductIdentifier eventId) {
		boolean cancelled = eventManagement.cancelEvent(eventId);
		if (cancelled) {
			bookingManagement.cancelEventBookings(eventId, null);
		}
		return "redirect:/myEvents";
	}


	/**
	 * Shows the details of a {@link Event}.
	 *
	 * @param eventId ID of the {@link Event}.
	 * @param model will never be {@literal null}.
	 * @return the view name
	 */
	@GetMapping("/event/{id}")
	public String showEvent(@PathVariable("id") Product.ProductIdentifier eventId, Model model,
								   @LoggedIn Optional<UserAccount> userAccount) {
		Optional<Event> eventOptional = eventManagement.findById(eventId);
		UserAccount account = userAccount.orElse(null);

		if (eventOptional.isEmpty()) {
			return "redirect:/events";
		}

		model.addAttribute("bookedHouses", bookingManagement.getBookedHousesForEvent(eventId));
		model.addAttribute("event", eventOptional.get());
		model.addAttribute("dates", eventManagement.getCalendarData(eventOptional.get()));
		model.addAttribute("today", TimeManager.getTime());
		List<House> houses = bookingManagement.getBookedHousesForEvent(eventId).toList();
		List<Map<String, Object>> simplifiedHouses = houses.stream().map(house -> {
			Map<String, Object> houseData = new HashMap<>();
			houseData.put("id", Objects.requireNonNull(house.getId()).toString());
			houseData.put("name", house.getName());
			houseData.put("description", house.getDescription());
			houseData.put("location", house.getLocation());
			houseData.put("maxPerson", house.getMaxPerson());
			return houseData;
		}).toList();
		model.addAttribute("simplifiedHouses", simplifiedHouses);

		model.addAttribute("comments", commentManagement.findByProductId(eventId));
		model.addAttribute("commentForm", new CommentForm());
		model.addAttribute("eventId", eventId);
		model.addAttribute("customerAllowedToComment",
			bookingManagement.customerAllowedToComment(account, eventId, CommentType.EVENTCOMMENT));

		if(account != null){
			model.addAttribute("userIsOwner", eventOptional.get().getEventStaffId().equals(account.getId()));
		}

		return "event/event";
	}

	/**
	 * Shows the site to collect Data for {@link EventForm} to edit a {@link Event} of type BIG_EVENT.
	 *
	 * @param model will never be {@literal null}.
	 * @param eventId ID of the {@link Event}.
	 * @return the view name
	 */
	@GetMapping("/editBigEvent/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	public String showEditBigEvent(Model model, @PathVariable("id") Product.ProductIdentifier eventId) {
		Optional<Event> optionalEvent = eventManagement.findById(eventId);

		if (optionalEvent.isEmpty() || !(optionalEvent.get().getType().equals(EventType.BIG_EVENT))) {
			return "redirect:/myEvents";
		}

		Event bigEvent = optionalEvent.get();
		model.addAttribute("bigEvent", bigEvent);
		model.addAttribute("dateError", null);

		double ticketprice = bigEvent.getPrice() != null ? bigEvent.getPrice().getNumber().doubleValue() : 0.0;

		EventForm editForm = new EventForm(
			bigEvent.getName(),
			bigEvent.getDescription(),
			bigEvent.getLocation().getAddressString(),
			bigEvent.getInterval().getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
			bigEvent.getInterval().getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
			bigEvent.getTime(),
			ticketprice, bigEvent.getTickets(),
			bigEvent.getEventStaffId());

		editForm.setEventId(eventId);
		model.addAttribute("bigEventForm", editForm);
		model.addAttribute("houseList", eventManagement.getHouses());
		model.addAttribute("images", bigEvent.getImagePaths());

		return "event/editBigEvent";
	}

	/**
	 * Shows the site to collect Data for {@link EventForm} to edit a {@link Event} of type SMALL_EVENT.
	 *
	 * @param model will never be {@literal null}.
	 * @param eventId ID of the {@link Event}.
	 * @return the view name
	 */
	@GetMapping("/editSmallEvent/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	public String showEditSmallEvent(Model model, @PathVariable("id") Product.ProductIdentifier eventId) {
		Optional<Event> optionalEvent = eventManagement.findById(eventId);

		if (optionalEvent.isEmpty() || !(optionalEvent.get().getType().equals(EventType.SMALL_EVENT))) {
			return "redirect:/myEvents";
		}

		Event smallEvent = optionalEvent.get();
		model.addAttribute("smallEvent", smallEvent);

		EventForm editForm = new EventForm(
			smallEvent.getName(),
			smallEvent.getDescription(),
			smallEvent.getLocation().getAddressString(),
			smallEvent.getInterval().getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
			smallEvent.getInterval().getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
			smallEvent.getTime(),
			smallEvent.getEventStaffId(),
			smallEvent.getRecurrencePattern(),
			smallEvent.getEndOfRecurrence().toString());

		editForm.setEventId(eventId);
		model.addAttribute("smallEventForm", editForm);
		model.addAttribute("dateError", null);
		model.addAttribute("recurrencePatternEnum", RecurrencePattern.values());
		model.addAttribute("images", smallEvent.getImagePaths());

		return "event/editSmallEvent";
	}


	/**
	 * Edits a {@link Event} of type BIG_EVENT.
	 *
	 * @param bigEventForm the {@link EventForm} with the collected Data to create a {@link Event} of type BIG_EVENT.
	 * @param result the BindingResult to catch errors.
	 * @param eventId ID of the {@link Event} of type BIG_EVENT.
	 * @param model will never be {@literal null}.
	 * @param files multiple files of type MultipartFile to add Images to the {@link Event} of type BIG_EVENT.
	 * @param tickets int of how many tickets are to be added to the {@link Event} of type BIG_EVENT.
	 * @return the view name
	 */
	@PostMapping("/editBigEvent/{eventId}")
	@PreAuthorize("hasRole('EventStaff')")
	public String editBigEvent(@Valid @ModelAttribute("bigEventForm") EventForm bigEventForm, BindingResult result,
							   @PathVariable Product.ProductIdentifier eventId, Model model, @RequestParam("files") MultipartFile[] files,
							   @RequestParam int tickets) {


		Optional <Event> e = eventManagement.findById(eventId);
		if(e.isEmpty()){
			return "event/editBigEvent";
		}
		Event event = e.get();

		if (!eventManagement.checkInvalidInterval(LocalDate.parse(
			bigEventForm.getStartDate()).atTime(LocalTime.parse("00:01")),
			LocalDate.parse(bigEventForm.getEndDate()).atTime(LocalTime.parse("23:59")))) {
			model.addAttribute("dateError", "Der Zeitraum muss gültig sein und in der Zukunft liegen!");
			model.addAttribute("bigEventForm", bigEventForm);
			model.addAttribute("bigEvent", eventManagement.findById(eventId).get());
			model.addAttribute("images", eventManagement.findById(eventId).get().getImagePaths());
			return "event/editBigEvent";
		}
		if (files.length !=0) {
			eventManagement.addImages(eventId, files);
		}

		bigEventForm.setTickets(tickets);
		Interval newInterval = Interval.from(LocalDate.parse(bigEventForm.getStartDate())
			.atTime(LocalTime.parse("00:01")))
			.to(LocalDate.parse(bigEventForm.getEndDate())
			.atTime(LocalTime.parse("23:59")));

		if (!event.getInterval().equals(newInterval)) {
			/* cancel all bookings for houses when the interval changes */
			bookingManagement.cancelEventBookings(eventId, newInterval);
		}

		boolean updated = eventManagement.editBigEvent(eventId, bigEventForm);
		if (!updated) {
			return "redirect:/editBigEvent/" + bigEventForm.getEventId();
		}
		return "redirect:/myEvents";

	}

	/**
	 * Edits a {@link Event} of type SMALL_EVENT.
	 *
	 * @param model will never be {@literal null}.
	 * @param smallEventForm the {@link EventForm} with the collected Data to create a {@link Event} of type SMALL_EVENT.
	 * @param result the BindingResult to catch errors.
	 * @param eventId ID of the {@link Event} of type SMALL_EVENT.
	 * @param files multiple files of type MultipartFile to add Images to the {@link Event} of type SMALL_EVENT.
	 * @return the view name
	 */
	@PostMapping("/editSmallEvent/{eventId}")
	@PreAuthorize("hasRole('EventStaff')")
	public String editSmallEvent(Model model, @Valid @ModelAttribute("smallEventForm") EventForm smallEventForm,
								 BindingResult result, @PathVariable Product.ProductIdentifier eventId,
								 @RequestParam("files") MultipartFile[] files, RedirectAttributes attributes) {

		Optional <Event> e = eventManagement.findById(eventId);
		if(e.isEmpty()){
			return "event/editSmallEvent";
		}
		Event event = e.get();


		if (!eventManagement.checkInvalidInterval(LocalDate.parse(smallEventForm.getStartDate())
				.atTime(LocalTime.parse("00:01")),
			LocalDate.parse(smallEventForm.getEndDate()).atTime(LocalTime.parse("23:59")))) {
			model.addAttribute("dateError", "Der Zeitraum muss gültig sein und in der Zukunft liegen!");
			model.addAttribute("recurrencePatternEnum", RecurrencePattern.values());
			model.addAttribute("smallEventForm", smallEventForm);
			model.addAttribute("smallEvent", eventManagement.findById(eventId).get());
			model.addAttribute("images", eventManagement.findById(eventId).get().getImagePaths());
			return "event/editSmallEvent";
		}

		if(!eventManagement.checkValidEndOfRecurrence(smallEventForm.getStartDate(),
			smallEventForm.getEndOfRecurrence(), smallEventForm.getRecurrencePattern())){
			String howMuchOfWhat = switch (smallEventForm.getRecurrencePattern()) {
				case NONE -> "Wenn das angezeigt wird läuft irgendwas gewaltig falsch";
				case WEEKLY -> "eine Woche";
				case MONTHLY -> "einen Monat";
				case YEARLY -> "ein Jahr";
			};

			model.addAttribute("recurrenceError",
				"Das Datum muss gültig sein und mindestens " + howMuchOfWhat + " in der Zukunft liegen!");
			model.addAttribute("recurrencePatternEnum", RecurrencePattern.values());
			model.addAttribute("smallEventForm", smallEventForm);
			model.addAttribute("smallEvent", eventManagement.findById(eventId).get());
			model.addAttribute("images", eventManagement.findById(eventId).get().getImagePaths());

			return "event/editSmallEvent";
		}

		event = eventManagement.findById(eventId).get();
		Interval newInterval = Interval.from(LocalDate.parse(smallEventForm.getStartDate())
			.atTime(LocalTime.parse("00:01")))
			.to(LocalDate.parse(smallEventForm.getEndDate())
			.atTime(LocalTime.parse("23:59")));

		if (!event.getInterval().equals(newInterval)) {
			/* cancel all bookings for houses when the interval changes */
			bookingManagement.cancelEventBookings(eventId, newInterval);
			return "redirect:/myEvents/chooseAdvertismentRadius/" + event.getId();

		}
		eventManagement.editSmallEvent(eventId, smallEventForm);
		if (files.length!=0){
			eventManagement.addImages(eventId, files);
		}
		boolean updated = eventManagement.editSmallEvent(eventId, smallEventForm);
		if (!updated) {
			return "redirect:/editSmallEvent/" + eventId;
		}


		model.addAttribute("currentPath", "/myEvents");
		return "redirect:/myEvents";

	}

	@GetMapping("/cancelEditEvent/{id}")
	@PreAuthorize("hasRole('EventStaff')")
	public String cancelEditEvent(@PathVariable Product.ProductIdentifier id){
		eventManagement.addDefaultImageIfNone(id);
		return "redirect:/myEvents";
	}
}
