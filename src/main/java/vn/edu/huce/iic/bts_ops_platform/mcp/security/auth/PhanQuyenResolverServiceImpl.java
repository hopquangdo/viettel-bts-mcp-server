package vn.edu.huce.iic.bts_ops_platform.mcp.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhanQuyenResolverServiceImpl implements PhanQuyenResolverService {
    private final QuyenRepository quyenRepository;

    @Override
    public Collection<? extends GrantedAuthority> resolveAuthorities(UUID quyenId) {
        if (quyenId == null) return List.of();
        return quyenRepository.findByIdAndNgayXoaIsNull(quyenId)
                .filter(quyen -> Boolean.TRUE.equals(quyen.getHoatDong()))
                .map(quyen -> List.<GrantedAuthority>of(
                        new SimpleGrantedAuthority("ROLE_" + quyen.getMa()),
                        new SimpleGrantedAuthority("QUYEN_" + quyenId)))
                .orElseGet(List::of);
    }

    @Override
    public void evictCache(UUID quyenId) {
    }
}
