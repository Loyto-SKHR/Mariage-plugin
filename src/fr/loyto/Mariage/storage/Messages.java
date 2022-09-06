package fr.loyto.Mariage.storage;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.loyto.Mariage.Main;

public class Messages {
	private Main plugin;
	private FileConfiguration messages;
		
	public Messages(Main main) {
		plugin = main;
			
		File messagesFile = new File("plugins/Mariage/messages.yml");
		if(!messagesFile.exists()) {
			plugin.saveResource("messages.yml", true);
		}
		messages = YamlConfiguration.loadConfiguration(messagesFile);
	}
		
	public String get(String key) {
		String msg = messages.getString("messages." + key);
		if(msg != null)
		{
			msg = ChatColor.translateAlternateColorCodes('&', msg);
		}
		return msg == null ? "" : msg;
	}
}
