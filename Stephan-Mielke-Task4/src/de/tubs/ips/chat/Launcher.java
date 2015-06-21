package de.tubs.ips.chat;

import java.util.LinkedList;
import java.util.List;

import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.server.ChatServer;

public class Launcher {

	private final List<Runnable> modules;

	public Launcher(String[] args) {
		modules = new LinkedList<Runnable>();
		// #ifdef Client
		modules.add(new ChatClient(args.length > 0 ? args[0] : "anonymous"));
		// #endif
		// #ifdef Server
		// @ modules.add(new ChatServer());
		// #endif
	}

	public static void main(String[] args) {
		Launcher launcher = new Launcher(args);
		launcher.run();
	}

	private void run() {
		for (Runnable module : modules) {
			Thread thread = new Thread(module);
			thread.start();
		}
	}
}
