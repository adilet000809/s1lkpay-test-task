package com.example.accountservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final RestTemplate restTemplate;

    @Autowired
    public JwtFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //Reference to Auth service to retrieve authorized client's ID by sending JWT
        String jwt = extractJwtFromRequest(request);
        if (jwt != null) {
            try {
                //Putting JWT in AUTHORIZATION header
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                //A special endpoint in Auth microservice to validate and retrieve client's ID
                String authServiceUrl = "http://auth-service:8080/api/auth";
                Long clientId = restTemplate.exchange(authServiceUrl + "/validate", HttpMethod.GET, new HttpEntity<>(headers), Long.class).getBody();
                //Setting retrieved client's ID in request attribute for further use
                request.setAttribute("clientId", clientId);
                filterChain.doFilter(request, response);
            } catch (HttpClientErrorException.Unauthorized e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    //A method for extraction of JWT from request headers
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}