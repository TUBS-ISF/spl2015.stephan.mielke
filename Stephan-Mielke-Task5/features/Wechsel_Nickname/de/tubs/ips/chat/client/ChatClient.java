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

	private static final Pattern pNick = Pattern.compile(
			"^(/nick)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
	
	protected void help() {
		original();
		System.out.printf("zum Wechseln des Nicks: %s%n", pNick.pattern());
	}
	
	private boolean checkInput(String input) {
		if (original(input)) {
			return true;
		}
		
		Matcher matcher;
		if ((matcher = pNick.matcher(input)) != null && matcher.matches()) {
			nickChange(matcher);
			return true;
		}
		return false;
	}
	
	private void nickChange(Matcher matcher) {
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
