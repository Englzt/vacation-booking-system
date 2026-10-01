package vacationrental.booking;

import org.javamoney.moneta.Money;
import org.jetbrains.annotations.NotNull;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderCompletionFailure;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import vacationrental.messages.BookingNoticeEvent;
import vacationrental.messages.BookingRefundEvent;
import vacationrental.messages.BookingStatusChangedEvent;
import vacationrental.comments.CommentManagement;
import vacationrental.comments.CommentType;
import vacationrental.eventcatalog.Event;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.eventcatalog.EventType;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.location.Location;
import vacationrental.util.TimeManager;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.salespointframework.core.Currencies.EURO;

@Service
public class BookingManagement implements OrderManagement<Booking> {
	private final BookingRepository bookings;
	private final HouseManagement houseManagement;
	private final UserAccountManagement userAccountManagement;
	private final EventManagement eventManagement;
	private final ApplicationEventPublisher eventPublisher;
	private final CommentManagement commentManagement;

	@Value("${vacationrental.cancellation-deadline}")
	private int cancellationDeadline;

	BookingManagement(ApplicationEventPublisher eventPublisher,
					  BookingRepository bookings,
					  HouseManagement houseManagement,
					  UserAccountManagement userAccountManagement,
					  EventManagement eventManagement,
					  CommentManagement commentManagement) {
		this.houseManagement = houseManagement;
		this.bookings = bookings;
		this.userAccountManagement = userAccountManagement;
		this.eventManagement = eventManagement;
		this.eventPublisher = eventPublisher;
		this.commentManagement = commentManagement;
	}

	public void addBooking(Booking booking) {
		bookings.save(booking);
	}

	public Streamable<Booking> findAll() {
		return bookings.findAll();
	}

	public Streamable<Booking> findByBookingStatus(BookingStatus status) {
		return bookings.findByStatus(status);
	}

	public Booking findById(Order.OrderIdentifier id) {
		return bookings.findByOrderIdentifier(id).orElse(null);
	}

	/**
	 * Creates a rental information object for a house and interval.
	 * @param house {@link House}
	 * @param interval {@link Interval}
	 * @return {@link RentalInformation}
	 */
	public RentalInformation createRentalInformation(House house, Interval interval) {
		return new RentalInformation(house, interval);
	}


	/**
	 * Creates a personal booking for a house. Returns true if the booking was successful.
	 *
	 * @param house_id       Product.ProductIdentifier
	 * @param interval_start String
	 * @param interval_end   String
	 * @param user_id        UserAccount.UserAccountIdentifier
	 * @return boolean
	 */
	public Booking createPersonalBooking(Product.ProductIdentifier house_id,
										 String interval_start, String interval_end,
										 UserAccount.UserAccountIdentifier user_id) {

		House house = houseManagement.findById(house_id).get();

		LocalDateTime start_date = LocalDate.parse(interval_start).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(interval_end).atTime(LocalTime.parse("23:59"));

		if (!checkInvalidInterval(start_date, end_date)) {
			return null;
		}

		Interval interval = Interval.from(start_date).to(end_date);

		if (isHouseAvailable(house_id, interval, null, false) == null) {
			return null;
		}
		Booking booking = bookings.save(new Booking(user_id, new RentalInformation(house, interval)));

		eventPublisher.publishEvent(new BookingStatusChangedEvent(booking,
			booking.getRentalInformation().getHouse().getLandlordId()));

		return booking;
	}

	/**
	 * Creates an event booking for a house. Returns true if the booking was successful.
	 *
	 * @param house_id       Product.ProductIdentifier
	 * @param interval_start String
	 * @param interval_end   String
	 * @param user_id        UserAccount.UserAccountIdentifier
	 * @param event_id       Product.ProductIdentifier
	 * @return boolean
	 */
	public boolean createEventBooking(Product.ProductIdentifier house_id,
									  String interval_start, String interval_end,
									  UserAccount.UserAccountIdentifier user_id,
									  Product.ProductIdentifier event_id) {

		House house = houseManagement.findById(house_id).get();

		LocalDateTime start_date = LocalDate.parse(interval_start).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(interval_end).atTime(LocalTime.parse("23:59"));

		if (start_date.isAfter(end_date) || start_date.isBefore(TimeManager.getTime())) {
			return false;
		}

		Interval interval = Interval.from(start_date).to(end_date);

		if (isHouseAvailable(house_id, interval, null, false) == null) {
			return false;
		}

		bookings.save(new Booking(user_id, event_id, new RentalInformation(house, interval)));

		return true;
	}

	/**
	 * creates a Ticket Booking for the given Event in bound with a personal booking
	 *
	 * @param account        {@link UserAccount.UserAccountIdentifier}
	 * @param eventId        {@link Product.ProductIdentifier}
	 * @param tickets        {@link Integer}
	 * @param house_id       {@link Product.ProductIdentifier}
	 * @param interval_start {@link String}
	 * @param interval_end   {@link String}
	 */
	public void createTicketBooking(UserAccount.UserAccountIdentifier account,
									Product.ProductIdentifier eventId,
									int tickets,
									Product.ProductIdentifier house_id,
									String interval_start, String interval_end) {
		if (account == null || eventId == null || tickets <= 0) {
			return;
		}

		House house = houseManagement.findById(house_id).get();

		LocalDateTime start_date = LocalDate.parse(interval_start).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(interval_end).atTime(LocalTime.parse("23:59"));

		if (!checkInvalidInterval(start_date, end_date)) {
			return;
		}

		Interval interval = Interval.from(start_date).to(end_date);

		Event e = eventManagement.findById(eventId).get();
		bookings.save(new Booking(account, e, tickets, new RentalInformation(house, interval)));
	}

