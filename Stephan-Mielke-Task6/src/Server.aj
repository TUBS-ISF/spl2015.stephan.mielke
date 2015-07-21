import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;
import org.jetbrains.annotations.NotNull;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public aspect Server {

    pointcut mainMethod(): execution(public static void main(String[])) && target(de.tubs.ips.chat.Launcher);

    after(): mainMethod() {
        Thread thread = new Thread(new ChatServer());
        thread.start();
    }


    public class ChatImpl implements Chat {

        private final HashMap<String, Set<ChatListener>> channels;
        private final HashMap<String, ChatListener> users;

        public ChatImpl() {
            channels = new HashMap<String, Set<ChatListener>>();
            users = new HashMap<String, ChatListener>();
        }

        public ChatMessage[] getMessages(@NotNull String channel, int n)
            throws IllegalArgumentException, RemoteException {
            if (n < 0) {
                throw new IllegalArgumentException(
                    "der Index von get() muss größergleich 0 sein");
            }
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
}
