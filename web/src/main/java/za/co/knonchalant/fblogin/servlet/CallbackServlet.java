package za.co.knonchalant.fblogin.servlet;

import facebook4j.Facebook;
import facebook4j.FacebookException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by evan on 15/02/22.
 */
public class CallbackServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Facebook facebook = (Facebook) request.getSession().getAttribute("facebook");
        String oauthCode = request.getParameter("code");

        try {
            facebook.getOAuthAccessToken(oauthCode);

        } catch (FacebookException e) {
            throw new ServletException(e);
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}
