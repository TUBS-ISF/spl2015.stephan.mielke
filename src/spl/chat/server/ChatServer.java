package spl.chat.server;

import spl.chat.Chat;
import spl.chat.utils.Helper;

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
public class ChatServer {

    protected static boolean debug;
    protected final String NAME;
    protected final Chat chat;
    private Registry registry;

    /**
     * Erstellt einen Server mit einem Chat
     */
    public ChatServer() throws RemoteException, SQLException {
        Properties properties = null;
        try {
            properties = Helper.getConfig(Paths.get("server.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (properties != null) {
            NAME = properties.getProperty("name");
            debug = Boolean.parseBoolean(properties.getProperty("debug"));
        } else {
            NAME = "chat";
            debug = true;
        }

        chat = new ChatImpl(debug);
        registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        ChatServer server = null;
        try {
            server = new ChatServer();
            server.start();
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
        } catch (SQLException e) {
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
            server.exit();
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

    /**
     * Startet den VSBoardServer
     *
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    private void start() throws RemoteException, AlreadyBoundException {
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
}
