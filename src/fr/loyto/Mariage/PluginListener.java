package fr.loyto.Mariage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PluginListener implements Listener {
	Main plugin;
	
	public PluginListener(Main main) {
		plugin = main;
	}
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if(player.hasPermission("marry.kiss") & player.isSneaking()) {
			if(plugin.storage.isMarried(player)) {
				Entity entity = event.getRightClicked();
				if(entity != null & (entity instanceof Player)) {
					Player player2 = (Player)entity;
					if(plugin.storage.getPartner(player).equalsIgnoreCase(player2.getName())){
						if(plugin.storage.canKiss(player, plugin)) {
							plugin.storage.addKiss(player);
							player.sendMessage(plugin.messages.get("info.kissyoutopartner"));
							player2.sendMessage(plugin.messages.get("info.kisspartnertoyou"));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if(plugin.storage.isMarried(player)) {
			Player partner = Bukkit.getPlayer(plugin.storage.getPartner(player));
			if(partner != null) {
				partner.sendMessage(plugin.messages.get("info.partnerconnection"));
				player.sendMessage(plugin.messages.get("info.partneronlinr"));
			}
			else {
				player.sendMessage(plugin.messages.get("info.partneroffline"));
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(plugin.storage.isMarried(player)) {
			Player partner = Bukkit.getPlayer(plugin.storage.getPartner(player));
			if(partner != null) {
				partner.sendMessage(plugin.messages.get("info.partnerdisconnect"));
			}
		}
	}
}
