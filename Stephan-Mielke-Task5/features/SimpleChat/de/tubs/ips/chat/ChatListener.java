package de.tubs.ips.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Stephan
 */
public interface ChatListener extends Remote {
    /**
     * Methode zum Anzeigen von neuen Nachrichten
     *
     * @param message
     *
     * @throws RemoteException
     */
    void newMessage(ChatMessage message) throws RemoteException;

    void privateMessage(ChatMessage message) throws RemoteException;

    String getName() throws RemoteException;
}
