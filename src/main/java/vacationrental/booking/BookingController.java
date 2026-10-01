/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vacationrental.booking;

import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Order;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import vacationrental.eventcatalog.Event;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.housecatalog.House;
import vacationrental.housecatalog.HouseManagement;
import vacationrental.util.TimeManager;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
class BookingController {

	private final BookingManagement bookingManagement;
	private final HouseManagement houseManagement;
	private final UserAccountManagement userAccountManagement;
	private final EventManagement eventManagement;
	private final ResourceBundle properties;

	@Value("${vacationrental.cancellation-deadline}")
	private int cancellationDeadline;

	public BookingController (BookingManagement bookingManagement,
							  HouseManagement houseManagement,
							  UserAccountManagement userAccountManagement, EventManagement eventManagement) {
		this.houseManagement = houseManagement;
		this.bookingManagement = bookingManagement;
		this.userAccountManagement = userAccountManagement;
		this.eventManagement = eventManagement;
		this.properties = ResourceBundle.getBundle("messages");
	}

	/**
	 * adds all required attributes to display the bookings of an EventStaff; executed by clicking 'bookings' as EventStaff
	 * @param model {@link Model}
	 * @param userAccount {@link UserAccount}
	 * @return template eventstaffrentals
	 */
	@GetMapping("/eventstaffrentals")
	@PreAuthorize("hasRole('EventStaff')")
	String getEventStaffRentals(Model model, @LoggedIn Optional<UserAccount> userAccount) {
		UserAccount.UserAccountIdentifier eventStaffId = userAccount.get().getId();
		model.addAttribute("bookings",bookingManagement.getAllBookingsWithNameByCustomer(eventStaffId));
		model.addAttribute("bookingsReserved",
			bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.RESERVED, eventStaffId));


