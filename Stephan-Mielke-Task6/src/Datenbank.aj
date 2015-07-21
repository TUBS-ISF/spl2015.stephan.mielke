import de.tubs.ips.chat.ChatMessage;

import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public aspect Datenbank {

    private static final String Server.ChatImpl.addMessage = "INSERT INTO MESSAGES(CHANNEL, MEMBER, TIME, MESSAGE) VALUES (?, ?, ?, ?)";
    private static final String Server.ChatImpl.getIMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id DESC FETCH FIRST ? ROWS ONLY";
    private static final String Server.ChatImpl.getAllMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id ASC";
    private Connection Server.ChatImpl.connection;

    public Connection Server.ChatImpl.getCon() {
        return connection;
    }

    public void Server.ChatImpl.setCon(Connection con) {
        connection = con;
    }

    pointcut constructor(): execution(Server.ChatImpl.new(..)) && this(Server.ChatImpl);

    after(): constructor() {
        String protocol = "jdbc:derby:";
        String dbName = "chatDB";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(protocol + dbName
                + ";create=true");
            Statement statement = connection.createStatement();
            statement
                .execute("CREATE TABLE APP.messages (\n"
                    + "id BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n"
                    + "channel VARCHAR(255) NOT NULL,\n"
                    + "member VARCHAR(255) NOT NULL,\n"
                    + "time BIGINT NOT NULL,\n"
                    + "message LONG VARCHAR NOT NULL\n" + ")");
        } catch (SQLException e) {
            // #ifdef Debug_Mode
            // @ e.printStackTrace();
            // #endif
        }

        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();

        t.setCon(connection);
    }

    pointcut getMessagesMethod(String channel, int n): execution(public ChatMessage[] Server.ChatImpl.getMessages(String, int)) && args(channel, n) && this(Server.ChatImpl);

    ChatMessage[] around(String channel, int n): getMessagesMethod(channel, n) {
        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();
        List<ChatMessage> messages = new LinkedList<ChatMessage>();
        if (t.getCon() != null) {
            try {
                PreparedStatement preparedStatement = t.getCon()
                    .prepareStatement(Server.ChatImpl.getIMessagesFromChannel);
                preparedStatement.setString(1, channel);
                preparedStatement.setInt(2, n);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    messages.add(new ChatMessage(channel, resultSet
                        .getString(1), resultSet.getLong(2), resultSet
                        .getString(3)));
                }
                Collections.reverse(messages);
            } catch (SQLException e) {
                return new ChatMessage[0];
            }
        }
        return messages.toArray(new ChatMessage[messages.size()]);
    }

    pointcut getAllMethod(String channel): execution(public ChatMessage[] getAll(String)) && args(channel) && target(Server.ChatImpl);

    ChatMessage[] around(String channel): getAllMethod(channel) {
        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();

        List<ChatMessage> messages = new LinkedList<ChatMessage>();
        if (t.getCon() != null) {
            try {
                PreparedStatement preparedStatement = t.getCon()
                    .prepareStatement(Server.ChatImpl.getAllMessagesFromChannel);
                preparedStatement.setString(1, channel);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    messages.add(new ChatMessage(channel, resultSet
                        .getString(1), resultSet.getLong(2), resultSet
                        .getString(3)));
                }
            } catch (SQLException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
                return new ChatMessage[0];
            }
        }
        return messages.toArray(new ChatMessage[messages.size()]);
    }

    pointcut postMethod(final String channel, final ChatMessage message): execution(public void post(String,
        ChatMessage)) && args(channel, message) && target(Server.ChatImpl);

    before(String channel, ChatMessage message): postMethod(channel, message) {
        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();

        if (t.getCon() != null) {
            try {
                PreparedStatement preparedStatement = t.getCon()
                    .prepareStatement(Server.ChatImpl.addMessage);
                preparedStatement.setString(1, channel);
                preparedStatement.setString(2, message.getMember());
                preparedStatement.setLong(3, message.getTime());
                preparedStatement.setString(4, message.getMessage());
                preparedStatement.execute();
            } catch (SQLException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }
        }
    }
}
