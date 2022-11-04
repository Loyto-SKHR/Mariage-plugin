package fr.loyto.Mariage;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class marryCommandExecutor implements CommandExecutor {
	Main plugin;
	
	//Liste des demandes de marriage
	ArrayList<MarryRequest> marryRequests = new ArrayList<MarryRequest>();
	
	public marryCommandExecutor(Main main) {
		plugin = main;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if((sender instanceof Player)) {
			Player player = (Player)sender;
			if(args.length > 0) {
				switch(args[0]) {
					case "info": //Affiche les informations d'un couple
						info(player, args);
						break;
					case "list": //Affiche la liste des couples
						list(player, args);
						break;
					case "tp": //Téléporte le joueur sur son/sa partenaire
						tp(player);
						break;
					case "home": //Téléporte le joueur au marry home
						tpHome(player, args);
						break;
					case "sethome": //Définit le marry home
						setHome(player);
						break;
					case "delhome": //Suprime le marry home
						delHome(player);
						break;
					case "chat": //Discussion privé avec son/sa partenaire
						chat(player, args);
						break;
					case "kiss": //Embrasser son/sa partenaire
						kiss(player);
						break;
					case "gift": //Offre un cadeau à son/sa partenaire
						gift(player);
						break;
					case "divorce":
						divorce(player, args);
						break;
					case "accept":
						accept(player);
						break;
					case "decline":
						decline(player);
						break;
					case "reload": //Recharge la configuration et les données joueur
						reload(player);
						break;
					case "help":  //Affiche le menu d'aide
						help(player);
						break;
					case "version":
						player.sendMessage(ChatColor.GREEN + "Mariage V1.0 par Loyto_SKHR");
						break;
					default:  //A changer par le système de mariage
						if(player.hasPermission("marry.priest") & args.length > 1) {
							marry(player, args);
						}
						else {
							help(player);
						}
						break;
				}
			}
			else {
				help(player); //Affiche le menu d'aide
			}
		}

		return true;
	}
	
	private void help(Player player) {
		player.sendMessage(ChatColor.GOLD + "====== " + ChatColor.YELLOW + "Mariage" + ChatColor.GOLD + " ======");
		player.sendMessage(ChatColor.GOLD + "/marry list" + ChatColor.WHITE + " - " + plugin.messages.get("help.list"));
		player.sendMessage(ChatColor.GOLD + "/marry info <player>" + ChatColor.WHITE + " - " + plugin.messages.get("help.info"));
		player.sendMessage(ChatColor.GOLD + "/marry <player> <player>" + ChatColor.WHITE + " - " + plugin.messages.get("help.marry"));
		player.sendMessage(ChatColor.GOLD + "/marry divorce [joueur]" + ChatColor.WHITE + " - " + plugin.messages.get("help.divorce"));
		player.sendMessage(ChatColor.GOLD + "/marry tp" + ChatColor.WHITE + " - " + plugin.messages.get("help.tp"));
		player.sendMessage(ChatColor.GOLD + "/marry home" + ChatColor.WHITE + " - " + plugin.messages.get("help.home"));
		player.sendMessage(ChatColor.GOLD + "/marry sethome" + ChatColor.WHITE + " - " + plugin.messages.get("help.sethome"));
		player.sendMessage(ChatColor.GOLD + "/marry delhome" + ChatColor.WHITE + " - " + plugin.messages.get("help.delhome"));
		player.sendMessage(ChatColor.GOLD + "/marry kiss" + ChatColor.WHITE + " - " + plugin.messages.get("help.kiss"));
		player.sendMessage(ChatColor.GOLD + "/marry gift" + ChatColor.WHITE + " - " + plugin.messages.get("help.gift"));
		player.sendMessage(ChatColor.GOLD + "/marry reload" + ChatColor.WHITE + " - " + plugin.messages.get("help.reload"));
	}
	
	private void info(Player player, String[] args) {
		if(player.hasPermission("marry.info")) {
			if(args.length > 1) {
				if(plugin.storage.isMarried(args[1])) {
					plugin.storage.getMarriageInfo(player, args[1]);
				}
				else {
					player.sendMessage(plugin.messages.get("error.playernotmarried"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.getplayer"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void list(Player player, String[] args) {
		if(player.hasPermission("marry.list")) {
			ArrayList<String[]> MarriedPlayers = plugin.storage.getMarriedPlayers();
			
			int page = 1;
			int max = 1;
			int nbPages = (int)(MarriedPlayers.size()/10);
			if(MarriedPlayers.size()%10 > 0) {
				nbPages++;
			}
			

			if(args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
					
					if(page < 1) {
						page = 1;
					}
					else if(page > nbPages) {
						page = nbPages;
					}
					
					
				}
				catch(NumberFormatException e) {
					page = 1;
				}
			}
			
			if(MarriedPlayers.size() < 10) {
				max = MarriedPlayers.size();
			}
			else if(page == nbPages & MarriedPlayers.size()%10 < 10) {
				max = ((MarriedPlayers.size()%10)+(10*(page-1)));
			}
			else {
				max = (10+(10*(page-1)));
			}
			
			
			player.sendMessage(ChatColor.GOLD + "====== " + ChatColor.YELLOW + page + "/" + nbPages + ChatColor.GOLD + " ======");
			for(int i = (0+(10*(page-1))); i < max; i++) {
				player.sendMessage(ChatColor.GREEN + MarriedPlayers.get(i)[0] + ChatColor.GRAY + " + " + ChatColor.GREEN + MarriedPlayers.get(i)[1]);
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void tp(Player player) {
		if(player.hasPermission("marry.tp")) {
			if(plugin.storage.isMarried(player)) {
				Player partner = Bukkit.getPlayer(plugin.storage.getPartner(player));
				if(partner != null) {
					int cost = plugin.config.getInt("prices.tp");
					if(cost > 0) {
						if(plugin.economy.getBalance(player.getName()) >= cost) {
							plugin.economy.withdrawPlayer(player.getName(), cost);
							player.sendMessage(String.format(plugin.messages.get("info.tpcost"), cost));
						}
						else {
							player.sendMessage(plugin.messages.get("error.notenough"));
							Bukkit.getConsoleSender().sendMessage("[Mariage] Teleportation annule, le joueur n'a pas asser d'argent");
							return;
						}
					}
					
					player.sendMessage(String.format(plugin.messages.get("info.tpdelay"), plugin.config.getInt("delay.tp")));
					BukkitRunnable tpPartner = new TpTask(player, partner, player.getLocation(), plugin);
					tpPartner.runTaskLater(plugin, plugin.config.getInt("delay.tp")*20);
				}
				else {
					player.sendMessage(plugin.messages.get("error.partnernotco"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.younotmarried"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void tpHome(Player player, String[] args) {
		if(player.hasPermission("marry.home")) {
			if((player.hasPermission("marry.home.other")) & args.length > 1) {
				if(plugin.storage.isMarried(args[1])) {
					if(plugin.storage.hasHome(args[1])) {
						player.teleport(plugin.storage.getHome(args[1]));
						player.sendMessage(ChatColor.GREEN + "Téléportation au marry home de " + args[1]);
					}
					else {
						player.sendMessage(plugin.messages.get("error.playernotmarryhome"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.playernotmarried"));
				}
			}
			else {
				if(plugin.storage.isMarried(player)) {
					if(plugin.storage.hasHome(player.getName())) {
						int cost = plugin.config.getInt("prices.home");
						if(cost > 0) {
							if(plugin.economy.getBalance(player.getName()) >= cost) {
								plugin.economy.withdrawPlayer(player.getName(), cost);
								player.sendMessage(String.format(plugin.messages.get("info.tpcost"), cost));
							}
							else {
								player.sendMessage(plugin.messages.get("error.notenough"));
								Bukkit.getConsoleSender().sendMessage("[Mariage] Teleportation annule, le joueur n'a pas asser d'argent");
								return;
							}
						}
						
						player.sendMessage(String.format(plugin.messages.get("info.tpdelay"), plugin.config.getInt("delay.home")));
						BukkitRunnable tpHome = new TpHomeTask(player, player.getLocation(), plugin);
						tpHome.runTaskLater(plugin, plugin.config.getInt("delay.home")*20);
					}
					else {
						player.sendMessage(plugin.messages.get("error.notmarryhome"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.younotmarried"));
				}
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void setHome(Player player) {
		if(player.hasPermission("marry.home")) {
			if(plugin.storage.isMarried(player)) {
				if(plugin.storage.allowedWorld(player.getWorld().getName(), plugin)) {
					if(plugin.storage.allowedFaction(player, player.getLocation(), plugin)) {
						int cost = plugin.config.getInt("prices.sethome");
						if(cost > 0) {
							if(plugin.economy.getBalance(player.getName()) >= cost) {
								plugin.economy.withdrawPlayer(player.getName(), cost);
								player.sendMessage(String.format(plugin.messages.get("info.sethomecost"), cost));
							}
							else {
								player.sendMessage(plugin.messages.get("error.notenough"));
								return;
							}
						}
						
						plugin.storage.setHome(player);
					}
					else {
						player.sendMessage(plugin.messages.get("error.sethomeisinnotallowedland"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.sethomeisinnotalloweddim"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.younotmarried"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void delHome(Player player) {
		if(player.hasPermission("marry.home")) {
			if(plugin.storage.isMarried(player)) {
				if(plugin.storage.hasHome(player.getName())) {
					plugin.storage.delHome(player);
				}
				player.sendMessage(plugin.messages.get("info.homedelete"));
			}
			else {
				player.sendMessage(plugin.messages.get("error.younotmarried"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void chat(Player player, String[] args) {
		if(player.hasPermission("marry.chat")) {
			if(plugin.storage.isMarried(player)) {
				Player partner = Bukkit.getPlayer(plugin.storage.getPartner(player));
				if(partner != null) {
					String msg = "";
					for(String arg : args) {
						msg += " " + arg;
					}
					if(args.length > 1) {
						msg = msg.substring(6);
					}
					else {
						msg = msg.substring(5);
					}
					
					//Message entre joueur
					player.sendMessage(ChatColor.RED + "❥" + ChatColor.GOLD + "moi" + ChatColor.GRAY + "─➢" + ChatColor.GOLD + partner.getName() + ChatColor.GRAY + "» " + ChatColor.WHITE + msg);
					partner.sendMessage(ChatColor.RED + "❥" + ChatColor.GOLD + player.getName() + ChatColor.GRAY + "─➢" + ChatColor.GOLD + "moi"  + ChatColor.GRAY + "» " + ChatColor.WHITE + msg);
					
					//Message log
					Bukkit.getConsoleSender().sendMessage("[Mariage] (chat) " + player.getName() + " -> " + partner.getName() + "> " + msg);
					
					//Message spy
					Bukkit.broadcast(ChatColor.RED + "❥" + ChatColor.GOLD + player.getName() + ChatColor.GRAY + "─➢" + ChatColor.GOLD + partner.getName() + ChatColor.GRAY + "» " + ChatColor.WHITE + msg, "marry.spy");
				}
				else {
					player.sendMessage(plugin.messages.get("error.partnernotco"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.younotmarried"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void kiss(Player player) {
		if(player.hasPermission("marry.kiss")) {
			if(plugin.storage.isMarried(player)) {
				Player partner = Bukkit.getPlayer(plugin.storage.getPartner(player));
				if(partner != null) {
					if(plugin.storage.getRange(player, partner) <= 10) {
						if(plugin.storage.canKiss(player, plugin)) {
							plugin.storage.addKiss(player);
							player.sendMessage(plugin.messages.get("info.kissyoutopartner"));
							partner.sendMessage(plugin.messages.get("info.kisspartnertoyou"));
						}
						else {
							player.sendMessage(String.format(plugin.messages.get("info.pleasewait"), plugin.storage.getTimeoutKiss(player, plugin)));
						}
					}
					else {
						player.sendMessage(plugin.messages.get("error.partneroutrange"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.partnernotco"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.younotmarried"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	@SuppressWarnings("deprecation")
	private void gift(Player player) {
		if(player.hasPermission("marry.gift")) {
			if(plugin.storage.isMarried(player)) {
				Player partner = Bukkit.getPlayer(plugin.storage.getPartner(player));
				if(partner != null) {
					ItemStack item = player.getItemInHand();
					if(item.getTypeId() != 0) {
						if(plugin.storage.getRange(player, partner) <= 10) {
							if(plugin.storage.canGift(player, plugin)) {
								if(!plugin.combatApi.isInCombat(player) & !plugin.combatApi.isInCombat(partner)) {
									if(plugin.storage.allowedWorld(player.getWorld().getName(), plugin)) {
										if(plugin.storage.allowedFaction(player, player.getLocation(), plugin) & plugin.storage.allowedFaction(player, partner.getLocation(), plugin)) {
											plugin.storage.addGift(player);
											
											PlayerInventory inv = partner.getInventory();
											if(inv.firstEmpty() >= 0) {
												player.getInventory().removeItem(item);
												inv.addItem(item);
												Bukkit.getConsoleSender().sendMessage("[Mariage] (gift) " + player.getName() + " a donne " + item.getTypeId() + ":" + item.getData() + "x" + item.getAmount() + " a " + partner.getName());
												
												player.sendMessage(plugin.messages.get("info.giftyoutopartner"));
												partner.sendMessage(plugin.messages.get("info.giftpartnertoyou"));
											}
											else {
												player.sendMessage(plugin.messages.get("error.noslot"));
											}
										}
										else {
											player.sendMessage(plugin.messages.get("error.giftisinnotallowedland"));
										}
									}
									else {
										player.sendMessage(plugin.messages.get("error.giftisinnotalloweddim"));
									}
								}
								else {
									player.sendMessage(plugin.messages.get("error.youorpartnercombat"));
								}
							}
							else {
								player.sendMessage(String.format(plugin.messages.get("info.pleasewait"), plugin.storage.getTimeoutGift(player, plugin)));
							}
						}
						else {
							player.sendMessage(plugin.messages.get("error.partneroutrange"));
						}
					}
					else {
						player.sendMessage(plugin.messages.get("error.iteminhand"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.partnernotco"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.younotmarried"));
			}
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void divorce(Player player, String[] args) {
		if(player.hasPermission("marry.divorceother") & args.length > 1) {
			if(plugin.storage.isMarried(args[1])) {
				Bukkit.broadcastMessage(String.format(plugin.messages.get("info.divorce"), args[1], plugin.storage.getPartner(args[1])));
				Bukkit.getConsoleSender().sendMessage("[Mariage] (forced divorce) " + args[1] + " a divorce de " + plugin.storage.getPartner(args[1]) + " par " + player.getName());
				
				plugin.storage.delMarriage(args[1]);
			}
			else {
				player.sendMessage(plugin.messages.get("error.playernotmarried"));
			}
		}
		else if(plugin.storage.isMarried(player)) {
			Bukkit.broadcastMessage(String.format(plugin.messages.get("info.divorce"), player.getName(), plugin.storage.getPartner(player)));
			Bukkit.getConsoleSender().sendMessage("[Mariage] (divorce) " + player.getName() + " a divorce de " + plugin.storage.getPartner(player));
			
			plugin.storage.delMarriage(player);
		}
		else {
			player.sendMessage(plugin.messages.get("error.younotmarried"));
		}
	}
	
	private void reload(Player player) {
		if(player.hasPermission("marry.reload")) {
			player.sendMessage(ChatColor.GREEN + "Reload du plugin en cours");
			plugin.load();
			player.sendMessage(ChatColor.GREEN + "Reload du plugin fini");
		}
		else {
			player.sendMessage(plugin.messages.get("error.noperm"));
		}
	}
	
	private void marry(Player player, String[] args) {
		if(plugin.storage.canMarry(player, plugin)) {
			Player marry1 = Bukkit.getPlayer(args[0]);
			Player marry2 = Bukkit.getPlayer(args[1]);
			if(marry1 != null & marry2 != null) {
				if(!marry1.equals(marry2)) {
					if(plugin.storage.getRange(marry1, marry2) <= 20) {
						if(!plugin.storage.isMarried(marry1) & !plugin.storage.isMarried(marry2)) {
							for(MarryRequest marryRequest : marryRequests) {
								if(marryRequest.marry1.equals(marry1) | marryRequest.marry1.equals(marry2) | marryRequest.marry2.equals(marry1) | marryRequest.marry2.equals(marry2)) {
									player.sendMessage(plugin.messages.get("error.alreadyrequest"));
									return;
								}
							}
							int cost = plugin.config.getInt("prices.marry");
							if(cost > 0) {
								if((plugin.economy.getBalance(marry1.getName()) >= cost) & (plugin.economy.getBalance(marry2.getName()) >= cost)) {
									marryRequests.add(new MarryRequest(player, marry1, marry2));
									player.chat(String.format(plugin.messages.get("info.marriagerequest"), marry1.getName(), marry2.getName()));
									marry1.sendMessage(plugin.messages.get("info.msgacceptdeny"));
								}
								else {
									player.sendMessage(plugin.messages.get("error.oneplayernotmoney"));
								}
							}
						}
						else {
							player.sendMessage(plugin.messages.get("error.oneplayeralreadymarried"));
						}
					}
					else {
						player.sendMessage(plugin.messages.get("error.marriageoutrange"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.selfmarriage"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.oneplayernotco"));
			}
		}
		else {
			player.sendMessage(String.format(plugin.messages.get("info.pleasewait"), plugin.storage.getTimeoutMarry(player, plugin)));
		}
	}
	
	private void accept(Player player) {
		if(!plugin.storage.isMarried(player)) {
			for(MarryRequest marryRequest : marryRequests) {
				if(marryRequest.marry1.equals(player)) {
					if(!marryRequest.hasAccept(player)) {
						if(Bukkit.getPlayer(marryRequest.marry2.getName()) != null & Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
							marryRequest.accept(player);
							player.chat(plugin.messages.get("info.iaccept"));
							if(marryRequest.hasAccept(player) & marryRequest.hasAccept(marryRequest.marry2)) {
								if(Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
									int cost = plugin.config.getInt("prices.marry");
									if(cost > 0) {
										if((plugin.economy.getBalance(player.getName()) >= cost) & (plugin.economy.getBalance(marryRequest.marry2.getName()) >= cost)) {
											marryRequest.priest.chat(String.format(plugin.messages.get("info.married"), player.getName(), marryRequest.marry2.getName()));
											plugin.storage.addMarriage(player, marryRequest.marry2, marryRequest.priest);
											Bukkit.getConsoleSender().sendMessage("[Mariage] (marry) " + marryRequest.priest.getName() + " vient de marie " + player.getName() + " avec " + marryRequest.marry2.getName());
											
											plugin.economy.withdrawPlayer(player.getName(), cost);
											plugin.economy.withdrawPlayer(marryRequest.marry2.getName(), cost);
											player.sendMessage(String.format(plugin.messages.get("info.marriagecost"), cost));
											marryRequest.marry2.sendMessage(String.format(plugin.messages.get("info.marriagecost"), cost));
											marryRequests.remove(marryRequest);
										}
									}
									else {
										player.sendMessage(plugin.messages.get("error.cancelmarriagenotmoney"));
										marryRequest.marry2.sendMessage(plugin.messages.get("error.cancelmarriagenotmoney"));
										marryRequest.priest.sendMessage(plugin.messages.get("error.cancelmarriagenotmoney"));
									}
								}
							}
							else {
								marryRequest.priest.chat(String.format(plugin.messages.get("info.marriagerequest"), marryRequest.marry2.getName(), player.getName()));
								marryRequest.marry2.sendMessage(plugin.messages.get("info.msgacceptdeny"));
							}
						}
						else {
							if(Bukkit.getPlayer(marryRequest.marry1.getName()) != null) {
								marryRequest.marry1.sendMessage(plugin.messages.get("error.cancelmarriagedeco"));
							}
							if(Bukkit.getPlayer(marryRequest.marry2.getName()) != null) {
								marryRequest.marry2.sendMessage(plugin.messages.get("error.cancelmarriagedeco"));
							}
							if(Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
								marryRequest.priest.sendMessage(plugin.messages.get("error.cancelmarriagedeco"));
							}
							marryRequests.remove(marryRequest);
						}
					}
					else {
						player.sendMessage(plugin.messages.get("error.alreadyaccept"));
					}
					return;
				}
				else if(marryRequest.marry2.equals(player)) {
					if(!marryRequest.hasAccept(player)) {
						if(Bukkit.getPlayer(marryRequest.marry1.getName()) != null & Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
							marryRequest.accept(player);
							player.chat(plugin.messages.get("info.iaccept"));
							if(marryRequest.hasAccept(player) & marryRequest.hasAccept(marryRequest.marry1)) {
								if(Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
									int cost = plugin.config.getInt("prices.marry");
									if(cost > 0) {
										if((plugin.economy.getBalance(player.getName()) >= cost) & (plugin.economy.getBalance(marryRequest.marry2.getName()) >= cost)) {
											marryRequest.priest.chat(String.format(plugin.messages.get("info.maried"), player.getName(), marryRequest.marry1.getName()));
											plugin.storage.addMarriage(player, marryRequest.marry1, marryRequest.priest);
											Bukkit.getConsoleSender().sendMessage("[Mariage] (marry) " + marryRequest.priest.getName() + " vient de marie " + player.getName() + " avec " + marryRequest.marry2.getName());
											
											plugin.economy.withdrawPlayer(player.getName(), cost);
											plugin.economy.withdrawPlayer(marryRequest.marry1.getName(), cost);
											player.sendMessage(String.format(plugin.messages.get("info.marriagecost"), cost));
											marryRequest.marry1.sendMessage(String.format(plugin.messages.get("info.marriagecost"), cost));
											marryRequests.remove(marryRequest);
										}
									}
									else {
										player.sendMessage(plugin.messages.get("error.cancelmarriagenotmoney"));
										marryRequest.marry2.sendMessage(plugin.messages.get("error.cancelmarriagenotmoney"));
										marryRequest.priest.sendMessage(plugin.messages.get("error.cancelmarriagenotmoney"));
									}
								}
							}
							else {
								marryRequest.priest.chat(String.format(plugin.messages.get("info.marriagerequest"), marryRequest.marry1.getName(), player.getName()));
								marryRequest.marry1.sendMessage(plugin.messages.get("info.msgacceptdeny"));
							}
						}
						else {
							if(Bukkit.getPlayer(marryRequest.marry1.getName()) != null) {
								marryRequest.marry1.sendMessage(plugin.messages.get("error.cancelmarriagedeco"));
							}
							if(Bukkit.getPlayer(marryRequest.marry2.getName()) != null) {
								marryRequest.marry2.sendMessage(plugin.messages.get("error.cancelmarriagedeco"));
							}
							if(Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
								marryRequest.priest.sendMessage(plugin.messages.get("error.cancelmarriagedeco"));
							}
							marryRequests.remove(marryRequest);
						}
					}
					else {
						player.sendMessage(plugin.messages.get("error.alreadyaccept"));
					}
					return;
				}
			}
			player.sendMessage(plugin.messages.get("error.norequest"));
		}
		else {
			player.sendMessage(plugin.messages.get("error.alreadymarried"));
		}
	}
	
	private void decline(Player player) {
		if(!plugin.storage.isMarried(player)) {
			for(MarryRequest marryRequest : marryRequests) {
				if(marryRequest.marry1.equals(player) | marryRequest.marry2.equals(player)) {
					if(Bukkit.getPlayer(marryRequest.marry1.getName()) != null) {
						marryRequest.marry1.sendMessage(plugin.messages.get("error.cancelmarriagedecline"));
					}
					if(Bukkit.getPlayer(marryRequest.marry2.getName()) != null) {
						marryRequest.marry2.sendMessage(plugin.messages.get("error.cancelmarriagedecline"));
					}
					if(Bukkit.getPlayer(marryRequest.priest.getName()) != null) {
						marryRequest.priest.sendMessage(plugin.messages.get("error.cancelmarriagedecline"));
					}
					
					marryRequests.remove(marryRequest);
					return;
				}
			}
			player.sendMessage(plugin.messages.get("error.norequest"));
		}
		else {
			player.sendMessage(plugin.messages.get("error.alreadymarried"));
		}
	}
}

class TpTask extends BukkitRunnable {
	Player player1;
	Player player2;
	Location origin;
	Main plugin;
	
	public TpTask(Player p1, Player p2, Location o, Main main) {
		player1 = p1;
		player2 = p2;
		origin = o;
		plugin = main;
	}

	@Override
	public void run() {
		if(Bukkit.getPlayer(player1.getName()) != null) {
			if(Bukkit.getPlayer(player2.getName()) != null) {
				if(!plugin.combatApi.isInCombat(player1) & !plugin.combatApi.isInCombat(player2)) {
					if(plugin.storage.allowedWorld(player2.getWorld().getName(), plugin)) {
						if(plugin.storage.allowedFaction(player1, player2.getLocation(), plugin)) {
							if(plugin.storage.notMoove(player1.getLocation(), origin)) {
								player1.teleport(player2.getLocation());
								player1.sendMessage(plugin.messages.get("info.youtopartner"));
								player2.sendMessage(plugin.messages.get("info.partnertoyou"));
								Bukkit.getConsoleSender().sendMessage("[Mariage] " + player1.getName() + " a ete teleporter sur " + player2.getName());
							}
							else {
								player1.sendMessage(plugin.messages.get("error.tpmoove"));
							}
						}
						else {
							player1.sendMessage(plugin.messages.get("error.partnernotallowedland"));
						}
					}
					else {
						player1.sendMessage(plugin.messages.get("error.partnernotalloweddim"));
					}
				}
				else {
					player1.sendMessage(plugin.messages.get("error.youorpartnercombat"));
				}
			}
			else {
				player1.sendMessage(plugin.messages.get("error.partnerisnotanymoreconn"));
			}
		}
	}
}

class TpHomeTask extends BukkitRunnable {
	Player player;
	Location origin;
	Main plugin;
	
	public TpHomeTask(Player p, Location o, Main main) {
		player = p;
		origin = o;
		plugin = main;
	}

	@Override
	public void run() {
		if(Bukkit.getPlayer(player.getName()) != null) {
			if(!plugin.combatApi.isInCombat(player)) {
				Location home = plugin.storage.getHome(player);
				if(plugin.storage.allowedWorld(home.getWorld().getName(), plugin)) {
					if(plugin.storage.notMoove(player.getLocation(), origin)) {
						player.teleport(home);
						player.sendMessage(plugin.messages.get("info.tptohome"));
						Bukkit.getConsoleSender().sendMessage("[Mariage] " + player.getName() + " a ete teleporter a son marry home");
					}
					else {
						player.sendMessage(plugin.messages.get("error.tpmoove"));
					}
				}
				else {
					player.sendMessage(plugin.messages.get("error.homeisinnotalloweddim"));
				}
			}
			else {
				player.sendMessage(plugin.messages.get("error.incombat"));
			}
		}
	}
	
}
