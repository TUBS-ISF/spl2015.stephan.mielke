package de.tubs.ips.chat.server;

import com.beust.jcommander.Parameter;
import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.utils.Helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Stephan
 */
public class ChatServer implements Runnable {

    protected static boolean debug;
    protected final String NAME;
    protected final Chat chat;
    private Registry registry;

    /**
     * Erstellt einen Server mit einem Chat
     */
    public ChatServer(boolean debug, boolean db) throws RemoteException, SQLException {
        NAME = "chat";
        ChatServer.debug = debug;

        chat = new ChatImpl(debug, db);
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        ChatServer server = null;
        try {
            server = new ChatServer(args.length > 0 ? Boolean.valueOf(args[0]) : false, args.length > 1 ? Boolean.valueOf(args[1]) : true);
            server.run();
        } catch (RemoteException e) {
            System.err.println("Server can not be started");
            if (ChatServer.debug)
                e.printStackTrace();
            System.exit(-1);
        } catch (SQLException e) {
            System.err.println("Server can not be started");
            if (ChatServer.debug)
                e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Startet den VSBoardServer
     *
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    private void start() throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

        if (debug)
            System.out.println("chat created");

        final Chat remBoard = (Chat) UnicastRemoteObject.exportObject(chat, 0);

        if (debug)
            System.out.println("chat exported");

        registry.rebind(NAME, remBoard);

        if (debug)
            try {
                System.out.printf("chat in registry: %s%n", InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
    }

    private void exit() throws NotBoundException, RemoteException {
        registry.unbind(NAME);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        try {
            start();
        } catch (RemoteException e) {
            System.err.println("Server can not be started");
            if (ChatServer.debug)
                e.printStackTrace();
            System.exit(-1);
        } catch (AlreadyBoundException e) {
            System.err.println("Server can not be started");
            if (ChatServer.debug)
                e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("server is running");

        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("type \"exit\" for exit");
        } while (!scanner.nextLine().equals("exit"));

        scanner.close();

        try {
            exit();
        } catch (NotBoundException e) {
            System.err.println("Server error");
            if (ChatServer.debug)
                e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("Server error");
            if (ChatServer.debug)
                e.printStackTrace();
        }
        System.out.println("server shuting down");
        System.exit(0);
    }
}
