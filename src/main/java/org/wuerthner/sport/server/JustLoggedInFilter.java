package org.wuerthner.sport.server;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/sport/*")
public class JustLoggedInFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing needed here
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpSession session = httpReq.getSession(false);

        if (session != null && httpReq.getUserPrincipal() != null) {
            if (session.getAttribute("justLoggedIn") == null) {
                // First time after successful login
                session.setAttribute("justLoggedIn", Boolean.TRUE);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing needed here
    }
}
