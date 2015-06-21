package de.tubs.ips.chat.server.features;

import de.tubs.ips.chat.ChatMessage;
import de.tubs.ips.chat.server.IChatLog;

import java.sql.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DatabaseLog implements IChatLog {

	private static final String addMessage = "INSERT INTO MESSAGES(CHANNEL, MEMBER, TIME, MESSAGE) VALUES (?, ?, ?, ?)";
	private static final String getIMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id DESC FETCH FIRST ? ROWS ONLY";
	private static final String getAllMessagesFromChannel = "SELECT MEMBER, TIME, MESSAGE FROM MESSAGES WHERE CHANNEL = ? ORDER BY id ASC";
	private final Connection connection;

	public DatabaseLog() {
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

	public void store(String channel, ChatMessage message) {
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

	public ChatMessage[] load(String channel, int n) {
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

	public ChatMessage[] loadAll(String channel) {
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

}
