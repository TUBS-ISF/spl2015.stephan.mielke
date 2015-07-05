package de.tubs.ips.chat.client;

import de.tubs.ips.chat.ChatListener;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 */
public class ChatClient implements ChatListener, Serializable, Runnable {

    private static final Pattern pJoin = Pattern.compile(
        "^(/join)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern pPart = Pattern.compile(
        "^(/part)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern pSwitch = Pattern.compile("^(/(\\d+))\\s*$",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern pPost = Pattern.compile(
        "^(/(\\d+))\\s+(.*?)\\s*$", Pattern.CASE_INSENSITIVE);

    protected void help() {
        original();
        System.out.printf("zum Betreten eines Channels: %s%n",
            pJoin.pattern());
        System.out.printf("zum Verlassen eines Channels: %s%n",
            pPart.pattern());
        System.out.printf("zum Channel wechseln: %s%n", pSwitch.pattern());
        System.out.printf("zum Channel wechseln und schreiben: %s%n",
            pPost.pattern());
    }

    private boolean checkInput(String input) {
        if (original(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = pJoin.matcher(input)) != null && matcher.matches()) {
            join(matcher);
            return true;
        } else if ((matcher = pPart.matcher(input)) != null
            && matcher.matches()) {
            part(matcher);
            return true;
        } else if ((matcher = pSwitch.matcher(input)) != null
            && matcher.matches()) {
            switchActiveChannel(matcher);
            return true;
        } else if ((matcher = pPost.matcher(input)) != null
            && matcher.matches()) {
            post(matcher);
            return true;
        }
        return false;
    }

    private void post(Matcher matcher) {
        if (!switchActiveChannel(matcher)) {
            return;
        }

        String input = matcher.group(3);
        post(input);
    }

    private void part(Matcher matcher) {
        String channelName = matcher.group(2);
        if (!getChannels().values().contains(channelName)) {
            System.out
                .printf("Sie sind nicht im Channel \"%s\" und können diesen somit nicht Verlassen%n",
                    channelName);
            return;
        }

        try {
            getChat().unlisten(channelName, getCallback());
        } catch (RemoteException e) {
            // #ifdef Debug_Mode
            // @ e.printStackTrace();
            // #endif
        }

        getChannels().remove(channelName);

        if (getChannel().equals(channelName)) {
            if (getChannels().values().isEmpty()) {
                setChannel(null);
            } else {
                setChannel(
                    getChannels()
                        .values()
                        .toArray(
                            new String[getChannels().values().size()])[0]);
            }
        }

        System.out.printf("Sie haben den Channel \"%s\" verlassen%n",
            channelName);
    }

    private void join(Matcher matcher) {
        String channelName = matcher.group(2);

        if (getChannels().values().contains(channelName)) {
            System.out
                .printf("Sie sind schon im Channel \"%s\"%n", channelName);
            return;
        }

        try {
            getChat().listen(channelName, getCallback());
            setChannel(channelName);

            int i = 0;
            String c;
            do {
                i++;
                c = getChannels().get(String.valueOf(i));
            } while (c != null);

            getChannels().put(String.valueOf(i), channelName);

            System.out.printf("Sie sind dem Channel \"%s\" beigetreten%n",
                channelName);
        } catch (RemoteException e) {
            // #ifdef Debug_Mode
            // @ e.printStackTrace();
            // #endif
        }
    }
}
