package de.tubs.ips.chat;

import org.jetbrains.annotations.NotNull;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 */
public interface Chat extends Remote {
	/**
	 * Gibt die neutes n Nachrichten zurück
	 *
	 * @param channel
	 * @param n
	 * @return
	 * @throws IllegalArgumentException
	 *             n < 0
	 * @throws RemoteException
	 */
	ChatMessage[] get(@NotNull String channel, int n)
			throws IllegalArgumentException, RemoteException;

	ChatMessage[] getAll(@NotNull String channel)
			throws IllegalArgumentException, RemoteException;

	/**
	 * Registriert sich als Listener am Board
	 *
	 * @param channel
	 * @param listener
	 * @throws RemoteException
	 */
	void listen(@NotNull String channel, @NotNull ChatListener listener)
			throws RemoteException;

	void listen(@NotNull ChatListener listener) throws RemoteException;

	void unlisten(@NotNull String channel, @NotNull ChatListener listener)
			throws RemoteException;

	void unlisten(@NotNull ChatListener listener) throws RemoteException;

	void changeNick(@NotNull String oldNick, @NotNull String newNick)
			throws RemoteException;

	ChatListener getChatListener(@NotNull String name) throws RemoteException;

	/**
	 * Sendet eine Nachricht an das Board und verteilt diese an alle Listener
	 *
	 * @param channel
	 * @param message
	 * @throws RemoteException
	 */
	void post(@NotNull String channel, @NotNull ChatMessage message)
			throws RemoteException;

	String[] getUsers(@NotNull String channel) throws RemoteException;
}
