package de.tubs.ips.chat.client;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;
import de.tubs.ips.chat.utils.Helper;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephan
 */
public class ChatClient implements ChatListener, Serializable {

    protected static final Pattern pExit = Pattern.compile("^(/quit)\\s*$");
    protected static final Pattern pNick = Pattern.compile("^(/nick)\\s+(\\w+?)\\s*$");
    protected static final Pattern pJoin = Pattern.compile("^(/join)\\s+(\\w+?)\\s*$");
    protected static final Pattern pList = Pattern.compile("^(/list)\\s*$");
    protected static final Pattern pMsg = Pattern.compile("^(/msg)\\s+(\\w+?)\\s+(\\w+?)\\s*$");
    protected static final Pattern pNames = Pattern.compile("^(/names)\\s+(\\w+?)\\s*$");
    protected static final Pattern pPart = Pattern.compile("^(/part)\\s+(\\w+?)\\s*$");
    protected static final Pattern pPost = Pattern.compile("^(/(\\d+))\\s+(.*?)\\s*$");
    protected static final Pattern pSwitch = Pattern.compile("^(/(\\d+))\\s*$");
    protected static final Pattern pHelp = Pattern.compile("^(/help|/\\?)$");
    protected static final Pattern pGet = Pattern.compile("^(/get)\\s+(\\w+?)\\s+(\\d+)\\s*$");
    protected static final Pattern pGetAll = Pattern.compile("^(/getall)\\s+(\\w+?)\\s*$");

    protected static final Pattern pConnect = Pattern.compile("^/connect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$");
    protected static final Pattern pDisconnect = Pattern.compile("^/disconnect\\s+\"([a-zA-Z0-9\\.]+)\"\\s+(\\d+)\\s*$");

    private static final long serialVersionUID = 899243651902242507L;

    protected final boolean debug;
    protected final String NAME;
    private final Map<String, String> channels = new HashMap<String, String>();
    private final Scanner scanner = new Scanner(System.in);
    protected Chat chat;
    private String nickname;
    private String channel;
    private ChatListener callback;

