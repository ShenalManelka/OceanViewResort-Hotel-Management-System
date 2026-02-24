package util;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = { "/dashboard.html", "/admin/*" })
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // 1. Prevent Caching of protected pages
        // This ensures the browser doesn't show the dashboard when clicking 'back'
        // after logout
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        httpResponse.setDateHeader("Expires", 0); // Proxies.

        String loginURI = httpRequest.getContextPath() + "/login.html";
        String loginServletURI = httpRequest.getContextPath() + "/admin/login";

        boolean loggedIn = (session != null && session.getAttribute("user") != null);
        boolean loginRequest = httpRequest.getRequestURI().equals(loginURI);
        boolean loginServletRequest = httpRequest.getRequestURI().equals(loginServletURI);

        if (loggedIn || loginRequest || loginServletRequest) {
            // Already logged in or requesting login page
            if (loggedIn && loginRequest) {
                // If logged in, don't show login page, go to dashboard
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard.html");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            // Not logged in and trying to access protected resource
            httpResponse.sendRedirect(loginURI);
        }
    }

    @Override
    public void destroy() {
    }
}
