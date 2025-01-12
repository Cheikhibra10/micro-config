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

/**
 * A custom implementation of a JWT authentication converter.
 * This class extracts roles from a JWT and converts them into Spring Security authorities.
 */
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    // Default client ID used to extract roles from resource_access in the JWT
    private static final String DEFAULT_CLIENT_ID = "micro-services-api";

    /**
     * Converts a given JWT into a Mono containing an AbstractAuthenticationToken.
     * Extracts roles from the JWT claims, maps them to authorities, and creates a JwtAuthenticationToken.
     *
     * @param jwt the JWT to convert
     * @return a Mono containing the authentication token with roles as authorities
     */
    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        // Extract roles from both "realm_access" and "resource_access" claims
        List<String> allRoles = Stream.concat(
                        // Get roles from the "realm_access" claim
                        getRolesFromClaim(jwt, "realm_access", "roles").stream(),
                        // Get roles from the "resource_access" claim for the default client ID
                        getRolesFromClaim(jwt, "resource_access", DEFAULT_CLIENT_ID, "roles").stream()
                )
                .distinct() // Remove duplicate roles
                .toList();

        // Convert roles into a list of Spring Security GrantedAuthority objects
        List<SimpleGrantedAuthority> authorities = allRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())) // Add "ROLE_" prefix and uppercase
                .toList();

        // Return a JwtAuthenticationToken containing the JWT and the extracted authorities
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    /**
     * Extracts roles from a nested structure in the JWT claims.
     * Navigates through the keys provided to find the target collection of roles.
     *
     * @param jwt  the JWT containing the claims
     * @param keys the keys used to navigate to the roles (e.g., "realm_access", "roles")
     * @return a list of roles as strings; returns an empty list if roles are not found or invalid
     */
    private List<String> getRolesFromClaim(Jwt jwt, String... keys) {
        try {
            // Start with the root claims map from the JWT
            Object value = jwt.getClaims();

            // Navigate through the provided keys to locate the target value
            for (String key : keys) {
                if (value instanceof Map) {
                    // Descend one level deeper into the map
                    value = ((Map<?, ?>) value).get(key);
                } else {
                    // If the value is not a map, return an empty list (invalid structure)
                    return Collections.emptyList();
                }
            }

            // If the final value is a collection, convert it to a list of strings
            if (value instanceof Collection) {
                return ((Collection<?>) value).stream()
                        .filter(String.class::isInstance) // Ensure only string values are processed
                        .map(String.class::cast) // Cast each value to a string
                        .toList(); // Collect results into a list
            }
        } catch (ClassCastException e) {
            // Handle any unexpected type errors gracefully (optional logging can be added here)
        }

        // Return an empty list if roles are not found or an error occurs
        return Collections.emptyList();
    }
}

