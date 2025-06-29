package com.mcm.backend.app.api.utils.configurations;

import com.mcm.backend.app.api.utils.components.CurrentUserResolver;
import com.mcm.backend.app.api.utils.components.ValidatedBodyResolver;
import com.mcm.backend.app.api.utils.requestbody.RequestBodyUtilResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ValidatedBodyResolver validatedBodyResolver;
    private final CurrentUserResolver currentUserResolver;
    private final RequestBodyUtilResolver requestBodyUtilResolver;

    public WebConfig(ValidatedBodyResolver validatedBodyResolver, CurrentUserResolver currentUserResolver, RequestBodyUtilResolver requestBodyUtilResolver) {
        this.validatedBodyResolver = validatedBodyResolver;
        this.currentUserResolver = currentUserResolver;
        this.requestBodyUtilResolver = requestBodyUtilResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(validatedBodyResolver);
        resolvers.add(currentUserResolver);
        resolvers.add(requestBodyUtilResolver);
    }
}
