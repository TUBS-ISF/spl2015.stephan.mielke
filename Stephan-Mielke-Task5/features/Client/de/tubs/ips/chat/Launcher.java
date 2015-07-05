package de.tubs.ips.chat;

import de.tubs.ips.chat.client.ChatClient;

public class Launcher {
    public static void main(String[] args) {
        original(args);
        Thread thread = new Thread(new ChatClient(args.length > 0 ? args[0] : "anonymous"));
        thread.start();
    }
}
