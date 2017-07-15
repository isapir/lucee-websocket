package net.twentyonesolutions.lucee.websocket.connections;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import net.twentyonesolutions.lucee.app.LuceeApp;
import net.twentyonesolutions.lucee.app.LuceeAppListener;
import net.twentyonesolutions.lucee.app.LuceeApps;
import net.twentyonesolutions.lucee.core.Dumper;
import net.twentyonesolutions.lucee.websocket.HandshakeHandler;
import net.twentyonesolutions.lucee.websocket.WebSocket;
import net.twentyonesolutions.lucee.websocket.WebsocketUtil;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static net.twentyonesolutions.lucee.websocket.Constants.*;

/**
 * Created by Igal on 3/5/2017.
 */
public class ConnectionManager implements lucee.runtime.type.Objects {

	// different connection manager for each endpoint
	static ConcurrentHashMap<String, ConnectionManager> connectionManagers = new ConcurrentHashMap();

	static int cleanupInterval = 60;

	/**
	 * registers the LuceeAppListener with the ConnectionManager for the passed endpoint. the ConnectionManager is
	 * created if not exists.
	 *
	 * @param endpoint
	 * @param luceeAppListener
	 * @return
	 */
	public static ConnectionManager setAppListener(String endpoint, LuceeAppListener luceeAppListener) {

		ConnectionManager result = getConnectionManager(endpoint);
		result.setAppListener(luceeAppListener);
		return result;
	}

	/**
	 * return the ConnectionManager for the passed endpoint. the ConnectionManager is created if not exists.
	 *
	 * @param endpoint
	 * @return
	 */
	public static ConnectionManager getConnectionManager(String endpoint) {

		ConnectionManager result = connectionManagers.computeIfAbsent(endpoint, ConnectionManager::new);
		return result;
	}

	/**
	 * returns the ConnectionManager for the endpoint of the passed websocket
	 *
	 * @param wsSession
	 * @return
	 */
	public static ConnectionManager getConnectionManager(Session wsSession) {

		return getConnectionManager((String) wsSession.getUserProperties().get(HandshakeHandler.KEY_ENDPOINT_PATH));
	}

	/**
	 *
	 * @param sec
	 * @return
	 */
	public static ConnectionManager getConnectionManager(ServerEndpointConfig sec) {

		return getConnectionManager(sec.getPath());
	}

