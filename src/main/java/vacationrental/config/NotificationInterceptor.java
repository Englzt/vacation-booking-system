package vacationrental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import vacationrental.messages.MessageManagement;
import vacationrental.messages.MessageStatus;
import vacationrental.account.User;
import vacationrental.account.UserManagement;

import java.util.Optional;

@Component
public class NotificationInterceptor implements HandlerInterceptor {

	private final MessageManagement messageManagement;
	private final UserManagement userManagement;

	public NotificationInterceptor(MessageManagement messageManagement, UserManagement userManagement) {
		this.messageManagement = messageManagement;
		this.userManagement = userManagement;
	}

	/**
	 * This function is called after the request has been handled by the controller to get the amount of unread messages
	 * and add it to the model, to avoid having to this in every controller.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler (or {@link Object}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param modelAndView the {@code ModelAndView} that the handler returned
	 * (can also be {@code null})
	 * @throws Exception
	 */
	@Override
	public void postHandle(HttpServletRequest request,
						   HttpServletResponse response,
						   Object handler,
						   ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (request.getUserPrincipal() == null || !authentication.isAuthenticated()) {
				return;
			}

			String username = request.getUserPrincipal().getName();
			Optional<User> user = userManagement.findByUsername(username);
			Optional<UserAccount> userAccount = user.map(User::getUserAccount);

			int unreadMessagesCount = messageManagement.findByUser_UserAccountIdAndStatus(
				String.valueOf(userAccount.get().getId()),
				MessageStatus.UNREAD).toList().size();

			modelAndView.addObject("unreadMessagesCount", unreadMessagesCount);
		}
	}
}