		return "booking/eventstaffrentals";

	}

	/**
	 * adds all required attributes to display the bookings of a Landlord; executed by clicking 'bookings' as Landlord
	 * @param model {@link Model}
	 * @param userAccount {@link UserAccount}
	 * @return template landlordrentals
	 */
	@GetMapping("/landlordrentals")
	@PreAuthorize("hasRole('Landlord')")
	String getLandlordRentals(Model model, @LoggedIn Optional<UserAccount> userAccount){
		UserAccount.UserAccountIdentifier landlord_id = userAccount.get().getId();
		if (!model.containsAttribute("error")) {
			model.addAttribute("error", null);
		}

		List<Map<Booking, String>> allBookings = new ArrayList<>();
		allBookings.add(bookingManagement.getBookingsWithCustomerByLandlordAndStatus(landlord_id, BookingStatus.OPEN));
		allBookings.add(bookingManagement.getBookingsWithCustomerByLandlordAndStatus(landlord_id, BookingStatus.RESERVED));
		allBookings.add(bookingManagement.getBookingsWithCustomerByLandlordAndStatus(landlord_id, BookingStatus.PAID));

		model.addAttribute("noBookings", allBookings.stream().mapToInt(Map::size).sum() == 0);
		model.addAttribute("bookings", allBookings);


		model.addAttribute("currentPath", "/landlordrentals");
		return "booking/landlordrentals";
	}

	/**
	 * adds all required attributes to display the bookings of a Customer; executed by clicking 'bookings' as Customer
	 * @param model {@link Model}
	 * @param userAccount {@link UserAccount}
	 * @return template customerrentals
	 */
	@GetMapping("/customerrentals")
	@PreAuthorize("hasRole('Customer')")
	String getCustomerRentals(Model model, @LoggedIn Optional<UserAccount> userAccount){
		if (!model.containsAttribute("error")) {
			model.addAttribute("error", null);
		}

		UserAccount.UserAccountIdentifier user_id = userAccount.get().getId();

		List<Map<Booking, String>> allBookings = new ArrayList<>();
		allBookings.add(bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.OPEN, user_id));
		allBookings.add(bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.RESERVED, user_id));
		allBookings.add(bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.PAID, user_id));
		allBookings.add(bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.COMPLETED, user_id));
		allBookings.add(bookingManagement.getBookingWithNamesByStatusAndCustomer(BookingStatus.CANCELED, user_id));

		model.addAttribute("noBookings", allBookings.stream().mapToInt(Map::size).sum() == 0);
		model.addAttribute("bookings", allBookings);

		return "booking/customerrentals";
	}


	/**
	 * adds all attributes to display the ticket bookings of a Customer; executed by clicking 'Tickets' as Customer
	 * @param model {@link Model}
	 * @param userAccount {@link UserAccount}
	 * @return template tickets
	 */
	@GetMapping("/tickets")
	@PreAuthorize("hasRole('Customer')")
	String getCustomerTickets(Model model, @LoggedIn Optional<UserAccount> userAccount){
		model.addAttribute("ticketBookings", bookingManagement.getTicketBookingsWithEvent(userAccount.get().getId()));
		return "booking/tickets";
	}


	/**
	 * adds all required attributes to display the refund page for single tickets: executed by clicking refund as customer
	 * @param id {@link Order.OrderIdentifier}
	 * @param model {@link Model}
	 * @return template cancelSingleTickets
	 */
	@GetMapping("/tickets/refund/{id}")
	@PreAuthorize("hasRole('Customer')")
	String showTicketRefundPage(@PathVariable Order.OrderIdentifier id, Model model){
		Booking booking = bookingManagement.findById(id);
		model.addAttribute("booking", booking);
		model.addAttribute("event", eventManagement.findById(booking.getEvent()).get());
		if (!model.containsAttribute("eventStartedError")) {
			model.addAttribute("eventStartedError", null);
		}
		return "booking/cancelSingleTickets";
	}


	/**
	 * refunds the chosen tickets and returning to the ticket overview
	 * @param id {@link Order.OrderIdentifier}
	 * @param refundCount {@link List} of {@link Integer}
	 * @return redirect to tickets
	 */
	@PostMapping("/tickets/refund/{id}")
	@PreAuthorize("hasRole('Customer')")
	String refundSingleTickets(@PathVariable Order.OrderIdentifier id, @RequestParam List<Integer> refundCount,
							   RedirectAttributes attributes){

		Booking booking = bookingManagement.findById(id);
		Event event = eventManagement.findById(booking.getEvent()).get();
		LocalDateTime deadline = event.getInterval().getStart().minusDays(cancellationDeadline);
		if(deadline.isBefore(TimeManager.getTime()) || deadline.isEqual(TimeManager.getTime())){
			attributes.addFlashAttribute("eventStartedError", "Event has already been started");
			return "redirect:/tickets/refund/" + id;
		}
		bookingManagement.refundSingleTicket(booking, refundCount);
		return "redirect:/tickets";
	}


	/**
	 * adds all required attributes to display the details of a booking; executed by clicking 'details' on a booking
	 * @param id {@link Order.OrderIdentifier}
	 * @param model {@link Model}
	 * @return template rentaldetails
	 */
	@GetMapping("/bookings/{id}")
	@PreAuthorize("!hasRole('Admin')")
	String viewRentalDetails(@PathVariable Order.OrderIdentifier id, Model model){
		Booking booking = bookingManagement.findById(id);
		if (booking == null) {
			return "redirect:/";
		}
		if(booking.getRentalType() != RentalType.TICKET){
			if (booking.getRentalType() == RentalType.EVENT) {
				model.addAttribute("event", eventManagement.findById(booking.getEvent()).get());
			} else if (booking.getRentalType() == RentalType.PERSONAL) {
				model.addAttribute("customer", userAccountManagement.get(booking.getUserAccountIdentifier()).get());
			}

			UserAccount landlord = null;
			if (booking.getRentalInformation().getHouse() != null) {
				landlord = userAccountManagement.get(booking.getRentalInformation().getHouse().getLandlordId()).get();
			}

			model.addAttribute("landlord", landlord);
			model.addAttribute("booking", booking);
			model.addAttribute("rental", booking.getRentalInformation());
			return "booking/rentaldetails";
		}

		Event event = eventManagement.findById(booking.getEvent()).get();
		model.addAttribute("eventStaff", userAccountManagement.get(event.getEventStaffId()).get());
		model.addAttribute("landlord",
			userAccountManagement.get(booking.getRentalInformation().getHouse().getLandlordId()).get());
		model.addAttribute("event", event);
		model.addAttribute("booking", booking);
		return "booking/ticketDetails";

	}

	/**
	 * executed by clicking 'make reservation' as a customer;
	 * checks the chosen interval, checks if an event booked the house during the interval or if there are other problems
	 * and creates a new Booking for the house if possible
	 * @param house_id {@link Product.ProductIdentifier}
	 * @param interval_start {@link String}
	 * @param interval_end {@link String}
	 * @param model {@link Model}
	 * @param attributes {@link RedirectAttributes}
	 * @param userAccount {@link UserAccount}
	 * @return same Page as before || bookEventsToHouse || houses
	 */
	@PostMapping(value = "/rent")
	@PreAuthorize("hasRole('Customer')")
	public String rentHouse(@RequestParam Product.ProductIdentifier house_id,
							 @RequestParam String interval_start,
							 @RequestParam String interval_end,
							 Model model, RedirectAttributes attributes,
							 @LoggedIn Optional<UserAccount> userAccount) {

		if (interval_start.equals(interval_end)) {
			attributes.addFlashAttribute("dateError", "Check-In und Check-Out dürfen nicht am selben Tag liegen!");
			return "redirect:/house/" + house_id;
		}

		Optional<House> House = houseManagement.findById(house_id);
		Interval interval = Interval.from(LocalDate.parse(interval_start).atTime(LocalTime.parse("00:01")))
			.to(LocalDate.parse(interval_end).atTime(LocalTime.parse("23:59")));

		/* check if interval is valid */
		if(!bookingManagement.checkInvalidInterval(LocalDate.parse(interval_start).atTime(LocalTime.parse("00:01"))
			, LocalDate.parse(interval_end).atTime(LocalTime.parse("23:59")))
		){
			attributes.addFlashAttribute("dateError", "Der Zeitraum muss gültig sein und in der Zukunft liegen!");
			return "redirect:/house/" + house_id;
		}

		if (bookingManagement.isHouseAvailable(house_id, interval, null, false) == null){
			attributes.addFlashAttribute("dateError", "Das Haus ist in diesem Zeitraum leider schon vergeben!");
			return "redirect:/house/" + house_id;
		}

		/* get all events that overlap with the selected interval */
		List<Event> overlappingEvents = bookingManagement.getAllEventsForHouseDuringInterval(house_id,
			LocalDate.parse(interval_start).atTime(LocalTime.parse("00:01")),
			LocalDate.parse(interval_end).atTime(LocalTime.parse("23:59")));

		Pair<Optional<Event> , MonetaryAmount> eventMoney =
			bookingManagement.getOverlapEventsAndPrice(overlappingEvents);

		Event eventWithoutTicket = eventMoney.getFirst().orElse(null);
		MonetaryAmount totalPrice = eventMoney.getSecond();

		if(eventWithoutTicket != null){
			attributes.addFlashAttribute("dateError", "Das Event - "
				+ eventWithoutTicket.getName()
				+ " - ist ausgebucht! Buchung im Zeitraum nicht möglich!");
			return "redirect:/house/" + house_id;
		}

		if(!overlappingEvents.isEmpty() &&
			bookingManagement.isHouseAvailable(house_id, interval, null, false) == RentalType.TICKET
		){
			model.addAttribute("overlappingEvents", overlappingEvents);
			model.addAttribute("intervalStart", interval_start);
			model.addAttribute("intervalEnd", interval_end);
			model.addAttribute("houseId", house_id);
			model.addAttribute("totalPrice", totalPrice);
			model.addAttribute("house", House.get());

			return "booking/bookEventsToHouse";
		}

		String redirectUrl = UriComponentsBuilder.fromUriString("/rent/bookingConfirmation")
			.queryParam("houseId", house_id)
			.queryParam("intervalStart", interval_start)
			.queryParam("intervalEnd", interval_end)
			.toUriString();

		return "redirect:" + redirectUrl;
	}

	/**
	 * executed after a customer tried to book an event to the chosen house in the chosen interval;
	 * iterates through all events in the interval and creates some bookings with the given parameters
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param intervalStart {@link String}
	 * @param intervalEnd {@link String}
	 * @param eventId {@link List} of {@link Product.ProductIdentifier}
	 * @param bookedTickets {@link List} of {@link Integer}
	 * @param userAccount {@link UserAccount}
	 * @return redirect to houses
	 */
	@PostMapping("/rent/createBookings")
	@PreAuthorize("hasRole('Customer')")
	String rentHouseWithEvents (@RequestParam Product.ProductIdentifier houseId,
								@RequestParam String intervalStart,
								@RequestParam String intervalEnd,
								@RequestParam(required = false)  List<Product.ProductIdentifier> eventId,
								@RequestParam(required = false)  List<Integer> bookedTickets,
								@LoggedIn Optional<UserAccount> userAccount,
								Model model){
		if (eventId != null){
			bookingManagement.createEventBookings(eventId, bookedTickets, intervalStart, intervalEnd,
				userAccount.get(), houseId);
		}
		Booking booking = bookingManagement.createPersonalBooking(houseId, intervalStart, intervalEnd,
			userAccount.get().getId());

		model.addAttribute("createdBooking", booking);
		return "booking/bookingSuccess";
	}

	/**
	 * Handles the mapping for the booking confirmation page
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param intervalStart {@link String}
	 * @param intervalEnd {@link String}
	 * @param eventId {@link List} of {@link Product.ProductIdentifier}
	 * @param bookedTickets {@link List} of {@link Integer}
	 * @param userAccount {@link UserAccount}
	 * @param model {@link Model}
	 * @return bookingConfirmation template
	 */
	@GetMapping("/rent/bookingConfirmation")
	@PreAuthorize("hasRole('Customer')")
	String bookingConfirmation(@RequestParam Product.ProductIdentifier houseId,
							   @RequestParam String intervalStart,
							   @RequestParam String intervalEnd,
							   @RequestParam(required = false) List<Product.ProductIdentifier> eventId,
							   @RequestParam(required = false) List<Integer> bookedTickets,
							   @LoggedIn Optional<UserAccount> userAccount,
							   Model model){

		House house = houseManagement.findById(houseId).get();
		Money housePrice = bookingManagement.calculateHousePrice(houseId, intervalStart, intervalEnd);
		Money totalPrice = housePrice;

		Map<Event, Integer> bookedEvents = new HashMap<>();
		Money eventPrice = null;

		if (eventId != null){
			for (int i = 0; i < eventId.size(); i++){
				bookedEvents.put(eventManagement.findById(eventId.get(i)).get(), bookedTickets.get(i));
			}
			eventPrice = eventManagement.calculateTotalEventPrice(bookedEvents);
			totalPrice = housePrice.add(eventPrice);
		}

		Interval interval = Interval.from(LocalDate.parse(intervalStart).atTime(LocalTime.parse("00:01")))
			.to(LocalDate.parse(intervalEnd).atTime(LocalTime.parse("23:59")));

		model.addAttribute("house", house);
		model.addAttribute("duration", interval.getDuration().toDays());
		model.addAttribute("bookedEvents", bookedEvents);
		model.addAttribute("housePrice", housePrice);
		model.addAttribute("eventPrice", eventPrice);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("deposit", bookingManagement.calculateDeposit(houseId, interval));
		model.addAttribute("houseId", house.getId());
		model.addAttribute("intervalStart", intervalStart);
		model.addAttribute("intervalEnd", intervalEnd);
		model.addAttribute("eventId", eventId);
		model.addAttribute("bookedTickets", bookedTickets);

		return "booking/bookingConfirmation";
	}

	/**
	 * Handles the mapping for cancelling a booking
	 * @param model {@link Model}
	 * @param booking_id {@link Order.OrderIdentifier}
	 * @param origin {@link String}
	 * @param attributes {@link RedirectAttributes}
	 * @return bookingCancellation template
	 */
	@PostMapping("/cancel")
	String cancelBooking(Model model,
						 @RequestParam Order.OrderIdentifier booking_id,
						 @RequestParam String origin,
						 RedirectAttributes attributes){
		Booking booking = bookingManagement.findById(booking_id);

		LocalDateTime deadline = booking.getRentalInformation().getInterval().getStart().minusDays(cancellationDeadline);
		boolean eligible = TimeManager.getTime().isBefore(deadline);
		boolean has_started = TimeManager.getTime().isAfter(booking.getRentalInformation().getInterval().getStart())
			|| TimeManager.getTime().isEqual(booking.getRentalInformation().getInterval().getStart());

		Money amount = bookingManagement.calculateDeposit(booking);
		boolean fully_paid = booking.getStatus() == BookingStatus.PAID;
		if (fully_paid) {
			amount = bookingManagement.calculatePrice(booking);
		}

		Map<String, Booking> ticketList = new HashMap<>();

		Streamable<Booking> tickets = bookingManagement.getTicketBookingsForPersonal(booking_id);

		Money amount_tickets = Money.of(0, "EUR");

		for(Booking ticket : tickets){
			Event event = eventManagement.findById(ticket.getEvent()).get();
			if (ticket.getStatus() == BookingStatus.CANCELED || event.isCancelled()) {
				continue;
			}
			ticketList.put(eventManagement.findById(ticket.getEvent()).get().getName(), ticket);
			amount_tickets = amount_tickets.add(ticket.getBookingPrice().multiply(ticket.getTickets()));
		}

		model.addAttribute("tickets", ticketList);
		model.addAttribute("booking", booking);
		model.addAttribute("amount", amount);
		model.addAttribute("amount_tickets", amount_tickets);
		model.addAttribute("has_started", has_started);
		model.addAttribute("deadline", deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
		model.addAttribute("eligible", eligible);
		model.addAttribute("fully_paid", fully_paid);
		model.addAttribute("origin", origin);

		return "booking/bookingCancellation";
	}

	/**
	 * approve a rental as a landlord
	 * @param attributes {@link RedirectAttributes}
	 * @param booking_id {@link Order.OrderIdentifier}
	 * @return redirect to landlordrentals
	 */
	@PostMapping("/approve")
	@PreAuthorize("hasRole('Landlord')")
	String approveRental(RedirectAttributes attributes, @RequestParam Order.OrderIdentifier booking_id){
		Booking booking = bookingManagement.findById(booking_id);
		if(booking.getRentalType().equals(RentalType.ADVERTISING)){
			bookingManagement.setBookingStatusPaid(booking);
			return "redirect:/landlordrentals";
		}
		RentalType availability = bookingManagement.isHouseAvailable(booking.getRentalInformation().getHouse().getId(),
			booking.getRentalInformation().getInterval(), booking_id, true);
		boolean personalBookingPossible = availability == RentalType.VACANT || availability == RentalType.TICKET;
		if (personalBookingPossible) {
			bookingManagement.setBookingStatusReserved(booking);
			attributes.addFlashAttribute("error", null);
		} else {
			String error_msg = properties.getString("booking.error.unavailable");
			attributes.addFlashAttribute("error", error_msg);
		}
		return "redirect:/landlordrentals";
	}

	/**
	 * decline a rental as a landlord or customer / event staff
	 * @param booking_id {@link Order.OrderIdentifier}
	 * @param origin {@link String}
	 * @return redirect to origin, which is one of the templates customerrentals, landlordrentals, eventstaffrentals
	 */
	@PostMapping("/decline")
	@PreAuthorize("!hasRole('Admin')")
	String declineRental(@RequestParam Order.OrderIdentifier booking_id, @RequestParam String origin,
						 RedirectAttributes attributes){
		boolean result = bookingManagement.setBookingStatusCanceled(bookingManagement.findById(booking_id));
		if (!result) {
			String error_msg = properties.getString("booking.error.cant_cancel");
			attributes.addFlashAttribute("error", error_msg);
			return "redirect:/" + origin;
		}

		attributes.addFlashAttribute("error", null);
		return "redirect:/" + origin;
	}

	/**
	 * confirm the deposit for a rental as a landlord
	 * @param booking_id {@link Order.OrderIdentifier}
	 * @return redirect to landlordrentals
	 */
	@PostMapping("/confirmpayment")
	@PreAuthorize("hasRole('Landlord')")
	String confirmRentalPayment(@RequestParam Order.OrderIdentifier booking_id){
		bookingManagement.setBookingStatusPaid(bookingManagement.findById(booking_id));
		return "redirect:/landlordrentals";
	}

	/**
	 * sends a late notice to the customer.
	 * @param booking_id {@link Order.OrderIdentifier}
	 * @return redirect to landlordrentals
	 */
	@PostMapping("/sendlatenotice")
	@PreAuthorize("hasRole('Landlord')")
	String sendLateNotice(@RequestParam Order.OrderIdentifier booking_id){
		bookingManagement.sendLateNotice(bookingManagement.findById(booking_id));
		return "redirect:/landlordrentals";
	}

	/**
	 * Handles mapping for the house statistics (meaning current bookings) page
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param model {@link Model}
	 * @param userAccount {@link UserAccount}
	 * @return openBookings template
	 */
	@GetMapping("/openBookings/{houseId}")
	@PreAuthorize("hasRole('Landlord')")
	String getHouseStatistics(@PathVariable("houseId") Product.ProductIdentifier houseId, Model model,
							  @LoggedIn Optional<UserAccount> userAccount){
		UserAccount.UserAccountIdentifier landlordId = userAccount.get().getId();
		if (!model.containsAttribute("error")) {
			model.addAttribute("error", null);
		}
		model.addAttribute("house_id", houseId);
		model.addAttribute("bookingsOpen",
			bookingManagement.getPersonalBookingsWithCustomerByHouseLandlord(houseId, landlordId, BookingStatus.OPEN));
		model.addAttribute("bookingsReserved",
			bookingManagement.getPersonalBookingsWithCustomerByHouseLandlord(houseId, landlordId, BookingStatus.RESERVED));
		model.addAttribute("currentBookings",
			bookingManagement.getPersonalBookingsWithCustomerByHouseLandlord(houseId, landlordId, BookingStatus.PAID));
		model.addAttribute("eventBookings",
			bookingManagement.getEventBookingsWithCustomerByHouseAndLandlord(houseId, landlordId));
		return "house/openBookings";
	}

	/**
	 * Handles mapping for the rental history (meaning past bookings) page
	 * @param houseId {@link Product.ProductIdentifier}
	 * @param model {@link Model}
	 * @param userAccount {@link UserAccount}
	 * @return rentalHistory template
	 */
	@GetMapping("/rentalHistory/{house_Id}")
	@PreAuthorize("hasRole('Landlord')")
	String getHouseBookingHistory(@PathVariable("house_Id") Product.ProductIdentifier houseId, Model model,
								  @LoggedIn Optional<UserAccount> userAccount) {
		UserAccount.UserAccountIdentifier landlordId = userAccount.get().getId();
		if (!model.containsAttribute("error")) {
			model.addAttribute("error", null);
		}
		model.addAttribute("allBookings",
			bookingManagement.getPastBookingsWithCustomerByHouseAndLandlord(houseId, landlordId) );
		return "house/rentalHistory";
	}
}