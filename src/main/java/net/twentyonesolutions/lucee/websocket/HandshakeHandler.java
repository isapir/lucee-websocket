package net.twentyonesolutions.lucee.websocket;

import lucee.commons.io.log.Log;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Session;
import net.twentyonesolutions.lucee.app.LuceeAppListener;
import net.twentyonesolutions.lucee.app.LuceeApps;
import net.twentyonesolutions.lucee.websocket.connections.ConnectionManager;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.twentyonesolutions.lucee.websocket.Constants.*;


/**
 * Created by Admin on 9/25/2016.
 */
public class HandshakeHandler extends ServerEndpointConfig.Configurator {

//    public static final String KEY_URI_AUTHORITY = "javax.websocket.endpoint.authority";        // host:port = same as CGI.HTTP_HOST
    public static final String KEY_ENDPOINT_PATH = "javax.websocket.endpoint.path";             // the endpoint path, e.g. /chat/{channel}
    public static final String KEY_LUCEE_APP_KEY = "net.twentyonesolutions.lucee.app.appkey";   // same as KEY_URI_AUTHORITY + KEY_ENDPOINT_PATH
    public static final String KEY_LUCEE_SESSION = "lucee.runtime.type.scope.Session";          // gives access to Lucee Session Scope
    public static final String KEY_LUCEE_STRUCT  = "lucee.runtime.type.Struct";                 // provides struct for setting/getting arbitrary properties
    public static final String KEY_CONN_MANAGER  = ConnectionManager.class.getCanonicalName();


    public static String idCookieName = "cfid";

