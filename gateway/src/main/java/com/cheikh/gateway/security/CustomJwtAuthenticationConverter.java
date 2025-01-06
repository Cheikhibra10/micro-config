package com.cheikh.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private static final String DEFAULT_CLIENT_ID = "micro-services-api";


    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<String> allRoles = Stream.concat(
                        getRolesFromClaim(jwt, "realm_access", "roles").stream(),
                        getRolesFromClaim(jwt, "resource_access", DEFAULT_CLIENT_ID, "roles").stream()
                )
                .distinct()
                .toList();

        List<SimpleGrantedAuthority> authorities = allRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }


    private List<String> getRolesFromClaim(Jwt jwt, String... keys) {
        try {
            Object value = jwt.getClaims();

            for (String key : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(key);
                } else {
                    return Collections.emptyList();
                }
            }

            if (value instanceof Collection) {
                return ((Collection<?>) value).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
        } catch (ClassCastException e) {
        }

        return Collections.emptyList();
    }
}

