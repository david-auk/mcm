package com.mcm.backend.app.routes.utils.configurations;

import com.mcm.backend.app.routes.utils.components.ValidatedBodyResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ValidatedBodyResolver validatedBodyResolver;

    public WebConfig(ValidatedBodyResolver validatedBodyResolver) {
        this.validatedBodyResolver = validatedBodyResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(validatedBodyResolver);
    }
}
