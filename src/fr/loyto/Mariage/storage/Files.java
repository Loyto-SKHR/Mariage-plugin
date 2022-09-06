package fr.loyto.Mariage.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.loyto.Mariage.Main;

public class Files extends Storage {
	HashMap<String, FileConfiguration> marriedPlayers;
	Main plugin;
	String path = "plugins/Mariage/players";
	
	public Files(Main main) {
		plugin = main;
		//Fichiers joueur
		loadMarriedPlayer();
	}
	
	//Permet de sauvegarder un fichier player
	public void save(File filePlayer, FileConfiguration playerFile) {
		playerFile.options().copyDefaults(true);
		try {
			playerFile.save(filePlayer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void loadMarriedPlayer() {
		marriedPlayers = new HashMap<String, FileConfiguration>();
		File playerDir = new File(path);
		if(playerDir.exists()) {
			File[] playerFiles = playerDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".yml");
				}
			});
			
			if(playerFiles.length > 0) {
				for(File playerFile : playerFiles) {
					FileConfiguration player = YamlConfiguration.loadConfiguration(playerFile);
					String name;
					
					if(plugin.config.getBoolean("storage.useuuid")) {
						name = player.getString("Name");
					}
					else {
						name = playerFile.getName().substring(0, playerFile.getName().length() - 4);
					}
					
					marriedPlayers.put(name, player);
				}
			}
		}
		else {
			playerDir.mkdir();
		}
	}

	@Override
	public ArrayList<String[]> getMarriedPlayers() {
		ArrayList<String[]> marriageList = new ArrayList<String[]>();
		
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			String marriedTo = marriedPlayer.getValue().getString("MarriedTo");
			if(!listContains(marriageList, new String[] {marriedPlayer.getKey(), marriedTo})) {
				marriageList.add(new String[] {marriedPlayer.getKey(), marriedTo});
			}
		}
		
		return marriageList;
	}
	
	private boolean listContains(ArrayList<String[]> marriageList, String[] marriage) {
		for(String[] m : marriageList) {
			if((m[0].equals(marriage[0]) & m[1].equals(marriage[1])) | (m[0].equals(marriage[1]) & m[1].equals(marriage[0]))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isMarried(Player player) {
		return marriedPlayers.containsKey(player.getName());
	}

	@Override
	public boolean isMarried(String player) {
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void getMarriageInfo(Player player, String marry) {
		String partner = getPartner(marry);
		String priest = getPriest(marry);
		boolean home = hasHome(marry);
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(getDate(marry));

		player.sendMessage(ChatColor.GOLD + "====== " + ChatColor.YELLOW + marry + ChatColor.GOLD + " ======");
		player.sendMessage(ChatColor.GOLD + "Marie a: " + ChatColor.GRAY + partner);
		player.sendMessage(ChatColor.GOLD + "Par: " + ChatColor.GRAY + priest);
		player.sendMessage(ChatColor.GOLD + "Le: " + ChatColor.GRAY + date);
		player.sendMessage(ChatColor.GOLD + "Home: " +  (home ? ChatColor.GREEN + "oui" : ChatColor.RED + "non"));
	}
	
	@Override
	public Date getDate(String player) {
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				return (Date)marriedPlayer.getValue().get("MarriedDay");
			}
		}
		return null;
	}
	
	@Override
	public String getPriest(String player) {
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				return marriedPlayer.getValue().getString("MarriedBy");
			}
		}
		return null;
	}

	@Override
	public String getPartner(Player player) {
		FileConfiguration playerFile = marriedPlayers.get(player.getName());
		if(playerFile != null) {
			return playerFile.getString("MarriedTo");
		}
		return null;
	}
	
	@Override
	public String getPartner(String player) {
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				return marriedPlayer.getValue().getString("MarriedTo");
			}
		}
		return null;
	}
	
	public String getPartnerUUID(Player player) {
		FileConfiguration playerFile = marriedPlayers.get(player.getName());
		if(playerFile != null) {
			return playerFile.getString("MarriedToUUID");
		}
		return null;
	}
	
	public String getPartnerUUID(String player) {
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				return marriedPlayer.getValue().getString("MarriedToUUID");
			}
		}
		return null;
	}
	
	public String getPlayerUUID(String player) {

		File playerDir = new File(path);
		File[] playerFiles = playerDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".yml");
			}
		});
		
		if(playerFiles.length > 0) {
			for(File playerFile : playerFiles) {
				FileConfiguration playerConf = YamlConfiguration.loadConfiguration(playerFile);

				if(playerConf.getString("Name").equalsIgnoreCase(player)) {
					return playerFile.getName().substring(0, playerFile.getName().length() - 4);
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean hasHome(String player) {
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				if(marriedPlayer.getValue().getString("MarriedHome.location.World") != null) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		
		return false;
	}

	@Override
	public Location getHome(Player player) {
		World world = Bukkit.getWorld(marriedPlayers.get(player.getName()).getString("MarriedHome.location.World"));
		if(world != null) {
			FileConfiguration playerFile = marriedPlayers.get(player.getName());
			return new Location(
				world,
				playerFile.getDouble("MarriedHome.location.X"),
				playerFile.getDouble("MarriedHome.location.Y"),
				playerFile.getDouble("MarriedHome.location.Z")
			);
		}
		return null;
	}

	@Override
	public Location getHome(String marry) {
		FileConfiguration playerFile = null;
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(marry.equalsIgnoreCase(marriedPlayer.getKey())) {
				playerFile = marriedPlayer.getValue();
			}
		}
		
		
		World world = Bukkit.getWorld(playerFile.getString("MarriedHome.location.World"));
		if(world != null) {
			return new Location(
				world,
				playerFile.getDouble("MarriedHome.location.X"),
				playerFile.getDouble("MarriedHome.location.Y"),
				playerFile.getDouble("MarriedHome.location.Z")
			);
		}
		return null;
	}

	@Override
	public void setHome(Player player) {
		Location playerPos = player.getLocation();
		FileConfiguration playerFile = marriedPlayers.get(player.getName());
		FileConfiguration partnerFile = marriedPlayers.get(getPartner(player));

		Bukkit.getConsoleSender().sendMessage("[Mariage] nouveau home pour " + player.getName() + " & " + getPartner(player) + " en " + (int)playerPos.getX() + ", " + (int)playerPos.getY() + ", " + (int)playerPos.getZ() + ", " + playerPos.getWorld().getName());
		playerFile.set("MarriedHome.location.World", playerPos.getWorld().getName());
		playerFile.set("MarriedHome.location.X", playerPos.getX());
		playerFile.set("MarriedHome.location.Y", playerPos.getY());
		playerFile.set("MarriedHome.location.Z", playerPos.getZ());
		partnerFile.set("MarriedHome.location.World", playerPos.getWorld().getName());
		partnerFile.set("MarriedHome.location.X", playerPos.getX());
		partnerFile.set("MarriedHome.location.Y", playerPos.getY());
		partnerFile.set("MarriedHome.location.Z", playerPos.getZ());

		if(plugin.config.getBoolean("storage.useuuid")) {
			save(new File(path + "/" + player.getUniqueId().toString().replace("-", "") + ".yml"), playerFile);
			save(new File(path + "/" + getPartnerUUID(player) + ".yml"), partnerFile);
		}
		else {
			save(new File(path + "/" + player.getName() + ".yml"), playerFile);
			save(new File(path + "/" + getPartner(player) + ".yml"), partnerFile);
		}
	}
	
	@Override
	public void delHome(Player player) {
		FileConfiguration playerFile = marriedPlayers.get(player.getName());
		FileConfiguration partnerFile = marriedPlayers.get(getPartner(player));

		playerFile.set("MarriedHome.location.World", null);
		playerFile.set("MarriedHome.location.X", null);
		playerFile.set("MarriedHome.location.Y", null);
		playerFile.set("MarriedHome.location.Z", null);
		partnerFile.set("MarriedHome.location.World", null);
		partnerFile.set("MarriedHome.location.X", null);
		partnerFile.set("MarriedHome.location.Y", null);
		partnerFile.set("MarriedHome.location.Z", null);
		
		if(plugin.config.getBoolean("storage.useuuid")) {
			save(new File(path + "/" + player.getUniqueId().toString().replace("-", "") + ".yml"), playerFile);
			save(new File(path + "/" + getPartnerUUID(player) + ".yml"), partnerFile);
		}
		else {
			save(new File(path + "/" + player.getName() + ".yml"), playerFile);
			save(new File(path + "/" + getPartner(player) + ".yml"), partnerFile);
		}
	}

	@Override
	public void addMarriage(Player marry1, Player marry2, Player priest) {
		File fileMarry1 = new File("plugins/Mariage/players/" + marry1.getName() + ".yml");
		File fileMarry2 = new File("plugins/Mariage/players/" + marry2.getName() + ".yml");
		FileConfiguration marry1File = YamlConfiguration.loadConfiguration(fileMarry1);
		FileConfiguration marry2File = YamlConfiguration.loadConfiguration(fileMarry2);
		
		//Marry1
		marry1File.set("MarriedStatus", "Married");
		marry1File.set("MarriedTo", marry2.getName());
		if(plugin.config.getBoolean("storage.useuuid")) {
			marry1File.set("Name", marry1.getName());
			marry1File.set("MarriedToUUID", marry2.getUniqueId().toString().replace("-", ""));
		}
		marry1File.set("MarriedBy", priest.getName());
		marry1File.set("MarriedDay", Calendar.getInstance().getTime());
		marry1File.set("MarriedHome", "");
		marry1File.set("PvP", false);
		
		//Marry2
		marry2File.set("MarriedStatus", "Married");
		marry2File.set("MarriedTo", marry1.getName());
		if(plugin.config.getBoolean("storage.useuuid")) {
			marry1File.set("Name", marry2.getName());
			marry1File.set("MarriedToUUID", marry1.getUniqueId().toString().replace("-", ""));
		}
		marry2File.set("MarriedBy", priest.getName());
		marry2File.set("MarriedDay", Calendar.getInstance().getTime());
		marry2File.set("MarriedHome", "");
		marry2File.set("PvP", false);

		
		if(plugin.config.getBoolean("storage.useuuid")) {
			save(new File(path + "/" + marry1.getUniqueId().toString().replace("-", "") + ".yml"), marry1File);
			save(new File(path + "/" + marry2.getUniqueId().toString().replace("-", "") + ".yml"), marry2File);
		}
		else {
			save(new File(path + "/" + marry1.getName() + ".yml"), marry1File);
			save(new File(path + "/" + marry2.getName() + ".yml"), marry2File);
		}
		loadMarriedPlayer();
	}

	@Override
	public void delMarriage(Player player) {
		
		if(plugin.config.getBoolean("storage.useuuid")) {
			new File("plugins/Mariage/players/" + player.getUniqueId().toString().replace("-", "") + ".yml").delete();
			new File("plugins/Mariage/players/" + getPartnerUUID(player) + ".yml").delete();
		}
		else {
			new File("plugins/Mariage/players/" + player.getName() + ".yml").delete();
			new File("plugins/Mariage/players/" + getPartner(player) + ".yml").delete();
		}
		
		loadMarriedPlayer();
	}

	@Override
	public void delMarriage(String player) {
		String marry = null;
		
		for(Entry<String, FileConfiguration> marriedPlayer : marriedPlayers.entrySet()) {
			if(player.equalsIgnoreCase(marriedPlayer.getKey())) {
				marry = marriedPlayer.getKey();
			}
		}
		
		
		if(plugin.config.getBoolean("storage.useuuid")) {
			new File("plugins/Mariage/players/" + getPlayerUUID(marry) + ".yml").delete();
			new File("plugins/Mariage/players/" + getPartnerUUID(marry) + ".yml").delete();
		}
		else {
			new File("plugins/Mariage/players/" + marry + ".yml").delete();
			new File("plugins/Mariage/players/" + getPartner(marry) + ".yml").delete();
		}
		
		loadMarriedPlayer();
	}

}