	final ConcurrentMap<String, Channel> channels = new ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER);
	final String id;
	private LuceeAppListener appListener;
	private boolean isLogEnabled = false;

	private ConnectionManager(String id) {

		this.id = id;

		LuceeApp luceeApp = LuceeApps.getAppListener(this.id).getApp();

		List<String> availableLoggers = luceeApp.getLoggerNames();
		if (!availableLoggers.contains("websocket")) {
			// websocket log is not configured, log a warning message to application log.
			luceeApp.log(Log.LEVEL_WARN,
					"WebSocket logs are disabled. To enable logging please define a logger named [websocket] in the Web Admin.",
					"websocket", "application");
		}
		else {
			this.isLogEnabled = true;
		}

		Timer timer = new Timer();
		timer.schedule(new Cleanup(), cleanupInterval * 1000, cleanupInterval * 1000);
	}

	public int subscribe(String chanId, WebSocket websocket) {

		Channel channel = getChannel(chanId, true);
		int subs = channel.subscribe(websocket);

		WebsocketUtil.invokeListenerMethodWithNamedArgs(this.appListener, LISTENER_METHOD_ON_SUBSCRIBE, "channel",
				chanId, "subscribers", subs, "websocket", websocket, "connectionManager", this);

		log(Log.LEVEL_INFO, websocket.getId() + " subscribed to " + chanId);

		return channel.getSubscriberCount();
	}

	public int unsubscribe(String chanId, WebSocket websocket) {

		Channel channel = getChannel(chanId);

		if (channel != null) {

			int subs = channel.unsubscribe(websocket);

			WebsocketUtil.invokeListenerMethodWithNamedArgs(this.appListener, LISTENER_METHOD_ON_UNSUBSCRIBE, "channel",
					chanId, "subscribers", subs, "websocket", websocket, "connectionManager", this);

			log(Log.LEVEL_INFO, websocket.getId() + " unsubscribed from " + chanId);

			if (subs > 0)
				return subs;

			removeChannel(chanId);
		}

		return 0;
	}

	public void log(int level, String message) {

		if (!this.isLogEnabled)
			return;

		Log logger = LuceeApps.getAppListener(this.id).getApp().getConfigWeb().getLog("websocket");
		logger.log(level, "websocket", message);
	}

	public void broadcast(String chanId, CharSequence s) {

		Channel channel = getChannel(chanId);

		if (channel == null) {

			log(Log.LEVEL_WARN, "channel " + chanId + " not found.  broadcast failed.");
			return;
		}

		Set<WebSocket> subscribers = channel.getSubscribers();

		log(Log.LEVEL_DEBUG, "channel " + chanId + " broadcasting message to " + subscribers.size() + " connections");

		for (WebSocket connection : subscribers) {

			boolean sent = connection.sendText(s.toString());

			if (!sent) {

				if (!connection.isOpen())
					unsubscribeAll(connection);
			}
		}
	}

	/**
	 * unsubscribe the connection from all of the channels to which it is subscribed by calling unsubscribe() for each
	 * chanel id
	 *
	 * @param websocket
	 */
	public void unsubscribeAll(WebSocket websocket) {

		// iterate over getChannelNames() which returns a copy and not getChannels() which is a reference to the actual
		// list so that we don't get a ConcurrentModification error
		ArrayList<String> channelNames = new ArrayList(websocket.getChannels());

		for (String chanId : channelNames) {

			unsubscribe(chanId, websocket);
		}
	}

	/**
	 * removes a channel and notifies "channel-closed", channelId
	 *
	 * @param chanId
	 */
	public synchronized void removeChannel(String chanId) {

		Channel channel = channels.remove(chanId);

		if (channel != null) {

			WebsocketUtil.invokeListenerMethodWithNamedArgs(this.appListener, LISTENER_METHOD_ON_CHANNEL_CLOSE,
					"channel", chanId, "connectionManager", this);

			log(Log.LEVEL_INFO, "channel " + chanId + " closed");
		}
	}

	/**
	 * returns a Channel object by its id. if the channel does not exist and doCreate is true then creates the channel
	 * and notifies "channel-opened", channelId
	 *
	 * @param chanId
	 * @param doCreate
	 * @return
	 */
	public Channel getChannel(String chanId, boolean doCreate) {

		Channel channel = channels.get(chanId);

		if (channel == null && doCreate) {

			synchronized (this) {

				channel = channels.get(chanId);

				if (channel == null) {

					channel = new Channel(chanId);
					channels.put(chanId, channel);

					WebsocketUtil.invokeListenerMethodWithNamedArgs(this.appListener, LISTENER_METHOD_ON_CHANNEL_OPEN,
							"channel", chanId, "connectionManager", this);

					log(Log.LEVEL_INFO, "channel " + chanId + " opened");
				}
			}
		}

		return channel;
	}

	/**
	 * returns getChannel(chanId, false)
	 *
	 * @param chanId
	 * @return
	 */
	public Channel getChannel(String chanId) {

		return getChannel(chanId, false);
	}

	/**
	 * returns a new Map of open channels where the key is the channel id and the value is the number of subscribers
	 *
	 * @return
	 */
	public Map<String, Integer> getChannels() {

		Map<String, Integer> result = new HashMap();

		for (Map.Entry<String, Channel> e : channels.entrySet()) {

			result.put(e.getKey(), e.getValue().getSubscriberCount());
		}

		return result;
	}

	public String getId() {

		return id;
	}

	public LuceeAppListener getAppListener() {

		return this.appListener;
	}

	public void setAppListener(LuceeAppListener listener) {

		this.appListener = listener;
	}

	// <editor-fold desc="Objects interface">

	/**
	 * method to print out information to a object as HTML
	 *
	 * @param pageContext
	 *            page context object
	 * @param maxlevel
	 *            max level to display
	 * @param properties
	 *            properties data
	 * @return dump object to display
	 */
	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {

		DumpTable result = Dumper.toDumpData(this);
		return result;
	}

	/**
	 * return property
	 *
	 * @param pc
	 *            PageContext
	 * @param key
	 *            Name of the Property
	 * @param defaultValue
	 * @return return value of the Property
	 */
	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return null;
	}

	/**
	 * return property or getter of the ContextCollection
	 *
	 * @param pc
	 *            PageContext
	 * @param key
	 *            Name of the Property
	 * @return return value of the Property
	 * @throws PageException
	 */
	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		return null;
	}

	/**
	 * sets a property (Data Member) value of the object
	 *
	 * @param pc
	 * @param propertyName
	 *            property name to set
	 * @param value
	 *            value to insert
	 * @return value set to property
	 * @throws PageException
	 */
	@Override
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {
		return null;
	}

	/**
	 * sets a property (Data Member) value of the object
	 *
	 * @param pc
	 * @param propertyName
	 *            property name to set
	 * @param value
	 *            value to insert
	 * @return value set to property
	 */
	@Override
	public Object setEL(PageContext pc, Collection.Key propertyName, Object value) {
		return null;
	}

	/**
	 * calls a method of the object
	 *
	 * @param pc
	 * @param methodName
	 *            name of the method to call
	 * @param arguments
	 *            arguments to call method with
	 * @return return value of the method
	 * @throws PageException
	 */
	@Override
	public Object call(PageContext pc, Collection.Key methodName, Object[] arguments) throws PageException {

		if (METHOD_BROADCAST.equals(methodName)) {

			broadcast((String) arguments[0], (CharSequence) arguments[1]);
			return null;
		}

		if (METHOD_GET_CHANNEL.equals(methodName))
			return this.getChannel((String) arguments[0]);

		if (METHOD_GET_CHANNELS.equals(methodName))
			return this.getChannels();

		if (METHOD_LOG.equals(methodName)) {

			this.log(Log.LEVEL_INFO, arguments[0].toString());
			return null;
		}

		if (METHOD_SUBSCRIBE.equals(methodName)) {

			this.subscribe((String) arguments[0], (WebSocket) arguments[1]);
			return null;
		}

		if (METHOD_UNSUBSCRIBE.equals(methodName)) {

			this.unsubscribe((String) arguments[0], (WebSocket) arguments[1]);
			return null;
		}

		if (METHOD_UNSUBSCRIBE_ALL.equals(methodName)) {

			this.unsubscribeAll((WebSocket) arguments[0]);
			return null;
		}

		throw LuceeApps.toPageException(new UnsupportedOperationException((methodName + "() is not implemented")));
	}

	/**
	 * call a method of the Object with named arguments
	 *
	 * @param pc
	 *            PageContext
	 * @param methodName
	 *            name of the method
	 * @param args
	 *            Named Arguments for the method
	 * @return return result of the method
	 * @throws PageException
	 */
	@Override
	public Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct args) throws PageException {

		throw LuceeApps.toPageException(
				new UnsupportedOperationException(("callWithNamedValues() is not implemented")));
	}

	/**
	 * cast the castable value to a string, other than the Method toString, this Method can throw a Exception
	 *
	 * @return String representation of the Object
	 * @throws PageException
	 *             thrown when fail to convert to a string
	 */
	@Override
	public String castToString() throws PageException {
		return null;
	}

	/**
	 * cast the castable value to a string, return the default value, when the method is not castable
	 *
	 * @param defaultValue
	 *            default value returned in case not able to convert to a string
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
	 * @throws PageException
	 *             thrown when fail to convert to a boolean
	 */
	@Override
	public boolean castToBooleanValue() throws PageException {
		return false;
	}

	/**
	 * cast the castable value to a boolean value
	 *
	 * @param defaultValue
	 *            default value returned in case not able to convert to a boolean
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
	 * @throws PageException
	 *             thrown when fail to convert to a double value
	 */
	@Override
	public double castToDoubleValue() throws PageException {
		return 0;
	}

	/**
	 * cast the castable value to a double value
	 *
	 * @param defaultValue
	 *            default value returned in case not able to convert to a date object
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
	 * @throws PageException
	 *             thrown when fails to convert to a date object
	 */
	@Override
	public DateTime castToDateTime() throws PageException {
		return null;
	}

	/**
	 * cast the castable value to a date time object
	 *
	 * @param defaultValue
	 *            returned when it is not possible to cast to a dateTime object
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

	// </editor-fold>

	/**
	 *
	 */
	class Cleanup extends TimerTask {

		/**
		 * The action to be performed by this timer task.
		 */
		@Override
		public void run() {

			log(Log.LEVEL_TRACE, Cleanup.class.getCanonicalName() + " is running at " + (new Date()).toString());

			for (Channel channel : channels.values()) {

				Set<WebSocket> connections = channel.getSubscribers();

				for (WebSocket connection : connections) {

					if (!connection.isOpen()) {

						unsubscribe(channel.getId(), connection); // unsubscribe() will also notify observers
					}
				}
			}
		}
	}

}