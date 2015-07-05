package de.tubs.ips.chat.server;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;

import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author Stephan
 */
public class ChatImpl implements Chat {

	private static final String addMessage = "INSERT INTO MESSAGES(CHANNEL, MEMBER, TIME, MESSAGE) VALUES (?, ?, ?, ?)";
	private static final String getIMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id DESC FETCH FIRST ? ROWS ONLY";
	private static final String getAllMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id ASC";
	private final Connection connection;
	
	public ChatImpl() {
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
			// @ System.out.println("Table exists");
			// @ e.printStackTrace();
			// #endif
		}
		this.connection = connection;
	}
	
	public ChatMessage[] get(@NotNull String channel, int n)
			throws IllegalArgumentException, RemoteException {
		original(channel, n);
		
		List<ChatMessage> messages = new LinkedList<ChatMessage>();
		if (connection != null) {
			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement(getIMessagesFromChannel);
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
				// #ifdef Debug_Mode
				// @ e.printStackTrace();
				// #endif
				return new ChatMessage[0];
			}
		}
		return messages.toArray(new ChatMessage[messages.size()]);
	}

	public ChatMessage[] getAll(@NotNull String channel)
			throws IllegalArgumentException, RemoteException {
		List<ChatMessage> messages = new LinkedList<ChatMessage>();
		if (connection != null) {
			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement(getAllMessagesFromChannel);
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

	public void post(@NotNull final String channel,
			@NotNull final ChatMessage message) throws RemoteException {

		original(channel, message);
		
		if (connection != null) {
			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement(addMessage);
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
