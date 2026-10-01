package vacationrental.messages;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

public interface MessageRepository extends CrudRepository<Message, Long> {


	Streamable<Message> findByUserId_UserAccountId(String userAccountId);

	Streamable<Message> findByUserId_UserAccountIdAndStatus(String userAccountId, MessageStatus status, Sort sort);

}
