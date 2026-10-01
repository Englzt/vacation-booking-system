package vacationrental.housecatalog;

import jakarta.validation.Valid;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vacationrental.account.UserManagement;
import vacationrental.booking.BookingManagement;
import vacationrental.comments.CommentForm;
import vacationrental.comments.CommentManagement;
import vacationrental.comments.CommentType;
import vacationrental.eventcatalog.EventManagement;
import vacationrental.util.TimeManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.salespointframework.core.Currencies.EURO;

@Controller
public class HouseController {

	private final HouseManagement houseManagement;
	private final EventManagement eventManagement;
	private final BookingManagement bookingManagement;
	private final CommentManagement commentManagement;
	private final UserManagement userManagement;

	public HouseController(HouseManagement houseManagement,
						   EventManagement eventManagement,
						   BookingManagement bookingManagement,
						   CommentManagement commentManagement,
						   UserManagement userManagement){
		this.bookingManagement = bookingManagement;
		this.houseManagement = houseManagement;
		this.eventManagement = eventManagement;
		this.commentManagement = commentManagement;
		this.userManagement = userManagement;
	}

	/**
	 * method to display the full house catalog
	 * @param model
	 * @return
	 */
	@GetMapping("/houses")
	public String showHouseCatalog(Model model){
		model.addAttribute("allHouses", houseManagement.findAll());

		model.addAttribute("currentPath", "/houses");
		return "house/houses";
	}

	/**
	 * method for the starting page
	 * @param model
	 * @return
	 */
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("allHouses", houseManagement.findAll());

