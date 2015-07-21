import de.tubs.ips.chat.ChatMessage;

import java.util.*;

public aspect NachrichtenLog {

    private final Map<String, List<ChatMessage>> Server.ChatImpl.log = Collections
        .synchronizedMap(new HashMap<String, List<ChatMessage>>());

    public Map<String, List<ChatMessage>> Server.ChatImpl.getLog() {
        return log;
    }

    pointcut getMessagesMethod(String channel, int n): execution(public ChatMessage[] Server.ChatImpl.getMessages(String, int)) && args(channel, n) && this(Server.ChatImpl);

    ChatMessage[] around(String channel, int n): getMessagesMethod(channel, n) {
        proceed(channel, n);
        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();

        List<ChatMessage> messages = new LinkedList<ChatMessage>();
        if (t.getLog().containsKey(channel)) {
            messages = t.getLog().get(channel);
            messages = messages.subList(0, Math.min(n, messages.size()));
            Collections.reverse(messages);
        }
        return messages.toArray(new ChatMessage[messages.size()]);
    }

    pointcut getAllMethod(String channel): execution(public ChatMessage[] getAll(String)) && args(channel) && target(Server.ChatImpl);

    ChatMessage[] around(String channel): getAllMethod(channel) {
        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();

        List<ChatMessage> messages = new LinkedList<ChatMessage>();
        if (t.getLog().containsKey(channel)) {
            messages = t.getLog().get(channel);
            Collections.reverse(messages);
        }
        return messages.toArray(new ChatMessage[messages.size()]);
    }

    pointcut postMethod(final String channel, final ChatMessage message): execution(public void post(String,
        ChatMessage)) && args(channel, message) && target(Server.ChatImpl);

    before(String channel, ChatMessage message): postMethod(channel, message) {
        Server.ChatImpl t = (Server.ChatImpl) thisJoinPoint.getThis();
        if (!t.getLog().containsKey(channel)) {

            synchronized (t.getLog()) {
                if (!t.getLog().containsKey(channel)) {
                    t.getLog().put(channel, Collections
                        .synchronizedList(new LinkedList<ChatMessage>()));
                }
            }
        }
        t.getLog().get(channel).add(message);
    }
}
