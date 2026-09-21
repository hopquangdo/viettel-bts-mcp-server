package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.security.AuthorityPrefix;
import vn.edu.huce.iic.bts_ops_platform.common.security.FullAccessRoleCodes;
import vn.edu.huce.iic.bts_ops_platform.config.AppCacheProperties;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.PhanQuyenQuyenHan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.QuyenHan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.PhanQuyenQuyenHanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenHanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository.QuyenRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.services.PhanQuyenResolverService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhanQuyenResolverServiceImpl implements PhanQuyenResolverService {

    private static final String CACHE_NAME = "phanquyen-authority";

    private final QuyenRepository quyenRepository;
    private final QuyenHanRepository quyenHanRepository;
    private final PhanQuyenQuyenHanRepository phanQuyenQuyenHanRepository;
    private final CacheService cacheService;
    private final AppCacheProperties cacheProperties;

    @Override
    @Transactional(readOnly = true)
    public Collection<? extends GrantedAuthority> resolveAuthorities(UUID quyenId) {
        if (quyenId == null) {
            return List.of();
        }
        Optional<String[]> cached = cacheService.get(CACHE_NAME, quyenId.toString(), String[].class);
        if (cached.isPresent()) {
            return List.of(cached.get()).stream().map(SimpleGrantedAuthority::new).toList();
        }
        List<GrantedAuthority> authorities = loadAuthorities(quyenId);
        String[] authorityNames = authorities.stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
        cacheService.put(CACHE_NAME, quyenId.toString(), authorityNames, cacheProperties.phanquyenAuthorityTtl());
        return authorities;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenScope resolveTokenScope(UUID quyenId) {
        if (quyenId == null) {
            return new TokenScope(null, false, List.of(), TOKEN_SCOPE_VERSION);
        }
        Quyen quyen = quyenRepository.findByIdAndNgayXoaIsNull(quyenId).orElse(null);
        if (quyen == null || !Boolean.TRUE.equals(quyen.getHoatDong())) {
            return new TokenScope(null, false, List.of(), TOKEN_SCOPE_VERSION);
        }
        List<String> permissions = resolveAuthorities(quyenId).stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(AuthorityPrefix.HAN))
                .map(authority -> authority.substring(AuthorityPrefix.HAN.length()))
                .toList();
        boolean fullAccess = FullAccessRoleCodes.isFullAccess(quyen.getMa(), quyen.getTen());
        return new TokenScope(quyen.getMa(), fullAccess, permissions, TOKEN_SCOPE_VERSION);
    }

    @Override
    public void evictCache(UUID quyenId) {
        if (quyenId != null) {
            cacheService.evict(CACHE_NAME, quyenId.toString());
        } else {
            cacheService.evictAll(CACHE_NAME);
        }
    }

    private List<GrantedAuthority> loadAuthorities(UUID quyenId) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        Quyen quyen = quyenRepository.findByIdAndNgayXoaIsNull(quyenId).orElse(null);
        if (quyen == null || !Boolean.TRUE.equals(quyen.getHoatDong())) {
            return List.copyOf(authorities);
        }

        authorities.add(new SimpleGrantedAuthority(AuthorityPrefix.roleQuyen(quyen.getMa())));
        authorities.add(new SimpleGrantedAuthority(AuthorityPrefix.QUYEN + quyenId));

        if (FullAccessRoleCodes.isFullAccess(quyen.getMa(), quyen.getTen())) {
            addAllActiveQuyenHan(authorities);
            return new ArrayList<>(authorities);
        }

        List<PhanQuyenQuyenHan> mappings = phanQuyenQuyenHanRepository.findActiveByQuyenMa(quyen.getMa());
        for (PhanQuyenQuyenHan mapping : mappings) {
            UUID quyenHanId = mapping.getId().getQuyenHanId();
            quyenHanRepository.findByIdAndNgayXoaIsNull(quyenHanId).ifPresent(quyenHan -> {
                if (Boolean.TRUE.equals(quyenHan.getHoatDong())) {
                    authorities.add(new SimpleGrantedAuthority(AuthorityPrefix.quyenHan(quyenHan.getMa())));
                }
            });
        }

        return new ArrayList<>(authorities);
    }

    private void addAllActiveQuyenHan(Set<GrantedAuthority> authorities) {
        for (QuyenHan quyenHan : quyenHanRepository.findByNgayXoaIsNull()) {
            if (Boolean.TRUE.equals(quyenHan.getHoatDong())) {
                authorities.add(new SimpleGrantedAuthority(AuthorityPrefix.quyenHan(quyenHan.getMa())));
            }
        }
    }
}
