package vacationrental.housecatalog;

import jakarta.validation.constraints.*;
import org.javamoney.moneta.Money;

import static org.salespointframework.core.Currencies.EURO;

public class HouseForm {
	@NotBlank(message = "Der Name darf nicht leer sein")
	private String name;

	@NotBlank(message = "Die Beschreibung darf nicht leer sein")
	private String description;

	@NotBlank(message = "Die Adresse darf nicht leer sein")
	@Pattern(regexp = ".+?\\s+[A-Za-z0-9]+,\\s+\\d{5}\\s+.+?,\\s+.+?",
		message = "Die Adresse muss im richtigen Format sein!")
	private String location;


	private Money priceMoney;

	@Min(value = 1, message = "Es muss mindsten PLatz fuer eine Person geben!")
	@NotNull(message = "Der Personenzahl darf nicht leer sein")
	private int maxPerson;

	@NotNull(message = "Die Bettenanzahl darf nicht leer sein")
	private int beds;

	@NotNull(message = "Die Kuechenanzahl darf nicht leer sein")
	private int kitchen;

	@NotNull(message = "Die Badezimmeranzahl darf nicht leer sein")
	private int bathrooms;
	private boolean parkingSpot;
	private boolean handicappedAccessible;

	/**
	 * house form constructor where all parameters a filled in 'by hand'
	 * @param name
	 * @param description
	 * @param location
	 * @param price_money
	 * @param maxPerson
	 * @param beds
	 * @param kitchen
	 * @param bathrooms
	 * @param parkingSpot
	 * @param handicappedAccessible
	 */
	public HouseForm(String name, String description, String location, Money price_money,
					 int maxPerson, int beds, int kitchen, int bathrooms,
					 boolean parkingSpot, boolean handicappedAccessible) {
		this.name = name;
		this.description = description;
		this.location = location;
		this.priceMoney = price_money;
		this.maxPerson = maxPerson;
		this.beds = beds;
		this.kitchen = kitchen;
		this.bathrooms = bathrooms;
		this.parkingSpot = parkingSpot;
		this.handicappedAccessible = handicappedAccessible;
	}

	/**
	 * creating a prefilled house form for the edit pages in myHouses
	 * @param house
	 */
	public HouseForm(House house) {
		this.name = house.getName();
		this.description = house.getDescription();
		this.location = house.getLocation().getAddressString();
		this.priceMoney = Money.of(house.getPrice().getNumber().doubleValue(),EURO);
		this.maxPerson = house.getMaxPerson();
		this.beds = house.getBeds();
		this.kitchen = house.getKitchen();
		this.bathrooms = house.getBathrooms();
		this.parkingSpot = house.getParkingSpot();
		this.handicappedAccessible = house.getHandicappedAccessible();
	}

	/**
	 * default empty house form constructor
	 */
	public HouseForm() {
		this.name = null;
		this.description = null;
		this.location = null;
		this.maxPerson = 0;
		this.beds = 0;
		this.kitchen = 0;
		this.bathrooms = 0;
		this.parkingSpot = false;
		this.handicappedAccessible = false;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public Money getPriceMoney() {
		return priceMoney;
	}

	public int getMaxPerson() {
		return maxPerson;
	}

	public int getBeds() {
		return beds;
	}

	public int getKitchen() {
		return kitchen;
	}

	public int getBathrooms() {
		return bathrooms;
	}

	public boolean getParkingSpot() {
		return parkingSpot;
	}

	public boolean getHandicappedAccessible() {
		return handicappedAccessible;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setPriceMoney(Money price) {
		this.priceMoney = price;
	}

	public void setMaxPerson(int max_person) {
		this.maxPerson = max_person;
	}

	public void setBeds(int beds) {
		this.beds = beds;
	}

	public void setKitchen(int kitchen) {
		this.kitchen = kitchen;
	}

	public void setBathrooms(int bathrooms) {
		this.bathrooms = bathrooms;
	}

	public void setParkingSpot(boolean parking_spot) {
		this.parkingSpot = parking_spot;
	}

	public void setHandicappedAccessible(boolean handicapped_accessible) {
		this.handicappedAccessible = handicapped_accessible;
	}


}
