package de.tubs.ips.chat;

import java.rmi.RemoteException;
//#ifdef Datenbank
//@import java.sql.SQLException;
//#endif

import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.server.ChatServer;

public class Launcher {

	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.run(args);
	}

	private void run(String[] args) {
		// #ifdef Client
		// @ ChatClient chatClient = new ChatClient(args.length > 0 ? args[0]
		// @ : "anonymous");
		// @ Thread clientThread = new Thread(chatClient);
		// @ clientThread.start();
		// #endif
		// #ifdef Server
		try {
			ChatServer chatServer = new ChatServer();
			Thread serverThread = new Thread(chatServer);
			serverThread.start();
		} catch (RemoteException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}
		// #ifdef Datenbank
		// @ catch (SQLException e) {
		// #ifdef Debug_Mode
		// @ e.printStackTrace();
		// #endif
		// @ }
		// #endif
		// #endif
	}
}
