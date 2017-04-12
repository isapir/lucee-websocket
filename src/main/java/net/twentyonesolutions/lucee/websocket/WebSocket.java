package net.twentyonesolutions.lucee.websocket;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import net.twentyonesolutions.lucee.app.LuceeApps;
import net.twentyonesolutions.lucee.core.Dumper;
import net.twentyonesolutions.lucee.websocket.connections.ConnectionManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.twentyonesolutions.lucee.websocket.Constants.*;

/**
 * Created by Admin on 10/12/2016.
 *
 * Provides a wrapper around the javax.websocket.Session so that CFML code can interact with it more efficiently
 */
public class WebSocket implements javax.websocket.Session, Objects {


    final Session wsSession;
//    final Struct struct;


    public WebSocket(Session websocketSession){

        this.wsSession = websocketSession;
//        this.struct = (Struct)websocketSession.getUserProperties().get(HandshakeHandler.WEBSOCKET_METHOD_LUCEE_STRUCT);  // created and set during handshake

        /*
        this.struct.setEL(WEBSOCKET_METHOD_WEBSOCKET_ID, this.wsSession.getId());

        Struct pathParams = LuceeApps.getCreationUtil().createStruct();
        this.struct.put(WEBSOCKET_METHOD_PATH_PARAMETERS, pathParams);
        for (Map.Entry<String, String> e : wsSession.getPathParameters().entrySet()){
            pathParams.setEL(e.getKey(), e.getValue());
        } //*/

        /*
        Map<String, String> pathParameters = wsSession.getPathParameters();
        Iterator it = pathParameters.entrySet().iterator();
        while (it.hasNext()){

            Map.Entry e = (Map.Entry) it.next();
            pathParams.setEL(LuceeApps.toKey(e.getKey().toString()), e.getValue());
        } //*/

        /*
        Set<Session> openSessions = this.wsSession.getOpenSessions();
        Array arr = LuceeApps.getCreationUtil().createArray();
        Iterator<Session> it = openSessions.iterator();
        while (it.hasNext()){

            try {
                arr.append(it.next().getId());
            } catch (PageException e) {
                e.printStackTrace();
            }
        }

        this.struct.put("websocketSessions", arr);  // a list of the open Websocket Session IDs from the remote IP
        //*/
    }

    /**
     * Get the container that created this session.
     *
     * @return the container that created this session.
     */
    @Override
    public WebSocketContainer getContainer() {
        return wsSession.getContainer();
    }

