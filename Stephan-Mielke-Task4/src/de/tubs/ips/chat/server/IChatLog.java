package de.tubs.ips.chat.server;

import de.tubs.ips.chat.ChatMessage;

public interface IChatLog {
	void store(String channel, ChatMessage message);

	ChatMessage[] load(String channel, int n);

	ChatMessage[] loadAll(String channel);

}
