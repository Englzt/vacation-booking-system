package vacationrental.util;

import org.salespointframework.time.BusinessTime;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TimeManager {

	private static BusinessTime businessTime = null;

	TimeManager(BusinessTime businessTime) {
		TimeManager.businessTime = businessTime;
	}

	/**
	 * Get the current business time.
	 * @return {@link LocalDateTime}
	 */
	public static LocalDateTime getTime() {
		return businessTime.getTime();
	}

	/**
	 * Forward the business time by the given duration.
	 * @param duration {@link Duration}
	 */
	public void forward(Duration duration) {
		businessTime.forward(duration);
	}
}