	public void createAdvertisingBooking(UserAccount.UserAccountIdentifier eventStaffId,
										 Product.ProductIdentifier eventId, Interval eventInterval, House house) {
		bookings.save(new Booking(eventStaffId, eventId, new RentalInformation(house, eventInterval), EventType.SMALL_EVENT));
	}


	/**
	 * Checks if the interval is valid. Returns true if the interval is valid.
	 *
	 * @param start_date {@link LocalDateTime}
	 * @param end_date  {@link LocalDateTime}
	 * @return {@link Boolean}
	 */
	public boolean checkInvalidInterval(LocalDateTime start_date, LocalDateTime end_date) {
		/* conversion to LocalDate because LocalDateTime start is for 00:01, end for 23:59
			-> would allow booking to start and end on same day */
		LocalDate start = start_date.toLocalDate();
		LocalDate end = end_date.toLocalDate();
		return !start.isAfter(end) && !start_date.isBefore(TimeManager.getTime()) && !start.equals(end);
	}


	/**
	 * Checks if a house is available for a given interval and rental type. Returns true if the house is available.
	 *
	 * @param house_id    Product.ProductIdentifier
	 * @param interval    Interval
	 * @param booking_id  Order.OrderIdentifier - used to check if the booking is the same as the one being checked
	 * @param is_approval boolean - used for confirming bookings only
	 * @return RentalType where:
	 * <ul>
	 *     <li><code>TICKET</code>: House is available for personal booking in combination with an event ticket</li>
	 *     <li><code>VACANT</code>: House is available for either booking type</li>
	 *     <li><code>null</code>: House is not available for booking</li>
	 * </ul>
	 */

	public RentalType isHouseAvailable(Product.ProductIdentifier house_id, Interval interval,
									   Order.OrderIdentifier booking_id, boolean is_approval) {
		Streamable<Booking> list = findByHouseId(house_id);

		boolean personalAvailable = true;
		boolean newEventAvailable = true;
		boolean eventTicketAvailable = false;

		for (Booking booking : list) {
			if (booking.getRentalInformation().getInterval().overlaps(interval)
				&& booking.getStatus() != BookingStatus.CANCELED) {
				if (booking.getId().equals(booking_id)) {
					continue;
				}

				if (booking.getRentalType() == RentalType.PERSONAL) {
					if (is_approval && booking.getStatus() == BookingStatus.OPEN) {
						continue;
					}
					personalAvailable = false;
					newEventAvailable = false;
				} else if (booking.getRentalType() == RentalType.EVENT && booking.getStatus() != BookingStatus.OPEN) {
					newEventAvailable = false;
					eventTicketAvailable = true;
				}
			}
		}

		if (personalAvailable && newEventAvailable) {
			return RentalType.VACANT;
		}


		boolean notEventRequest = false;
		if (booking_id != null) {
			/* check for null because this function is sometimes called with null */
			notEventRequest = bookings.findById(booking_id).get().getRentalType() != RentalType.EVENT;
		}

		if (eventTicketAvailable && personalAvailable && (notEventRequest || !is_approval)) {
			return RentalType.TICKET;
		}

		return null;
	}

	/**
	 * returning all Events which booked the specific house in the given Interval (relevant for booking a house as User)
	 *
	 * @param houseId  Product.ProductIdentifier
	 * @param start_date LocalDateTime
	 * @param end_date  LocalDateTime
	 * @return List&lt;Event&gt;
	 */
	public List<Event> getAllEventsForHouseDuringInterval(Product.ProductIdentifier houseId,
														  LocalDateTime start_date, LocalDateTime end_date) {

		Interval interval = Interval.from(start_date).to(end_date);
		Streamable<Booking> bookings = findByHouseIdAndRentalType(houseId, RentalType.EVENT);
		List<Event> overlappingEvents = new ArrayList<>();
		for (Booking booking : bookings) {
			if (booking.getStatus() == BookingStatus.CANCELED || booking.getStatus() == BookingStatus.OPEN) {
				continue;
			}
			Optional<Event> event = eventManagement.findById(booking.getEvent());
			if (event.isEmpty()) {
				continue;
			}
			Interval eventInterval = event.get().getInterval();
			if ((eventInterval.overlaps(interval)
				|| eventInterval.getStart().equals(interval.getStart())
				|| eventInterval.getEnd().equals(interval.getEnd()))
				&& booking.getStatus() != BookingStatus.CANCELED) {
				overlappingEvents.add(event.get());
			}
		}
		return overlappingEvents;
	}

	@NotNull
	public Booking save(@NotNull Booking booking) {
		return bookings.save(booking);
	}

	@NotNull
	@Override
	public Optional<Booking> get(@NotNull Order.OrderIdentifier orderIdentifier) {
		return bookings.findByOrderIdentifier(orderIdentifier);
	}

	public boolean contains(@NotNull Order.OrderIdentifier orderIdentifier) {
		return bookings.findByOrderIdentifier(orderIdentifier).isPresent();
	}

	@NotNull
	@Override
	public Streamable<Booking> findBy(OrderStatus orderStatus) {
		return null;
	}

	@NotNull
	@Override
	public Streamable<Booking> findBy(Interval interval) {
		return null;
	}

	@NotNull
	@Override
	public Streamable<Booking> findBy(UserAccount userAccount) {
		return null;
	}

	@NotNull
	@Override
	public Streamable<Booking> findBy(UserAccount userAccount, Interval interval) {
		return null;
	}

	@Override
	public void completeOrder(Booking order) throws OrderCompletionFailure {
		return;
	}

	@Override
	public boolean payOrder(@NotNull Booking order) {
		return false;
	}

	@Override
	public boolean cancelOrder(@NotNull Booking order, @NotNull String reason) {
		return false;
	}

