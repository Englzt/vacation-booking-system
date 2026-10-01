package vacationrental.housecatalog;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode;

/**
 * Module tests for catalog.
 *
 * 
 */
@ApplicationModuleTest(mode = BootstrapMode.DIRECT_DEPENDENCIES)
class HouseCatalogIntegrationTests {

	@Autowired HouseCatalog catalog;
	@Autowired ConfigurableApplicationContext context;

}