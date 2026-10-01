package vacationrental.location;

import jakarta.persistence.Embeddable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
public class Location {
	private String street;
	private String houseNumber;
	private String postalCode;
	private String city;
	private String country;
	private double longitude = 0.0;
	private double latitude = 0.0;

	/**
	 * Constructor for creating a location from individual address components.
	 * @param street {@link String}
	 * @param houseNumber {@link String}
	 * @param postalCode {@link String}
	 * @param city {@link String}
	 * @param country {@link String}
	 */
	public Location(String street, String houseNumber, String postalCode, String city, String country) {
		this.street = street;
		this.houseNumber = houseNumber;
		this.postalCode = postalCode;
		this.city = city;
		this.country = country;
		getCoordinates();
	}

	/**
	 * Constructor for creating a location from an full address string.
	 * @param address {@link String}
	 */
	public Location(String address) {
		String regex = "^(.+?)\\s+([A-Za-z0-9]+),\\s+(\\d{5})\\s+(.+?),\\s+(.+?)$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(address);

		if (matcher.matches()) {
			this.street = matcher.group(1);
			this.houseNumber = matcher.group(2);
			this.postalCode = matcher.group(3);
			this.city = matcher.group(4);
			this.country = matcher.group(5);
		}

		getCoordinates();
	}

	protected Location() {
	}

	/**
	 * Get the coordinates of the location using the Geocoding API.
	 */
	private void getCoordinates() {

		Geocoding geocoding = new Geocoding();
		geocoding.setAddress(getAddressString());

		Coordinates[] coords;
		coords = geocoding.request();
		if (coords == null) {
			return;
		}
		for (Coordinates coord : coords) {
			this.longitude = coord.getLongitude();
			this.latitude = coord.getLatitude();
		}

	}

	/**
	 * Get the address as a string.
	 * @return {@link String}
	 */
	public String getAddressString() {
		return street + " " + houseNumber + ", " + postalCode + " " + city + ", " + country;
	}

	/**
	 * Get the street and city of the location.
	 * @return {@link String}
	 */
	public String getStreetCity() {
		return street + ", " + city;
	}

	/**
	 * Get the street, house number, and city of the location.
	 * @return {@link String}
	 */
	public String getStreetCityNumber() {
		return street + " " + houseNumber + ", " + city;
	}

	/**
	 * Get the OSM url for the location.
	 * @return {@link String}
	 */
	public String getUrl() {
		String url = "https://www.openstreetmap.org/export/embed.html?bbox=13.601589202880861%2C51." +
			"00025294523934%2C13.888263702392578%2C51.094358987187135&amp;layer=mapnik";

		if (Double.compare(latitude, 0.0) != 0 && Double.compare(longitude, 0.0) != 0) {
			url += "&marker=" + latitude + "%2C" + longitude;
		}

		return url;
	}

	/**
	 * Calculate the distance between two locations.
	 * @param location {@link Location}
	 * @return {@link Double}
	 */
	public double distanceTo(Location location) {
		// result in meters
		double radius = 6371000;
		double deltaX = Math.toRadians(location.longitude - longitude) *
			Math.cos(Math.toRadians((latitude + location.latitude) / 2));
		double deltaY = Math.toRadians(location.latitude - latitude);
		return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) * radius;
	}

	/**
	 * Get the latitude of the location.
	 * @return {@link Double}
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the longitude of the location.
	 * @return {@link Double}
	 */
	public double getLongitude() {
		return longitude;
	}
}