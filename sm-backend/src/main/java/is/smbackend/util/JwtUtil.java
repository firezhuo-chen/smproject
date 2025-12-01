package is.smbackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    // 密钥（生产环境应从配置文件读取）
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token 有效期：2小时
    private static final long EXPIRATION = 2 * 60 * 60 * 1000;
    
    // Token 刷新阈值：30分钟内即将过期时可刷新
    private static final long REFRESH_THRESHOLD = 30 * 60 * 1000;

    /**
     * 生成 Token
     */
    public String generateToken(String userId, String userName, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("userType", userType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 获取用户ID
     */
    public String getUserId(String token) {
        return parseToken(token).get("userId", String.class);
    }

    /**
     * 从 Token 获取用户名
     */
    public String getUserName(String token) {
        return parseToken(token).get("userName", String.class);
    }

    /**
     * 从 Token 获取用户类型
     */
    public String getUserType(String token) {
        return parseToken(token).get("userType", String.class);
    }

    /**
     * 检查 Token 是否需要刷新（即将过期）
     */
    public boolean needRefresh(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            // 如果距离过期时间小于刷新阈值，则需要刷新
            return expiration.getTime() - System.currentTimeMillis() < REFRESH_THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String userId = claims.get("userId", String.class);
            String userName = claims.get("userName", String.class);
            String userType = claims.get("userType", String.class);
            return generateToken(userId, userName, userType);
        } catch (Exception e) {
            return null;
        }
    }
}
