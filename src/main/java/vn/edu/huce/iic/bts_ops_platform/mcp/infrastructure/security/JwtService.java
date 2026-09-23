package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.JwtProperties;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguoidung.NguoiDung;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.AuthErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.security.auth.PhanQuyenResolverService.TokenScope;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_LOAI = "loai";
    public static final String LOAI_ACCESS = "access";
    public static final String LOAI_REFRESH = "refresh";
    /** Short-lived token the backend issues per chat turn so the MCP server can identify the end user. */
    public static final String LOAI_MCP = "mcp";
    public static final String MCP_AUDIENCE = "mcp";
    public static final String MCP_ISSUER = "bts-backend";
    /** Long enough for one agent run (LLM + tool calls), short enough to limit reuse. */
    public static final Duration MCP_TOKEN_TTL = Duration.ofMinutes(10);
    public static final String CLAIM_TEN_DANG_NHAP = "tenDangNhap";
    public static final String CLAIM_HO_TEN = "hoTen";
    public static final String CLAIM_QUYEN_ID = "quyenId";
    public static final String CLAIM_KHU_VUC_ID = "khuVucId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_FULL_ACCESS = "fa";
    public static final String CLAIM_PERMISSIONS = "han";
    public static final String CLAIM_SCOPE_VERSION = "sv";

    private final JwtProperties jwtProperties;

    public String createAccessToken(NguoiDung nguoiDung) {
        return createToken(nguoiDung, LOAI_ACCESS, jwtProperties.accessTokenExpirationMs());
    }

    public String createAccessToken(NguoiDung nguoiDung, TokenScope scope) {
        return createToken(nguoiDung, LOAI_ACCESS, jwtProperties.accessTokenExpirationMs(), scope);
    }

    public String createRefreshToken(NguoiDung nguoiDung) {
        long millis = jwtProperties.refreshTokenExpirationDays() * 24L * 60L * 60L * 1000L;
        return createToken(nguoiDung, LOAI_REFRESH, millis);
    }

    /**
     * Token that lets the chatbot act on behalf of {@code user} towards the MCP server only
     * ({@code aud=mcp}). It carries identity and area scope; permissions are resolved by the MCP
     * server from {@code quyenId}, as for a normal access token. It is rejected as an access token
     * (different {@code loai}) and an access token is rejected by the MCP server.
     */
    public String createMcpToken(JwtUserPrincipal user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(MCP_ISSUER)
                .audience().add(MCP_AUDIENCE).and()
                .subject(user.id().toString())
                .claim(CLAIM_LOAI, LOAI_MCP)
                .claim(CLAIM_TEN_DANG_NHAP, user.tenDangNhap())
                .claim(CLAIM_HO_TEN, user.hoTen())
                .claim(CLAIM_QUYEN_ID, user.quyenId() != null ? user.quyenId().toString() : null)
                .claim(CLAIM_KHU_VUC_ID, user.khuVucId() != null ? user.khuVucId().toString() : null)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(MCP_TOKEN_TTL)))
                .signWith(signingKey())
                .compact();
    }

    /** Verifies a token issued by {@link #createMcpToken}: signature, expiry, type, issuer and audience. */
    public JwtUserPrincipal verifyMcpToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!LOAI_MCP.equals(claims.get(CLAIM_LOAI, String.class))
                    || !MCP_ISSUER.equals(claims.getIssuer())
                    || claims.getAudience() == null
                    || !claims.getAudience().contains(MCP_AUDIENCE)) {
                throw new AppException(AuthErrorCode.TOKEN_KHONG_HOP_LE, "Loại token không hợp lệ");
            }
            return new JwtUserPrincipal(
                    UUID.fromString(claims.getSubject()),
                    claims.get(CLAIM_TEN_DANG_NHAP, String.class),
                    claims.get(CLAIM_HO_TEN, String.class),
                    parseUuidClaim(claims.get(CLAIM_QUYEN_ID, String.class)),
                    parseUuidClaim(claims.get(CLAIM_KHU_VUC_ID, String.class)));
        } catch (ExpiredJwtException ex) {
            throw new AppException(AuthErrorCode.TOKEN_HET_HAN, "Token đã hết hạn");
        } catch (AppException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AppException(AuthErrorCode.TOKEN_KHONG_HOP_LE, "Token không hợp lệ");
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.accessTokenExpirationMs() / 1000L;
    }

    public JwtUserPrincipal verifyToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get(CLAIM_LOAI, String.class);
            if (!expectedType.equals(type)) {
                throw new AppException(AuthErrorCode.TOKEN_KHONG_HOP_LE, "Loại token không hợp lệ");
            }
            UUID id = UUID.fromString(claims.getSubject());
            String tenDangNhap = claims.get(CLAIM_TEN_DANG_NHAP, String.class);
            String hoTen = claims.get(CLAIM_HO_TEN, String.class);
            UUID quyenId = parseUuidClaim(claims.get(CLAIM_QUYEN_ID, String.class));
            UUID khuVucId = parseUuidClaim(claims.get(CLAIM_KHU_VUC_ID, String.class));
            return new JwtUserPrincipal(id, tenDangNhap, hoTen, quyenId, khuVucId);
        } catch (ExpiredJwtException ex) {
            throw new AppException(AuthErrorCode.TOKEN_HET_HAN, "Token đã hết hạn");
        } catch (AppException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AppException(AuthErrorCode.TOKEN_KHONG_HOP_LE, "Token không hợp lệ");
        }
    }

    private String createToken(NguoiDung nguoiDung, String type, long ttlMs) {
        return createToken(nguoiDung, type, ttlMs, null);
    }

    private String createToken(NguoiDung nguoiDung, String type, long ttlMs, TokenScope scope) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(nguoiDung.getId().toString())
                .claim(CLAIM_LOAI, type)
                .claim(CLAIM_TEN_DANG_NHAP, nguoiDung.getTenDangNhap())
                .claim(CLAIM_HO_TEN, nguoiDung.getHoTen())
                .claim(CLAIM_QUYEN_ID, nguoiDung.getQuyenId() != null ? nguoiDung.getQuyenId().toString() : null)
                .claim(CLAIM_KHU_VUC_ID, nguoiDung.getKhuVucId() != null ? nguoiDung.getKhuVucId().toString() : null)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)));
        if (LOAI_ACCESS.equals(type) && scope != null) {
            builder.claim(CLAIM_ROLE, scope.role())
                    .claim(CLAIM_FULL_ACCESS, scope.fullAccess())
                    .claim(CLAIM_SCOPE_VERSION, scope.version());
            if (!scope.fullAccess()) {
                builder.claim(CLAIM_PERMISSIONS, scope.permissions());
            }
        }
        return builder.signWith(signingKey()).compact();
    }

    private UUID parseUuidClaim(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
