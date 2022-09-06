package fr.loyto.Mariage;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;

import fr.loyto.Mariage.storage.Messages;
import fr.loyto.Mariage.storage.Storage;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	public Main plugin = this;
	public static File configFile;
	public FileConfiguration config;
	public Storage storage;
	public Messages messages;
	public CombatTagApi combatApi;
	public Economy economy;
	public PluginManager pluginManager;
	
	@Override
	public void onEnable() {
		//Fichier config
		load();
		
		//Ajout de l'api CombatTag
		combatApi = new CombatTagApi((CombatTag)getServer().getPluginManager().getPlugin("CombatTag"));
		if(combatApi == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Mariage] Impossible d'obtenir l'api CombatTag");
			return;
		}
		
		//Ajout de l'api Vault
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
		if(economy == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Mariage] Impossible d'obtenir l'api Vault");
			return;
		}
        
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Mariage] Plugin active !");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Mariage V1.0 par Loyto_SKHR");
		
		//Cr�ation de la commande marry
		getCommand("marry").setExecutor(new marryCommandExecutor(plugin));
		
		//Listener (event)
		pluginManager = getServer().getPluginManager();
		Listener listener = new PluginListener(plugin);
		pluginManager.registerEvents(listener, plugin);
	}
	
	public void load() {
		saveDefaultConfig();
		configFile = new File("plugins/Mariage/config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		
		storage = Storage.getStorage(config.getString("storage.type"), plugin);
		messages = new Messages(plugin);
	}
	
	
	
	@Override
	public void onDisable() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Mariage] Plugin arrete !");
	}
}
