package vacationrental.eventcatalog;


import com.mysema.commons.lang.Assert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccount;
import vacationrental.location.Location;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.salespointframework.core.Currencies.EURO;

/**
 * An extension of {@link Product} to add Event specific Parameters and Methods.
 *
 * @author Erik Schneider
 */
@Entity
public class Event extends Product {

	private String description;
	private Location location;
	private Interval interval;
	private LocalTime time;
	private boolean isCancelled = false;
	private EventType type;
	private int tickets;
	private int maxTickets;
	private UserAccount.UserAccountIdentifier eventStaffId;
	private RecurrencePattern recurrencePattern;
	private LocalDate endOfRecurrence;
	private double averageRating;


	@ElementCollection
	private final List<String> imagePaths = new ArrayList<>();

	@SuppressWarnings("unused")
	public Event() {
	}

	// erstmal generelle Event, unterteilung auf small/big kommt noch
	public Event(String name,
				 String description,
				 Location location,
				 Interval interval,
				 LocalTime time,
				 double ticketprice,
				 int tickets,
				 EventType type,
				 UserAccount.UserAccountIdentifier eventStaffId) {
		super(name, Money.of(ticketprice, EURO));

		Assert.notNull(name, "Name must not be null");
		Assert.notNull(description, "Description must not be null");
		Assert.notNull(location, "Location must not be null");
		Assert.notNull(time, "Time must not be null");
		Assert.notNull(ticketprice, "Price must not be null");

		this.description = (description != null && description.length() >= 255)
			? description.substring(0, 252) + "..."
			: description;
		this.location = location;
		this.interval = interval;
		this.time = time;
		this.type = type;
		this.tickets = tickets;
		this.maxTickets = tickets;
		this.eventStaffId = eventStaffId;
		this.recurrencePattern = RecurrencePattern.NONE;
		this.averageRating = 0.0;
	}

	public Event(String name,
				 String description,
				 Location location,
				 Interval interval,
				 LocalTime time,
				 EventType type,
				 UserAccount.UserAccountIdentifier eventStaffId,
				 RecurrencePattern recurrencePattern,
				 LocalDate endOfRecurrence) {
		super(name, Money.of(0.0, EURO));
		this.description = description;
		this.location = location;
		this.interval = interval;
		this.time = time;
		this.type = type;
		this.tickets = 0;
		this.eventStaffId = eventStaffId;
		this.recurrencePattern = recurrencePattern;
		this.endOfRecurrence = endOfRecurrence;
		this.averageRating = 0.0;
	}


	public String getDescription() {
		return description;
	}

	public Location getLocation() {
		return location;
	}

	public Interval getInterval() {
		return interval;
	}

	public LocalTime getTime() {
		return time;
	}


	public double getTicketprice() {
		return this.getPrice().getNumber().doubleValue();
	}

	//für booking und löschen von Events, mit refund
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * Cancel a {@link Event}.
	 */
	public void cancel() {
		this.isCancelled = true;
	}

	public EventType getType() {
		return type;
	}

	public int getTickets() {
		return tickets;
	}

	public UserAccount.UserAccountIdentifier getEventStaffId() {
		return eventStaffId;
	}

	public void setEventStaffId(UserAccount.UserAccountIdentifier eventStaffId) {
		this.eventStaffId = eventStaffId;
	}

	public void setDescription(String description) {
		this.description = (description != null && description.length() >= 255)
			? description.substring(0, 252) + "..."
			: description;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setInterval(Interval interval) {
		this.interval = interval;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public void setTickets(int tickets) {
		this.tickets = tickets;
	}

	/**
	 * @param obj
	 * @return boolean if the {@link Event} is equal to another {@link Event}.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Event){
			return Objects.equals(((Event) obj).getId(), this.getId());
		}

		return false;
	}

	public String getStartDateString() {
		return interval.getStart().format(DateTimeFormatter.ofPattern("dd.MM"));
	}

	public String getEndDateString() {
		return interval.getEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	public String getFEStartDateString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
		return interval.getStart().format(formatter);
	}

	public String getFEEndDateString() {
		DateTimeFormatter formatter;
		if (interval.getStart().getYear() == interval.getEnd().getYear()) {
			formatter = DateTimeFormatter.ofPattern("dd.MM");
		} else {
			formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		}
		return interval.getEnd().format(formatter);
	}

	public String getTypeString() {
		return type.toString();
	}

	public List<String> getImagePaths() {
		return imagePaths;
	}

	/**
	 * Adds a path of where an image is saved to the {@link Event}.
	 *
	 * @param imagePath String of path where image is saved.
	 */
	public void addImagePath(String imagePath) {
		this.imagePaths.add(imagePath);
	}

	/**
	 * Removes a path of where an image is saved from the {@link Event}.
	 *
	 * @param imagePath String of path where image is saved.
	 */
	public void removeImagePath(String imagePath){
		for (String path : this.imagePaths) {
			if (path.equals(imagePath) || path.contains(imagePath)) {
				this.imagePaths.remove(path);
				break;
			}
		}
	}

	/**
	 * Decrease the number of available tickets of a {@link Event}.
	 *
	 * @param numberOfTickets int by how many tickets the available amount is to be decreased.
	 * @return boolean if decreasing of tickets was successful.
	 */
	public boolean decreaseTickets(int numberOfTickets) {
		this.tickets -= numberOfTickets;
		return this.tickets >= 0;
	}

	/**
	 * Increase the number of available tickets and max tickets of a {@link Event}.
	 *
	 * @param numberOfTickets int by how many tickets the available amount is to be increased.
	 */
	public void increaseTickets(int numberOfTickets) {
		this.tickets += numberOfTickets;
		this.maxTickets += numberOfTickets;
	}

	public void setRecurrencePattern(RecurrencePattern recurrencePattern){this.recurrencePattern = recurrencePattern;}

	public RecurrencePattern getRecurrencePattern(){return recurrencePattern;}

	public void setEndOfRecurrence(LocalDate endOfRecurrence){this.endOfRecurrence = endOfRecurrence;}

	/**
	 * Get the end of the recurrence
	 *
	 * @return for {@link Event} of type BIG_EVENT the endDate, for {@link Event} of type SMALL_EVENT the endOfRecurrence
	 */
	public LocalDate getEndOfRecurrence(){
		if (recurrencePattern.equals(RecurrencePattern.NONE)) {
			/* return normal end date for big events to avoid null pointers */
			return interval.getEnd().toLocalDate();
		}
		return endOfRecurrence;
	}

	public int getMaxTickets() {
		return maxTickets;
	}

	public void setMaxTickets(int maxTickets) {
		this.maxTickets = maxTickets;
	}

	public double getAverageRating() {
		return averageRating;
	}
	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}
}
