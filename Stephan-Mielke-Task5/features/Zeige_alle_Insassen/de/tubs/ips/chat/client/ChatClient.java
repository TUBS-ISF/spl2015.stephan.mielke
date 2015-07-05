package de.tubs.ips.chat.client;

import de.tubs.ips.chat.Chat;
import de.tubs.ips.chat.ChatListener;
import de.tubs.ips.chat.ChatMessage;

import java.io.Serializable;
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
public class ChatClient implements ChatListener, Serializable, Runnable {

	private static final Pattern pNames = Pattern.compile(
			"^(/names)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
	
	protected void help() {
		original();
		System.out.printf(
				"zum Anzeige aller Mitglieder eines Channels: %s%n",
				pNames.pattern());
	}
	
	private boolean checkInput(String input) {
		if (original(input)) {
			return true;
		}
		
		Matcher matcher;
		if ((matcher = pNames.matcher(input)) != null && matcher.matches()) {
			names(matcher);
			return true;
		}
		return false;
	}
	
	private void names(Matcher matcher) {
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