	@Override
	public Booking delete(@NotNull Booking order) {
		return null;
	}

	@NotNull
	@Override
	public Page<Booking> findAll(@NotNull Pageable pageable) {
		return null;
	}

	/**
	 * Landlord sets the BookingStatus to reserved, message to Customer.
	 *
	 * @param booking changed Booking
	 */
	public void setBookingStatusReserved(Booking booking) {
		booking.setReserved();
		bookings.save(booking);
		eventPublisher.publishEvent(new BookingStatusChangedEvent(booking, booking.getUserAccountIdentifier()));
	}

	/**
	 * Landlord or Customer cancel Booking. Message to Landlord and Customer.
	 *
	 * @param booking changed Booking
	 */
	public boolean setBookingStatusCanceled(Booking booking) {

		/* canceled and completed bookings can't be canceled */
		if (booking.isCanceled() || booking.isCompleted()) {
			return false;
		}

		/* started bookings can't be canceled */
		LocalDateTime start_date = booking.getRentalInformation().getInterval().getStart();
		if (start_date.equals(TimeManager.getTime()) || start_date.isBefore(TimeManager.getTime())) {
			return false;
		}

		booking.setCanceled();
		bookings.save(booking);

		/* check if cancellation occurred before the deadline -> refund */
		LocalDateTime deadline = booking.getRentalInformation().getInterval().getStart().minusDays(cancellationDeadline);

		if (TimeManager.getTime().isBefore(deadline)
			&& booking.getRentalType() != RentalType.EVENT
			&& booking.getRentalType() != RentalType.ADVERTISING) {
			/* send refund message, but only if not event or ad */
			eventPublisher.publishEvent(new BookingRefundEvent(booking.getUserAccountIdentifier(), booking));
		}

		eventPublisher.publishEvent(new BookingStatusChangedEvent(booking, booking.getUserAccountIdentifier()));


		if (booking.getRentalType() == RentalType.EVENT) {

			List<Booking> tickets = bookings.findBy_HouseId_EventId_NotStatus_NotStatus1(
				booking.getRentalInformation().getHouse().getId(),
				booking.getEvent(),
				BookingStatus.CANCELED,
				BookingStatus.OPEN);

			for (Booking ticket : tickets) {
				setBookingStatusCanceled(ticket);
			}

			Streamable<Booking> ticketBookings = bookings.findBy_UserId_LandlordId_Interval_Type_NotStatus(
				booking.getUserAccountIdentifier(),
				booking.getRentalInformation().getHouse().getLandlordId(),
				booking.getRentalInformation().getInterval(),
				RentalType.TICKET,
				BookingStatus.CANCELED);

			for(Booking b: ticketBookings) {
				setBookingStatusCanceled(b);
			}

			if (booking.getRentalInformation().getHouse() != null) {
				// house is null after deletion
				eventPublisher.publishEvent(new BookingStatusChangedEvent(booking,
					booking.getRentalInformation().getHouse().getLandlordId()));
			}
		} else if (booking.getRentalType() == RentalType.TICKET) {
			/* if type is ticket -> cancel all personal bookings as well */
			Event e = eventManagement.findById(booking.getEvent()).get();

			if (e.isCancelled()) {
				return false;
			}

			e.increaseTickets(booking.getTickets());
			eventPublisher.publishEvent(new BookingStatusChangedEvent(booking, e.getEventStaffId()));

			Streamable<Booking> houseBookings = bookings.findBy_UserId_LandlordId_Interval_Type_NotStatus(
				booking.getUserAccountIdentifier(),
				booking.getRentalInformation().getHouse().getLandlordId(),
				booking.getRentalInformation().getInterval(),
				RentalType.PERSONAL,
				BookingStatus.CANCELED);

			/* cancel house booking as well */
			for(Booking b: houseBookings) {
				setBookingStatusCanceled(b);
			}

			return true;


		} else if (booking.getRentalType() == RentalType.PERSONAL) {
			/* if type is Personal -> cancel all ticket bookings as well */
			Streamable<Booking> ticketBookings = bookings.findBy_UserId_LandlordId_Interval_Type_NotStatus(
				booking.getUserAccountIdentifier(),
				booking.getRentalInformation().getHouse().getLandlordId(),
				booking.getRentalInformation().getInterval(),
				RentalType.TICKET,
				BookingStatus.CANCELED);

			for(Booking b: ticketBookings) {
				setBookingStatusCanceled(b);
			}

			return true;
		}
		return true;
	}

	/**
	 * When the booking interval is over, the booking is set to completed.
	 *
	 * @param booking changed Booking
	 */
	public void setBookingStatusCompleted(Booking booking) {
		if (booking.isCanceled() || booking.isCompleted()) {
			return;
		}
		booking.setCompleted();
		bookings.save(booking);
		eventPublisher.publishEvent(new BookingStatusChangedEvent(booking, booking.getUserAccountIdentifier()));
		if (booking.getRentalInformation().getHouse() != null) {
			eventPublisher.publishEvent(new BookingStatusChangedEvent(booking,
				booking.getRentalInformation().getHouse().getLandlordId()));
		}
	}

	/**
	 * sends a late notice to the customer.
	 *
	 * @param booking {@link Booking}
	 */
	public void sendLateNotice(Booking booking) {
		eventPublisher.publishEvent(new BookingNoticeEvent(booking.getUserAccountIdentifier(), booking));
	}

