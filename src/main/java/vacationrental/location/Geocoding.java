package vacationrental.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;


import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Get coordinates for an address from HTTP.
 * https://github.com/megatomAA/OpenStreetMap/tree/master
 */

public class Geocoding {

	private String address;
	private String format = "json";
	private String url = "https://nominatim.openstreetmap.org/search?q=${address}&format=${format}";

	public Geocoding() {
		/* empty because of spring */
	}

	/**
	 * Get the address
	 *
	 * @return address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Set the address
	 *
	 * @param address the address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Get HTTP response format
	 *
	 * @return format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Set HTTP response format
	 *
	 * @param format format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Coordinates[] request() {
		Coordinates[] coords;
		String url = getUrl();
		try {
			url = url.replace("${address}", URLEncoder.encode(getAddress(), StandardCharsets.UTF_8));
			url = url.replace("${format}", getFormat());

			URI objUri = new URI(url);
			URL obj = objUri.toURL();
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder response = new StringBuilder();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				JSONArray json = new JSONArray(response.toString());
				if (json.isEmpty()) {
					return null;
				}

				coords = new Coordinates[json.length()];
				for (int i = 0; i < json.length(); i++) {
					JSONObject jsonObj = json.getJSONObject(i);

					Coordinates coord = new Coordinates();
					coord.setLatitude(Float.parseFloat(jsonObj.getString("lat")));
					coord.setLongitude(Float.parseFloat(jsonObj.getString("lon")));
					coord.setAddressLabel(jsonObj.getString("display_name"));

					coords[i] = coord;
				}
				return coords;
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
