package de.tubs.ips.chat.client.features;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.client.IClientCommand;

public class ZeigeAlleInsassen implements IClientCommand {

	private final ChatClient client;

	private static final Pattern pNames = Pattern.compile(
			"^(/names)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

	public ZeigeAlleInsassen(ChatClient client) {
		this.client = client;
	}

	public String getHelp() {
		String ret = "";
		ret += String.format(
				"zum Anzeige aller Mitglieder eines Channels: %s%n",
				pNames.pattern());
		return ret;
	}

	public boolean run(String input) {
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
			String[] users = client.getChat().getUsers(channelName);
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
