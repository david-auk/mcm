package com.mcm.backend.app.api.utils.components;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.security.SecurityContextUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter p) {
        return p.hasParameterAnnotation(CurrentUser.class) && TableEntity.class.isAssignableFrom(p.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter p, ModelAndViewContainer mav, NativeWebRequest wr,
                                  WebDataBinderFactory bf) throws Exception {

        UUID userId = SecurityContextUtil.getCurrentUserId();
        @SuppressWarnings("unchecked")
        Class<? extends TableEntity> cls = (Class<? extends TableEntity>) p.getParameterType();

        try (DAO<? extends com.mcm.backend.app.database.core.components.tables.TableEntity, UUID> dao
                     = DAOFactory.createDAO(cls)) {
            var entity = dao.get(userId);
            if (entity == null) {
                throw new JsonErrorResponseException("User not found", HttpStatus.NOT_FOUND);
            }
            return entity;
        }
    }
}