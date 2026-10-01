package vacationrental.location;

import org.json.JSONObject;
/**
 * GPS coordinates : latitude and longitude.
 * https://github.com/megatomAA/OpenStreetMap/tree/master
 */
public class Coordinates {
	
	private float latitude;
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	private float longitude;
	
	private String addressLabel = "";
	
	public Coordinates(float latitude, float longitude) {
		super();
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	public Coordinates() {
	}

	public String toString() {
		return getLatitude() + "-" + getLongitude();
	}
    public String getAddressLabel() {
        return addressLabel;
    }
    public void setAddressLabel(String addressLabel) {
        this.addressLabel = addressLabel;
    }
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("display_name", getAddressLabel());
        json.put("lat", getLatitude());
        json.put("lon", getLongitude());
        return json;
    }

}