	/**
	 * Get a streamable of all bookings with the given status and customer id.
	 *
	 * @param user_id UserAccount.UserAccountIdentifier
	 * @param status  BookingStatus
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> findByCustomerAndStatus(UserAccount.UserAccountIdentifier user_id, BookingStatus status) {
		return bookings.findByUserAccountIdentifierAndStatus(user_id, status);
	}

	/**
	 * Landlord confirms the payment. Message send to Customer.
	 *
	 * @param booking changed Booking
	 */
	public void setBookingStatusPaid(Booking booking) {
		booking.setPaid();
		bookings.save(booking);
		eventPublisher.publishEvent(new BookingStatusChangedEvent(booking, booking.getUserAccountIdentifier()));
	}

	/**
	 * find all bookings by the given user_id
	 * @param user_id UserAccount.UserAccountIdentifier
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> findByAccountIdentifier(UserAccount.UserAccountIdentifier user_id) {
		return bookings.findByUserAccountIdentifier(user_id);
	}

	/**
	 * find all bookings by the given house_id
	 * @param id Product.ProductIdentifier
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> findByHouseId(Product.ProductIdentifier id) {
		return bookings.findByRentalInformation_House_Id(id);
	}

	/**
	 * find all bookings by the given house_id and rental type
	 * @param id Product.ProductIdentifier
	 * @param rentalType RentalType
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> findByHouseIdAndRentalType(Product.ProductIdentifier id, RentalType rentalType) {
		return bookings.findByRentalInformation_House_IdAndRentalType(id, rentalType);
	}

	/**
	 * find all bookings by the given landlord id and given status
	 * @param landlord_id Product.ProductIdentifier
	 * @param status BookingStatus
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> findByLandlordIdAndStatus(UserAccount.UserAccountIdentifier landlord_id,
														 BookingStatus status) {
		return bookings.findByRentalInformation_House_LandlordIdAndStatus(landlord_id, status);
	}

	/**
	 * Returns a map of all bookings with the name of the customer.
	 * Used to display the bookings in the landlords rental overview.
	 *
	 * @param landlord_id UserAccount.UserAccountIdentifier
	 * @param status      BookingStatus
	 * @return Map&lt;Booking, String details&gt;
	 * <br>details is the name of the customer or the name of the event
	 */
	public Map<Booking, String> getBookingsWithCustomerByLandlordAndStatus(UserAccount.UserAccountIdentifier landlord_id,
																		   BookingStatus status) {

		Streamable<Booking> bookings = findByLandlordIdAndStatus(landlord_id, status);
		Map<Booking, String> bookingAndCustomer = new HashMap<>();
		for (Booking booking : bookings) {
			if (booking.getRentalType().equals(RentalType.PERSONAL)) {
				String username = userAccountManagement.get(booking.getUserAccountIdentifier()).get().getUsername();
				bookingAndCustomer.put(booking, username);
			} else if (!booking.getRentalType().equals(RentalType.TICKET)) {
				String event_name = eventManagement.findById(booking.getEvent()).get().getName();
				bookingAndCustomer.put(booking, event_name);
			}
		}
		return bookingAndCustomer;
	}

	/**
	 * Returns a map of all bookings with the name of the landlord.
	 * Used to display the bookings in the customers rental overview.
	 *
	 * @param status     BookingStatus
	 * @param account_id UserAccount.UserAccountIdentifier
	 * @return Map&lt;Booking, String details&gt;
	 * <br>details is the name of the landlord or the name of the event
	 */
	public Map<Booking, String> getBookingWithNamesByStatusAndCustomer(BookingStatus status,
																	   UserAccount.UserAccountIdentifier account_id) {
		Streamable<Booking> bookings = findByCustomerAndStatus(account_id, status);
		return getBookingStringMap(bookings);
	}

	/**
	 * Returns a map of all bookings with the name of the landlord.
	 * @param bookings Streamable<Booking>
	 * @return Map&lt;Booking, String details&gt;
	 */
	@NotNull
	private Map<Booking, String> getBookingStringMap(Streamable<Booking> bookings) {
		Map<Booking, String> bookingAndNames = new HashMap<>();
		for (Booking booking : bookings) {
			if (booking.getRentalType().equals(RentalType.PERSONAL)) {
				String landlord_name = "Deleted";
				if (booking.getRentalInformation().getHouse() != null) {
					UserAccount.UserAccountIdentifier landlordId = booking.getRentalInformation().getHouse().getLandlordId();
					landlord_name = userAccountManagement.get(landlordId).get().getUsername();
				}
				bookingAndNames.put(booking, landlord_name);
			} else {
				String event_name = eventManagement.findById(booking.getEvent()).get().getName();
				bookingAndNames.put(booking, event_name);
			}
		}

		return bookingAndNames;
	}

	/**
	 * Returns a map of all bookings with the name of the landlord.
	 * Used to display the bookings in the customers rental overview.
	 *
	 * @param account_id UserAccount.UserAccountIdentifier
	 * @return Map&lt;Booking, String details&gt;
	 * <br>details is the name of the landlord or the name of the event
	 */
	public Map<Booking, String> getAllBookingsWithNameByCustomer(UserAccount.UserAccountIdentifier account_id) {
		Streamable<Booking> bookings = findByAccountIdentifier(account_id);
		return getBookingStringMap(bookings);
	}

	/**
	 * Returns a map of all ticket Bookings mapped to their events, booked by the given customer. Used in customerrentals
	 *
	 * @param account_id UserAccount.UserAccountIdentifier
	 * @return Map&lt;Booking, Event&gt;
	 */
	public Map<Booking, Event> getTicketBookingsWithEvent(UserAccount.UserAccountIdentifier account_id) {
		Streamable<Booking> ticketBookings = bookings.findByUserAccountIdentifierAndRentalType(account_id, RentalType.TICKET);
		Map<Booking, Event> bookingAndEvents = new HashMap<>();
		for (Booking booking : ticketBookings) {
			Event e = eventManagement.findById(booking.getEvent()).get();
			bookingAndEvents.put(booking, e);
		}
		return bookingAndEvents;
	}


