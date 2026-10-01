package vacationrental.util;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;

/**
 * Controller for the time management. Handles requests for forwarding the simulated time. For debug purposes only.
 */
@Controller
class TimeController {

	TimeManager timeManager;
	TimeController(TimeManager timeManager) {
		this.timeManager = timeManager;
	}

	/**
	 * Forward the time by the given amount of days and redirect to the origin page.
	 * @param model {@link Model}
	 * @param amount {@link String}
	 * @param origin_type {@link String}
	 * @param origin_id {@link String}
	 * @return String
	 */
	@GetMapping("/forward")
	String forward(Model model, @RequestParam String amount,
				   @RequestParam String origin_type, @RequestParam String origin_id) {
		timeManager.forward(Duration.ofDays(Integer.parseInt(amount)));
		if (origin_id == null || origin_id.equals("null")) {
			return "redirect:/" + origin_type;
		}
		return "redirect:/" + origin_type + "/" + origin_id;
	}

}
