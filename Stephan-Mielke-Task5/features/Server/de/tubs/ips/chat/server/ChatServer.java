package de.tubs.ips.chat.server;

import de.tubs.ips.chat.Chat;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Stephan
 */
public class ChatServer implements Runnable {

    protected final String NAME;
    protected final Chat chat;
    private Registry registry;

    /**
     * Erstellt einen Server mit einem Chat
     */
    public ChatServer() {
        NAME = "chat";

        chat = new ChatImpl();
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        ChatServer server = null;
        server = new ChatServer();
        server.run();
    }

    /**
     * Startet den VSBoardServer
     *
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    private void start() throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

        // #ifdef Debug_Mode
        // @ System.out.println("chat created");
        // #endif

        final Chat remBoard = (Chat) UnicastRemoteObject.exportObject(chat, 0);

        // #ifdef Debug_Mode
        // @ System.out.println("chat exported");
        // #endif

        registry.rebind(NAME, remBoard);

        // #ifdef Debug_Mode
        // @ try {
        // @ System.out.printf("chat in registry: %s%n", InetAddress
        // @ .getLocalHost().getHostAddress());
        // @ } catch (UnknownHostException e) {
        // @ e.printStackTrace();
        // @ }
        // #endif
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's <code>run</code> method to be called in that separately executing thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        try {
            start();
        } catch (RemoteException e) {
            System.err.println("Server can not be started");
            // #ifdef Debug_Mode
            // @ e.printStackTrace();
            // #endif
            System.exit(-1);
        } catch (AlreadyBoundException e) {
            System.err.println("Server can not be started");
            // #ifdef Debug_Mode
            // @ e.printStackTrace();
            // #endif
            System.exit(-1);
        }
        System.out.println("server is running");

        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        } while (true);
    }
}