	/**
	 * Returns a list of all houses which are nearby to a location and have not been booked for an event.
	 *
	 * @param location Location
	 * @param eventId  Product.ProductIdentifier
	 * @return List&lt;House&gt;
	 */
	public List<House> getNearbyFreeHousesToLocation(Location location, Product.ProductIdentifier eventId, int radius) {
		Streamable<House> houses = houseManagement.findAll();
		Interval eventInterval = eventManagement.findById(eventId).get().getInterval();
		List<House> nearbyHouses = new ArrayList<>();
		for (House house : houses) {
			if (radius >= location.distanceTo(house.getLocation())) {
				List<Booking> list = bookings.findByEventIdAndRentalInformation_House_Id(eventId, house.getId());

				// check list for reserved bookings only
				boolean contains_reserved = list.stream().anyMatch(booking -> booking.getStatus() == BookingStatus.RESERVED);

				// check if this event has an existing booking for this house which is not canceled
				boolean booking_exists = list.stream().anyMatch(booking -> booking.getEvent().equals(eventId)
					&& booking.getStatus() != BookingStatus.CANCELED);

				if (contains_reserved || booking_exists || isHouseAvailable(house.getId(), eventInterval, null, false) == null) {
					continue;
				}
				nearbyHouses.add(house);
			}
		}
		return nearbyHouses;
	}


	/**
	 * Books houses for an event. Creates a booking for each house and event.
	 *
	 * @param houseIds   List&lt;Product.ProductIdentifier&gt;
	 * @param event      Event
	 * @param account_id UserAccount.UserAccountIdentifier
	 * @return boolean
	 */
	public boolean bookHousesForEvent(List<Product.ProductIdentifier> houseIds, Event event,
									  UserAccount.UserAccountIdentifier account_id) {
		String intervalStart = event.getInterval().getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String intervalEnd = event.getInterval().getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Product.ProductIdentifier eventId = event.getId();
		for (Product.ProductIdentifier houseId : houseIds) {
			createEventBooking(houseId, intervalStart, intervalEnd, account_id, eventId);
		}
		return true;

	}

	/**
	 * Returns map of all dates where bookings have been made for a single house. Used for calendar on houseDetails page.
	 *
	 * @param id Product.ProductIdentifier
	 * @return Map&lt;String date, String booking info&gt;
	 * <br>booking info is either "Personal" or the name of the event
	 */
	public Map<String, String> getCalendarData(Product.ProductIdentifier id) {

		TreeMap<String, String> combinedDates = new TreeMap<>();

		/* Get list of all events */
		Streamable<Booking> eventBookings = findByHouseIdAndRentalType(id, RentalType.EVENT);
		for (Booking booking : eventBookings) {
			if (booking.getStatus() == BookingStatus.CANCELED || booking.getStatus() == BookingStatus.OPEN) {
				continue;
			}
			Event event = eventManagement.findById(booking.getEvent()).get();
			LocalDateTime start = event.getInterval().getStart();
			LocalDateTime end = event.getInterval().getEnd();

			LocalDateTime current = start;

			while (current.isBefore(end) || current.isEqual(end)) {
				String date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				String info = event.getName();
				if (event.getTickets() > 0) {
					info += "|" + event.getTickets() + " Tickets (" + event.getTicketprice() + "€)";
				}
				combinedDates.put(date, info);
				current = current.plusDays(1);
			}
		}

		/* Get list of all personal bookings */
		Streamable<Booking> personalBookings = findByHouseIdAndRentalType(id, RentalType.PERSONAL);
		for (Booking booking : personalBookings) {
			if (booking.getStatus() == BookingStatus.CANCELED) {
				continue;
			}
			LocalDateTime start = booking.getRentalInformation().getInterval().getStart();
			LocalDateTime end = booking.getRentalInformation().getInterval().getEnd();
			LocalDateTime current = start;

			while (current.isBefore(end) || current.isEqual(end)) {
				String date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				combinedDates.put(date, "Personal");
				current = current.plusDays(1);
			}
		}

		return combinedDates;
	}


	/**
	 * Cancels all bookings for an event and creates new bookings if a new interval is provided
	 *
	 * @param eventId     Product.ProductIdentifier
	 * @param newInterval Interval
	 */
	public void cancelEventBookings(Product.ProductIdentifier eventId, Interval newInterval) {
		Streamable<Booking> eventBookings = bookings.findByEventId(eventId);
		for (Booking booking : eventBookings) {
			setBookingStatusCanceled(booking);


			if (newInterval != null && booking.getRentalType() != RentalType.ADVERTISING) {
				/* if newInterval was passed a new event booking is created because the
				 * timeframe of the event has been changed and the old booking is canceled
				 */
				createEventBooking(booking.getRentalInformation().getHouse().getId(),
					newInterval.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
					newInterval.getEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
					booking.getUserAccountIdentifier(), eventId);
			}

		}
	}


	/**
	 * Returns a list of all houses which have been booked for an event.
	 * Used to display the houses on the eventDetails page.
	 *
	 * @param eventId Product.ProductIdentifier
	 * @return Streamable&lt;House&gt;
	 */
	public Streamable<House> getBookedHousesForEvent(Product.ProductIdentifier eventId) {
		/* Returns a list of all houses which have been booked for an event
		 * used to display the houses on the eventDetails page
		 */
		List<Booking> eventBookings = bookings.findBy_EventId_Type_NotStatus_NotStatus1_NotStatus2(eventId,
			RentalType.EVENT, BookingStatus.COMPLETED, BookingStatus.CANCELED, BookingStatus.OPEN);
		List<House> houses = new ArrayList<>();
		eventBookings.forEach(booking -> houses.add(booking.getRentalInformation().getHouse()));
		return Streamable.of(houses);
	}

