package com.alamo.asistencia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MobileBlockFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userAgent = request.getHeader("User-Agent");
        String path = request.getRequestURI();

        // Bloqueo solo en landing y login
        if (isProtectedPath(path) && userAgent != null && isMobile(userAgent)) {
            response.sendRedirect("/mobile");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Rutas donde NO se debe permitir acceso desde móvil
    private boolean isProtectedPath(String path) {
        return path.equals("/") ||
               path.equals("/usuarios/cargarLogin") ||
               path.equals("/usuarios/cargarLogin/") ||
               path.equals("/login") ||          // por si usas /login
               path.equals("/login/");           // por seguridad
    }

    // Detecta si es móvil
    private boolean isMobile(String userAgent) {
        String ua = userAgent.toLowerCase();
        return ua.contains("android") ||
               ua.contains("iphone") ||
               ua.contains("ipad") ||
               ua.contains("mobile");
    }

    // Evitar filtrar recursos estáticos
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/webjars/")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".svg");
    }
}
