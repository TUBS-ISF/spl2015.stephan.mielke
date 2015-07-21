import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public aspect ChannelFreiWaehlbar {

    public Pattern Client.ChatClient.pJoin = Pattern.compile(
        "^(/join)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
    public Pattern Client.ChatClient.pPart = Pattern.compile(
        "^(/part)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
    public Pattern Client.ChatClient.pSwitch = Pattern.compile("^(/(\\d+))\\s*$",
        Pattern.CASE_INSENSITIVE);
    public Pattern Client.ChatClient.pPost = Pattern.compile(
        "^(/(\\d+))\\s+(.*?)\\s*$", Pattern.CASE_INSENSITIVE);

    pointcut helpMethod(): execution(public void Client.ChatClient.help()) && this(Client.ChatClient);

    after(): helpMethod() {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();
        System.out.printf("zum Verbinden: %s%n", t.pConnect.pattern());
        System.out.printf("zum Disconecten: %s%n", t.pDisconnect.pattern());
    }

    pointcut checkInputMethod(String input): execution(private boolean Client.ChatClient.checkInput(String)) && args(input) && target(Client.ChatClient);

    boolean around(String input): checkInputMethod(input) {
        Client.ChatClient t = (Client.ChatClient) thisJoinPoint.getThis();

        if (proceed(input)) {
            return true;
        }

        Matcher matcher;
        if ((matcher = t.pJoin.matcher(input)) != null && matcher.matches()) {
            t.join(matcher);
            return true;
        } else if ((matcher = t.pPart.matcher(input)) != null
            && matcher.matches()) {
            t.part(matcher);
            return true;
        } else if ((matcher = t.pSwitch.matcher(input)) != null
            && matcher.matches()) {
            t.switchActiveChannel(matcher);
            return true;
        } else if ((matcher = t.pPost.matcher(input)) != null
            && matcher.matches()) {
            t.post(matcher);
            return true;
        }
        return false;
    }

    private void Client.ChatClient.post(Matcher matcher) {
        if (!switchActiveChannel(matcher)) {
            return;
        }

        String input = matcher.group(3);
        post(input);
    }

    private void Client.ChatClient.part(Matcher matcher) {
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

    private void Client.ChatClient.join(Matcher matcher) {
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

    private boolean Client.ChatClient.switchActiveChannel(Matcher matcher) {
        String c = channels.get(matcher.group(2));

        if (c == null) {
            System.out.printf("Der Shortkey \"%s\" ist nicht belegt",
                matcher.group(2));
            return false;
        }

        channel = c;
        return true;
    }
}
