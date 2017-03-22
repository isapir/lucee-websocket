package net.twentyonesolutions.lucee.websocket;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by Admin on 9/25/2016.
 *
 * This class can be used to initialize Java EE Sessions by the Java EE container.  To use it it must be loaded before
 * the Handshake takes place, so it should be done with a Listener in web.xml
 */
public class HttpSessionInitializer implements ServletRequestListener {


    public HttpSessionInitializer(){
        // TODO: log debug level only
        System.out.println(HttpSessionInitializer.class.getSimpleName() + ":");
    }


    /**
     * Receives notification that a ServletRequest is about to come
     * into scope of the web application.
     *
     * @param sre the ServletRequestEvent containing the ServletRequest
     *            and the ServletContext representing the web application
     */
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        // TODO: log debug level only
        System.out.println(HttpSessionInitializer.class.getSimpleName() + ": requestInitialized");

        ServletContext servletContext = sre.getServletContext();
        HttpServletRequest servletRequest = (HttpServletRequest) sre.getServletRequest();

        // initialize the HttpSession
        servletRequest.getSession();
    }


    /**
     * Receives notification that a ServletRequest is about to go out
     * of scope of the web application.
     *
     * @param sre the ServletRequestEvent containing the ServletRequest
     *            and the ServletContext representing the web application
     */
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {}

}