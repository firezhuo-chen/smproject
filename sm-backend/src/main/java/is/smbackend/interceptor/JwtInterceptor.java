package is.smbackend.interceptor;

import is.smbackend.exception.BusinessException;
import is.smbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 请求（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取 Token
        String token = request.getHeader("Authorization");
        
        // 如果请求头没有，尝试从 URL 参数获取（用于文件下载）
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        
        if (token == null || token.isEmpty()) {
            throw new BusinessException("未登录，请先登录");
        }

        // 去掉 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("登录已过期，请重新登录");
        }

        // 将用户信息存入请求属性，供后续使用
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("userName", jwtUtil.getUserName(token));
        request.setAttribute("userType", jwtUtil.getUserType(token));

        // 检查 Token 是否需要刷新（即将过期）
        if (jwtUtil.needRefresh(token)) {
            String newToken = jwtUtil.refreshToken(token);
            if (newToken != null) {
                // 将新 Token 放入响应头，前端可以获取并更新
                response.setHeader("X-New-Token", newToken);
                response.setHeader("Access-Control-Expose-Headers", "X-New-Token");
            }
        }

        return true;
    }
}
