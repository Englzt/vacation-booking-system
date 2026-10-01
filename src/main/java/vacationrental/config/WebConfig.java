package vacationrental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * At start of program a upload directory is created where uploaded product images are stored in it.
 * Also adds notificationinterceptor to the interceptor registry.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Value("${upload.directory}")
	private String uploadDirectory;
	
	@Autowired
	private NotificationInterceptor notificationInterceptor;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/**")
			.addResourceLocations("file:" + uploadDirectory + "/");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(notificationInterceptor);
	}
}