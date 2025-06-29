package it.uniroma3.siw_events.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/event_images/**")
                .addResourceLocations("file:uploads/event_images/");
        registry.addResourceHandler("/uploads/profile_images/**")
                .addResourceLocations("file:uploads/profile_images/");
        registry.addResourceHandler("/uploads/event_posted_images/**")
                .addResourceLocations("file:uploads/event_posted_images/");
    }
}