    /**
     * Called by the container after it has formulated a handshake response resulting from
     * a well-formed handshake request. The container has already
     * checked that this configuration has a matching URI, determined the
     * validity of the origin using the checkOrigin method, and filled
     * out the negotiated subprotocols and extensions based on this configuration.
     * Custom configurations may override this method in order to inspect
     * the request parameters and modify the handshake response that the server has formulated.
     * and the URI checking also.
     *
     * <p>If the developer does not override this method, no further
     * modification of the request and response are made by the implementation.
     *
     * @param sec the configuration object involved in the handshake
     * @param request  the opening handshake request.
     * @param response the proposed opening handshake response
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {

        String listenerKey = getListenerKey(sec, request);                  // endpoint either with or without the host
        ConnectionManager connMgr = ConnectionManager.getConnectionManager(listenerKey);

        try {

            connMgr.log(Log.LEVEL_TRACE, "enter HandshakeHandler.modifyHandshake()");

            Map<String, Object> userProps = sec.getUserProperties();

            userProps.put(HandshakeHandler.KEY_ENDPOINT_PATH, listenerKey); // set the key as endpoint path to get the correct ConnectionManager etc.

            LuceeAppListener appListener = WebsocketUtil.getLuceeAppListener(listenerKey);
            if (appListener == null){

                connMgr.log(Log.LEVEL_ERROR, "HandshakeHandler.modifyHandshake() error; No component listener found for [" + listenerKey + "]");
                throw new RuntimeException("No Component Listener was found for " + listenerKey);
            }

            userProps.put(HandshakeHandler.KEY_LUCEE_APP_KEY, listenerKey); // make the listenerKey available via UserProperties

            userProps.put(KEY_CONN_MANAGER, connMgr);

            userProps.put("request_uri", request.getRequestURI().toString());
            userProps.put("request_uri_path", request.getRequestURI().getPath());

            String httpHost = request.getRequestURI().getAuthority();
            if (httpHost != null)
                userProps.put("http_host", request.getRequestURI().getAuthority());

            userProps.put(PROPERTY_CHANNELS, new HashSet<String>());

            Map<String, List<String>> reqHeaders = request.getHeaders();
            List<String> rawCookies = reqHeaders.get("cookie");
            String idCookieValue = null;

            if (rawCookies != null && !rawCookies.isEmpty()){

                Map<String, String> cookies = parseRequestHeaderCookie(rawCookies.get(0));

                idCookieValue = cookies.get(idCookieName);
                if (idCookieValue != null)
                    userProps.put(idCookieName, idCookieValue);        // store cfid in UserProperties for future use
            }

            connMgr.log(Log.LEVEL_DEBUG, "HandshakeHandler.modifyHandshake(); " + listenerKey + "; " + idCookieName + ": " + (idCookieValue != null ? idCookieValue : ""));

            Session sessionScope = null;
            if (idCookieValue != null){

                // retrieve Lucee Session and make it available via UserProperties
                sessionScope = appListener.getApp().getSessionScope(idCookieValue);

//                do not cache a reference to the Session scope as the session may expire and the reference will be to a ghost, i.e. websocket listener may modify the session but Lucee will never the changes
//                userProps.put(HandshakeHandler.KEY_LUCEE_SESSION, sessionScope);
            }

            Struct struct = LuceeApps.getCreationUtil().createStruct(); // create a cfml struct and put it in user properties for easier cfml access
            for (Map.Entry<String, Object> e : userProps.entrySet()){

                String key = e.getKey();
                if (key.indexOf('.') > -1){                 // TODO: why?
                    key = key.substring(key.lastIndexOf('.') + 1);
                }

                Object val = e.getValue();
                if (val instanceof InetSocketAddress)       // TODO: why?
                    val = val.toString();

                struct.setEL(LuceeApps.toKey(key), val);
            }
            userProps.put(KEY_LUCEE_STRUCT, struct);    // add it after we populated it from UserProperties so that we don't get a self reference

            // the listener can manipulate the args, or refuse the connection by returning false or throwing an exception
            Object luceeResult = WebsocketUtil.invokeListenerMethodWithNamedArgs(
                     appListener
                    ,LISTENER_METHOD_ON_HANDSHAKE
                    ,ARG_ENDPOINT_CONFIG, sec
                    ,ARG_REQUEST, request
                    ,ARG_RESPONSE, response
                    ,ARG_SESSION_SCOPE, sessionScope
                    ,ARG_APPLICATION_SCOPE, appListener.getApp().getApplicationScope()
            );

            // if the listener's onHandshake returned false then refuse the connection. listener's onHandshake can also throw an exception to refuse the connection
            if ((luceeResult instanceof Exception) || LuceeApps.isBooleanFalse(luceeResult)){

                connMgr.log(Log.LEVEL_INFO, "listener.onHandshake() refused the connection.");
                throw new RuntimeException("connection refused by listener.onHandshake()");
            }
        }
        catch (NullPointerException npe){
            connMgr.log(Log.LEVEL_ERROR, npe.toString());
            npe.printStackTrace(System.out);    // helps with debugging NPEs at runtime
        }

        connMgr.log(Log.LEVEL_TRACE, "exit HandshakeHandler.modifyHandshake()");
    }


    private static Map<String, String> parseRequestHeaderCookie(String requestHeaderCookie){

        Map<String, String> result = new TreeMap();

        String name, value;
        String[] cookies = requestHeaderCookie.split(";"), cookieParts;

        for (String c : cookies) {

            cookieParts = c.split("=");

            if (cookieParts.length != 2)
                continue;

            try {

                name = cookieParts[0].trim();
                value = URLDecoder.decode(cookieParts[1].trim(), "UTF-8");
                result.put(name, value);
            }
            catch (UnsupportedEncodingException e) {}     // UTF-8 is always supported
        }

        return result;
    }


    /**
     * parses the endpoint from the incoming Websocket connection and generates a listener key based on the endpoint
     * only, so that all hosts listening will handle the connection to the endpoint
     *
     * @param sec
     * @param request
     * @return
     */
    public String getListenerKey(ServerEndpointConfig sec, HandshakeRequest request){

        String endpoint = sec.getPath();
        return endpoint;
    }


}