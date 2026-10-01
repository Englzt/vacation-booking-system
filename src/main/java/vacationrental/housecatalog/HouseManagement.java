package vacationrental.housecatalog;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vacationrental.eventcatalog.Event;
import vacationrental.location.Location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Service
@Transactional
public class HouseManagement {

    private final HouseCatalog catalog;

	private final UserAccountManagement userAccountManagement;
	private final HouseCatalog houseCatalog;

	@Value("${upload.directory}")
	private Path uploadDirectory;

	/**
	 * Initialize the upload directory.
	 *
	 * @throws IOException if the directory could not be created.
	 */
	@PostConstruct
	public void init() throws IOException {
		uploadDirectory = Paths.get(System.getProperty("user.home"), "uploads");
		Files.createDirectories(uploadDirectory);
	}

	HouseManagement(HouseCatalog catalog, UserAccountManagement userAccountManagement, HouseCatalog houseCatalog) {
        this.catalog = catalog;
		this.userAccountManagement = userAccountManagement;
		this.houseCatalog = houseCatalog;
	}

	/**
	 * adding a house with the given house form to the overview of the given landlord
	 * @param houseForm
	 * @param landlord_id
	 * @return
	 */
	public House addHouse(HouseForm houseForm, UserAccount.UserAccountIdentifier landlord_id){
        ProductIdentifier id;


        
        String name = houseForm.getName();
		Location location = new Location(houseForm.getLocation());
        Money price = houseForm.getPriceMoney();
        String description = houseForm.getDescription();
        int maxPerson = houseForm.getMaxPerson();
        int beds = houseForm.getBeds();
        int kitchen = houseForm.getKitchen();
        int bathrooms = houseForm.getBathrooms();
        boolean parkingSpot = houseForm.getParkingSpot();
        boolean handicappedAccessible = houseForm.getHandicappedAccessible();

        return catalog.save(new House(name, price, description, location, maxPerson, beds, kitchen, bathrooms,
			parkingSpot, handicappedAccessible, landlord_id));
    }

	public House addHouse(House house){
		return catalog.save(house);
	}

    public House updateHouse(ProductIdentifier id, HouseForm houseForm){
        // Setter der einzelen geändereten paramerter
        Optional<House> item_optional = catalog.findById(id);

		if(item_optional.isEmpty()){
			return null;
		}
		House item = item_optional.get();

        item.setName(houseForm.getName());
		item.setDescription(houseForm.getDescription());
		item.setMaxPerson(houseForm.getMaxPerson());
		item.setBeds(houseForm.getBeds());
		item.setKitchen(houseForm.getKitchen());
		item.setBathrooms(houseForm.getBathrooms());
		item.setParkingSpot(houseForm.getParkingSpot());
		item.setHandicappedAccessible(houseForm.getHandicappedAccessible());
		item.setPrice(houseForm.getPriceMoney());
		item.setLocation(new Location(houseForm.getLocation()));

        return item;
    }


	/**
	 * deleting a house with the given id
	 * @param id
	 * @return
	 */
    public boolean deleteHouse(Product.ProductIdentifier id){
        Optional<House> delHouse = catalog.findById(id);
		if(delHouse.isEmpty()){
			return false;
		}
		catalog.delete(delHouse.get());
        return true;
    }


    public Optional<House> findById(ProductIdentifier id){
        return catalog.findById(id);
    }
    
    public Streamable<House> findByName(String name){
        return catalog.findByName(name);
    }

    public Streamable<House> findAll(){
        return catalog.findAll();
    }

	/**
	 * returning the name of the landlord associated to the given house_id as String
	 * @param id
	 * @return
	 */
	public String getLandlordName(ProductIdentifier id){
		House house = catalog.findById(id).get();
		UserAccount user = userAccountManagement.get(house.getLandlordId()).get();
		return user.getUsername();
	}


	/**
	 * returning a Streamable of Pairs matching all existing houses to their specific name of the landlord for better overview in the frontend
	 * @return
	 */
	public Streamable<Pair<House, String>> getAllHousesWithLandlordNames(){
		Streamable<House> houses = findAll();
		ArrayList<Pair<House, String>> housesLandlords = new ArrayList<>();

		for (House house: houses){
			String name = userAccountManagement.get(house.getLandlordId()).get().getUsername();
			housesLandlords.add(Pair.of(house, name));
		}

		return Streamable.of(housesLandlords);
	}

	public Streamable<House> findByLandlordId(UserAccount.UserAccountIdentifier user_id){
		return catalog.findByLandlordId_UserAccountId(user_id.toString());
	}

	/**
	 * Adds an image to a house's list of image paths.
	 *
	 * @param houseId the id of the house
	 * @param path the path to the image
	 */
	private void addImageToHouse(ProductIdentifier houseId, String path) {
		Optional<House> house = catalog.findById(houseId);
		if (house.isPresent()) {
			house.get().addImagePath(path);
			catalog.save(house.get());
		}
	}

