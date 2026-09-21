package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services;

import org.springframework.security.core.GrantedAuthority;
import vn.edu.huce.iic.bts_ops_platform.common.security.AuthorityPrefix;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import java.util.Collection;
import java.util.UUID;

public interface PhanQuyenResolverService {

    int TOKEN_SCOPE_VERSION = 1;

    Collection<? extends GrantedAuthority> resolveAuthorities(UUID quyenId);

    default TokenScope resolveTokenScope(UUID quyenId) {
        List<String> authorities = resolveAuthorities(quyenId).stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .toList();
        String role = authorities.stream()
                .filter(authority -> authority.startsWith(AuthorityPrefix.QUYEN))
                .map(authority -> authority.substring(AuthorityPrefix.QUYEN.length()))
                .findFirst()
                .orElse(null);
        List<String> permissions = authorities.stream()
                .filter(authority -> authority.startsWith(AuthorityPrefix.HAN))
                .map(authority -> authority.substring(AuthorityPrefix.HAN.length()))
                .collect(Collectors.toUnmodifiableList());
        boolean fullAccess = authorities.stream().anyMatch(AuthorityPrefix::isFullAccessAuthority);
        return new TokenScope(role, fullAccess, permissions, TOKEN_SCOPE_VERSION);
    }

    record TokenScope(String role, boolean fullAccess, List<String> permissions, int version) {
        public TokenScope {
            permissions = permissions == null ? List.of() : List.copyOf(permissions);
        }
    }

    void evictCache(UUID quyenId);
}
