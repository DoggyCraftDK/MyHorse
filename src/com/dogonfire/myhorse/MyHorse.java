package com.dogonfire.myhorse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.dogonfire.myhorse.events.HorseGrownUpEvent;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class MyHorse extends JavaPlugin {
	private static MyHorse plugin;
	public boolean vaultEnabled = false;
	public boolean luckPermsEnabled = false;
	public boolean worldGuardEnabled = false;
	private LanguageManager languageManager = null;
	private PermissionsManager permissionsManager = null;
	private HorseManager horseManager = null;
	private HorseOwnerManager ownerManager = null;
	private FileConfiguration config = null;
	private Commands commands = null;
	public boolean debug = false;
	public boolean downloadLanguageFile = true;
	public boolean horseDamageDisabled = true;
	public boolean allowChestOnAllHorses = false;
	public boolean alwaysShowHorseName = true;
	public boolean autoClaim = true;
	public boolean economyEnabled = false;
	public boolean useUpdateNotifications = true;
	public boolean useHorseTeleportation = false;
	public boolean metricsOptOut = false;
	public int amountOfSecondsBeforeAdult = 2400;
	private List<UUID> allowedWorlds = new ArrayList<UUID>();
	private List<EntityDamageEvent.DamageCause> damageProtection = new ArrayList<DamageCause>();
	public String serverName = "Your Server";
	public String language = "english";
	private Economy economy = null;
	private Chat chat = null;
	private Permission permission = null;
	public int maxHorsesPrPlayer = 3;
	public Runnable timer;
	public BukkitTask task;

	public static final String NMS = "v1_16_R3";

	public boolean isCombatibleServer() {
		try {
			Class<?> theClass = Class.forName("net.minecraft.server." + NMS + ".ItemStack");

			return theClass != null;
		} catch (Exception ex) {
			return false;
		}
	}

	public Economy getEconomy() {
		return economy;
	}

	public Permission getPermissions() {
		return permission;
	}

	public Chat getChat() {
		return chat;
	}

	public HorseManager getHorseManager() {
		return this.horseManager;
	}

	public HorseOwnerManager getOwnerManager() {
		return this.ownerManager;
	}

	public LanguageManager getLanguageManager() {
		return this.languageManager;
	}

	public PermissionsManager getPermissionsManager() {
		return this.permissionsManager;
	}

	public void log(String message) {
		Logger.getLogger("minecraft").info("[" + getDescription().getFullName() + "] " + message);
	}

	public void logSevere(String message) {
		Logger.getLogger("minecraft").severe("[" + getDescription().getFullName() + "] " + message);
	}

	public void logDebug(String message) {
		if (this.debug) {
			Logger.getLogger("minecraft").info("[" + getDescription().getFullName() + "] " + message);
		}
	}

	public void sendInfo(Player player, String message) {
		player.sendMessage(ChatColor.AQUA + message);
	}

	public void reloadSettings() {
		reloadConfig();

		loadSettings();
	}

	public void loadSettings() {
		this.config = getConfig();

		this.allowChestOnAllHorses = this.config.getBoolean("Settings.AllowChestOnAllHorses", false);
		this.metricsOptOut = this.config.getBoolean("Settings.MetricsOptOut", false);
		this.useUpdateNotifications = this.config.getBoolean("Settings.DisplayUpdateNotifications", true);
		this.debug = this.config.getBoolean("Settings.Debug", false);
		this.downloadLanguageFile = this.config.getBoolean("Settings.DownloadLanguageFile", true);
		this.amountOfSecondsBeforeAdult = this.config.getInt("Settings.HorseTimeBeforeAdultInSeconds", 2400);
		this.serverName = this.config.getString("Settings.ServerName", "Your Server");
		this.alwaysShowHorseName = this.config.getBoolean("Settings.alwaysShowHorseName", true);

		List<String> damageList = this.config.getStringList("Settings.DamageProtections");
		if ((damageList == null) || (damageList.size() == 0)) {
			log("No damage protection settings found in config file.");
			log("Writing default damage protections to config.");

			damageList = new ArrayList<String>();

			damageList.add(EntityDamageEvent.DamageCause.PROJECTILE.name());
			damageList.add(EntityDamageEvent.DamageCause.POISON.name());
			damageList.add(EntityDamageEvent.DamageCause.MELTING.name());
			damageList.add(EntityDamageEvent.DamageCause.MAGIC.name());
			damageList.add(EntityDamageEvent.DamageCause.CUSTOM.name());
			damageList.add(EntityDamageEvent.DamageCause.DROWNING.name());
			damageList.add(EntityDamageEvent.DamageCause.FIRE.name());
			damageList.add(EntityDamageEvent.DamageCause.FIRE_TICK.name());
			damageList.add(EntityDamageEvent.DamageCause.ENTITY_ATTACK.name());
			damageList.add(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION.name());
			damageList.add(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION.name());
			damageList.add(EntityDamageEvent.DamageCause.LIGHTNING.name());
			damageList.add(EntityDamageEvent.DamageCause.LAVA.name());
			damageList.add(EntityDamageEvent.DamageCause.SUFFOCATION.name());

			this.config.set("Settings.DamageProtections", damageList);

			saveConfig();
		}
		damageList = this.config.getStringList("Settings.DamageProtections");
		this.damageProtection.clear();

		this.useHorseTeleportation = this.config.getBoolean("Settings.HorseTeleportationEnabled", false);
		for (String damageName : damageList) {
			this.damageProtection.add(EntityDamageEvent.DamageCause.valueOf(damageName));
			logDebug(damageName + " can NOT damage owned horses");
		}
		List<String> worldNames = this.config.getStringList("Settings.Worlds");
		if ((worldNames == null) || (worldNames.size() == 0)) {
			log("No worlds found in config file.");
			worldNames = new ArrayList<String>();
			for (World world : getServer().getWorlds()) {
				this.allowedWorlds.add(world.getUID());
				worldNames.add(world.getName());
				log("Enabled in world '" + world.getName() + "'");
			}
			this.config.set("Settings.Worlds", worldNames);
			saveConfig();
		} else {
			for (String worldName : worldNames) {
				World world = getServer().getWorld(worldName);
				if (world == null) {
					log("Could NOT enable MyHorse in world '" + worldName + "'. No world found with such name.");
				} else {
					this.allowedWorlds.add(world.getUID());
					log("Enabled in '" + worldName + "'");
				}
			}
			if (worldNames.size() == 0) {
				log("WARNING: No worlds are set in config file. MyHorse is DISABLED on this server!");
			}
		}
		if (permission != null) {
			checkForNewGroups();
			checkForDeletedGroups();
		}
	}

	public ChatColor getHorseNameColorForPlayer(UUID playerId) {
		if (playerId == null) {
			return ChatColor.GOLD;
		}

		String groupName;

		try {
			groupName = getPermissionsManager().getGroup(this.getServer().getOfflinePlayer(playerId).getName());
		} catch (Exception ex) {
			log("ERROR getting group name for player " + this.getServer().getOfflinePlayer(playerId).getName() + ":"
					+ ex.getMessage());
			return ChatColor.GOLD;
		}

		ChatColor nameColor;

		try {
			nameColor = ChatColor.valueOf(this.config.getString("Groups." + groupName + ".HorseNameColor"));
		} catch (Exception ex) {
			log("Could not get horse name color from player " + this.getServer().getOfflinePlayer(playerId).getName()
					+ "'s group '" + groupName + "' in config.yml!");
			nameColor = ChatColor.GOLD;
		}
		return nameColor;
	}

	public int getMaximumHorsesForPlayer(String playerName) {
		return this.config.getInt("Groups." + getPermissionsManager().getGroup(playerName) + ".MaximumHorses");
	}

	public void saveSettings() {
		this.config.set("Settings.ServerName", this.serverName);
		this.config.set("Settings.Debug", this.debug);
		this.config.set("Settings.DownloadLanguageFile", this.downloadLanguageFile);
		this.config.set("Settings.InvulnerableHorses", this.horseDamageDisabled);
		this.config.set("Settings.HorseTeleportationEnabled", this.useHorseTeleportation);
		this.config.set("Settings.HorseTimeBeforeAdultInSeconds", this.amountOfSecondsBeforeAdult);
		this.config.set("Settings.AllowChestOnAllHorses", this.allowChestOnAllHorses);
		this.config.set("Settings.DisplayUpdateNotifications", this.useUpdateNotifications);
		this.config.set("Settings.MetricsOptOut", this.metricsOptOut);
		this.config.set("Settings.alwaysShowHorseName", alwaysShowHorseName);

		saveConfig();
	}

	private boolean setupPermissions() {

		RegisteredServiceProvider<Permission> permissionPlugin = getServer().getServicesManager().getRegistration(Permission.class);
		if(permissionPlugin == null) return false;
		permission = permissionPlugin.getProvider();
		log("Using " + permission.getName() + " for Permissions.");
		return true;
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatPlugin = getServer().getServicesManager().getRegistration(Chat.class);
		if(chatPlugin == null) return false;
		chat = chatPlugin.getProvider();
		log("Using " + chatPlugin.getProvider().getName() + " for Chat.");
		return true;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		log("Using " + rsp.getProvider().getName() + " for Economy.");
		return true;
	}

	public void onEnable() {
		if (!this.isCombatibleServer()) {
			log("* Your server is not compatible with the MyHorse plugin");
			log("* This MyHorse plugin is only compatible with a " + NMS + " server");
			this.setEnabled(false);
			return;
		}

		plugin = this;

		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			logSevere("Vault was not found! Disables permissions, chat and economy for this plugin.");
			vaultEnabled = false;
		} else {
			log("Vault found!");
			vaultEnabled = true;
		}

		if (!setupPermissions())
			logSevere("No vault compatible Permissions plugin was found!");
		if (!setupChat())
			logSevere("No vault compatible Chat plugin was found!");
		if (!setupEconomy())
			logSevere("No vault compatible Economy plugin was found! Disables buy/sell features");

		this.permissionsManager = new PermissionsManager(this);
		this.languageManager = new LanguageManager(this);
		this.horseManager = new HorseManager(this);
		this.ownerManager = new HorseOwnerManager(this);
		this.commands = new Commands(this);

		loadSettings();
		saveSettings();

		this.languageManager.load();
		this.horseManager.load();
		this.ownerManager.load();
		this.permissionsManager.load();

		getServer().getPluginManager().registerEvents(new EventListener(this), this);

		if (!this.metricsOptOut) {
			try {
				Metrics metrics = new Metrics(this);

				com.dogonfire.myhorse.Metrics.Graph serversGraph = metrics.createGraph("Servers");

				serversGraph.addPlotter(new Metrics.Plotter("Servers") {
					@Override
					public int getValue() {
						return 1;
					}
				});

				serversGraph.addPlotter(new Metrics.Plotter("Using vault") {
					@Override
					public int getValue() {
						if (MyHorse.this.economyEnabled) {
							return 1;
						}

						return 0;
					}
				});

				com.dogonfire.myhorse.Metrics.Graph permissionsUsedGraph = metrics
						.createGraph("Permission plugins used");

				permissionsUsedGraph.addPlotter(new Metrics.Plotter("Using PermissionsBukkit") {
					public int getValue() {
						if (MyHorse.this.getPermissionsManager().getPermissionPluginName()
								.equals("PermissionsBukkit")) {
							return 1;
						}
						return 0;
					}
				});
				permissionsUsedGraph.addPlotter(new Metrics.Plotter("Using PermissionsEx") {
					public int getValue() {
						if (MyHorse.this.getPermissionsManager().getPermissionPluginName().equals("PermissionsEx")) {
							return 1;
						}
						return 0;
					}
				});
				permissionsUsedGraph.addPlotter(new Metrics.Plotter("Using GroupManager") {
					public int getValue() {
						if (MyHorse.this.getPermissionsManager().getPermissionPluginName().equals("GroupManager")) {
							return 1;
						}
						return 0;
					}
				});
				permissionsUsedGraph.addPlotter(new Metrics.Plotter("Using bPermissions") {
					public int getValue() {
						if (MyHorse.this.getPermissionsManager().getPermissionPluginName().equals("bPermissions")) {
							return 1;
						}
						return 0;
					}
				});

				metrics.start();
			} catch (Exception ex) {
				log("Failed to submit metrics :-(");
			}
		}

		timer = new Runnable() {

			@Override
			public void run() {
				boolean babyHorseFound = false;
				for (UUID horseIdentifier : getHorseManager().getAllHorses()) {
					if (getHorseManager().isBabyHorse(horseIdentifier)) {
						if (getHorseManager().getBabyTimeBeforeAdult(horseIdentifier) <= 0) {
							HorseGrownUpEvent horseGrownUpEvent = new HorseGrownUpEvent(horseIdentifier);
							if (!horseGrownUpEvent.isCancelled()) {
								Bukkit.getScheduler().runTask(plugin,
										() -> Bukkit.getPluginManager().callEvent(horseGrownUpEvent));
								getHorseManager().setBabyForHorse(horseIdentifier, false);
							}
						}
						babyHorseFound = true;
					}
				}
				if (!babyHorseFound)
					task.cancel();
			}
		};
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, timer, 0, 20L * 2L);
		getHorseManager().updateHorseEntities();
	}

	public void onDisable() {
		if (!this.isCombatibleServer()) {
			return;
		}

		reloadSettings();

		saveSettings();

		this.horseManager.save();
	}

	public void checkForNewGroups() {
		for (String groupName : getPermissionsManager().getGroups()) {
			if (getConfig().getString("Groups." + groupName) == null) {
				getConfig().set("Groups." + groupName + ".HorseNameColor", ChatColor.GOLD.name());
				getConfig().set("Groups." + groupName + ".MaximumHorses", Integer.valueOf(5));
				saveConfig();
			}
		}
	}

	public void checkForDeletedGroups() {
		List<String> permissionGroups = Arrays.asList(getPermissionsManager().getGroups());
		for (String configGroup : getConfig().getConfigurationSection("Groups").getKeys(false)) {
			if (!permissionGroups.contains(configGroup)) {
				getConfig().set("Groups." + configGroup, null);
				saveConfig();
			}
		}
	}

	public void addGroup(String groupName) {
		getConfig().set("Groups." + groupName + ".HorseNameColor", ChatColor.GOLD.name());
		getConfig().set("Groups." + groupName + ".MaximumHorses", Integer.valueOf(5));
		saveConfig();
	}

	public void deleteGroup(String groupName) {
		getConfig().set("Groups." + groupName, null);
		saveConfig();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return this.commands.onCommand(sender, cmd, label, args);
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return commands.onTabComplete(sender, cmd, label, args);
	}

	public boolean isAllowedInWorld(World world) {
		return this.allowedWorlds.contains(world.getUID());
	}

	public boolean isDamageProtection(EntityDamageEvent.DamageCause damage) {
		return this.damageProtection.contains(damage);
	}
}