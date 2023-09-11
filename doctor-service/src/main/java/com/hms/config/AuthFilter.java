package com.hms.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hms.services.CustomUserDetailsService;
import com.hms.utils.JwtUtil;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    	String requestUrl = request.getRequestURL().toString();
        
        if (requestUrl.endsWith("/register") || requestUrl.endsWith("/login") || requestUrl.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

    	
//        Cookie[] jwtCookies = request.getCookies();
        String token = null;
        if(request.getHeader("Authorization") != null) {
        	token = request.getHeader("Authorization");
        }
//        System.out.println("cookies: "+jwtCookies);
//        if (jwtCookies != null) {
//            for (Cookie cookie : jwtCookies) {
//                if (cookie.getName().equalsIgnoreCase("authorization")) {
////                     cookie.setValue(null);
////                     cookie.setMaxAge(0);
////                     cookie.setPath("/");
////                     response.addCookie(cookie);
//                    token = cookie.getValue();
//                    // System.out.println(token);
//                }
//            }
//        }
        String email = null;

        if (token != null) {
            try {
                email = this.jwtUtil.extractUsername(token);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(email);
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } else {
            System.out.println("Invalid Token");
            if (token == null || SecurityContextHolder.getContext().getAuthentication() == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("You need to log in to access this resource");
                return;
            }
        }

        filterChain.doFilter(request, response);

    }

}
