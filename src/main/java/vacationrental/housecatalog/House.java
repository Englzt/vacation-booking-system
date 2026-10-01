package vacationrental.housecatalog;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;
import vacationrental.location.Location;

import java.util.ArrayList;
import java.util.List;

@Entity
public class House extends Product {

	private UserAccount.UserAccountIdentifier landlordId;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String description;
	private Location location;
	private int maxPerson;
	private int beds;
	private int kitchen;
	private int bathrooms;
	private boolean parkingSpot;
	private boolean handicappedAccessible;
	private double averageRating;

	@ElementCollection
	private final List<String> imagePaths = new ArrayList<>();

	public UserAccount.UserAccountIdentifier getLandlordId() {
		return landlordId;
	}

	/**
	 * standard constructor to get an almost enpty house
	 * @param name
	 * @param price
	 * @param landlordId
	 */
	public House(String name, Money price, UserAccount.UserAccountIdentifier landlordId) {
		super(name, price);
		this.landlordId = landlordId;
		this.averageRating = 0.0;

	}

	/**
	 * default constructor
	 */
	public House() {
	}

	/**
	 * constructor to set up a full house with all parameters
	 * @param name
	 * @param price
	 * @param description
	 * @param location
	 * @param maxPerson
	 * @param beds
	 * @param kitchen
	 * @param bathrooms
	 * @param parkingSpot
	 * @param Accessible
	 * @param landlordId
	 */
	public House(String name,
				 Money price,
				 String description,
				 Location location,
				 int maxPerson,
				 int beds,
				 int kitchen,
				 int bathrooms,
				 boolean parkingSpot,
				 boolean Accessible,
				 UserAccount.UserAccountIdentifier landlordId) {

		super(name, price);

		this.description = (description != null && description.length() >= 255)
			? description.substring(0, 252) + "..."
			: description;
		this.maxPerson = maxPerson;
		this.beds = beds;
		this.kitchen = kitchen;
		this.bathrooms = bathrooms;
		this.parkingSpot = parkingSpot;
		this.handicappedAccessible = Accessible;

		this.location = location;
		this.landlordId = landlordId;
		this.averageRating = 0.0;
	}

	@Override
	public String toString() {
		return "House:  " +
			this.description + ", " +
			this.maxPerson + ", " +
			this.beds + ", " +
			this.kitchen + ", " +
			this.bathrooms + ", " +
			this.parkingSpot + ", " +
			this.handicappedAccessible;
	}


	public Location getLocation() {
		return location;
	}

	public String getDescription() {
		return description;
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

	public void setDescription(String description) {
		this.description = (description != null && description.length() >= 255)
			? description.substring(0, 252) + "..."
			: description;
	}

	public void setMaxPerson(int maxPerson) {
		this.maxPerson = maxPerson;
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

	public void setParkingSpot(boolean parkingSpot) {
		this.parkingSpot = parkingSpot;
	}

	public void setHandicappedAccessible(boolean accessibility) {
		this.handicappedAccessible = accessibility;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<String> getImagePaths(){
		return imagePaths;
	}

	/**
	 * adds a new imagePath
	 * @param imagePath
	 */
	public void addImagePath(String imagePath){
		this.imagePaths.add(imagePath);
	}

	/**
	 * removes a given imagePath
	 * @param imagePath
	 */
	public void removeImagePath(String imagePath){
		for (String path : this.imagePaths) {
			if (path.equals(imagePath) || path.contains(imagePath)) {
				this.imagePaths.remove(path);
				break;
			}
		}
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}
	public double getAverageRating() {
		return averageRating;
	}
}
