import de.tubs.ips.chat.ChatMessage;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public aspect ZeigeAlteNachrchten {
    public Pattern Client.ChatClient.pGet = Pattern.compile(
        "^(/get)\\s+(\\w+?)\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
    public Pattern Client.ChatClient.pGetAll = Pattern.compile(
        "^(/getall)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

    pointcut helpMethod(): execution(public void Client.ChatClient.help()) && this(Client.ChatClient);

    after(): helpMethod() {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();
        System.out.printf("zum Holen alter Nachrichten: %s%n",
            t.pGet.pattern());
        System.out.printf("zum Holen alter Nachrichten: %s%n",
            t.pGetAll.pattern());
    }

    pointcut checkInputMethod(String input): execution(private boolean Client.ChatClient.checkInput(String)) && args(input) && target(Client.ChatClient);

    boolean around(String input): checkInputMethod(input) {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();

        if (proceed(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = t.pGet.matcher(input)) != null && matcher.matches()) {
            t.getMessages(matcher);
            return true;
        } else if ((matcher = t.pGetAll.matcher(input)) != null
            && matcher.matches()) {
            t.getAll(matcher);
            return true;
        }
        return false;
    }

    private void Client.ChatClient.getMessages(Matcher matcher) {
        String channelName = matcher.group(2);
        int i = Integer.parseInt(matcher.group(3));
        System.out.printf(
            "Zeige die letzten %d Nachrichten des Channel \"%s\" an%n", i,
            channelName);
        try {
            final ChatMessage[] messages = getChat().getMessages(channelName, i);
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

    private void Client.ChatClient.getAll(Matcher matcher) {
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
