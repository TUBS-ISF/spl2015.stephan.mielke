package de.tubs.ips.chat.client.features;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tubs.ips.chat.ChatMessage;
import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.client.IClientCommand;

public class ZeigeAlteNachrichten implements IClientCommand {

	private final ChatClient client;

	private static final Pattern pGet = Pattern.compile(
			"^(/get)\\s+(\\w+?)\\s+(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern pGetAll = Pattern.compile(
			"^(/getall)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

	public ZeigeAlteNachrichten(ChatClient client) {
		this.client = client;
	}

	public String getHelp() {
		String ret = "";
		ret += String.format("zum Holen alter Nachrichten: %s%n",
				pGet.pattern());
		ret += String.format("zum Holen alter Nachrichten: %s%n",
				pGetAll.pattern());
		return ret;
	}

	public boolean run(String input) {
		Matcher matcher;
		if ((matcher = pGet.matcher(input)) != null && matcher.matches()) {
			get(matcher);
			return true;
		} else if ((matcher = pGetAll.matcher(input)) != null
				&& matcher.matches()) {
			getAll(matcher);
			return true;
		}
		return false;
	}

	private void get(Matcher matcher) {
		String channelName = matcher.group(2);
		int i = Integer.parseInt(matcher.group(3));
		System.out.printf(
				"Zeige die letzten %d Nachrichten des Channel \"%s\" an%n", i,
				channelName);
		try {
			final ChatMessage[] messages = client.getChat().get(channelName, i);
			for (final ChatMessage message : messages)
				client.printMessage(message);
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

	private void getAll(Matcher matcher) {
		String channelName = matcher.group(2);
		System.out.printf("Zeige alle Nachrichten des Channel \"%s\" an%n",
				channelName);
		try {
			final ChatMessage[] messages = client.getChat().getAll(channelName);
			for (final ChatMessage message : messages)
				client.printMessage(message);
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
