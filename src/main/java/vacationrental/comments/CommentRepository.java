package vacationrental.comments;

import org.salespointframework.catalog.Product;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends CrudRepository<Comment, UUID> {


	List<Comment> findByProductId(Product.ProductIdentifier productId);

	List<Comment> findByUserIdAndProductId(UserAccount.UserAccountIdentifier userId, Product.ProductIdentifier productId);

}
