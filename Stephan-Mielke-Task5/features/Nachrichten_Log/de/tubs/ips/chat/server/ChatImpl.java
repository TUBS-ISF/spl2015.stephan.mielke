package de.tubs.ips.chat.server;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatMessage;
import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Stephan
 */
public class ChatImpl implements Chat {

    private final Map<String, List<ChatMessage>> log;

    public ChatImpl() {
        log = Collections
            .synchronizedMap(new HashMap<String, List<ChatMessage>>());
    }

    public ChatMessage[] get(@NotNull String channel, int n)
        throws IllegalArgumentException, RemoteException {

        original(channel, n);

        List<ChatMessage> messages = new LinkedList<ChatMessage>();
        if (log.containsKey(channel)) {
            messages = log.get(channel);
            messages = messages.subList(0, Math.min(n, messages.size()));
            Collections.reverse(messages);
        }
        return messages.toArray(new ChatMessage[messages.size()]);
    }

    public ChatMessage[] getAll(@NotNull String channel)
        throws IllegalArgumentException, RemoteException {
        List<ChatMessage> messages = new LinkedList<ChatMessage>();
        if (log.containsKey(channel)) {
            messages = log.get(channel);
            Collections.reverse(messages);
        }
        return messages.toArray(new ChatMessage[messages.size()]);
    }

    public void post(@NotNull final String channel,
                     @NotNull final ChatMessage message) throws RemoteException {

        original(channel, message);

        synchronized (log) {
            if (!log.containsKey(channel)) {
                log.put(channel, Collections
                    .synchronizedList(new LinkedList<ChatMessage>()));
            }
        }

        log.get(channel).add(message);
    }
}
