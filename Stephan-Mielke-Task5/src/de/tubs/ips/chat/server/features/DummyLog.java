package de.tubs.ips.chat.server.features;

import de.tubs.ips.chat.ChatMessage;
import de.tubs.ips.chat.server.IChatLog;

public class DummyLog implements IChatLog {

	public DummyLog() {
	}

	public void store(String channel, ChatMessage message) {
	}

	public ChatMessage[] load(String channel, int n) {
		return new ChatMessage[0];
	}

	public ChatMessage[] loadAll(String channel) {
		return new ChatMessage[0];
	}

}
