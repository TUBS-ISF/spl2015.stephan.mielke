import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public aspect ZeigeAlleInsassen {
    public Pattern Client.ChatClient.pNames = Pattern.compile(
        "^(/names)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

    pointcut helpMethod(): execution(public void Client.ChatClient.help()) && this(Client.ChatClient);

    after(): helpMethod() {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();
        System.out.printf(
            "zum Anzeige aller Mitglieder eines Channels: %s%n",
            t.pNames.pattern());
    }

    pointcut checkInputMethod(String input): execution(private boolean Client.ChatClient.checkInput(String)) && args(input) && target(Client.ChatClient);

    boolean around(String input): checkInputMethod(input) {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();

        if (proceed(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = t.pNames.matcher(input)) != null && matcher.matches()) {
            t.names(matcher);
            return true;
        }
        return false;
    }

    private void Client.ChatClient.names(Matcher matcher) {
        String channelName = matcher.group(2);
        System.out.printf("Mitglieder des Channel \"%s\" sind:%n", channelName);
        try {
            String[] users = getChat().getUsers(channelName);
            for (String user : users) {
                System.out.println(user);
            }
        } catch (RemoteException e) {
            // #ifdef Debug_Mode
            // @ e.printStackTrace();
            // #endif
        }
    }
}
