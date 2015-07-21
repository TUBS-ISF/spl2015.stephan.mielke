import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public aspect WechselNickname {
    public Pattern Client.ChatClient.pNick = Pattern.compile(
        "^(/nick)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

    pointcut helpMethod(): execution(public void Client.ChatClient.help()) && this(Client.ChatClient);

    after(): helpMethod() {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();
        System.out.printf("zum Wechseln des Nicks: %s%n", t.pNick.pattern());
    }

    pointcut checkInputMethod(String input): execution(private boolean Client.ChatClient.checkInput(String)) && args(input) && target(Client.ChatClient);

    boolean around(String input): checkInputMethod(input) {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();

        if (proceed(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = t.pNick.matcher(input)) != null && matcher.matches()) {
            t.nickChange(matcher);
            return true;
        }
        return false;
    }

    private void Client.ChatClient.nickChange(Matcher matcher) {
        String newNickname = matcher.group(2);
        if (getChat() != null) {
            try {
                getChat().changeNick(getNickname(), newNickname);
                System.out.printf(
                    "Wechsel den Nickname von \"%s\" zu \"%s\"%n",
                    getNickname(), newNickname);
                setNickname(newNickname);
            } catch (RemoteException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }
        }
    }
}