		model.addAttribute("currentPath", "/houses");
		return "redirect:/houses";
	}


	/**
	 * displays one chosen house with all its details
	 * @param model
	 * @param id
	 * @param eventStartDate
	 * @param eventEndDate
	 * @param userAccount
	 * @return
	 */
	@GetMapping("/house/{id}")
	public String showHouse(Model model, @PathVariable Product.ProductIdentifier id,
							@RequestParam(required = false) LocalDateTime eventStartDate,
							@RequestParam(required = false) LocalDateTime eventEndDate,
							@LoggedIn Optional<UserAccount> userAccount){
		Optional<House> house = houseManagement.findById(id);
		UserAccount account = userAccount.orElse(null);
		if(house.isEmpty()){
			return "redirect:/houses";
		}

		if(eventStartDate == null || eventEndDate == null){
			model.addAttribute("eventStartDate", null);
			model.addAttribute("eventEndDate", null);
		}else{
			model.addAttribute("eventStartDate", eventStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			model.addAttribute("eventEndDate", eventEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

		}

		if (!model.containsAttribute("dateError")) {
			model.addAttribute("dateError", null);
		}
		model.addAttribute("house", house.get());
		model.addAttribute("images", house.get().getImagePaths());
		model.addAttribute("allEvents", bookingManagement.getAllAcceptedEventsForHouse(house.get().getId()));

		Map<String, String> dates = bookingManagement.getCalendarData(id);
		model.addAttribute("dates", dates);
		model.addAttribute("today", TimeManager.getTime());
		model.addAttribute("Landlord",
			userManagement.getUserAccountManagement().get(house.get().getLandlordId()).get().getUsername());

		model.addAttribute("LandlordImagePath", userManagement.findByUsername(
			userManagement.getUserAccountManagement().get(house.get().getLandlordId()).get().getUsername()
		).get().getImagePath());

		model.addAttribute("comments", commentManagement.findByProductId(id));
		model.addAttribute("commentForm", new CommentForm());
		model.addAttribute("houseId", id);
		model.addAttribute("customerAllowedToComment",
			bookingManagement.customerAllowedToComment(account, id, CommentType.HOUSECOMMENT));
		if(account != null){
			model.addAttribute("userIsOwner", house.get().getLandlordId().equals(account.getId()));
		}

		return "house/houseDetails";
	}


	/**
	 * displays all houses a landlord has created
	 * @param model
	 * @param userAccount
	 * @return
	 */
	@GetMapping("/myHouses")
	@PreAuthorize("hasRole('Landlord')")
	public String showMyHouses(Model model, @LoggedIn Optional<UserAccount> userAccount){
		model.addAttribute("allHouses", houseManagement.findByLandlordId(userAccount.get().getId()));


		model.addAttribute("currentPath", "/myHouses");
		return "house/myHouses";
	}

	/**
	 * displaying the edit page to a selected house in myHouses
	 * @param model
	 * @param id
	 * @return
	 */
	@GetMapping("/myHouses/edit/{id}")
	@PreAuthorize("hasRole('Landlord')")
	public String showEditForm(Model model, @PathVariable Product.ProductIdentifier id){
		Optional<House> optionalHouse = houseManagement.findById(id);
		if(optionalHouse.isEmpty()){
			return "redirect:/myHouses";
		}

		model.addAttribute("house", optionalHouse.get());
		model.addAttribute("houseForm", new HouseForm(optionalHouse.get()));
		model.addAttribute("images", optionalHouse.get().getImagePaths());
		return "house/editHouse";
	}

	/**
	 * edits the selected house after filling in the editForm
	 * @param model
	 * @param houseForm
	 * @param result
	 * @param id
	 * @param files
	 * @param priceValue
	 * @return
	 */
	@PostMapping("/myHouses/edit/{id}")
	@PreAuthorize("hasRole('Landlord')")
	public String editHouse(Model model, @Valid @ModelAttribute HouseForm houseForm, BindingResult result,
							@PathVariable Product.ProductIdentifier id, @RequestParam("files") MultipartFile[] files,
							@RequestParam("price_money_input") double priceValue){
		houseForm.setPriceMoney(Money.of(priceValue, EURO));

		Optional<House> optionalHouse = houseManagement.findById(id);
		if(result.hasErrors()){
			model.addAttribute("house", optionalHouse.get());
			return "house/editHouse";
		}

		houseManagement.addImages(id, files);

		houseManagement.updateHouse(id, houseForm);

		return "redirect:/myHouses";
	}

	/**
	 * called if teh landlord cancels the edit operation
	 * @param id
	 * @return
	 */
	@GetMapping("/cancelEditHouse/{id}")
	@PreAuthorize("hasRole('Landlord')")
	public String cancelEditHouse(@PathVariable Product.ProductIdentifier id){
		houseManagement.addDefaultImageIfNone(id);
		return "redirect:/myHouses";
	}

	/**
	 * displaying the page to delete a selected house
	 * @param model
	 * @param id
	 * @return
	 */
	@GetMapping("/myHouses/delete/{id}")
	@PreAuthorize("hasRole('Landlord')")
	public String showDeleteHouse(Model model, @PathVariable Product.ProductIdentifier id){
		Optional<House> optionalHouse = houseManagement.findById(id);
		if(optionalHouse.isEmpty()){
			return "redirect:/myHouses";
		}
		model.addAttribute("house", optionalHouse.get());
		return "house/deleteHouse";
	}

	/**
	 * deleting a selected house after confirmation
	 * @param id
	 * @return
	 */
	@PostMapping("/myHouses/delete/{id}")
	@PreAuthorize("hasRole('Landlord')")
	public String deleteHouse(@PathVariable Product.ProductIdentifier id){
		bookingManagement.deleteBookingsByHouseId(id);
		houseManagement.deleteHouse(id);
		return "redirect:/myHouses";
	}

	/**
	 * displaying the empty houseForm to add a new house
	 * @param model
	 * @param houseForm
	 * @return
	 */
	@GetMapping("/myHouses/addHouse")
	@PreAuthorize("hasRole('Landlord')")
	public String showAddHouseForm(Model model, @ModelAttribute(binding = false) HouseForm houseForm){
		model.addAttribute("houseForm", houseForm);
		return "house/addHouse";
	}

	/**
	 * adds a new house to the house catalog with the given parameters
	 * @param houseForm
	 * @param result
	 * @param model
	 * @param userAccount
	 * @param files
	 * @param priceInput
	 * @return
	 */
	@PostMapping("/myHouses/addHouse")
	@PreAuthorize("hasRole('Landlord')")
	public String addHouse(@ModelAttribute HouseForm houseForm, BindingResult result, Model model,
						   @LoggedIn Optional<UserAccount> userAccount,
						   @RequestParam("files") MultipartFile[] files,
						   @RequestParam("price_money_input") String priceInput) {
		try {
			double priceValue = Double.parseDouble(priceInput);
			houseForm.setPriceMoney(Money.of(priceValue, EURO));
		} catch (NumberFormatException e) {
			result.rejectValue("price_money", "invalid.format", "Ungültiges Preisformat.");
		}

		if (result.hasErrors()) {
			return "house/addHouse";
		}

		House house = houseManagement.addHouse(houseForm, userAccount.get().getId());
		houseManagement.addImages(house.getId(), files);

		return "redirect:/house/" + house.getId();
	}


	/**
	 * is called if a landlord deletes an image of a selected house
	 * @param id
	 * @param type
	 * @param entityId
	 * @return
	 */
	@GetMapping("/deleteImage/{id}")
	@PreAuthorize("!hasRole('Admin')")
	public String deleteImage(@PathVariable String id,
							  @RequestParam String type, @RequestParam Product.ProductIdentifier entityId){
		if (type.equals("house")) {
			houseManagement.deleteImageFromHouse(entityId, id);
		} else if (type.contains("event")) {
			eventManagement.deleteImageFromEvent(entityId, id);
		}

		String redirect = "/";
		if (type.equals("house")) {
			redirect = "redirect:/myHouses/edit/" + entityId;
		} else if (type.contains("event")) {
			if (type.contains("big")) {
				redirect = "redirect:/editBigEvent/" + entityId;
			} else {
				redirect = "redirect:/editSmallEvent/" + entityId;
			}
		}

		return redirect;
	}

	/**
	 * is called if a landlord wants to change the order of the pictures of a selected house
	 * @param id
	 * @param type
	 * @param entityId
	 * @param direction
	 * @return
	 */
	@GetMapping("/changeImageOrder/{id}")
	@PreAuthorize("!hasRole('Admin')")
	public String changeImageOrder(@PathVariable String id,
								   @RequestParam String type, @RequestParam Product.ProductIdentifier entityId, @RequestParam String direction){

		if (type.equals("house")) {
			houseManagement.changeImageOrder(entityId, id, direction);
		}

		String redirect = "/";
		if (type.equals("house")) {
			redirect = "redirect:/myHouses/edit/" + entityId;
		} else if (type.contains("event")) {
			if (type.contains("big")) {
				redirect = "redirect:/editBigEvent/" + entityId;
			} else {
				redirect = "redirect:/editSmallEvent/" + entityId;
			}
		}

		return redirect;
	}

}
