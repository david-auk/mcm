package com.mcm.backend.app.api.utils.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.PathVariable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.UUID;


// join-point helper to pull out @PathVariable UUIDs by name
public final class JoinPointUtils {
    private JoinPointUtils() {}

    public static <T> T extractPathVariable(ProceedingJoinPoint jp, String varName, Class<T> varType) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] args = jp.getArgs();

        // Try to find a matching @PathVariable by name or by type
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof PathVariable pv) {
                    boolean nameMatches = pv.value().equals(varName);
                    boolean typeMatches = varType.isInstance(arg);
                    if (nameMatches || typeMatches) {
                        try {
                            return varType.cast(arg);
                        } catch (ClassCastException ex) {
                            throw new IllegalStateException(
                                    "Found @PathVariable '" + varName + "' at parameter index " + i +
                                            " but could not cast value [" + arg + "] of type " +
                                            (arg != null ? arg.getClass().getName() : "null") +
                                            " to " + varType.getName() +
                                            " in method " + method, ex);
                        }
                    }
                }
            }
        }

        // If we get here, no match was found—build a detailed debug message
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to extract @PathVariable '")
                .append(varName)
                .append("' of type ")
                .append(varType.getName())
                .append(" from method ")
                .append(method)
                .append(".\nParameters detail:\n");

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            sb.append("  [")
                    .append(i)
                    .append("] value=")
                    .append(arg)
                    .append(" (")
                    .append(arg != null ? arg.getClass().getName() : "null")
                    .append("), annotations=[");
            for (Annotation ann : paramAnnotations[i]) {
                sb.append(ann.annotationType().getSimpleName()).append(",");
            }
            if (paramAnnotations[i].length > 0) {
                sb.setLength(sb.length() - 1); // drop trailing comma
            }
            sb.append("]\n");
        }

        throw new IllegalStateException(sb.toString());
    }
}