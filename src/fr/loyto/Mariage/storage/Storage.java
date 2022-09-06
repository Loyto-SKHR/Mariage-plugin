package fr.loyto.Mariage.storage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.mcore.ps.PS;

import fr.loyto.Mariage.Main;

public abstract class Storage {
	//List les joueurs marri�
	public abstract ArrayList<String[]> getMarriedPlayers();
	
	//Historique des kiss & gift & marry
	public HashMap<String, Timestamp> kiss = new HashMap<String, Timestamp>();
	public HashMap<String, Timestamp> gift = new HashMap<String, Timestamp>();
	public HashMap<String, Timestamp> marry = new HashMap<String, Timestamp>();
	
	//Permet de savoir si un joueur est mari�
	public abstract boolean isMarried(Player player);
	public abstract boolean isMarried(String player);
	
	//Obtient des informations sur le marriage d'un joueur
	public abstract void getMarriageInfo(Player player, String marry);
	
	//Obtient la date du mariage
	public abstract Date getDate(String player);
	
	//Obtient le nom du pr�tre
	public abstract String getPriest(String player);
	
	//Obtient le nom du partenaire du joueur
	public abstract String getPartner(Player player);
	public abstract String getPartner(String player);
	
	//Permet de savoir si le joueur � un marry home
	public abstract boolean hasHome(String player); 
	
	//Permet la t�l�portation au marry home
	public abstract Location getHome(Player player);
	public abstract Location getHome(String marry);
	
	//D�finit le marry home
	public abstract void setHome(Player player);
	
	//Suprime le marry home
	public abstract void delHome(Player player);
	
	//Ajoute un nouveau mariage
	public abstract void addMarriage(Player marry1, Player marry2, Player priest);
	
	//Suprime un mariage
	public abstract void delMarriage(Player player);
	public abstract void delMarriage(String player);
	
	public static Storage getStorage(String type, Main plugin) {
		if(type.equalsIgnoreCase("file")) {
			return new Files(plugin);
		}
		return new Mysql(plugin);
	}
	
	@SuppressWarnings("unchecked")
	public boolean allowedWorld(String world, Main plugin) {
		ArrayList<String> listWorlds = (ArrayList<String>)plugin.config.get("allowedDim");
		String[] worlds = listWorlds.toArray(new String[listWorlds.size()]);
		
		for(String w : worlds) {
			if(w.contentEquals(world)) {
				return true;
			}
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean allowedFaction(Player player, Location location, Main plugin) {
		ArrayList<String> listFac = (ArrayList<String>)plugin.config.get("unallowedFaction");
		Faction pos = BoardColls.get().getFactionAt(PS.valueOf(location));
		for(String f : listFac) {
			if(f.equalsIgnoreCase(pos.getName())) {
				return false;
			}
		}
		
		ArrayList<String> listFacRel = (ArrayList<String>)plugin.config.get("unallowedFactionRelation");
		Faction p = UPlayer.get(player).getFaction();
		for(String fr : listFacRel) {
			if(fr.equalsIgnoreCase(p.getRelationWish(pos).name())) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean canKiss(Player player, Main plugin) {
		if(getTimeoutKiss(player, plugin) > 0) {
			return false;
		}
		return true;
	}
	
	public int getTimeoutKiss(Player player, Main plugin) {
		if(kiss.containsKey(player.getName())) {
			int delay = plugin.config.getInt("delay.kiss") - (int)((System.currentTimeMillis() - kiss.get(player.getName()).getTime()) / 1000);
			return (delay > 0 ? delay : 0);
		}
		return 0;
	}
	
	public void addKiss(Player player) {
		kiss.put(player.getName(), new Timestamp(System.currentTimeMillis()));
	}
	
	public boolean canGift(Player player, Main plugin) {
		if(getTimeoutGift(player, plugin) > 0) {
			return false;
		}
		return true;
	}
	
	public int getTimeoutGift(Player player, Main plugin) {
		if(gift.containsKey(player.getName())) {
			int delay = plugin.config.getInt("delay.gift") - (int)((System.currentTimeMillis() - gift.get(player.getName()).getTime()) / 1000);
			return (delay > 0 ? delay : 0);
		}
		return 0;
	}
	
	public void addGift(Player player) {
		gift.put(player.getName(), new Timestamp(System.currentTimeMillis()));
	}
	
	public boolean canMarry(Player player, Main plugin) {
		if(getTimeoutMarry(player, plugin) > 0) {
			return false;
		}
		return true;
	}
	
	public int getTimeoutMarry(Player player, Main plugin) {
		if(marry.containsKey(player.getName())) {
			int delay = plugin.config.getInt("delay.gift") - (int)((System.currentTimeMillis() - marry.get(player.getName()).getTime()) / 1000);
			return (delay > 0 ? delay : 0);
		}
		return 0;
	}
	
	public void addMarry(Player player) {
		marry.put(player.getName(), new Timestamp(System.currentTimeMillis()));
	}
	
	public int getRange(Player p1, Player p2) {
		Location l1 = p1.getLocation();
		Location l2 = p2.getLocation();
		
		if(!l1.getWorld().getName().equals(l2.getWorld().getName())) {
			return 10000;
		}
		
		return (int)(Math.sqrt(Math.pow(Math.abs(l1.getX()-l2.getX()), 2) + Math.pow(Math.abs(l1.getZ()-l2.getZ()), 2) + Math.pow(Math.abs(l1.getY()-l2.getY()), 2)));
	}
	
	public boolean notMoove(Location l1, Location l2) {
		if((int)l1.getX() == (int)l2.getX() & (int)l1.getY() == (int)l2.getY() & (int)l1.getZ() == (int)l2.getZ()) {
			return true;
		}
		return false;
	}
}
