package at.htlhl.geo_tracking_solution.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        return new JwtAuthenticationToken(
                source,
                Stream.concat(
                        new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                        extractResourceRoles(source).stream()
                ).collect(Collectors.toSet())
        );
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        // Extracting account roles from resource_access
        var resourceAccess = jwt.getClaim("resource_access");
        List<String> accountRoles = new ArrayList<>();
        if (resourceAccess != null && resourceAccess instanceof Map) {
            var account = ((Map<String, Map<String, List<String>>>) resourceAccess).get("account");
            if (account != null) {
                accountRoles = account.getOrDefault("roles", new ArrayList<>());
            }
        }

        // Extracting realm roles from realm_access
        List<String> realmRoles = new ArrayList<>();
        var realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess instanceof Map) {
            realmRoles = ((Map<String, List<String>>) realmAccess).getOrDefault("roles", new ArrayList<>());
        }

        // Combine account roles and realm roles
        List<String> roles = new ArrayList<>();
        roles.addAll(accountRoles);
        roles.addAll(realmRoles);


        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_"))).collect(Collectors.toSet());
    }
}
