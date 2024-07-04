package com.andrio.todoapp.middleware;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.exception.AuthorizationFailedException;
import com.andrio.todoapp.service.TokenService;
import com.andrio.todoapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    @Autowired
    public JwtTokenInterceptor(TokenService tokenService, JwtUtil jwtUtil) {
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        try {
            if (token == null) {
                throw new AuthorizationFailedException("Authorization header is missing");
            }
            if (!token.startsWith("Bearer ")) {
                throw new AuthorizationFailedException("Invalid token format");
            }
            token = token.substring(7);
            if (tokenService.isTokenInvalid(token)) {
                throw new AuthorizationFailedException("Invalid token");
            }
            TodoUserDto todoUserDTO = jwtUtil.extractUserDetails(token);
            request.setAttribute("todoUserDTO", todoUserDTO);
            logger.error(todoUserDTO.toString());
            return true;
        } catch (AuthorizationFailedException | ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(convertObjectToJson(new ErrorResponse("Unable to verify token. Please login again.")));
            return false;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(convertObjectToJson(new ErrorResponse("Whoops! Something went wrong. Please try again later.")));
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Post-handle logic if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Cleanup logic if needed
    }

    private String convertObjectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}