	/**
	 * Deletes all bookings for a house and sets the house to null in the booking. Used when a landlord is deleted.
	 *
	 * @param id Product.ProductIdentifier
	 */
	public void deleteBookingsByHouseId(Product.ProductIdentifier id) {
		Streamable<Booking> list = findByHouseId(id);
		for (Booking booking : list) {
			setBookingStatusCanceled(booking);
			booking.getRentalInformation().setHouse(null);
		}
	}

	/**
	 * Cancels all bookings for a customer. Used when a customer is deleted.
	 *
	 * @param id UserAccount.UserAccountIdentifier
	 */
	public void cancelAllByCustomerId(UserAccount.UserAccountIdentifier id) {
		Streamable<Booking> list = findByAccountIdentifier(id);
		for (Booking booking : list) {
			booking.setCanceled();
			bookings.save(booking);
		}
	}

	/**
	 * Calculates the price of a booking based on the duration and the price of the house.
	 *
	 * @param booking {@link Booking}
	 * @return Money {@link Money}
	 */
	public Money calculatePrice(Booking booking) {
		int duration = (int) booking.getRentalInformation().getInterval().getDuration().toDays();
		return (Money) booking.getRentalInformation().getHouse().getPrice().multiply(duration);
	}

	/**
	 * Calculates house price for a given Interval
	 *
	 * @param houseId 	 Product.ProductIdentifier
	 * @param intervalStart String
	 * @param intervalEnd  String
	 * @return Money {@link Money}
	 */
	public Money calculateHousePrice(Product.ProductIdentifier houseId, String intervalStart, String intervalEnd) {
		LocalDateTime start_date = LocalDate.parse(intervalStart).atTime(LocalTime.parse("00:01"));
		LocalDateTime end_date = LocalDate.parse(intervalEnd).atTime(LocalTime.parse("23:59"));
		int duration = (int) Interval.from(start_date).to(end_date).getDuration().toDays();
		return (Money) houseManagement.findById(houseId).get().getPrice().multiply(duration);
	}


	/**
	 * Calculates the price of a bookings deposit based on the duration and the price of the house.
	 *
	 * @param booking {@link Booking}
	 * @return Money {@link Money}
	 */
	public Money calculateDeposit(Booking booking) {
		int duration = (int) booking.getRentalInformation().getInterval().getDuration().toDays();
		return (Money) booking.getRentalInformation().getHouse().getPrice().multiply(duration).multiply(0.1);
	}

	/**
	 * Calculates the price of a bookings deposit based on the duration and the price of the house. Used to calculate the
	 * deposit price before the booking is created.
	 *
	 * @param houseId {@link org.salespointframework.catalog.Product.ProductIdentifier}
	 * @param interval {@link Interval}
	 * @return Money {@link Money}
	 */
	public Money calculateDeposit(Product.ProductIdentifier houseId, Interval interval) {
		int duration = (int) interval.getDuration().toDays();
		return (Money) houseManagement.findById(houseId).get().getPrice().multiply(duration).multiply(0.1);
	}


	/**
	 * decreasing the number of tickets for a ticket booking by the given amount and save the changes
	 *
	 * @param booking         {@link Booking}
	 * @param numberOfTickets {@link int}
	 */
	public void decreaseTicketsForBooking(Booking booking, int numberOfTickets) {
		if (booking.getRentalType() != RentalType.TICKET) {
			return;
		}
		booking.decreaseTickets(numberOfTickets);
		bookings.save(booking);
	}

	/**
	 * Refunds tickets of a booking by the given amount and sets the booking status to canceled if all tickets are refunded
	 *
	 * @param booking     {@link Booking}
	 * @param refundCount {@link List<Integer>}
	 */
	public void refundSingleTicket(Booking booking, List<Integer> refundCount) {
		if (refundCount.isEmpty()) {
			return;
		}
		refundCount.removeLast();
		if ((booking.getTickets() - refundCount.size()) <= 0) {
			setBookingStatusCanceled(booking);

		} else {
			decreaseTicketsForBooking(booking, refundCount.size());
			eventManagement.findById(booking.getEvent()).get().increaseTickets(refundCount.size());
		}
	}

	/** Returns a pair of the first event without tickets and the total price of all overlapping events.
	 * @param overlappingEvents List&lt;Event&gt;
	 * @return Pair&lt;Optional&lt;Event&gt;, MonetaryAmount&gt;
	 */
	public Pair<Optional<Event>, MonetaryAmount> getOverlapEventsAndPrice(List<Event> overlappingEvents) {
		MonetaryAmount totalPrice = Money.of(0, EURO);
		Optional<Event> eventWithoutTicket = Optional.empty();
		for (Event event : overlappingEvents) {
			if (event.getTickets() <= 0) {
				eventWithoutTicket = Optional.of(event);
				break;
			}
			totalPrice = totalPrice.add(event.getPrice());
		}

		return Pair.of(eventWithoutTicket, totalPrice);
	}

	/**
	 * Creates a booking for an event.
	 *
	 * @param houseId      Product.ProductIdentifier
	 * @param intervalStart String
	 * @param intervalEnd  String
	 * @param userAccount UserAccount
	 * @param eventId      Product.ProductIdentifier
	 */
	public void createEventBookings(List<Product.ProductIdentifier> eventId, List<Integer> bookedTickets,
									String intervalStart, String intervalEnd, UserAccount userAccount,
									Product.ProductIdentifier houseId) {

		if (!eventId.isEmpty() || !bookedTickets.isEmpty()) {
			for (int i = 0; i < eventId.size(); i++) {
				createTicketBooking(userAccount.getId(), eventId.get(i), bookedTickets.get(i), houseId, intervalStart, intervalEnd);
				eventManagement.findById(eventId.get(i)).get().decreaseTickets(bookedTickets.get(i));
			}
		}
	}

