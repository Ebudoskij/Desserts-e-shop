package com.ebudoskij.dessert_shop.audit;

import com.ebudoskij.dessert_shop.model.User;
import com.ebudoskij.dessert_shop.repository.UserRepository;
import com.ebudoskij.dessert_shop.utils.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP aspect that populates {@link AuditContextHolder} before any service method runs
 * and clears it in a {@code finally} block after the method completes.
 *
 * <p>This is the "cross-cutting" half of the hybrid auditing strategy:
 * <ul>
 *   <li>The aspect handles <em>who</em> (user) and <em>where from</em> (IP) transparently.</li>
 *   <li>Each service method handles <em>what</em> changed by calling {@link AuditLogHelper}
 *       explicitly with the appropriate diff/snapshot payload.</li>
 * </ul>
 *
 * <p>Works with both HTTP requests (reads IP via {@link RequestContextHolder}) and
 * scheduled tasks (IP = null, user = null when no security context is present).
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditContextAspect {

    private final UserRepository userRepository;

    @Around(
        "execution(* com.ebudoskij.dessert_shop.service.impl.*ServiceImpl.*(..)) || " +
        "execution(* com.ebudoskij.dessert_shop.service.impl.OrderTimeoutScheduler.*(..))"
    )
    public Object populateAuditContext(ProceedingJoinPoint pjp) throws Throwable {
        User user = resolveCurrentUser();
        String ipAddress = resolveIpAddress();
        AuditContextHolder.set(new AuditContextHolder.AuditMetadata(user, ipAddress));
        try {
            return pjp.proceed();
        } finally {
            AuditContextHolder.clear();
        }
    }

    private User resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            log.debug("AuditContextAspect: could not resolve current user", e);
            return null;
        }
    }

    private String resolveIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            return HttpRequestUtils.getClientIp(attrs.getRequest());
        } catch (Exception e) {
            log.debug("AuditContextAspect: could not resolve IP address", e);
            return null;
        }
    }
}
