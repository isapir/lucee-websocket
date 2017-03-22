package net.twentyonesolutions.lucee.websocket;

import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by Admin on 12/6/2016.
 *
 * This version of HandshakeHandler ignores the Host portion of the Request URI when determining the listenerKey
 */
public class HandshakeHandlerWithHost extends HandshakeHandler {


    /**
     * parses the endpoint from the incoming Websocket connection and generates a listener key based on both the
     * httpHost and the endpoint, so that each httpHost can have its own listener
     *
     * @param sec
     * @param request
     * @return
     */
    @Override
    public String getListenerKey(ServerEndpointConfig sec, HandshakeRequest request){

        String endpoint = sec.getPath();
        String httpHost = request.getRequestURI().getAuthority();
        if (httpHost.indexOf('@') > -1){
            httpHost = httpHost.substring(httpHost.indexOf('@') + 1);
        }

        return httpHost + endpoint;
    }

}