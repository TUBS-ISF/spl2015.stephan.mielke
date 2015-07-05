package de.tubs.ips.chat.server.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tubs.ips.chat.ChatMessage;
import de.tubs.ips.chat.server.IChatLog;

public class NormalLog implements IChatLog {

	private final Map<String, List<ChatMessage>> log;

	public NormalLog() {
		log = Collections
				.synchronizedMap(new HashMap<String, List<ChatMessage>>());
	}

	public void store(String channel, ChatMessage message) {
		synchronized (log) {
			if (!log.containsKey(channel)) {
				log.put(channel, Collections
						.synchronizedList(new LinkedList<ChatMessage>()));
			}
		}

		log.get(channel).add(message);
	}

	public ChatMessage[] load(String channel, int n) {
		List<ChatMessage> messages = new LinkedList<ChatMessage>();
		if (log.containsKey(channel)) {
			messages = log.get(channel);
			messages = messages.subList(0, Math.min(n, messages.size()));
			Collections.reverse(messages);
		}
		return messages.toArray(new ChatMessage[messages.size()]);
	}

	public ChatMessage[] loadAll(String channel) {
		List<ChatMessage> messages = new LinkedList<ChatMessage>();
		if (log.containsKey(channel)) {
			messages = log.get(channel);
			Collections.reverse(messages);
		}
		return messages.toArray(new ChatMessage[messages.size()]);
	}

}
