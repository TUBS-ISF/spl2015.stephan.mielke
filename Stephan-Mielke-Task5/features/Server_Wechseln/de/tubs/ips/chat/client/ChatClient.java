package de.tubs.ips.chat.client;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 */
public class ChatClient implements ChatListener, Serializable, Runnable {

    protected static final Pattern pConnect = Pattern.compile(
        "^/connect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$",
        Pattern.CASE_INSENSITIVE);
    protected static final Pattern pDisconnect = Pattern.compile(
        "^/disconnect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$",
        Pattern.CASE_INSENSITIVE);

    protected void help() {
        original();
        System.out.printf("zum Verbinden: %s%n", pConnect.pattern());
        System.out.printf("zum Disconecten: %s%n", pDisconnect.pattern());
    }

    private boolean checkInput(String input) {
        if (original(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = pDisconnect.matcher(input)) != null
            && matcher.matches()) {
            disconnect();
            return true;
        }
        return false;
    }

    private void checkInputDisco(String input) {
        original(input);

        Matcher matcher;
        if ((matcher = pConnect.matcher(input)) != null
            && matcher.matches()) {
            try {
                connect(matcher);
            } catch (final RemoteException re) {
                System.err.println("Konnte zum Server nicht verbinden.");
                // #ifdef Debug_Mode
                // @ re.printStackTrace();
                // #endif
            } catch (final NotBoundException nbe) {
                System.err.println("Dienst existiert nicht auf dem Server");
                // #ifdef Debug_Mode
                // @ nbe.printStackTrace();
                // #endif
            } catch (IllegalArgumentException e) {
                chat = null;
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }
        }
    }

    private boolean checkInput(String input) {
        if (original(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = pDisconnect.matcher(input)) != null
            && matcher.matches()) {
            disconnect();
            return true;
        }
        return false;
    }

    private void connect(final Matcher matcher) throws RemoteException, NotBoundException {
        System.out.printf("Verbinde zu %s::%s\n..\n", matcher.group(1),
            matcher.group(2));

        final Registry registry = LocateRegistry.getRegistry(matcher.group(1),
            Integer.parseInt(matcher.group(2)));

        chat = (Chat) registry.lookup(NAME);
        chat.listen(callback);
        System.out.println("mit dem Server verbunden");
    }
}
