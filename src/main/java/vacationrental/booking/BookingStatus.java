package vacationrental.booking;

import org.salespointframework.order.OrderStatus;

public enum BookingStatus {
	OPEN(OrderStatus.OPEN),
	RESERVED(null),
	PAID(OrderStatus.PAID),
	COMPLETED(OrderStatus.COMPLETED),
	CANCELED(OrderStatus.CANCELED);

	private final OrderStatus baseStatus;

	BookingStatus(OrderStatus baseStatus) {
		this.baseStatus = baseStatus;
	}

}
