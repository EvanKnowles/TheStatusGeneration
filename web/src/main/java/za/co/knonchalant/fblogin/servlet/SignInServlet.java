package za.co.knonchalant.fblogin.servlet;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by evan on 15/02/22.
 */
public class SignInServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Facebook facebook = new FacebookFactory().getInstance();
        request.getSession().setAttribute("facebook", facebook);

        StringBuffer callbackURL = request.getRequestURL();
        int index = callbackURL.lastIndexOf("/");
        String queryString = request.getQueryString();
        if (queryString != null) {
            int scope = queryString.indexOf("scope");
            if (scope != -1) {
                facebook.setOAuthPermissions(queryString.substring(queryString.indexOf("=") + 1));
            }
        }

        callbackURL.replace(index, callbackURL.length(), "").append("/callback");

        response.sendRedirect(facebook.getOAuthAuthorizationURL(callbackURL.toString()));
    }
}
