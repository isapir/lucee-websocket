package net.twentyonesolutions.lucee.websocket;

import lucee.runtime.type.Collection;
import net.twentyonesolutions.lucee.app.LuceeApps;

/**
 * Created by Admin on 3/17/2017.
 */
public class Constants {


    public static final Collection.Key LISTENER_METHOD_ON_CLOSE = LuceeApps.toKey("onClose");
    public static final Collection.Key LISTENER_METHOD_ON_HANDSHAKE = LuceeApps.toKey("onHandshake");
    public static final Collection.Key LISTENER_METHOD_ON_OPEN = LuceeApps.toKey("onOpen");
    public static final Collection.Key LISTENER_METHOD_ON_MESSAGE = LuceeApps.toKey("onMessage");
    public static final Collection.Key LISTENER_METHOD_ON_ERROR = LuceeApps.toKey("onError");

    public static final Collection.Key LISTENER_METHOD_ON_CHANNEL_OPEN = LuceeApps.toKey("onChannelOpen");
    public static final Collection.Key LISTENER_METHOD_ON_CHANNEL_CLOSE = LuceeApps.toKey("onChannelClose");
    public static final Collection.Key LISTENER_METHOD_ON_SUBSCRIBE = LuceeApps.toKey("onSubscribe");
    public static final Collection.Key LISTENER_METHOD_ON_UNSUBSCRIBE = LuceeApps.toKey("onUnsubscribe");


    public static final Collection.Key WEBSOCKET_METHOD_GET_ASYNC_REMOTE = LuceeApps.toKey("getAsyncRemote");
    public static final Collection.Key WEBSOCKET_METHOD_GET_BASIC_REMOTE = LuceeApps.toKey("getBasicRemote");
    public static final Collection.Key WEBSOCKET_METHOD_GET_CONN_MANAGER = LuceeApps.toKey("getConnectionManager");
    public static final Collection.Key WEBSOCKET_METHOD_GET_SESSION_SCOPE = LuceeApps.toKey("getSessionScope");
    public static final Collection.Key WEBSOCKET_METHOD_GET_APPLICATION_SCOPE = LuceeApps.toKey("getApplicationScope");
    public static final Collection.Key WEBSOCKET_METHOD_GET_ID = LuceeApps.toKey("getId");
    public static final Collection.Key WEBSOCKET_METHOD_GET_MAX_IDLE_TIMEOUT = LuceeApps.toKey("getMaxIdleTimeout");
    public static final Collection.Key WEBSOCKET_METHOD_GET_OPEN_SESSIONS = LuceeApps.toKey("getOpenSessions");
    public static final Collection.Key WEBSOCKET_METHOD_GET_USER_PROPERTIES = LuceeApps.toKey("getUserProperties");
    public static final Collection.Key WEBSOCKET_METHOD_GET_REQUEST_URI = LuceeApps.toKey("getRequestURI");
    public static final Collection.Key WEBSOCKET_METHOD_IS_OPEN = LuceeApps.toKey("isOpen");
    public static final Collection.Key WEBSOCKET_METHOD_IS_SECURE = LuceeApps.toKey("isSecure");
    public static final Collection.Key WEBSOCKET_METHOD_SEND_TEXT = LuceeApps.toKey("sendText");
    public static final Collection.Key WEBSOCKET_METHOD_SEND_TEXT_ASYNC = LuceeApps.toKey("sendTextAsync");
    public static final Collection.Key WEBSOCKET_METHOD_SET_MAX_IDLE_TIMEOUT = LuceeApps.toKey("setMaxIdleTimeout");
    public static final Collection.Key WEBSOCKET_METHOD_SUBSCRIBE = LuceeApps.toKey("subscribe");
    public static final Collection.Key WEBSOCKET_METHOD_UNSUBSCRIBE = LuceeApps.toKey("unsubscribe");
    public static final Collection.Key WEBSOCKET_METHOD_UNSUBSCRIBE_ALL = LuceeApps.toKey("unsubscribeAll");



    //    public static final Collection.Key LISTENER_METHOD_ON_CONNECTION_EVENT = LuceeApps.toKey("onConnectionEvent");


    public static final Collection.Key ARG_SESSION_SCOPE = LuceeApps.toKey("sessionScope");
    public static final Collection.Key ARG_APPLICATION_SCOPE = LuceeApps.toKey("applicationScope");

    public static final Collection.Key ARG_CLOSE_REASON = LuceeApps.toKey("closeReason");
    public static final Collection.Key ARG_ENDPOINT_CONFIG = LuceeApps.toKey("endpointConfig");
    public static final Collection.Key ARG_ERROR = LuceeApps.toKey("error");
    public static final Collection.Key ARG_MESSAGE = LuceeApps.toKey("message");
    public static final Collection.Key ARG_REQUEST = LuceeApps.toKey("request");
    public static final Collection.Key ARG_RESPONSE = LuceeApps.toKey("response");
    public static final Collection.Key ARG_WEBSOCKET = LuceeApps.toKey("websocket");


    public static final Collection.Key METHOD_BROADCAST = LuceeApps.toKey("broadcast");
    public static final Collection.Key METHOD_GET_CHANNEL = LuceeApps.toKey("getChannel");
    public static final Collection.Key METHOD_GET_CHANNELS = LuceeApps.toKey("getChannels");
    public static final Collection.Key METHOD_SUBSCRIBE = LuceeApps.toKey("subscribe");
    public static final Collection.Key METHOD_UNSUBSCRIBE = LuceeApps.toKey("unsubscribe");
    public static final Collection.Key METHOD_UNSUBSCRIBE_ALL = LuceeApps.toKey("unsubscribeAll");
    public static final Collection.Key METHOD_LOG = LuceeApps.toKey("log");


    public static final String PROPERTY_CHANNELS = "channels";

}