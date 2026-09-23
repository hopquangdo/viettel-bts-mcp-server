package vn.edu.huce.iic.bts_ops_platform.security.auth;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PhanQuyenResolverService {
    int TOKEN_SCOPE_VERSION = 1;
    Collection<? extends GrantedAuthority> resolveAuthorities(UUID quyenId);
    default TokenScope resolveTokenScope(UUID quyenId) {
        return new TokenScope(null, false, List.of(), TOKEN_SCOPE_VERSION);
    }
    record TokenScope(String role, boolean fullAccess, List<String> permissions, int version) {
        public TokenScope { permissions = permissions == null ? List.of() : List.copyOf(permissions); }
    }
    void evictCache(UUID quyenId);
}
