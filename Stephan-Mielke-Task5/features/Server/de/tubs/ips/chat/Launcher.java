package de.tubs.ips.chat;

import de.tubs.ips.chat.server.ChatServer;

public class Launcher {
    public static void main(String[] args) {
        original(args);
        Thread thread = new Thread(new ChatServer());
        thread.start();
    }
}
