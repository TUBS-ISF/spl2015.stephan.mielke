package de.tubs.ips.chat.client;

public interface IClientCommand {
	String getHelp();

	boolean run(String input);
}
