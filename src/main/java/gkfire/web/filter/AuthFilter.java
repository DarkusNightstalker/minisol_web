package gkfire.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    public AuthFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try {

            // check whether session variable is set
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession ses = req.getSession(false);
            // allow user to proccede if url is login.xhtml or user logged in or
            // user is accessing any page in 
            // public folder
            String reqURI = req.getRequestURI();
//            System.out.println(req.getContextPath());
            Object o =null;
            if(ses != null){
                o = ses.getAttribute("user");
//                System.out.println(String.valueOf(o));
            }
            if (reqURI.indexOf("/login.xhtml") >= 0 || reqURI.contains(req.getContextPath()+"/service/") || reqURI.equals(req.getContextPath()+"/")
                    || (ses != null && ses.getAttribute("user") != null)
                    || reqURI.indexOf("/public/") >= 0
                    || reqURI.contains("javax.faces.resource") 
                    || reqURI.contains(req.getContextPath()+"/assets/")
                    || reqURI.contains(req.getContextPath()+"/vendor/")) {
                try {
                    if ((reqURI.indexOf("/login.xhtml") >= 0 || reqURI.equals(req.getContextPath()+"/") )&& ses != null && ses.getAttribute("user") != null) {
                        res.sendRedirect(req.getContextPath() + "/index.xhtml");
                    } else {
                        chain.doFilter(request, response);
                    }
                } catch (java.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            } else {
                res.sendRedirect(req.getContextPath() + "/login.xhtml");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("Mensaje de Error en Filtro: " + t.getMessage());
        }
    } // doFilter

    @Override
    public void destroy() {
    }
}
