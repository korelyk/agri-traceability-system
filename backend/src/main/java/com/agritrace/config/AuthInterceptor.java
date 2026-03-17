package com.agritrace.config;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicEndpoint(request)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Missing or invalid Authorization header");
            return false;
        }

        try {
            Claims claims = jwtUtil.parseToken(authHeader.substring(7));
            request.setAttribute("currentUserId", claims.getSubject());
            request.setAttribute("currentUsername", claims.get("username", String.class));
            request.setAttribute("currentUserRole", claims.get("role", String.class));
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response, "Token verification failed");
            return false;
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (uri.equals("/api/health")) {
            return true;
        }
        if (uri.equals("/api/statistics") || uri.equals("/api/blockchain/statistics")) {
            return true;
        }
        if (HttpMethod.POST.matches(method) && (uri.equals("/api/users/login") || uri.equals("/api/users/register"))) {
            return true;
        }
        if (HttpMethod.GET.matches(method) && (uri.startsWith("/api/trace/") || uri.startsWith("/api/verify/"))) {
            return true;
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