    /**
     * Registers a {@link MessageHandler} for incoming messages. Only one
     * {@link MessageHandler} may be registered for each message type (text,
     * binary, pong). The message type will be derived at runtime from the
     * provided {@link MessageHandler} instance. It is not always possible to do
     * this so it is better to use
     * {@link #addMessageHandler(Class, MessageHandler.Partial)}
     * or
     * {@link #addMessageHandler(Class, MessageHandler.Whole)}.
     *
     * @param handler The message handler for a incoming message
     * @throws IllegalStateException If a message handler has already been
     *                               registered for the associated message type
     */
    @Override
    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
        wsSession.addMessageHandler(handler);
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        return wsSession.getMessageHandlers();
    }

    @Override
    public void removeMessageHandler(MessageHandler listener) {
        wsSession.removeMessageHandler(listener);
    }

    @Override
    public String getProtocolVersion() {
        return wsSession.getProtocolVersion();
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return wsSession.getNegotiatedSubprotocol();
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return wsSession.getNegotiatedExtensions();
    }

    @Override
    public boolean isSecure() {
        return wsSession.isSecure();
    }

    @Override
    public boolean isOpen() {
        return wsSession.isOpen();
    }

    /**
     * Get the idle timeout for this session.
     *
     * @return The current idle timeout for this session in milliseconds. Zero
     * or negative values indicate an infinite timeout.
     */
    @Override
    public long getMaxIdleTimeout() {
        return wsSession.getMaxIdleTimeout();
    }

    /**
     * Set the idle timeout for this session.
     *
     * @param timeout The new idle timeout for this session in milliseconds.
     *                Zero or negative values indicate an infinite timeout.
     */
    @Override
    public void setMaxIdleTimeout(long timeout) {
        wsSession.setMaxIdleTimeout(timeout);
    }

    /**
     * Set the current maximum buffer size for binary messages.
     *
     * @param max The new maximum buffer size in bytes
     */
    @Override
    public void setMaxBinaryMessageBufferSize(int max) {
        wsSession.setMaxBinaryMessageBufferSize(max);
    }

    /**
     * Get the current maximum buffer size for binary messages.
     *
     * @return The current maximum buffer size in bytes
     */
    @Override
    public int getMaxBinaryMessageBufferSize() {
        return wsSession.getMaxBinaryMessageBufferSize();
    }

    /**
     * Set the maximum buffer size for text messages.
     *
     * @param max The new maximum buffer size in characters.
     */
    @Override
    public void setMaxTextMessageBufferSize(int max) {
        wsSession.setMaxTextMessageBufferSize(max);
    }

    /**
     * Get the maximum buffer size for text messages.
     *
     * @return The maximum buffer size in characters.
     */
    @Override
    public int getMaxTextMessageBufferSize() {
        return wsSession.getMaxTextMessageBufferSize();
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return wsSession.getAsyncRemote();
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return wsSession.getBasicRemote();
    }

    /**
     * Provides a unique identifier for the session. This identifier should not
     * be relied upon to be generated from a secure random source.
     *
     * @return A unique identifier for the session.
     */
    @Override
    public String getId() {
        return wsSession.getId();
    }

    /**
     * Close the connection to the remote end point using the code
     * {@link CloseReason.CloseCodes#NORMAL_CLOSURE} and an
     * empty reason phrase.
     *
     * @throws IOException if an I/O error occurs while the WebSocket session is
     *                     being closed.
     */
    @Override
    public void close() throws IOException {
        wsSession.close();
    }

    /**
     * Close the connection to the remote end point using the specified code
     * and reason phrase.
     *
     * @param closeReason The reason the WebSocket session is being closed.
     * @throws IOException if an I/O error occurs while the WebSocket session is
     *                     being closed.
     */
    @Override
    public void close(CloseReason closeReason) throws IOException {
        wsSession.close(closeReason);
    }

    @Override
    public URI getRequestURI() {
        return wsSession.getRequestURI();
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return null;
    }

    @Override
    public String getQueryString() {
        return wsSession.getQueryString();
    }

    @Override
    public Map<String, String> getPathParameters() {
        return wsSession.getPathParameters();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return wsSession.getUserProperties();
    }

    @Override
    public Principal getUserPrincipal() {
        return wsSession.getUserPrincipal();
    }

    /**
     * Obtain the set of open sessions associated with the same local endpoint
     * as this session.
     *
     * @return The set of currently open sessions for the local endpoint that
     * this session is associated with.
     */
    @Override
    public Set<Session> getOpenSessions() {
        return wsSession.getOpenSessions();
    }

    /**
     * Registers a {@link MessageHandler} for partial incoming messages. Only
     * one {@link MessageHandler} may be registered for each message type (text
     * or binary, pong messages are never presented as partial messages).
     *
     * @param clazz   The Class that implements T
     * @param handler The message handler for a incoming message
     * @throws IllegalStateException If a message handler has already been
     *                               registered for the associated message type
     * @since WebSocket 1.1
     */
    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) throws IllegalStateException {
        wsSession.addMessageHandler(clazz, handler);
    }

    /**
     * Registers a {@link MessageHandler} for whole incoming messages. Only
     * one {@link MessageHandler} may be registered for each message type (text,
     * binary, pong).
     *
     * @param clazz   The Class that implements T
     * @param handler The message handler for a incoming message
     * @throws IllegalStateException If a message handler has already been
     *                               registered for the associated message type
     * @since WebSocket 1.1
     */
    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) throws IllegalStateException {
        wsSession.addMessageHandler(clazz, handler);
    }


    public boolean sendBinary(ByteBuffer byteBuffer){

        if (wsSession.isOpen()) {

            try {

                wsSession.getBasicRemote().sendBinary(byteBuffer);
                return true;
            } catch (IOException ioe) {}
        }

        return false;
    }


    public boolean sendBinaryAsync(ByteBuffer byteBuffer){

        if (wsSession.isOpen()){

            wsSession.getAsyncRemote().sendBinary(byteBuffer);
            return true;
        }

        return false;
    }


    public boolean sendObject(Object o){

        if (wsSession.isOpen()) {

            try {

                wsSession.getBasicRemote().sendObject(o);
                return true;
            } catch (EncodeException e) {
                e.printStackTrace();
            } catch (IOException ioe) {}
        }

        return false;
    }


    public boolean sendObjectAsync(Object o) {

        if (wsSession.isOpen()){

            wsSession.getAsyncRemote().sendObject(o);
            return true;
        }

        return false;
    }


    public boolean sendText(String s){

        if (wsSession.isOpen()) {

            try {

                wsSession.getBasicRemote().sendText(s);
                return true;
            } catch (IOException ioe) {

                this.log(Log.LEVEL_WARN, ioe.toString());
            }
        }

        return false;
    }


    public boolean sendTextAsync(String s){

        if (wsSession.isOpen()){

            wsSession.getAsyncRemote().sendText(s);
            return true;
        }

        return false;
    }


    public ConnectionManager getConnectionManager(){

        return (ConnectionManager)wsSession.getUserProperties().get(HandshakeHandler.KEY_CONN_MANAGER);
    }


    public lucee.runtime.type.scope.Session getSessionScope(){

        return WebsocketUtil.getSessionScope(this.wsSession);
    }


    public lucee.runtime.type.scope.Application getApplicationScope(){

        return WebsocketUtil.getApplicationScope(this.wsSession);
    }


    public void log(int logLevel, String message){

        this.getConnectionManager().log(logLevel, this.getId() + ": " + message);
    }


    /**
     *
     * @return a reference to the Set with the names of the channels to which this connection is subscribed
     */
    public Set<String> getChannels(){

        return (Set)wsSession.getUserProperties().get(PROPERTY_CHANNELS);
    }


    public int subscribe(String channelId){

        return this.getConnectionManager().subscribe(channelId, this);
    }


    public int unsubscribe(String channelId){

        return this.getConnectionManager().unsubscribe(channelId, this);
    }


    public void unsubscribeAll(){

        this.getConnectionManager().unsubscribeAll(this);
    }


    /* *
     *
     * @return a new List with the names of the channels to which this connection is subscribed
     * /
    public List<String> getChannelNames(){

        List<String> result = new ArrayList( (Set)wsSession.getUserProperties().get("channels") );
        return result;
    } //*/


    public Struct getStruct(){

        return (Struct)this.wsSession.getUserProperties().get(HandshakeHandler.KEY_LUCEE_STRUCT);
    }

    public javax.websocket.Session getSession(){

        return this.wsSession;
    }

    public String toString(){

        return this.getClass().getName() + ": " + this.wsSession.toString();
    }

    public boolean equals(Object other){

        return this.wsSession.equals(other);
    }

    public int hashCode(){

        return this.wsSession.hashCode();
    }



    //<editor-fold desc="Objects interface">


    /**
     * method to print out information to a object as HTML
     *
     * @param pageContext page context object
     * @param maxlevel    max level to display
     * @param properties  properties data
     * @return dump object to display
     */
    @Override
    public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {

        /*
        try {

            // TODO: use Reflection with core:lucee.runtime.dump.DumpUtil

            Class cl = Class.forName("lucee.runtime.dump.DumpUtil");

            // public static DumpData toDumpData(Object o, PageContext pageContext, int maxlevel, DumpProperties props)

            Method method = cl.getMethod("toDumpData", Object.class, PageContext.class, int.class, DumpProperties.class);

            // the method identifies the object as a Reference instead of a simple Java Object
            DumpTable result = (DumpTable)method.invoke(null,(Object)this, pageContext, maxlevel, properties);

            DumpData ddStruct = getStruct().toDumpData(pageContext, maxlevel, properties);
            result.appendRow(1, new Dumper.DumpString("[data]"), ddStruct);

            return result;

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return new Dumper.DumpString(e);
        }
        //*/

        DumpTable result = Dumper.toDumpData(this);
        DumpData ddStruct = getStruct().toDumpData(pageContext, maxlevel, properties);
        result.appendRow(1, new Dumper.DumpString("[data]"), ddStruct);
        return result;
    }

    /**
     * return property
     *
     * @param pc           PageContext
     * @param key          Name of the Property
     * @param defaultValue
     * @return return value of the Property
     */
    @Override
    public Object get(PageContext pc, Collection.Key key, Object defaultValue) {

        return getStruct().get(key, defaultValue);
    }

    /**
     * return property or getter of the ContextCollection
     *
     * @param pc  PageContext
     * @param key Name of the Property
     * @return return value of the Property
     * @throws PageException
     */
    @Override
    public Object get(PageContext pc, Collection.Key key) throws PageException {

        return getStruct().get(key);
    }

    /**
     * sets a property (Data Member) value of the object
     *
     * @param pc
     * @param propertyName property name to set
     * @param value        value to insert
     * @return value set to property
     * @throws PageException
     */
    @Override
    public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {

        return getStruct().set(propertyName, value);
    }

    /**
     * sets a property (Data Member) value of the object
     *
     * @param pc
     * @param propertyName property name to set
     * @param value        value to insert
     * @return value set to property
     */
    @Override
    public Object setEL(PageContext pc, Collection.Key propertyName, Object value) {

        return getStruct().setEL(propertyName, value);
    }

    /**
     * calls a method of the object
     *
     * @param pc
     * @param method name of the method to call
     * @param args  arguments to call method with
     * @return return value of the method
     * @throws PageException
     */
    @Override
    public Object call(PageContext pc, Collection.Key method, Object[] args) throws PageException {

        if (WEBSOCKET_METHOD_CLOSE.equals(method)){

            try {

                if (args.length > 0){

                    String reason = (args.length == 2) ? LuceeApps.getCastUtil().toString(args[1]) : "";
                    this.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(LuceeApps.getCastUtil().toIntValue(args[0])), reason));
                }
                else {

                    this.close();
                }
            } catch (Exception e) {
                log(Log.LEVEL_DEBUG, e.toString());
            }

            return null;
        }

        if (WEBSOCKET_METHOD_GET_ASYNC_REMOTE.equals(method))
            return this.getAsyncRemote();

        if (WEBSOCKET_METHOD_GET_BASIC_REMOTE.equals(method))
            return this.getBasicRemote();

        if (WEBSOCKET_METHOD_GET_CHANNELS.equals(method)){

            Array result = LuceeApps.getCreationUtil().createArray();

            for (String channel : this.getChannels())
                result.append(channel);

            return result;
        }

        if (WEBSOCKET_METHOD_GET_CONN_MANAGER.equals(method))
            return this.getConnectionManager();

        if (WEBSOCKET_METHOD_GET_ID.equals(method))
            return this.getId();

        if (WEBSOCKET_METHOD_GET_APPLICATION_SCOPE.equals(method))
            return this.getApplicationScope();

        if (WEBSOCKET_METHOD_GET_SESSION_SCOPE.equals(method))
            return this.getSessionScope();

        if (WEBSOCKET_METHOD_GET_MAX_IDLE_TIMEOUT.equals(method))
            return this.getMaxIdleTimeout();

        if (WEBSOCKET_METHOD_GET_OPEN_SESSIONS.equals(method))
            return this.getOpenSessions();

        if (WEBSOCKET_METHOD_GET_PATH_PARAMETERS.equals(method)){

            Struct result = LuceeApps.getCreationUtil().createStruct();
            result.putAll(this.getPathParameters());
            return result;
        }

        if (WEBSOCKET_METHOD_GET_REQUEST_URI.equals(method))
            return this.getRequestURI();

        if (WEBSOCKET_METHOD_GET_USER_PROPERTIES.equals(method))
            return this.getUserProperties();

        if (WEBSOCKET_METHOD_GET_WEBSOCKET_SESSION.equals(method))
            return this.wsSession;

        if (WEBSOCKET_METHOD_IS_OPEN.equals(method))
            return this.isOpen();

        if (WEBSOCKET_METHOD_IS_SECURE.equals(method))
            return this.isSecure();

        if (WEBSOCKET_METHOD_SEND_TEXT.equals(method))
            return this.sendText((String)args[0]);

        if (WEBSOCKET_METHOD_SEND_TEXT_ASYNC.equals(method))
            return this.sendTextAsync((String)args[0]);

        if (WEBSOCKET_METHOD_SET_MAX_IDLE_TIMEOUT.equals(method)){

            this.setMaxIdleTimeout(LuceeApps.getCastUtil().toLong(args[0]));
            return null;
        }

        if (WEBSOCKET_METHOD_SUBSCRIBE.equals(method))
            return this.subscribe((String)args[0]);

        if (WEBSOCKET_METHOD_UNSUBSCRIBE.equals(method))
            return this.unsubscribe((String)args[0]);

        if (WEBSOCKET_METHOD_UNSUBSCRIBE_ALL.equals(method)){

            this.unsubscribeAll();
            return null;
        }