	/**
	 * EventListener for dayHasPassed event, checks if a booking has passed its end date and sets the status to completed
	 *
	 * @param event {@link BusinessTime.DayHasPassed}
	 */
	@EventListener
	void dayPassed(BusinessTime.DayHasPassed event) {
		bookings.findAll().forEach(booking -> {
			if ((booking.getStatus() != BookingStatus.CANCELED && booking.getStatus() != BookingStatus.COMPLETED)
				&& (booking.getRentalInformation().getInterval().getEnd().isBefore(TimeManager.getTime()))) {
				setBookingStatusCompleted(booking);
			}
		});
	}

	/**
	 * creating an advertising booking for all houses within the radius
	 *
	 * @param event Event
	 * @param advertisingRadius int
	 * @param eventStaffId UserAccount.UserAccountIdentifier
	 */
	public void bookAdsForSmallEvent(Event event, int advertisingRadius, UserAccount.UserAccountIdentifier eventStaffId) {
		List<House> nearbyHouses = houseManagement.getAllNearbyHousesToLocation(advertisingRadius, event.getLocation());
		Product.ProductIdentifier eventId = event.getId();
		nearbyHouses.removeIf(house -> (!bookings.findBy_HouseId_EventId_NotStatus_NotStatus1(house.getId(),
			eventId, BookingStatus.COMPLETED, BookingStatus.CANCELED).isEmpty()));
		List<Interval> smallEventIntervals = eventManagement.getRecurringDates(event);
		Interval fullInterval = event.getInterval();
		if (!smallEventIntervals.isEmpty()) {
			LocalDateTime endDate = smallEventIntervals.getLast().getEnd();
			fullInterval = Interval.from(LocalDateTime.now()).to(endDate);
		}

		for (House house : nearbyHouses) {
			createAdvertisingBooking(eventStaffId, event.getId(), fullInterval, house);
		}
	}

	/**
	 * Receives a list of houses and removes all houses which have an active advertising booking for the given event,
	 * then returns the modified list.
	 * @param houses List&lt;House&gt;
	 * @param eventId Product.ProductIdentifier
	 * @return List&lt;House&gt;
	 */
	public List<House> removeHousesWithActiveAdvertising(List<House> houses, Product.ProductIdentifier eventId) {
		houses.removeIf(house -> (!bookings.findBy_HouseId_EventId_NotStatus_NotStatus1(house.getId(),
			eventId, BookingStatus.COMPLETED, BookingStatus.CANCELED).isEmpty()));
		return houses;
	}

	/**
	 * returning a list of all events which have an accepted advertising or event booking to this houseId
	 *
	 * @param houseId Product.ProductIdentifier
	 * @return List&lt;List&lt;Event&gt;&gt;
	 */
	public List<List<Event>> getAllAcceptedEventsForHouse(Product.ProductIdentifier houseId) {
		Streamable<Booking> bigEventBookings = bookings.findBy_HouseId_Type_Status(houseId,
			RentalType.EVENT, BookingStatus.RESERVED);
		Streamable<Booking> smallEventBookings = bookings.findBy_HouseId_Type_Status(houseId,
			RentalType.ADVERTISING, BookingStatus.PAID);

		List<Event> smallEvents = new ArrayList<>();
		for (Booking booking : smallEventBookings) {
			smallEvents.add(eventManagement.findById(booking.getEvent()).get());
		}

		List<Event> bigEvents = new ArrayList<>();
		for (Booking booking : bigEventBookings) {
			bigEvents.add(eventManagement.findById(booking.getEvent()).get());
		}

		List<List<Event>> allEvents = new ArrayList<>();
		allEvents.addAll(List.of(bigEvents));
		allEvents.addAll(List.of(smallEvents));

		return allEvents;
	}

	/**
	 * Returns all bookings for a house and event.
	 * @param houseId Product.ProductIdentifier
	 * @param landlordId UserAccount.UserAccountIdentifier
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> getEventBookingsByHouseAndLandlord(Product.ProductIdentifier houseId,
																  UserAccount.UserAccountIdentifier landlordId) {
		return bookings.findBy_HouseId_LandlordId_Type(houseId, landlordId, RentalType.EVENT);
	}

	/**
	 * Returns all bookings for a house which do match the given status.
	 * @param houseId Product.ProductIdentifier
	 * @param landlordId UserAccount.UserAccountIdentifier
	 * @param status BookingStatus
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> getPersonalBookingsByHouseLandlordAndStatus(Product.ProductIdentifier houseId,
																		   UserAccount.UserAccountIdentifier landlordId,
																		   BookingStatus status) {
		return bookings.findBy_HouseId_LandlordId_Type_Status(houseId, landlordId, RentalType.PERSONAL, status);
	}

	/**
	 * Returns all past bookings for a house and landlord.
	 *
	 * @param houseId    Product.ProductIdentifier
	 * @param landlordId UserAccount.UserAccountIdentifier
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> getPastBookingsByHouseLandlord(Product.ProductIdentifier houseId,
															  UserAccount.UserAccountIdentifier landlordId) {
		return bookings.findBy_houseId_LandlordId(houseId, landlordId);
	}

	/**
	 * Returns a map of all bookings with the name of the customer.
	 * Used to display the bookings in the landlords rental overview.
	 *
	 * @param houseId    Product.ProductIdentifier
	 * @param landlordId UserAccount.UserAccountIdentifier
	 * @param status     BookingStatus
	 * @return Map&lt;Booking, String details&gt;
	 * <br>details is the name of the customer or the name of the event
	 */
	public Map<Booking, String> getPersonalBookingsWithCustomerByHouseLandlord(Product.ProductIdentifier houseId,
																			   UserAccount.UserAccountIdentifier landlordId,
																			   BookingStatus status) {
		Streamable<Booking> bookings = getPersonalBookingsByHouseLandlordAndStatus(houseId, landlordId, status);

		Map<Booking, String> bookingsByStatus = new HashMap<>();
		for (Booking booking : bookings) {
			String username = userAccountManagement.get(booking.getUserAccountIdentifier()).get().getUsername();
			bookingsByStatus.put(booking, username);
		}
		return bookingsByStatus;
	}


