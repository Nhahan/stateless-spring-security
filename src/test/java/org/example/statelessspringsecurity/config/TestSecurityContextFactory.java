package org.example.statelessspringsecurity.config;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class TestSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AuthUser authUser = new AuthUser(customUser.userId(), customUser.email(), customUser.role());
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(authUser);

        context.setAuthentication(authentication);
        return context;
    }
}
