package fr.loyto.Mariage;

import org.bukkit.entity.Player;

public class MarryRequest {
	public Player priest;
	public Player marry1;
	public Player marry2;

	private boolean ma1 = false;
	private boolean ma2 = false;
	
	public MarryRequest(Player p, Player m1, Player m2) {
		priest = p;
		marry1 = m1;
		marry2 = m2;
	}
	
	public void accept(Player player) {
		if(player.equals(marry1)) {
			ma1 = true;
		}
		else if(player.equals(marry2)){
			ma2 = true;
		}
	}
	
	public boolean hasAccept(Player player) {
		if(player.equals(marry1)) {
			return ma1;
		}
		else if(player.equals(marry2)){
			return ma2;
		}
		return false;
	}
}
