package pe.edu.upc.bonotech.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upc.bonotech.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            if (token != null && tokenService.validateToken(token)) {
                String username = tokenService.getUsernameFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(username);
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                LOGGER.info("Token is not valid or not present. Proceeding as anonymous request.");
            }
        } catch (Exception e) {
            LOGGER.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    /**
     * ESTE METODO ES LA CLAVE.
     * Le dice a Spring: "Si la URL coincide con esto, SALTATE este filtro por completo".
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Lista de rutas que queremos ignorar (Swagger, autenticación, etc.)
        List<String> excludedPaths = Arrays.asList(
                "/v3/api-docs",
                "/swagger-ui",
                "/swagger-resources",
                "/webjars",
                "/favicon.ico",
                "/error",
                "/v1/authentication" // Opcional: si quieres que el login tampoco revise token
        );

        // Si la ruta actual empieza con alguna de las excluidas, retorna TRUE (No filtrar)
        return excludedPaths.stream().anyMatch(path::startsWith);
    }
}