	/**
	 * Delete an image from an {@link House}.
	 *
	 * @param id ID of the {@link House}.
	 * @param imageUrl String that contains the path where the image is stored.
	 */
    public void deleteImageFromHouse(ProductIdentifier id, String imageUrl) {
		Optional<House> house = catalog.findById(id);
		if (house.isPresent()) {
			house.get().removeImagePath(imageUrl);
			if (house.get().getImagePaths().isEmpty()) {
				house.get().addImagePath("/img/product/marvel.png");
			}
			catalog.save(house.get());
		}
    }

	/**
	 * Add images to an {@link House}.
	 *
	 * @param houseId ID of the {@link House}.
	 * @param files Array of MultipartFiles that contain the images to add.
	 */
	public void addImages(Product.ProductIdentifier houseId, MultipartFile[] files) {
		House house = catalog.findById(houseId).get();

		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}

			if (house.getImagePaths().size() >= 5) {
				break;
			}

			String fileName = UUID.randomUUID().toString();

			if ("image/jpeg".equals(file.getContentType()) || "image/jpg".equals(file.getContentType())) {
				fileName += ".jpg";
			} else if ("image/png".equals(file.getContentType())) {
				fileName += ".png";
			} else if ("image/webp".equals(file.getContentType())) {
				fileName += ".webp";
			} else {
				continue;
			}

			Path fileNameAndPath = uploadDirectory.resolve(fileName);

			try {
				Files.write(fileNameAndPath, file.getBytes());
				String imageUrl = "/images/" + fileName;
				addImageToHouse(houseId, imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* add default image if no image is set */
		if (house.getImagePaths().isEmpty()) {
			house.addImagePath("/img/product/marvel.png");
		}
	}

	/**
	 * Get all houses of a landlord.
	 *
	 * @param userAccount the landlord
	 * @return a streamable of houses
	 */
	public Streamable<House> getHousesByLandlordId(UserAccount userAccount) {
		return catalog.findByLandlordId_UserAccountId(userAccount.getId().toString());
	}

	/**
	 * Deletes all images in the upload directory when the application is shut down.
	 */
	@PreDestroy
	public void deleteImages() {
		File directory = new File(String.valueOf(uploadDirectory));
		if (directory.exists() && directory.isDirectory()) {
			for (File file : Objects.requireNonNull(directory.listFiles())) {
				if (!file.isDirectory()) {
					file.delete();
				}
			}
		}
	}


	/**
	 * returns all Houses within the given radius around a given location
	 * @param radius
	 * @param location
	 * @return
	 */
	public List<House> getAllNearbyHousesToLocation(int radius, Location location) {
		Streamable<House> allHouses = findAll();
		List<House> nearbyHouses = new ArrayList<>();
		for (House house : allHouses) {
			if(radius >= location.distanceTo(house.getLocation())) {
				nearbyHouses.add(house);
			}
		}
		return nearbyHouses;
	}


	/**
	 * updating the average Rating for the house and save the update
	 * @param houseId
	 * @param averageRating
	 * @return
	 */
	public House updateAverageRating(ProductIdentifier houseId, double averageRating) {
		House house = findById(houseId).get();
		house.setAverageRating(averageRating);
		return catalog.save(house);
	}

	/**
	 * is called if the landlord wants to change the order of some images of its house
	 * @param entityId
	 * @param id
	 * @param direction
	 */
	public void changeImageOrder(ProductIdentifier entityId, String id, String direction) {

		Optional<House> house = catalog.findById(entityId);
		if (house.isEmpty()) {
			return;
		}

		List<String> imagePaths = house.get().getImagePaths();
		int index = -1;
		for (int i = 0; i < house.get().getImagePaths().size(); i++) {
			if (house.get().getImagePaths().get(i).contains(id)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			return;
		}

		if ("up".equals(direction) && index > 0) {
			String prev = imagePaths.get(index - 1);
			imagePaths.set(index - 1, imagePaths.get(index));
			imagePaths.set(index, prev);
		} else if ("down".equals(direction) && index < imagePaths.size() - 1) {
			String next = imagePaths.get(index + 1);
			imagePaths.set(index + 1, imagePaths.get(index));
			imagePaths.set(index, next);
		}
		catalog.save(house.get());
	}

	/**
	 * adding a default house image if nothing was uploaded
	 * @param houseId
	 */
	public void addDefaultImageIfNone(ProductIdentifier houseId) {
		Optional<House> house = catalog.findById(houseId);
		if (house.isPresent() && house.get().getImagePaths().isEmpty()) {
			house.get().addImagePath("/img/product/marvel.png");
			catalog.save(house.get());
		}
	}
}
