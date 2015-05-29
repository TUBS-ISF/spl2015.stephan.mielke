package de.tubs.ips.chat;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.server.ChatServer;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help = false;

    @Parameter(names = {"--debug", "-d"}, description = "Debug mode")
    private boolean debug = false;

    @Parameter(names = {"--server", "-s"}, description = "start server")
    private boolean server = false;

    @Parameter(names = {"--client", "-c"}, description = "start client", arity = 1)
    private boolean client = true;

    @Parameter(names = {"--database", "-db"}, description = "start server with database")
    private boolean db = false;

    @Parameter(names = "--commands", variableArity = true, description = "supported commands (client) example: \"nick\" \"names\" \"get\"")
    private List<String> commands = new ArrayList<String>();

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        JCommander jCommander = new JCommander(launcher, args);
        launcher.run(jCommander);
    }

    private void run(JCommander jCommander) {
        if (help) {
            jCommander.usage();
            System.exit(0);
        }
        if (client) {
            ChatClient chatClient = new ChatClient("anonymous", debug, commands);
            Thread clientThread = new Thread(chatClient);
            clientThread.start();
        }
        if (server) {
            try {
                ChatServer chatServer = new ChatServer(debug, db);
                Thread serverThread = new Thread(chatServer);
                serverThread.start();
            } catch (RemoteException e) {
                //e.printStackTrace();
            } catch (SQLException e) {
                //e.printStackTrace();
            }
        }
    }
}
