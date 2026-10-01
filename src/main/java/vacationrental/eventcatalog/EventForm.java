package vacationrental.eventcatalog;

import java.time.LocalTime;

import jakarta.validation.constraints.*;

import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;

/**
 * A form backing class to collect data for the creation of {@link Event}s of type BIG_EVENT.
 *
 * @author Erik Schneider
 */
public class EventForm {
    private @NotEmpty String name;
    private @NotEmpty String description;

	@Pattern(regexp = ".+?\\s+[A-Za-z0-9]+,\\s+\\d{5}\\s+.+?,\\s+.+?",
		message = "Adresse entspricht nicht dem richtigen Format!")
    private @NotEmpty String location;

    private @NotNull String startDate;
	private @NotEmpty String endDate;
    private @NotNull LocalTime time;
    private double ticketprice;
	private @Min(value = 0) int tickets;
	private Product.ProductIdentifier eventId;
	private UserAccount.UserAccountIdentifier eventStaffId;
	private RecurrencePattern recurrencePattern;
	private String endOfRecurrence;

	// Big Event
    public EventForm(String name,
					 String description,
					 String location,
					 String startDate,
					 String endDate,
					 LocalTime time,
					 double ticketprice,
					 int tickets,
					 UserAccount.UserAccountIdentifier eventStaffId) {
		this.name = name;
		this.description = description;
		this.location = location;
		this.startDate = startDate;
		this.endDate = endDate;
        this.time = time;
        this.ticketprice = ticketprice;
        this.tickets = tickets;
		this.eventStaffId = eventStaffId;
	}
	// Small Event
	public EventForm(String name,
					 String description,
					 String location,
					 String startDate,
					 String endDate,
					 LocalTime time,
					 UserAccount.UserAccountIdentifier eventStaffId,
					 RecurrencePattern recurrencePattern,
					 String endOfRecurrence) {
		this.name = name;
		this.description = description;
		this.location = location;
		this.startDate = startDate;
		this.endDate = endDate;
		this.time = time;
		this.eventStaffId = eventStaffId;
		this.recurrencePattern = recurrencePattern;
		this.endOfRecurrence = endOfRecurrence;
	}

	public EventForm() {

	}

	public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getLocation(){
        return location;
    }

    public LocalTime getTime(){
        return time;
    }

    public double getTicketprice(){
		return ticketprice;
    }

	public int getTickets(){
		return tickets;
	}

	public String getStartDate(){
		return startDate;
	}

	public String getEndDate(){
		return endDate;
	}

	public UserAccount.UserAccountIdentifier getEventStaffId(){
		return eventStaffId;
	}

	public void setEventStaffId(UserAccount.UserAccountIdentifier eventStaffId){
		this.eventStaffId = eventStaffId;
	}

	public void setName(String name){
		this.name = name;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public void setLocation(String location){
		this.location = location;
	}
	public void setStartDate(String start_date){
		this.startDate = start_date;
	}
	public void setEndDate(String end_date){
		this.endDate = end_date;
	}
	public void setTime(LocalTime time){
		this.time = time;
	}
	public void setTicketprice(double ticketprice){
		this.ticketprice = ticketprice;
	}
	public void setTickets(int tickets){
		this.tickets = tickets;
	}
	public Product.ProductIdentifier getEventId(){
		return eventId;
	}
	public void setEventId(Product.ProductIdentifier eventId){
		this.eventId = eventId;
	}

	public void setRecurrencePattern(RecurrencePattern recurrencePattern){this.recurrencePattern = recurrencePattern;}
	public RecurrencePattern getRecurrencePattern(){return recurrencePattern;}
	public void setEndOfRecurrence(String endOfRecurrence){this.endOfRecurrence = endOfRecurrence;}
	public String getEndOfRecurrence(){return endOfRecurrence;}

	@AssertTrue(message = "Ticketprice must be 0 for SmallEvents and positive for BigEvents.")
	public boolean isTicketPriceValid() {
		if (recurrencePattern != null) {
			return Double.compare(ticketprice, 0.0) == 0;
		} else {
			return Double.compare(ticketprice, 0.0) > 0;
		}
	}
}
