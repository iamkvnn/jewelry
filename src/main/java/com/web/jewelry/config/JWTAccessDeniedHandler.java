package com.web.jewelry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.jewelry.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWTAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse apiResponse = new ApiResponse("0", "Access denied", "Forbidden");

        final ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
