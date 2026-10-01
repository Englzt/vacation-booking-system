package vacationrental.booking;

import jakarta.persistence.*;
import org.salespointframework.time.Interval;
import vacationrental.housecatalog.House;

import java.time.format.DateTimeFormatter;

@Embeddable
public class RentalInformation {

	private Interval interval;

	@ManyToOne
	private House house;

	public House getHouse() {
		return house;
	}

	public void setHouse(House house) {
		this.house = house;
	}

	RentalInformation(House house, Interval interval) {
		this.house = house;
		this.interval = interval;
	}

	public RentalInformation() {
	}

	public Interval getInterval() {
		return interval;
	}

	public String getStartDateString() {
		return interval.getStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	public String getEndDateString() {
		return interval.getEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

}
