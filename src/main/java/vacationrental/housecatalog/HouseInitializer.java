package vacationrental.housecatalog;


import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import vacationrental.account.UserManagement;
import vacationrental.location.Location;

import static org.salespointframework.core.Currencies.EURO;

// HouseCatalogInitializer
@Component
@Order(2)
public class HouseInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(HouseInitializer.class);

	private final HouseManagement management;

	private final UserManagement userManagement;

	HouseInitializer(HouseManagement management, UserManagement userManagement) {
		this.userManagement = userManagement;
		Assert.notNull(management, "Managment must not be null");
		this.management = management;
	}


	/**
	 * initializing some predefined houses to save time
	 */
	@Override
	public void initialize() {
		if (management.findAll().iterator().hasNext()) {
			return;
		}

		LOG.info("Creating default houses.");

		UserAccount.UserAccountIdentifier userId = userManagement.findByUsername("Landlord").get().getUserAccount().getId();
		UserAccount.UserAccountIdentifier userTestId = userManagement.findByUsername("myLandlord")
			.get().getUserAccount().getId();

		Location location1 = new Location("Moritzburg", "5", "01468", "Moritzburg", "Germany");
		Location location2 = new Location("Elbtalstraße", "3", "01662", "Meißen", "Germany");

		House h1 = management.addHouse(new House(
			"Woodland Cottage",
			Money.of(220, EURO),
			"Entfliehen Sie dem Alltag im 'Woodland Cottage' in der Nähe von Moritzburg. " +
				"Platz für bis zu 6 Personen, Wanderungen im Wald und entspannte Abende am Kamin erwarten Sie.",
			location1, 6, 3, 2, 2, true, true, userId
		));
		h1.addImagePath("/img/product/woodland_cottage_1.jpg");
		h1.addImagePath("/img/product/woodland_cottage_2.jpg");
		h1.addImagePath("/img/product/woodland_cottage_3.jpg");
		h1.addImagePath("/img/product/woodland_cottage_4.jpg");
		h1.addImagePath("/img/product/woodland_cottage_5.jpg");

		House h2 = management.addHouse(new House(
			"Elb Valley Retreat",
			Money.of(180, EURO),
			"Dieses Ferienhaus oberhalb des Elbtals bietet Blick auf die Weinberge von Meißen, " +
				"drei Schlafzimmer, einen Garten und eine Terrasse. Ideal für Familien.",
			location2, 5, 3, 1, 2, true, false, userId
		));
		h2.addImagePath("/img/product/elb_valley_1.jpeg");
		h2.addImagePath("/img/product/elb_valley_2.jpeg");
		h2.addImagePath("/img/product/elb_valley_3.jpeg");
		h2.addImagePath("/img/product/elb_valley_4.jpeg");
		h2.addImagePath("/img/product/elb_valley_5.jpeg");



		// TestHouses
		Location location3 = new Location("Radebeul", "12a", "01445", "Radebeul", "Germany");
		Location location4 = new Location("Sächsische Schweiz", "99", "01814", "Bad Schandau", "Germany");

		House t1 = management.addHouse(new House(
			"Historic Vineyard House",
			Money.of(250, EURO),
			"Dieses historische Haus in den Weinbergen von Radebeul bietet Tradition, " +
				"Komfort und einen großen Garten mit Sitzgelegenheiten. Perfekt für Weinliebhaber.",
			location3, 4, 2, 2, 1, true, true, userTestId
		));

		House t2 = management.addHouse(new House(
			"Mountain View Lodge",
			Money.of(300, EURO),
			"Diese Lodge in der Sächsischen Schweiz bietet eine spektakuläre Aussicht auf Sandsteinfelsen, " +
				"Platz für 8 Personen, einen Kamin und eine Sauna.",
			location4, 8, 4, 3, 3, true, true, userTestId
		));

		House t3 = management.addHouse(new House(
			"Riverfront Cabin",
			Money.of(200, EURO),
			"Direkt an der Elbe gelegen, bietet diese Hütte eine unvergessliche Aussicht und Zugang zum Fluss. " +
				"Perfekt für Paare oder kleine Familien.",
			location4, 3, 2, 1, 1, true, false, userTestId
		));


		t1.addImagePath("/img/product/historic_vineyard_1.jpeg");
		t1.addImagePath("/img/product/historic_vineyard_2.jpeg");
		t1.addImagePath("/img/product/historic_vineyard_3.jpeg");
		t1.addImagePath("/img/product/historic_vineyard_4.jpeg");
		t1.addImagePath("/img/product/historic_vineyard_5.jpeg");

		t2.addImagePath("/img/product/mountain_view_1.jpeg");
		t2.addImagePath("/img/product/mountain_view_2.jpeg");
		t2.addImagePath("/img/product/mountain_view_3.jpeg");
		t2.addImagePath("/img/product/mountain_view_4.jpeg");
		t2.addImagePath("/img/product/mountain_view_5.jpeg");

		t3.addImagePath("/img/product/riverfront_1.jpeg");
		t3.addImagePath("/img/product/riverfront_2.jpeg");
		t3.addImagePath("/img/product/riverfront_3.jpeg");
		t3.addImagePath("/img/product/riverfront_4.jpeg");
		t3.addImagePath("/img/product/riverfront_5.jpeg");
	}
}

