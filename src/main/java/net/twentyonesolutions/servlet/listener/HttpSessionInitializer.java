package net.twentyonesolutions.servlet.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Igal on 9/25/2016.
 *
 * This class is used to initialize Java EE Sessions by the Java EE container. To use it it must be loaded before the
 * Handshake takes place, so it should be done with a Listener in web.xml, i.e.
 *
 * We need the HttpSession in order to get the ServletContext in the WebSocket Handshake
 *
 * <listener> <listener-class>net.twentyonesolutions.servlet.listener.HttpSessionInitializer</listener-class>
 * </listener>
 */
public class HttpSessionInitializer implements ServletRequestListener {

	public HttpSessionInitializer() {

		System.out.println(HttpSessionInitializer.class.getName() + " initialized");
	}

	/**
	 * Receives notification that a ServletRequest is about to come into scope of the web application.
	 *
	 * @param sre
	 *            the ServletRequestEvent containing the ServletRequest and the ServletContext representing the web
	 *            application
	 */
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		// TODO: log debug level only
		// System.out.println(HttpSessionInitializer.class.getSimpleName() + ": requestInitialized");

		HttpServletRequest servletRequest = (HttpServletRequest) sre.getServletRequest();

		// call getSession() so that the Servlet Container will initialize the HttpSession
		servletRequest.getSession();
	}

	/**
	 * Receives notification that a ServletRequest is about to go out of scope of the web application.
	 *
	 * @param sre
	 *            the ServletRequestEvent containing the ServletRequest and the ServletContext representing the web
	 *            application
	 */
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
	}

}