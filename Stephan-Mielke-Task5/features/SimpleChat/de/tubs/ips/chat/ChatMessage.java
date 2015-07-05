package de.tubs.ips.chat;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * @author Stephan
 */
public class ChatMessage implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4198423796850187816L;

    /**
     * ID des Absenders
     */
    private final String member;
    /**
     * Inhalt der Nachricht
     */
    private final String message;
    private final String channel;
    /**
     * Title der Nachricht
     */
    private long time;

    /**
     * Erstellt eine neue Nachricht
     *
     * @param message
     */
    public ChatMessage(@NotNull final String channel,
                       @NotNull final String member, @NotNull final String message) {
        this(channel, member, System.currentTimeMillis(), message);
    }

    /**
     * Erstellt eine neue Nachricht
     *
     * @param message
     */
    public ChatMessage(@NotNull final String channel,
                       @NotNull final String member, final long time,
                       @NotNull final String message) {
        this.channel = channel;
        this.member = member;
        this.time = time;
        this.message = message;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     * @return
     */
    public String getMember() {
        return member;
    }

    public String getChannel() {
        return channel;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Channel: " + channel + "\nForm: " + member + "\nMessage: "
            + message;
    }

    public void updateTime() {
        time = System.currentTimeMillis();
    }
}
