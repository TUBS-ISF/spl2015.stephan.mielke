package de.tubs.ips.chat.server;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;

import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Stephan
 */
public class ChatImpl implements Chat {

	private final HashMap<String, Set<ChatListener>> channels;
	private final HashMap<String, ChatListener> users;
	
	public ChatImpl() {
		channels = new HashMap<String, Set<ChatListener>>();
		users = new HashMap<String, ChatListener>();
	}

	public ChatMessage[] get(@NotNull String channel, int n)
			throws IllegalArgumentException, RemoteException {
		if (n < 0)
			throw new IllegalArgumentException(
					"der Index von get() muss größergleich 0 sein");
		return new ChatMessage[0];
	}

	public ChatMessage[] getAll(@NotNull String channel)
			throws IllegalArgumentException, RemoteException {
		return new ChatMessage[0];
	}

	public void listen(@NotNull final String channel,
			@NotNull final ChatListener listener) throws RemoteException {
		// listen(listener);

		synchronized (channels) {
			Set<ChatListener> listeners = channels.get(channel);
			if (listeners == null) {
				listeners = new HashSet<ChatListener>();
				channels.put(channel, listeners);
			}
			listeners.add(listener);
		}
	}

	public void listen(@NotNull final ChatListener listener)
			throws RemoteException {
		synchronized (users) {
			String name = listener.getName();

			ChatListener oldListener = users.get(name);

			if (oldListener != null && listener != oldListener) {
				throw new IllegalArgumentException(
						String.format(
								"another user with the name \"%s\" is already connected",
								name));
			}

			users.put(listener.getName(), listener);
		}
	}

	public void unlisten(@NotNull final ChatListener listener)
			throws RemoteException {
		synchronized (users) {
			users.remove(listener.getName());
		}
		/*
		 * synchronized (channels) { for (Set<ChatListener> listeners :
		 * channels.values()) { listeners.remove(listener); } }
		 */
	}

	public void changeNick(@NotNull String oldNick, @NotNull String newNick)
			throws RemoteException {
		synchronized (users) {
			ChatListener listener = users.get(oldNick);
			if (listener == null) {
				throw new IllegalArgumentException(String.format(
						"No user with the name \"%s\"", oldNick));
			}

			users.put(newNick, listener);
			users.remove(oldNick);
		}
	}

	public void unlisten(@NotNull final String channel,
			@NotNull final ChatListener listener) throws RemoteException {
		synchronized (channels) {
			Set<ChatListener> listeners = channels.get(channel);
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}

	public void post(@NotNull final String channel,
			@NotNull final ChatMessage message) throws RemoteException {

		message.updateTime();
		synchronized (channels) {

			Set<ChatListener> listeners = channels.get(channel);

			if (listeners != null) {
				final Iterator<ChatListener> iter = listeners.iterator();
				while (iter.hasNext()) {
					final ChatListener listener = iter.next();
					// #ifdef Debug_Mode
					// @ System.out.println("send to " + listener);
					// #endif
					try {
						listener.newMessage(message);
					} catch (final RemoteException re) {
						// #ifdef Debug_Mode
						// @ System.out.println("remove zombie" + listener);
						// #endif
						iter.remove();
					}
				}
			}
		}
	}

	public ChatListener getChatListener(@NotNull String member)
			throws RemoteException {
		ChatListener listener = users.get(member);

		if (listener == null) {
			throw new IllegalStateException(String.format(
					"The user \"%s\" is not online", member));
		}

		return listener;
	}

	public String[] getUsers(@NotNull String channel) throws RemoteException {
		List<String> users = new LinkedList<String>();

		final Set<ChatListener> listeners = channels.get(channel);

		if (listeners == null) {
			return new String[0];
		}

		synchronized (listeners) {
			for (ChatListener listener : listeners) {
				try {
					users.add(listener.getName());
				} catch (RemoteException e) {
					// #ifdef Debug_Mode
					// @ e.printStackTrace();
					// #endif
				}
			}
		}

		return users.toArray(new String[users.size()]);
	}
}
