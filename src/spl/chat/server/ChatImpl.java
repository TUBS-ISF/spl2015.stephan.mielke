package spl.chat.server;

import org.jetbrains.annotations.NotNull;
import spl.chat.Chat;
import spl.chat.ChatListener;
import spl.chat.ChatMessage;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

/**
 * @author Stephan
 */
public class ChatImpl implements Chat {

    private static final String addMessage = "INSERT INTO MESSAGES(CHANNEL, MEMBER, TIME, MESSAGE) VALUES (?, ?, ?, ?)";
    private static final String getIMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id DESC FETCH FIRST ? ROWS ONLY";
    private static final String getAllMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id ASC";
    private final boolean debug;
    private final HashMap<String, Set<ChatListener>> channels;
    private final HashMap<String, ChatListener> users;
    private final Connection connection;
    //private static final String getAllMessages = "SELECT CHANNEL, MESSAGE FROM MESSAGES ORDER BY id";

    public ChatImpl(final boolean debug) throws SQLException {
        this.debug = debug;
        channels = new HashMap<String, Set<ChatListener>>();
        users = new HashMap<String, ChatListener>();

        String protocol = "jdbc:derby:";
        String dbName = "chatDB";

        connection = DriverManager.getConnection(protocol + dbName
                + ";create=true");

        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE APP.messages (\n" +
                    "id BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n" +
                    "channel VARCHAR(255) NOT NULL,\n" +
                    "member VARCHAR(255) NOT NULL,\n" +
                    "time BIGINT NOT NULL,\n" +
                    "message LONG VARCHAR NOT NULL\n" +
                    ")");
        } catch (SQLException e) {
            if (debug) {
                System.out.println("Table exists");
                e.printStackTrace();
            }
        }
    }

    public ChatMessage[] get(@NotNull String channel, int n) throws IllegalArgumentException, RemoteException {
        if (n < 0)
            throw new IllegalArgumentException("der Index von get() muss größergleich 0 sein");

        List<ChatMessage> messages = new LinkedList<ChatMessage>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(getIMessagesFromChannel);
            preparedStatement.setString(1, channel);
            preparedStatement.setInt(2, n);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                messages.add(new ChatMessage(channel, resultSet.getString(1), resultSet.getLong(2), resultSet.getString(3)));
            }

            Collections.reverse(messages);

        } catch (SQLException e) {
            if (debug)
                e.printStackTrace();
            return new ChatMessage[0];
        }

        return messages.toArray(new ChatMessage[messages.size()]);
    }

    public ChatMessage[] getAll(@NotNull String channel) throws IllegalArgumentException, RemoteException {
        List<ChatMessage> messages = new LinkedList<ChatMessage>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(getAllMessagesFromChannel);
            preparedStatement.setString(1, channel);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                messages.add(new ChatMessage(channel, resultSet.getString(1), resultSet.getLong(2), resultSet.getString(3)));
            }

        } catch (SQLException e) {
            if (debug)
                e.printStackTrace();
            return new ChatMessage[0];
        }

        return messages.toArray(new ChatMessage[messages.size()]);
    }

    public void listen(@NotNull final String channel, @NotNull final ChatListener listener) throws RemoteException {
        //listen(listener);

        synchronized (channels) {
            Set<ChatListener> listeners = channels.get(channel);
            if (listeners == null) {
                listeners = new HashSet<ChatListener>();
                channels.put(channel, listeners);
            }
            listeners.add(listener);
        }
    }

    public void listen(@NotNull final ChatListener listener) throws RemoteException {
        synchronized (users) {
            String name = listener.getName();

            ChatListener oldListener = users.get(name);

            if (oldListener != null && listener != oldListener) {
                throw new IllegalArgumentException(String.format("another user with the name \"%s\" is already connected", name));
            }

            users.put(listener.getName(), listener);
        }
    }

    public void unlisten(@NotNull final ChatListener listener) throws RemoteException {
        synchronized (users) {
            users.remove(listener.getName());
        }
        /*synchronized (channels) {
            for (Set<ChatListener> listeners : channels.values()) {
                listeners.remove(listener);
            }
        }*/
    }

    public void changeNick(@NotNull String oldNick, @NotNull String newNick) throws RemoteException {
        synchronized (users) {
            ChatListener listener = users.get(oldNick);
            if (listener == null) {
                throw new IllegalArgumentException(String.format("No user with the name \"%s\"", oldNick));
            }

            users.put(newNick, listener);
            users.remove(oldNick);
        }
    }

    public void unlisten(@NotNull final String channel, @NotNull final ChatListener listener) throws RemoteException {
        synchronized (channels) {
            Set<ChatListener> listeners = channels.get(channel);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    public void post(@NotNull final String channel, @NotNull final ChatMessage message) throws RemoteException {

        message.updateTime();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(addMessage);

            preparedStatement.setString(1, channel);
            preparedStatement.setString(2, message.getMember());
            preparedStatement.setLong(3, message.getTime());
            preparedStatement.setString(4, message.getMessage());

            preparedStatement.execute();

        } catch (SQLException e) {
            if (debug)
                e.printStackTrace();
        }

        synchronized (channels) {

            Set<ChatListener> listeners = channels.get(channel);

            if (listeners != null) {
                final Iterator<ChatListener> iter = listeners.iterator();
                while (iter.hasNext()) {
                    final ChatListener listener = iter.next();
                    if (debug)
                        System.out.println("send to " + listener);
                    try {
                        listener.newMessage(message);
                    } catch (final RemoteException re) {
                        if (debug)
                            System.out.println("remove zombie" + listener);
                        iter.remove();
                    }
                }
            }
        }
    }

    public ChatListener getChatListener(@NotNull String member) throws RemoteException {
        ChatListener listener = users.get(member);

        if (listener == null) {
            throw new IllegalStateException(String.format("The user \"%s\" is not online", member));
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
                    if (debug)
                        e.printStackTrace();
                }
            }
        }

        return users.toArray(new String[users.size()]);
    }
}
