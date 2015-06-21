package de.tubs.ips.chat.client.features;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tubs.ips.chat.client.ChatClient;
import de.tubs.ips.chat.client.IClientCommand;

public class ChannelFreiWaehlbar implements IClientCommand {

	private final ChatClient client;

	private static final Pattern pJoin = Pattern.compile(
			"^(/join)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern pPart = Pattern.compile(
			"^(/part)\\s+(\\w+?)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern pSwitch = Pattern.compile("^(/(\\d+))\\s*$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern pPost = Pattern.compile(
			"^(/(\\d+))\\s+(.*?)\\s*$", Pattern.CASE_INSENSITIVE);

	public ChannelFreiWaehlbar(ChatClient client) {
		this.client = client;
	}

	public String getHelp() {
		String ret = "";
		ret += String.format("zum Betreten eines Channels: %s%n",
				pJoin.pattern());
		ret += String.format("zum Verlassen eines Channels: %s%n",
				pPart.pattern());
		ret += String.format("zum Channel wechseln: %s%n", pSwitch.pattern());
		ret += String.format("zum Channel wechseln und schreiben: %s%n",
				pPost.pattern());
		return ret;
	}

	public boolean run(String input) {
		Matcher matcher;
		if ((matcher = pJoin.matcher(input)) != null && matcher.matches()) {
			join(matcher);
			return true;
		} else if ((matcher = pPart.matcher(input)) != null
				&& matcher.matches()) {
			part(matcher);
			return true;
		} else if ((matcher = pSwitch.matcher(input)) != null
				&& matcher.matches()) {
			switchActiveChannel(matcher);
			return true;
		} else if ((matcher = pPost.matcher(input)) != null
				&& matcher.matches()) {
			post(matcher);
			return true;
		}
		return false;
	}

	private boolean switchActiveChannel(Matcher matcher) {
		String c = client.getChannels().get(matcher.group(2));

		if (c == null) {
			System.out.printf("Der Shortkey \"%s\" ist nicht belegt",
					matcher.group(2));
			return false;
		}

		client.setChannel(c);
		return true;
	}

	private void post(Matcher matcher) {
		if (!switchActiveChannel(matcher)) {
			return;
		}

		String input = matcher.group(3);
		client.post(input);
	}

	private void part(Matcher matcher) {
		String channelName = matcher.group(2);
		if (!client.getChannels().values().contains(channelName)) {
			System.out
					.printf("Sie sind nicht im Channel \"%s\" und können diesen somit nicht Verlassen%n",
							channelName);
			return;
		}

		try {
			client.getChat().unlisten(channelName, client.getCallback());
		} catch (RemoteException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}

		client.getChannels().remove(channelName);

		if (client.getChannel().equals(channelName)) {
			if (client.getChannels().values().isEmpty()) {
				client.setChannel(null);
			} else {
				client.setChannel(client
						.getChannels()
						.values()
						.toArray(
								new String[client.getChannels().values().size()])[0]);
			}
		}

		System.out.printf("Sie haben den Channel \"%s\" verlassen%n",
				channelName);
	}

	private void join(Matcher matcher) {
		String channelName = matcher.group(2);

		if (client.getChannels().values().contains(channelName)) {
			System.out
					.printf("Sie sind schon im Channel \"%s\"%n", channelName);
			return;
		}

		try {
			client.getChat().listen(channelName, client.getCallback());
			client.setChannel(channelName);

			int i = 0;
			String c;
			do {
				i++;
				c = client.getChannels().get(String.valueOf(i));
			} while (c != null);

			client.getChannels().put(String.valueOf(i), channelName);

			System.out.printf("Sie sind dem Channel \"%s\" beigetreten%n",
					channelName);
		} catch (RemoteException e) {
			// #ifdef Debug_Mode
			// @ e.printStackTrace();
			// #endif
		}
	}
}