//        return Reflector.callMethod(this, methodName, arguments);     // requires Lucee-core module

        throw LuceeApps.toPageException(new UnsupportedOperationException((method + "() is not implemented")));
    }

    /**
     * call a method of the Object with named arguments
     *
     * @param pc         PageContext
     * @param methodName name of the method
     * @param args       Named Arguments for the method
     * @return return result of the method
     * @throws PageException
     */
    @Override
    public Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct args) throws PageException {

        throw LuceeApps.toPageException(new UnsupportedOperationException(("callWithNamedValues() is not implemented")));
    }

    /**
     * cast the castable value to a string, other than the Method toString, this
     * Method can throw a Exception
     *
     * @return String representation of the Object
     * @throws PageException thrown when fail to convert to a string
     */
    @Override
    public String castToString() throws PageException {
        return null;
    }

    /**
     * cast the castable value to a string, return the default value, when the
     * method is not castable
     *
     * @param defaultValue default value returned in case not able to convert to a string
     * @return String representation of the Object
     */
    @Override
    public String castToString(String defaultValue) {
        return null;
    }

    /**
     * cast the castable value to a boolean value
     *
     * @return boolean Value representation of the Object
     * @throws PageException thrown when fail to convert to a boolean
     */
    @Override
    public boolean castToBooleanValue() throws PageException {
        return false;
    }

    /**
     * cast the castable value to a boolean value
     *
     * @param defaultValue default value returned in case not able to convert to a boolean
     * @return boolean Value representation of the Object
     */
    @Override
    public Boolean castToBoolean(Boolean defaultValue) {
        return null;
    }

    /**
     * cast the castable value to a double value
     *
     * @return double Value representation of the Object
     * @throws PageException thrown when fail to convert to a double value
     */
    @Override
    public double castToDoubleValue() throws PageException {
        return 0;
    }

    /**
     * cast the castable value to a double value
     *
     * @param defaultValue default value returned in case not able to convert to a date object
     * @return double Value representation of the Object
     */
    @Override
    public double castToDoubleValue(double defaultValue) {
        return 0;
    }

    /**
     * cast the castable value to a date time object
     *
     * @return date time representation of the Object
     * @throws PageException thrown when fails to convert to a date object
     */
    @Override
    public DateTime castToDateTime() throws PageException {
        return null;
    }

    /**
     * cast the castable value to a date time object
     *
     * @param defaultValue returned when it is not possible to cast to a
     *                     dateTime object
     * @return date time representation of the Object
     */
    @Override
    public DateTime castToDateTime(DateTime defaultValue) {
        return null;
    }

    @Override
    public int compareTo(String str) throws PageException {
        return 0;
    }

    @Override
    public int compareTo(boolean b) throws PageException {
        return 0;
    }

    @Override
    public int compareTo(double d) throws PageException {
        return 0;
    }

    @Override
    public int compareTo(DateTime dt) throws PageException {
        return 0;
    }

    //</editor-fold>

}