package com.mcm.backend.app.api.utils.components;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null
                && TableEntity.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;
        UUID userId = (UUID) request.getAttribute("authenticatedUserId");

        if (userId == null) {
            throw new JsonErrorResponseException("No authentication bearer/info found");
        }

        @SuppressWarnings("unchecked")
        Class<? extends TableEntity> entityClass = (Class<? extends TableEntity>) parameter.getParameterType();

        try (DAO<? extends TableEntity, UUID> dao = DAOFactory.createDAO(entityClass)) {
            TableEntity entity = dao.get(userId);
            if (entity == null) throw new SecurityException("User not found");
            return entity;
        }
    }
}