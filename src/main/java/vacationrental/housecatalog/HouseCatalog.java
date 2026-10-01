package vacationrental.housecatalog; 

import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;

import java.util.Optional;

public interface HouseCatalog extends Catalog<House> {
    static final Sort DEFAULT_SORT = Sort.sort(House.class).by(House::getId).descending();

    Streamable<House> findByDescription(String description, Sort sort);
    default Streamable<House> findByDescription(String description){
        return findByDescription(description, DEFAULT_SORT);
    }

    Optional<House> findById(Product.ProductIdentifier id, Sort sort);

    default Optional<House> findById(Product.ProductIdentifier id){
        return findById(id, DEFAULT_SORT);
    }

	Streamable<House> findByLandlordId_UserAccountId(String userAccountId);
}

