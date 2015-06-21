package de.tubs.ips.chat.client.features;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.client.IClientCommand;

public class WechselNickname implements IClientCommand {

	private final ChatClient client;

	private static final Pattern pNick = Pattern.compile(
			"^(/nick)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);

	public WechselNickname(ChatClient client) {
		this.client = client;
	}

	public String getHelp() {
		String ret = "";
		ret += String.format("zum Wechseln des Nicks: %s%n", pNick.pattern());
		return ret;
	}

	public boolean run(String input) {
		Matcher matcher;
		if ((matcher = pNick.matcher(input)) != null && matcher.matches()) {
			nickChange(matcher);
			return true;
		}
		return false;
	}

	private void nickChange(Matcher matcher) {
		String newNickname = matcher.group(2);
		if (client.getChat() != null) {
			try {
				client.getChat().changeNick(client.getNickname(), newNickname);
				System.out.printf(
						"Wechsel den Nickname von \"%s\" zu \"%s\"%n",
						client.getNickname(), newNickname);
				client.setNickname(newNickname);
			} catch (RemoteException e) {
				// #ifdef Debug_Mode
				// @ e.printStackTrace();
				// #endif
			}
		}
	}
}
