package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Identity of the end user behind a request, populated from trusted headers set by the caller. */
public record McpUserPrincipal(
        UUID id,
        String tenDangNhap,
        String hoTen,
        UUID quyenId,
        UUID khuVucId,
        List<GrantedAuthority> resolvedAuthorities) implements UserDetails {

    public McpUserPrincipal(
            UUID id,
            String tenDangNhap,
            String hoTen,
            UUID quyenId,
            UUID khuVucId) {
        this(id, tenDangNhap, hoTen, quyenId, khuVucId, List.of());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (resolvedAuthorities != null && !resolvedAuthorities.isEmpty()) {
            return resolvedAuthorities;
        }
        if (quyenId == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_QUYEN_" + quyenId));
    }

    public McpUserPrincipal withAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return new McpUserPrincipal(
                id, tenDangNhap, hoTen, quyenId, khuVucId, new ArrayList<>(authorities));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return tenDangNhap;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
