import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public aspect Client {
    pointcut mainMethod(): execution(public static void main(String[])) && target(de.tubs.ips.chat.Launcher);

    after(): mainMethod() {
        Thread thread = new Thread(new ChatClient("anonymous"));
        thread.start();
    }

    public class ChatClient implements ChatListener, Serializable, Runnable {

        private static final long serialVersionUID = 899243651902242507L;
        protected final Pattern pExit = Pattern.compile("^(/quit)\\s*$",
            Pattern.CASE_INSENSITIVE);
        protected final Pattern pList = Pattern.compile("^(/list)\\s*$",
            Pattern.CASE_INSENSITIVE);
        protected final Pattern pMsg = Pattern.compile(
            "^(/msg)\\s+(\\w+?)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
        protected final Pattern pHelp = Pattern.compile("^(/help|/\\?)$");
        protected final String NAME;
        final Map<String, String> channels = new HashMap<String, String>();
        private final Scanner scanner = new Scanner(System.in);
        Chat chat;
        String nickname;
        String channel;
        ChatListener callback;

        public ChatClient(String nickname) {
            this.nickname = nickname;
            NAME = "chat";
        }

        public Chat getChat() {
            return chat;
        }

        public void setChat(Chat chat) {
            this.chat = chat;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public ChatListener getCallback() {
            return callback;
        }

        public void setCallback(ChatListener callback) {
            this.callback = callback;
        }

        public String getNAME() {
            return NAME;
        }

        public Map<String, String> getChannels() {
            return channels;
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
            System.out.printf("zum Beenden: %s%n", pExit.pattern());
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

        public void post(String input) {
            if (channel == null) {
                System.out.println("Sie haben keinen aktiven Channel");
                return;
            }

            ChatMessage message = new ChatMessage(channel, nickname, input);

            try {
                chat.post(channel, message);
            } catch (RemoteException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }
        }

        public void printMessage(final ChatMessage message) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(message.getTime());
            String time = calendar.getTime().toString();

            promt(message.getChannel());

            System.out.printf("%s %s: %s%n", time, message.getMember(),
                message.getMessage());
        }

        protected void start() {
            try {
                callback = (ChatListener) UnicastRemoteObject.exportObject(this, 0);
            } catch (RemoteException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }

            System.out
                .printf("Client ist gestartet. Sie sind noch nicht mit einem Server verbunden. Geben Sie \"/help\" für die Hilfe ein.");
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

        private void checkInputDisco(String input) {
            Matcher matcher;
            if ((matcher = pExit.matcher(input)) != null
                && matcher.matches()) {
                exit();
            } else if ((matcher = pHelp.matcher(input)) != null
                && matcher.matches()) {
                help();
            }
        }

        private boolean checkInput(String input) {
            Matcher matcher;
            if ((matcher = pExit.matcher(input)) != null
                && matcher.matches()) {
                exit();
                return true;
            } else if ((matcher = pHelp.matcher(input)) != null
                && matcher.matches()) {
                help();
                return true;
            } else if ((matcher = pList.matcher(input)) != null
                && matcher.matches()) {
                list();
                return true;
            }
            return false;
        }

        private void connected() {
            while (chat != null) {
                promt();
                final String input = scanner.nextLine();
                if (!checkInput(input)) {
                    post(input);
                }
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

        private void disconnected() {
            while (chat == null) {
                promt();
                final String input = scanner.nextLine();
                checkInputDisco(input);
            }
        }

        void disconnect() {
            try {
                chat.unlisten(callback);
            } catch (RemoteException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }

            try {
                UnicastRemoteObject.unexportObject(callback, true);
            } catch (NoSuchObjectException e) {
                // #ifdef Debug_Mode
                // @ e.printStackTrace();
                // #endif
            }

            channels.clear();
            channel = null;
            chat = null;

            System.out.println("Vom Server abgemeldet.");

        }

        /**
         * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
         * causes the object's <code>run</code> method to be called in that separately executing thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            System.out.println("Client gestartet... gib help ein für Hilfe");
            start();
        }
    }
}
