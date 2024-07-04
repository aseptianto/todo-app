package com.andrio.todoapp.middleware;

import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.service.TokenService;
import com.andrio.todoapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtTokenInterceptorTests {

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtTokenInterceptor jwtTokenInterceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = mock(HttpServletRequest.class);
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandleShouldAllowValidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(tokenService.isTokenInvalid("validToken")).thenReturn(false);
        when(jwtUtil.extractUserDetails("validToken")).thenReturn(new TodoUserDto(1L, "andrio@example.com", "User"));

        boolean result = jwtTokenInterceptor.preHandle(request, response, null);

        verify(request).setAttribute(eq("todoUserDTO"), any(TodoUserDto.class));
        assertTrue(result);
    }

    @Test
    void preHandleShouldRejectMissingAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = jwtTokenInterceptor.preHandle(request, response, null);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertFalse(result);
    }

    @Test
    void preHandleShouldRejectInvalidTokenFormat() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidTokenFormat");

        boolean result = jwtTokenInterceptor.preHandle(request, response, null);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertFalse(result);
    }

}