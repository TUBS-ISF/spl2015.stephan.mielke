package de.tubs.ips.chat.client;

import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 */
public class ChatClient implements ChatListener, Serializable, Runnable {

    private static final Pattern pGet = Pattern.compile(
        "^(/get)\\s+(\\w+?)\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern pGetAll = Pattern.compile(
        "^(/getall)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

    protected void help() {
        original();
        System.out.printf("zum Holen alter Nachrichten: %s%n",
            pGet.pattern());
        System.out.printf("zum Holen alter Nachrichten: %s%n",
            pGetAll.pattern());
    }

    private boolean checkInput(String input) {
        if (original(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = pGet.matcher(input)) != null && matcher.matches()) {
            get(matcher);
            return true;
        } else if ((matcher = pGetAll.matcher(input)) != null
            && matcher.matches()) {
            getAll(matcher);
            return true;
        }
        return false;
    }

    private void get(Matcher matcher) {
        String channelName = matcher.group(2);
        int i = Integer.parseInt(matcher.group(3));
        System.out.printf(
            "Zeige die letzten %d Nachrichten des Channel \"%s\" an%n", i,
            channelName);
        try {
            final ChatMessage[] messages = getChat().get(channelName, i);
            for (final ChatMessage message : messages) {
                printMessage(message);
            }
        } catch (final RemoteException re) {
            // #ifdef Debug_Mode
            // @ re.printStackTrace();
            // #endif
        } catch (final IllegalArgumentException iae) {
            // #ifdef Debug_Mode
            // @ iae.printStackTrace();
            // #endif
        }
    }

    private void getAll(Matcher matcher) {
        String channelName = matcher.group(2);
        System.out.printf("Zeige alle Nachrichten des Channel \"%s\" an%n",
            channelName);
        try {
            final ChatMessage[] messages = getChat().getAll(channelName);
            for (final ChatMessage message : messages) {
                printMessage(message);
            }
        } catch (final RemoteException re) {
            // #ifdef Debug_Mode
            // @ re.printStackTrace();
            // #endif
        } catch (final IllegalArgumentException iae) {
            // #ifdef Debug_Mode
            // @ iae.printStackTrace();
            // #endif
        }
    }

}
