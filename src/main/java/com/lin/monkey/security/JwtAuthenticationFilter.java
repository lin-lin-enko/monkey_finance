package com.lin.monkey.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // This method is called automatically for ALL HTTP-requests
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, // what came from client
                                    @NonNull HttpServletResponse response, // what is sent back to client
                                    @NonNull FilterChain filterChain) // next filter that will be used
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // passing request further
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);
        // if there's a user id but user isn't yet authenticated in this request
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // getting full user details (roles, privileges etc.)
            UserDetails userDetails = userDetailsService.loadUserById(userId);

            if (jwtUtil.validateToken(token)) {
                System.out.println("Token validated for userId: " + userId);
                // creating auth object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // adding request details (IP, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // setting auth into context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentication set in SecurityContext for: " + userDetails.getUsername());
            } else System.out.println("Token INVALID for userId: " + userId);
        }

        // passing request further
        filterChain.doFilter(request, response);
    }
}
