package za.co.knonchalant.fblogin.filter;

import facebook4j.Facebook;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by evan on 15/02/25.
 */
public class FacebookFilter implements Filter {

    private static final String[] FACEBOOK_AGENTS = {"facebookexternalhit/1.0", "facebookexternalhit/1.1"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String userAgent = request.getHeader("User-Agent");
        HttpSession session = request.getSession(true);

        Facebook facebook = (Facebook) session.getAttribute("facebook");

        if (facebook == null && isFacebook(userAgent)) {
            session.setAttribute("isFaceBook", true);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        session.setAttribute("isFaceBook", false);
        if (facebook == null && !request.getRequestURI().contains("signin")) {
            redirectToSignIn(request, response);
        } else {
            try {
                if (facebook != null && !request.getRequestURI().contains("signin")&& !request.getRequestURI().contains("callback")) {
                    facebook.getId();
                }
            } catch (Exception ex) {
                redirectToSignIn(request, response);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isFacebook(String userAgent) {
        for (String agent : FACEBOOK_AGENTS) {
            if (userAgent.contains(agent)) {
                return true;
            }
        }

        return false;
    }

    private void redirectToSignIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getContextPath() + "/signin";
        response.sendRedirect(path);
    }

    @Override
    public void destroy() {

    }
}