	/**
	 * Returns a map of all past bookings by a customer, which includes the landlords name. Used to show the past bookings
	 * in the customers rental overview.
	 * @param houseId Product.ProductIdentifier
	 * @param landlordId UserAccount.UserAccountIdentifier
	 * @return Map&lt;Booking, String details&gt;
	 */
	public Map<Booking, String> getPastBookingsWithCustomerByHouseAndLandlord(Product.ProductIdentifier houseId,
																			  UserAccount.UserAccountIdentifier landlordId) {
		Streamable<Booking> bookings = getPastBookingsByHouseLandlord(houseId, landlordId);

		Map<Booking, String> pastBookings = new HashMap<>();
		for (Booking booking : bookings) {
			String username = userAccountManagement.get(booking.getUserAccountIdentifier()).get().getUsername();
			pastBookings.put(booking, username);
		}
		return pastBookings;
	}


	/**
	 * Returns a map of all bookings with the name of the customer.
	 * Used to display the bookings in the landlords rental overview.
	 *
	 * @param houseId    Product.ProductIdentifier
	 * @param landlordId UserAccount.UserAccountIdentifier
	 * @return Map&lt;Booking, String details&gt;
	 * <br>details is the name of the event
	 */
	public Map<Booking, String> getEventBookingsWithCustomerByHouseAndLandlord(Product.ProductIdentifier houseId,
																			   UserAccount.UserAccountIdentifier landlordId) {
		Streamable<Booking> bookings = getEventBookingsByHouseAndLandlord(houseId, landlordId);

		Map<Booking, String> eventBookings = new HashMap<>();
		for (Booking booking : bookings) {
			if (!BookingStatus.COMPLETED.equals(booking.getStatus())) {
				String eventName = eventManagement.findById(booking.getEvent()).get().getName();
				eventBookings.put(booking, eventName);
			}
		}
		return eventBookings;
	}

	/**
	 * used for the Rental DataInitializer only to set all advertising bookings to paid
	 * (but generally finds all bookings to the given Type)
	 *
	 * @param rentalType RentalType
	 * @return List&lt;Booking&gt;
	 */
	public List<Booking> findByRentalType(RentalType rentalType) {
		return bookings.findByRentalType(rentalType);
	}


	/**
	 * returning only those Events which have an active Event Booking with an house
	 *
	 * @param events List&lt;Event&gt;
	 * @return List&lt;Event&gt;
	 */
	public List<Event> removeEventsWithoutHouse(List<Event> events) {

		List<Event> eventsWithHouses = new ArrayList<>();
		for (Event someEvent : events) {

			if (someEvent.getType().equals(EventType.BIG_EVENT)
				&& bookings.findBy_EventId_Type_NotStatus_NotStatus1_NotStatus2(someEvent.getId(),
				RentalType.EVENT, BookingStatus.COMPLETED, BookingStatus.CANCELED, BookingStatus.OPEN).isEmpty()) {

				continue;
			}
			eventsWithHouses.add(someEvent);

		}
		return eventsWithHouses;
	}

	/**
	 * returns a list of ticket bookings beloning to a personal booking. Used to cancel all ticket bookings when a
	 * personal booking is canceled.
	 * @param id Order.OrderIdentifier
	 * @return Streamable&lt;Booking&gt;
	 */
	public Streamable<Booking> getTicketBookingsForPersonal(Order.OrderIdentifier id) {
		Booking booking = bookings.findById(id).get();
		return bookings.findBy_UserId_LandlordId_Interval_Type_NotStatus(booking.getUserAccountIdentifier(),
			booking.getRentalInformation().getHouse().getLandlordId(),
			booking.getRentalInformation().getInterval(),
			RentalType.TICKET,
			BookingStatus.CANCELED);
	}


	/**
	 * checks if a customer has a completed booking for the product and has not commented yet: in case returning true
	 * @param account UserAccount
	 * @param productId Product.ProductIdentifier
	 * @param type CommentType
	 * @return boolean
	 */
	public boolean customerAllowedToComment(UserAccount account, Product.ProductIdentifier productId, CommentType type){
		if(account == null){
			return false;
		}else if(type == CommentType.HOUSECOMMENT){
			return !bookings.findBy_CustomerId_HouseId_Status
				(productId, account.getId(), BookingStatus.COMPLETED).isEmpty()
				&&
				commentManagement.findByAccountIdAndProductId(account.getId(), productId).isEmpty();
		}else{
			Event event = eventManagement.findById(productId).get();
			if(event.getType() == EventType.BIG_EVENT){
				return !bookings.findBy_CustomerId_EventId_Status
					(productId, account.getId(), BookingStatus.COMPLETED).isEmpty()
					&&
					commentManagement.findByAccountIdAndProductId(account.getId(), productId).isEmpty();
			}else{
				return commentManagement.findByAccountIdAndProductId(account.getId(), productId).isEmpty()
					&&
					event.getInterval().getStart().isBefore(TimeManager.getTime());
			}
		}

	}

}
