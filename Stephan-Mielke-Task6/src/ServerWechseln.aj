import de.tubs.ips.chat.Chat;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public aspect ServerWechseln {

    public Pattern Client.ChatClient.pConnect = Pattern.compile(
        "^/connect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$",
        Pattern.CASE_INSENSITIVE);
    public Pattern Client.ChatClient.pDisconnect = Pattern.compile(
        "^/disconnect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$",
        Pattern.CASE_INSENSITIVE);

    pointcut helpMethod(): execution(public void Client.ChatClient.help()) && this(Client.ChatClient);

    after(): helpMethod() {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();
        System.out.printf("zum Betreten eines Channels: %s%n",
            t.pJoin.pattern());
        System.out.printf("zum Verlassen eines Channels: %s%n",
            t.pPart.pattern());
        System.out.printf("zum Channel wechseln: %s%n", t.pSwitch.pattern());
        System.out.printf("zum Channel wechseln und schreiben: %s%n",
            t.pPost.pattern());
    }

    pointcut checkInputMethod(String input): execution(private boolean checkInput(String)) && args(input) && target(Client.ChatClient);

    boolean around(String input): checkInputMethod(input) {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();

        if (proceed(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = t.pDisconnect.matcher(input)) != null
            && matcher.matches()) {
            t.disconnect();
            return true;
        }
        return false;
    }

    pointcut checkInputDiscoMethod(String input): execution(private void checkInputDisco(String)) && args(input) && target(Client.ChatClient);

    void around(String input): checkInputDiscoMethod(input) {
        proceed(input);

        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();

        Matcher matcher;
        if ((matcher = t.pConnect.matcher(input)) != null
            && matcher.matches()) {
            try {
                t.connect(matcher);
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
                t.chat = null;
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }
        }
    }

    private void Client.ChatClient.connect(final Matcher matcher) throws RemoteException, NotBoundException {
        System.out.printf("Verbinde zu %s::%s\n..\n", matcher.group(1),
            matcher.group(2));

        final Registry registry = LocateRegistry.getRegistry(matcher.group(1),
            Integer.parseInt(matcher.group(2)));

        chat = (Chat) registry.lookup(NAME);
        chat.listen(callback);
        System.out.println("mit dem Server verbunden");
    }
}
