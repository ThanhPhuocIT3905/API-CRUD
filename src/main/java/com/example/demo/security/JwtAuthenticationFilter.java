package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter: Filter để xác thực JWT token trong mỗi request
 * 
 * Chức năng chính:
 * - Lấy JWT token từ Authorization header
 * - Xác thực tính hợp lệ của token
 * - Set authentication vào SecurityContext
 * - Bỏ qua các request không cần authentication
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Phương thức chính của filter, được gọi cho mỗi request
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Chuỗi filter
     * @throws ServletException Nếu có lỗi servlet
     * @throws IOException Nếu có lỗi I/O
     */
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Lấy JWT token từ request
            String jwt = getJwtFromRequest(request);
            
            // Nếu có token và token hợp lệ
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Lấy username từ token
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // Load user details từ username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Tạo authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, // Principal (user details)
                        null,        // Credentials (không cần cho JWT)
                        userDetails.getAuthorities() // Authorities/roles
                    );
                
                // Set details cho authentication (IP address, session ID, etc.)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication vào SecurityContext
                // Điều này cho phép Spring Security biết user đã được xác thực
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            // Token có chữ ký không hợp lệ (bị thay đổi hoặc không đúng secret key)
            logger.error("JWT signature validation failed: Token có thể đã bị thay đổi hoặc secret key không đúng", ex);
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
            // Token không đúng định dạng
            logger.error("JWT token is malformed: Token không đúng định dạng", ex);
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            // Token đã hết hạn
            logger.error("JWT token is expired: Token đã hết hạn", ex);
        } catch (io.jsonwebtoken.UnsupportedJwtException ex) {
            // Token không được hỗ trợ
            logger.error("JWT token is unsupported: Token không được hỗ trợ", ex);
        } catch (IllegalArgumentException ex) {
            // Token null hoặc rỗng
            logger.error("JWT claims string is empty: Token rỗng hoặc null", ex);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            // Không tìm thấy user trong database
            logger.error("User not found in database: Không tìm thấy người dùng trong database", ex);
        } catch (Exception ex) {
            // Các lỗi khác chưa được xác định
            logger.error("Unexpected error during JWT authentication: Lỗi không mong muốn trong quá trình xác thực JWT", ex);
        }
        
        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }

    /**
     * Lấy JWT token từ Authorization header
     * Format: "Bearer <token>"
     * @param request HTTP request
     * @return JWT token string hoặc null nếu không tìm thấy
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Lấy Authorization header
        String bearerToken = request.getHeader("Authorization");
        
        // Kiểm tra header có tồn tại và bắt đầu bằng "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Trả về token sau khi bỏ qua "Bearer " prefix
            return bearerToken.substring(7);
        }
        
        // Không tìm thấy token
        return null;
    }

    /**
     * Quy định khi nào filter nên được áp dụng
     * Có thể override để bỏ qua một số endpoint cụ thể
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Bỏ qua authentication cho các endpoint public
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.equals("/");
    }
}