    public ChatClient(String nickname) {
        this.nickname = nickname;

        Properties properties = null;
        try {
            properties = Helper.getConfig(Paths.get("server.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (properties != null) {
            NAME = properties.getProperty("name");
            debug = Boolean.parseBoolean(properties.getProperty("debug"));
        } else {
            NAME = "chat";
            debug = true;
        }

        try {
            callback = (ChatListener) UnicastRemoteObject.exportObject(this, 0);
        } catch (RemoteException e) {
            if (debug)
                e.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        final ChatClient app = new ChatClient(args.length > 0 ? args[0] : "anonymous");
        System.out.println("Client gestartet... gib help ein für Hilfe");
        app.run();
    }

    private void connect(final Matcher matcher) throws RemoteException, NotBoundException {
        System.out.printf("Verbinde zu %s::%s\n..\n", matcher.group(1), matcher.group(2));

        final Registry registry = LocateRegistry.getRegistry(matcher.group(1), Integer.parseInt(matcher.group(2)));

        chat = (Chat) registry.lookup(NAME);
        chat.listen(callback);
        System.out.println("mit dem Server verbunden");
    }

    /**
     * beendet den Clienten
     */
    protected void exit() {
        scanner.close();

        try {
            if (chat != null) {
                disconnect();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        System.out.println("Client wird beendet.");
        System.exit(0);
    }

    private void get(Matcher matcher) {
        String channelName = matcher.group(2);
        int i = Integer.parseInt(matcher.group(3));

        System.out.printf("Zeige die letzten %d Nachrichten des Channel \"%s\" an%n", i, channelName);

        try {
            final ChatMessage[] messages = chat.get(channelName, i);
            for (final ChatMessage message : messages)
                printMessage(message);
        } catch (final RemoteException re) {
            if (debug)
                re.printStackTrace();
        } catch (final IllegalArgumentException iae) {
            if (debug)
                iae.printStackTrace();
        }
    }

    private void getAll(Matcher matcher) {
        String channelName = matcher.group(2);

        System.out.printf("Zeige alle Nachrichten des Channel \"%s\" an%n", channelName);

        try {
            final ChatMessage[] messages = chat.getAll(channelName);
            for (final ChatMessage message : messages)
                printMessage(message);
        } catch (final RemoteException re) {
            if (debug)
                re.printStackTrace();
        } catch (final IllegalArgumentException iae) {
            if (debug)
                iae.printStackTrace();
        }
    }

    private String getShortKey(String channel) {
        String shortKey = "-";
        for (String key : channels.keySet()) {
            if (channels.get(key).equals(channel)) {
                shortKey = key;
            }
        }
        return shortKey;
    }

    private void promt() {
        promt(channel);
    }

    private void promt(String channel) {
        if (channel == null) {
            System.out.printf("[] ");
        } else {
            System.out.printf("[%s - %s] ", getShortKey(channel), channel);
        }
    }

    /**
     *
     */
    protected void help() {
        System.out.println("NYI");
    }

    public void newMessage(final ChatMessage message) throws RemoteException {
        printMessage(message);
    }

    public void privateMessage(ChatMessage message) throws RemoteException {
        System.out.println("NYI");
    }

    public String getName() throws RemoteException {
        return nickname;
    }

    private void post(String input) {
        Matcher matcher = pPost.matcher(input);

        if (matcher.matches()) {
            if (!switchActiveChannel(matcher)) {
                return;
            }

            input = matcher.group(3);
        }

        if (channel == null) {
            System.out.println("Sie haben keinen aktiven Channel");
            return;
        }

        ChatMessage message = new ChatMessage(channel, nickname, input);

        try {
            chat.post(channel, message);
        } catch (RemoteException e) {
            if (debug)
                e.printStackTrace();
        }
    }

    private void printMessage(final ChatMessage message) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(message.getTime());
        String time = calendar.getTime().toString();
        String shortKey = getShortKey(message.getChannel());

        promt(message.getChannel());

        System.out.printf("%s %s: %s%n", time, message.getMember(), message.getMessage());
    }

    private void nickChange(Matcher matcher) {
        String newNickname = matcher.group(2);

        if (chat != null) {
            try {
                chat.changeNick(nickname, newNickname);
                System.out.printf("Wechsel den Nickname von \"%s\" zu \"%s\"%n", nickname, newNickname);
                nickname = newNickname;

            } catch (RemoteException e) {
                if (debug)
                    e.printStackTrace();
            }
        }
    }

    protected void run() {

        System.out.printf("Client ist gestartet. Sie sind noch nicht mit einem Server verbunden. Geben Sie \"/help\" für die Hilfe ein.");
        System.out.println();

        while (true) {
            if (chat == null) {
                disconnected();
            }
            if (chat != null) {
                connected();
            }
        }
    }

    private void disconnected() {
        while (true) {
            promt();
            final String input = scanner.nextLine();
            Matcher matcher;
            if ((matcher = pConnect.matcher(input)) != null && matcher.matches()) {
                try {
                    connect(matcher);
                } catch (final RemoteException re) {
                    System.err.println("Konnte zum Server nicht verbinden.");
                    if (debug)
                        re.printStackTrace();
                } catch (final NotBoundException nbe) {
                    System.err.println("Dienst existiert nicht auf dem Server");
                    if (debug)
                        nbe.printStackTrace();
                } catch (IllegalArgumentException e) {
                    chat = null;
                    e.printStackTrace();
                }
                return;
            } else if ((matcher = pExit.matcher(input)) != null && matcher.matches()) {
                exit();
            } else if ((matcher = pHelp.matcher(input)) != null && matcher.matches()) {
                help();
            } else if ((matcher = pNick.matcher(input)) != null && matcher.matches()) {
                nickChange(matcher);
            }
        }
    }

    private void connected() {
        while (true) {
            promt();
            final String input = scanner.nextLine();
            Matcher matcher;
            if ((matcher = pDisconnect.matcher(input)) != null && matcher.matches()) {
                disconnect();
                return;
            } else if ((matcher = pJoin.matcher(input)) != null && matcher.matches()) {
                join(matcher);
            } else if ((matcher = pExit.matcher(input)) != null && matcher.matches()) {
                exit();
            } else if ((matcher = pHelp.matcher(input)) != null && matcher.matches()) {
                help();
            } else if ((matcher = pNick.matcher(input)) != null && matcher.matches()) {
                nickChange(matcher);
            } else if ((matcher = pPart.matcher(input)) != null && matcher.matches()) {
                part(matcher);
            } else if ((matcher = pList.matcher(input)) != null && matcher.matches()) {
                list();
            } else if ((matcher = pNames.matcher(input)) != null && matcher.matches()) {
                names(matcher);
            } else if ((matcher = pGet.matcher(input)) != null && matcher.matches()) {
                get(matcher);
            } else if ((matcher = pGetAll.matcher(input)) != null && matcher.matches()) {
                getAll(matcher);
            } else if ((matcher = pSwitch.matcher(input)) != null && matcher.matches()) {
                switchActiveChannel(matcher);
            } else {
                post(input);
            }
        }
    }

    private boolean switchActiveChannel(Matcher matcher) {
        String c = channels.get(matcher.group(2));

        if (c == null) {
            System.out.printf("Der Shortkey \"%s\" ist nicht belegt", matcher.group(2));
            return false;
        }

        channel = c;
        return true;
    }

    private void names(Matcher matcher) {
        String channelName = matcher.group(2);
        System.out.printf("Mitglieder des Channel \"%s\" sind:%n", channelName);
        try {
            String[] users = chat.getUsers(channelName);

            for (String user : users) {
                System.out.println(user);
            }
        } catch (RemoteException e) {
            if (debug)
                e.printStackTrace();
        }
    }

    private void list() {
        Collection<String> cs = channels.values();

        if (cs.isEmpty()) {
            System.out.println("Sie befinden sich in keinem Channel");
        } else {
            System.out.println("Sie befinden sich in folgenden Channels");
            for (String key : channels.keySet()) {
                System.out.printf("[%s] %s%n", key, channels.get(key));
            }
        }
    }

    private void part(Matcher matcher) {
        String channelName = matcher.group(2);
        if (!channels.values().contains(channelName)) {
            System.out.printf("Sie sind nicht im Channel \"%s\" und können diesen somit nicht Verlassen%n", channelName);
            return;
        }

        try {
            chat.unlisten(channelName, callback);
        } catch (RemoteException e) {
            if (debug)
                e.printStackTrace();
        }

        channels.remove(channelName);

        if (channel.equals(channelName)) {
            if (channels.values().isEmpty()) {
                channel = null;
            } else {
                channel = channels.values().toArray(new String[channels.values().size()])[0];
            }
        }

        System.out.printf("Sie haben den Channel \"%s\" verlassen%n", channelName);
    }

    private void disconnect() {
        try {
            chat.unlisten(callback);
        } catch (RemoteException e) {
            if (debug)
                e.printStackTrace();
        }

        try {
            UnicastRemoteObject.unexportObject(callback, true);
        } catch (NoSuchObjectException e) {
            if (debug)
                e.printStackTrace();
        }

        channels.clear();
        channel = null;
        chat = null;

        System.out.println("Vom Server abgemeldet.");

    }

    private void join(Matcher matcher) {
        String channelName = matcher.group(2);

        if (channels.values().contains(channelName)) {
            System.out.printf("Sie sind schon im Channel \"%s\"%n", channelName);
            return;
        }

        try {
            chat.listen(channelName, callback);
            channel = channelName;

            int i = 0;
            String c;
            do {
                i++;
                c = channels.get(String.valueOf(i));
            } while (c != null);

            channels.put(String.valueOf(i), channelName);

            System.out.printf("Sie sind dem Channel \"%s\" beigetreten%n", channelName);
        } catch (RemoteException e) {
            if (debug)
                e.printStackTrace();
        }
    }
}