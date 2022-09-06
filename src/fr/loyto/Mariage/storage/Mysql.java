package fr.loyto.Mariage.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loyto.Mariage.Main;

public class Mysql extends Storage {
	Main plugin;
	
	private Connection conn = null;
	
	public Mysql(Main main) {
		plugin = main;
		
		String host = plugin.config.getString("storage.mysql.host");
		String database = plugin.config.getString("storage.mysql.database");
		String user = plugin.config.getString("storage.mysql.user");
		String pass = plugin.config.getString("storage.mysql.password");

		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true&allowMultiQueries=true", user, pass);
			
			DatabaseMetaData dbm = conn.getMetaData();
			
			if(!checkTable(dbm, "marry_home")) {
				runStatement("CREATE TABLE IF NOT EXISTS `marry_home` (`marry_id` int(11) NOT NULL,`home_x` double NOT NULL,`home_y` double NOT NULL,`home_z` double NOT NULL,`home_world` varchar(45) NOT NULL DEFAULT 'world',PRIMARY KEY (`marry_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
			}
			if(!checkTable(dbm, "marry_partners")) {
				runStatement("CREATE TABLE IF NOT EXISTS `marry_partners` (`marry_id` int(11) NOT NULL AUTO_INCREMENT,`player1` int(11) NOT NULL,`player2` int(11) NOT NULL,`priest` int(11) DEFAULT NULL,`pvp_state` tinyint(1) NOT NULL DEFAULT '0',`date` datetime NOT NULL,PRIMARY KEY (`marry_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
			}
			if(!checkTable(dbm, "marry_players")) {
				runStatement("CREATE TABLE IF NOT EXISTS `marry_players` (`player_id` int(11) NOT NULL AUTO_INCREMENT,`name` varchar(20) NOT NULL,PRIMARY KEY (`player_id`),UNIQUE KEY `name` (`name`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkTable(DatabaseMetaData dbm, String table) {
		try {
			ResultSet rs = dbm.getTables(null, null, table, null);
			
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void runStatement(String query, Object... args) {
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			for(int i = 0; i < args.length; i++) {
				stmt.setObject(i+1, args[i]);
			}
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void runAsyncStatement(final String query, final Object... args) {
		BukkitRunnable asyncStatement = new BukkitRunnable() {
			@Override
			public void run() {
				runStatement(query, args);
			}
		};
		asyncStatement.runTaskAsynchronously(plugin);
	}
	
	private ResultSet runStatementRes(String query, Object... args) {
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			for(int i = 0; i < args.length; i++) {
				stmt.setObject(i+1, args[i]);
			}
			return stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ArrayList<String[]> getMarriedPlayers() {
		ArrayList<String[]> marriedPlayers = new ArrayList<String[]>();
		ResultSet list = runStatementRes("SELECT `mp1`.`name` as p1 ,`mp2`.`name` as p2 FROM `marry_partners` INNER JOIN `marry_players` AS mp1 ON `player1`=`mp1`.`player_id` INNER JOIN `marry_players` AS mp2 ON `player2`=`mp2`.`player_id`");
		
		try {
			while(list.next()) {
				marriedPlayers.add(new String[] {list.getString("p1"), list.getString("p2")});
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return marriedPlayers;
	}

	@Override
	public boolean isMarried(Player player) {
		return isMarried(player.getName());
	}

	@Override
	public boolean isMarried(String player) {
		try {
			return runStatementRes("SELECT `name` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `name`=?", player).next();
		} catch (SQLException e) {
			e.printStackTrace();
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
		ResultSet date = runStatementRes("SELECT `date` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `name`=?", player);
		try {
			if(date.next()) {
				return (Date)date.getTimestamp("date");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public String getPriest(String player) {
		ResultSet priest = runStatementRes("SELECT `name` FROM `marry_players` as `mp`, `marry_partners` as `mm`, (SELECT `marry_id` FROM `marry_partners` as `mm`, `marry_players` as `mp` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `name`=?) as `r` WHERE `mp`.`player_id`=`mm`.`priest` AND `mm`.`marry_id`=`r`.`marry_id`", player);
		try {
			if(priest.next()) {
				return priest.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String getPartner(Player player) {
		return getPartner(player.getName());
	}
	
	@Override
	public String getPartner(String player) {
		ResultSet priest = runStatementRes("SELECT `name` FROM `marry_players` as `mp`, `marry_partners` as `mm`, (SELECT `marry_id` FROM `marry_partners` as `mm`, `marry_players` as `mp` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `name`=?) as `r` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `name`!=? AND `mm`.`marry_id`=`r`.`marry_id`", player, player);
		try {
			if(priest.next()) {
				return priest.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public boolean hasHome(String player) {
		try {
			return runStatementRes("SELECT `marry_id` FROM `marry_home` WHERE `marry_id`=(SELECT `mm`.`marry_id` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `mp`.`name`=?)", player).next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Location getHome(Player player) {
		return getHome(player.getName());
	}

	@Override
	public Location getHome(String marry) {
		ResultSet home = runStatementRes("SELECT * FROM `marry_home` WHERE `marry_id`=(SELECT `mm`.`marry_id` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `mp`.`name`=?)", marry);
		try {
			if(home.next()) {
				return new Location(Bukkit.getWorld(home.getString("home_world")), home.getDouble("home_x"), home.getDouble("home_y"), home.getDouble("home_z"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void setHome(Player player) {
		Location home = player.getLocation();
		if(hasHome(player.getName())) {
			runStatement("UPDATE `marry_home` SET `home_world`=?, `home_x`=?, `home_y`=?, `home_z`=? WHERE marry_id=(SELECT `mm`.`marry_id` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `mp`.`name`=?)", home.getWorld().getName(), home.getX(), home.getY(), home.getZ(), player.getName());
		}
		else {
			runStatement("INSERT INTO `marry_home` (`marry_id`, `home_world`, `home_x`, `home_y`, `home_z`)VALUES ((SELECT `mm`.`marry_id` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `mp`.`name`=?), ?, ?, ?, ?)", player.getName(), home.getWorld().getName(), home.getX(), home.getY(), home.getZ());
		}
	}
	
	@Override
	public void delHome(Player player) {
		delHome(player.getName());
	}

	private void delHome(String player) {
		runStatement("DELETE FROM `marry_home` WHERE `marry_id`=(SELECT `mm`.`marry_id` FROM `marry_players` as `mp`, `marry_partners` as `mm` WHERE (`mp`.`player_id`=`mm`.`player1` OR `mp`.`player_id`=`mm`.`player2`) AND `mp`.`name`=?)", player);
	}
	
	private int getPlayerId(String name) {
		ResultSet player = runStatementRes("SELECT `player_id` FROM `marry_players` WHERE `name`=?", name);
		try {
			if(player.next()) {
				return player.getInt("player_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	@Override
	public void addMarriage(Player marry1, Player marry2, Player priest) {
		Date date = new Date();
		if(getPlayerId(marry1.getName()) < 0) {
			runStatement("INSERT INTO `marry_players` (`name`) VALUES (?)", marry1.getName());
		}
		if(getPlayerId(marry2.getName()) < 0) {
			runStatement("INSERT INTO `marry_players` (`name`) VALUES (?)", marry2.getName());
		}
		if(getPlayerId(priest.getName()) < 0) {
			runStatement("INSERT INTO `marry_players` (`name`) VALUES (?)", priest.getName());
		}
		runStatement("INSERT INTO `marry_partners` (`player1`, `player2`, `priest`, `date`) VALUES (?, ?, ?, ?)", getPlayerId(marry1.getName()), getPlayerId(marry2.getName()), getPlayerId(priest.getName()), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.getTime()));
	}

	@Override
	public void delMarriage(Player player) {
		delMarriage(player.getName());
	}

	@Override
	public void delMarriage(String player) {
		if(hasHome(player)) {
			delHome(player);
		}
		int pid = getPlayerId(player);
		runStatement("DELETE FROM `marry_partners` WHERE (?=`player1` OR ?=`player2`)", pid, pid);
	}

}
