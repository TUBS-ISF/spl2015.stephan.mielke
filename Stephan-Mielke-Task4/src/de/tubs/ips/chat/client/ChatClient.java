package de.tubs.ips.chat.client;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;
import de.tubs.ips.chat.client.features.*;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 */
public class ChatClient implements ChatListener, Serializable, Runnable {

	private final List<IClientCommand> plugins;

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public ChatListener getCallback() {
		return callback;
	}

	public void setCallback(ChatListener callback) {
		this.callback = callback;
	}

	public String getNAME() {
		return NAME;
	}

	public Map<String, String> getChannels() {
		return channels;
	}

	protected static final Pattern pExit = Pattern.compile("^(/quit)\\s*$",
			Pattern.CASE_INSENSITIVE);

	protected static final Pattern pList = Pattern.compile("^(/list)\\s*$",
			Pattern.CASE_INSENSITIVE);
	protected static final Pattern pMsg = Pattern.compile(
			"^(/msg)\\s+(\\w+?)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
	protected static final Pattern pHelp = Pattern.compile("^(/help|/\\?)$");
	protected static final Pattern pConnect = Pattern.compile(
			"^/connect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$",
			Pattern.CASE_INSENSITIVE);
	protected static final Pattern pDisconnect = Pattern.compile(
			"^/disconnect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final long serialVersionUID = 899243651902242507L;

	protected final String NAME;
	private final Map<String, String> channels = new HashMap<String, String>();
	private final Scanner scanner = new Scanner(System.in);
	protected Chat chat;
	private String nickname;
	private String channel;
	private ChatListener callback;

	public ChatClient(String nickname) {
		this.nickname = nickname;
		NAME = "chat";
		plugins = new LinkedList<IClientCommand>();
		// #ifdef Channel_frei_waehlbar
		// @ plugins.add(new ChannelFreiWaehlbar(this));
		// #endif
		// #ifdef Wechsel_Nickname
		plugins.add(new WechselNickname(this));
		// #endif
		// #ifdef Zeige_alle_Insassen
		plugins.add(new ZeigeAlleInsassen(this));
		// #endif
		// #ifdef Zeige_alte_Nachrichten
		// @ plugins.add(new ZeigeAlteNachrichten(this));
		// #endif
	}

	public static void main(final String[] args) {
		final ChatClient app = new ChatClient(args.length > 0 ? args[0]
				: "anonymous");
		System.out.println("Client gestartet... gib help ein für Hilfe");
		app.start();
	}

	private void connect(final Matcher matcher) throws RemoteException,
			NotBoundException {
		System.out.printf("Verbinde zu %s::%s\n..\n", matcher.group(1),
				matcher.group(2));

		final Registry registry = LocateRegistry.getRegistry(matcher.group(1),
				Integer.parseInt(matcher.group(2)));

		chat = (Chat) registry.lookup(NAME);
		chat.listen(callback);
		System.out.println("mit dem Server verbunden");
	}

	/**
	 * beendet den Clienten
	 */
	protected void exit() {
		scanner.close();

		try {
			if (chat != null) {
				disconnect();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.out.println("Client wird beendet.");
		System.exit(0);
	}

	private String getShortKey(String channel) {
		String shortKey = "-";
		for (String key : channels.keySet()) {
			if (channels.get(key).equals(channel)) {
				shortKey = key;
			}
		}
		return shortKey;
	}

	private void promt() {
		promt(channel);
	}

	private void promt(String channel) {
		if (channel == null) {
			System.out.printf("[] ");
		} else {
			System.out.printf("[%s - %s] ", getShortKey(channel), channel);
		}
	}

	/**
     *
     */
	protected void help() {
		System.out.printf("zum Verbinden: %s%n", pConnect.pattern());
		System.out.printf("zum Disconecten: %s%n", pDisconnect.pattern());
		System.out.printf("zum Beenden: %s%n", pExit.pattern());
		for (IClientCommand p : plugins) {
			System.out.print(p.getHelp());
		}
	}

	public void newMessage(final ChatMessage message) throws RemoteException {
		printMessage(message);
	}

	public void privateMessage(ChatMessage message) throws RemoteException {
		System.out.println("NYI");
	}

	public String getName() throws RemoteException {
		return nickname;
	}

	public void post(String input) {
		if (channel == null) {
			System.out.println("Sie haben keinen aktiven Channel");
			return;
		}

		ChatMessage message = new ChatMessage(channel, nickname, input);

		try {
			chat.post(channel, message);
		} catch (RemoteException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}
	}

	public void printMessage(final ChatMessage message) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(message.getTime());
		String time = calendar.getTime().toString();

		promt(message.getChannel());

		System.out.printf("%s %s: %s%n", time, message.getMember(),
				message.getMessage());
	}

	protected void start() {
		try {
			callback = (ChatListener) UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}

		System.out
				.printf("Client ist gestartet. Sie sind noch nicht mit einem Server verbunden. Geben Sie \"/help\" für die Hilfe ein.");
		System.out.println();

		while (true) {
			if (chat == null) {
				disconnected();
			}
			if (chat != null) {
				connected();
			}
		}
	}

	private void disconnected() {
		while (true) {
			promt();
			final String input = scanner.nextLine();
			Matcher matcher;
			if ((matcher = pConnect.matcher(input)) != null
					&& matcher.matches()) {
				try {
					connect(matcher);
				} catch (final RemoteException re) {
					System.err.println("Konnte zum Server nicht verbinden.");
					// #ifdef Debug_Mode
					// @ re.printStackTrace();
					// #endif
				} catch (final NotBoundException nbe) {
					System.err.println("Dienst existiert nicht auf dem Server");
					// #ifdef Debug_Mode
					// @ nbe.printStackTrace();
					// #endif
				} catch (IllegalArgumentException e) {
					chat = null;
					// #ifdef Debug_Mode
					// @ e.printStackTrace();
					// #endif
				}
				return;
			} else if ((matcher = pExit.matcher(input)) != null
					&& matcher.matches()) {
				exit();
			} else if ((matcher = pHelp.matcher(input)) != null
					&& matcher.matches()) {
				help();
			}
		}
	}

	private void connected() {
		while (true) {
			promt();
			final String input = scanner.nextLine();
			Matcher matcher;
			if ((matcher = pDisconnect.matcher(input)) != null
					&& matcher.matches()) {
				disconnect();
				return;
			} else if ((matcher = pExit.matcher(input)) != null
					&& matcher.matches()) {
				exit();
			} else if ((matcher = pHelp.matcher(input)) != null
					&& matcher.matches()) {
				help();
			} else if ((matcher = pList.matcher(input)) != null
					&& matcher.matches()) {
				list();
			} else {
				boolean isChecked = false;
				for (IClientCommand p : plugins) {
					isChecked = isChecked | p.run(input);
				}
				if (!isChecked) {
					post(input);
				}
			}
		}
	}

	private void list() {
		Collection<String> cs = channels.values();

		if (cs.isEmpty()) {
			System.out.println("Sie befinden sich in keinem Channel");
		} else {
			System.out.println("Sie befinden sich in folgenden Channels");
			for (String key : channels.keySet()) {
				System.out.printf("[%s] %s%n", key, channels.get(key));
			}
		}
	}

	private void disconnect() {
		try {
			chat.unlisten(callback);
		} catch (RemoteException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}

		try {
			UnicastRemoteObject.unexportObject(callback, true);
		} catch (NoSuchObjectException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}

		channels.clear();
		channel = null;
		chat = null;

		System.out.println("Vom Server abgemeldet.");

	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used to
	 * create a thread, starting the thread causes the object's <code>run</code>
	 * method to be called in that separately executing thread.
	 * <p/>
	 * The general contract of the method <code>run</code> is that it may take
	 * any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	public void run() {
		System.out.println("Client gestartet... gib help ein für Hilfe");
		start();
	}
}
