package com.dogonfire.myhorse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.TraderLlama;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dogonfire.myhorse.LanguageManager.LANGUAGESTRING;
import com.dogonfire.myhorse.utils.MathUtils;
import com.dogonfire.myhorse.utils.TimeUtils;

public class Commands {
	private MyHorse plugin = null;
	private final String[] commands = { "help", "name", "lock", "unlock", "addfriend", "removefriend", "list",
			"setowner", "select", "info", "claim", "goaway", "kill" };
	
	private final String[] adminRelatedCommands = { "spawn"};
	private final String[] ownerRelatedCommands = { "reload" };

	Commands(MyHorse p) {
		this.plugin = p;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if ((sender instanceof Player)) {
			player = (Player) sender;
		}
		if (player == null) {
			if ((cmd.getName().equalsIgnoreCase("myhorse")) || (cmd.getName().equalsIgnoreCase("mh"))) {
				if (args.length == 0) {
					CommandMyHorse(sender);
					this.plugin.log(sender.getName() + " /mh");
					return true;
				}
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						this.plugin.reloadSettings();

						this.plugin.getHorseManager().load();

						this.plugin.getHorseManager().updateHorseEntities();

						return true;
					}
				}
			}
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("myhorse")) || (cmd.getName().equalsIgnoreCase("mh"))) {
			if (args.length == 0) {
				CommandMyHorse(sender);
				this.plugin.log(sender.getName() + " /mh");
				return true;
			}

			switch (args[0].toLowerCase()) {
			case "spawn":
				if (CommandSpawn(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse spawn");
				}
				return true;
			case "list":
				if (CommandList(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse list");
				}
				return true;
			case "comehere":
				if (CommandComeHere(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse comehere");
				}
				return true;
			case "info":
				if (CommandInfo(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse info");
				}
				return true;
			case "claim":
				if (CommandClaim(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse claim");
				}
				return true;
			case "lock":
				if (CommandLock(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse lock");
				}
			case "unlock":
				if (CommandUnlock(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse unlock");
				}
				return true;
			case "goaway":
				if (CommandGoAway(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse goaway");
				}
				return true;
			case "kill":
				if (CommandKill(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse kill");
				}
				return true;
			case "sell":
				if (CommandSell(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse sell");
				}
				return true;
			case "reload":
				if (CommandReload(sender)) {
					this.plugin.log(sender.getName() + " /myhorse reload");
				}
				return true;
			case "help":
				if (CommandHelp(sender)) {
					this.plugin.log(sender.getName() + " /myhorse help");
				}
				return true;
			case "goto":
				if (CommandGoto(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse goto");
				}
				return true;
			case "select":
				if (CommandSelect(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse select");
				}
				return true;
			case "addfriend":
				if (CommandAddFriend(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse addfriend");
				}
				return true;
			case "removefriend":
				if (CommandRemoveFriend(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse removefriend");
				}
				return true;
			case "setowner":
				if (CommandSetOwner(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse setowner");
				}
				return true;
			case "name":
				if (CommandSetName(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse name");
				}
				return true;
			case "chest":
				if (CommandChest(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse chest");
				}
				return true;
			case "removechest":
				if (CommandRemoveChest(player, args)) {
					this.plugin.log(sender.getName() + " /myhorse removechest");
				}
				return true;
			default:
				return true;
			}
		}
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		Map<String, Boolean> commandsForPlayer = getCommandsForPlayer(player);

		if ((cmd.getName().equalsIgnoreCase("myhorse")) || (cmd.getName().equalsIgnoreCase("mh"))) {
			if (args.length == 1) {
				return commandsForPlayer.keySet().stream().filter(commandsForPlayer::get)
						.filter(e -> e.contains(args[0])).collect(Collectors.toList());
			}

			switch (args[0].toLowerCase()) {
			case "removefriend":
				this.plugin.getHorseManager()
						.getHorseFriends(
								this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId()))
						.stream().map(id -> plugin.getHorseManager().getNameForHorse(id)).collect(Collectors.toList());
			case "removechest":
				return this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).stream()
						.filter(id -> this.plugin.getHorseManager().isHorseCarryingChest(id))
						.map(id -> this.plugin.getHorseManager().getNameForHorse(id)).collect(Collectors.toList());
			case "lock":
				return this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).stream()
						.filter(id -> !this.plugin.getHorseManager().isHorseLocked(id))
						.map(id -> this.plugin.getHorseManager().getNameForHorse(id)).collect(Collectors.toList());
			case "unlock":
				return this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).stream()
						.filter(id -> this.plugin.getHorseManager().isHorseLocked(id))
						.map(id -> this.plugin.getHorseManager().getNameForHorse(id)).collect(Collectors.toList());
			case "kill":
			case "goaway":
			case "goto":
			case "select":
			case "comehere":
			case "info":
				return this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).stream()
						.map(id -> this.plugin.getHorseManager().getNameForHorse(id))
						.filter(name -> name.toLowerCase().startsWith(args[1])).collect(Collectors.toList());
			default:
				return null;
			}

		}
		return null;
	}

	private Map<String, Boolean> getCommandsForPlayer(Player player) {
		Map<String, Boolean> commandStates = new HashMap<String, Boolean>();

		commandStates.put("sell", this.plugin.economyEnabled);
		commandStates.put("chest", this.plugin.allowChestOnAllHorses);
		commandStates.put("removechest", this.plugin.allowChestOnAllHorses);
		commandStates.put("comehere", this.plugin.useHorseTeleportation);
		commandStates.put("goto", this.plugin.useHorseTeleportation);

		for (String cmd : commands) {
			if (this.plugin.getPermissionsManager().hasPermission(player, "myhorse." + cmd)) {
				commandStates.put(cmd, true);
			}
		}
		if (player.isOp()) {
			for (String cmd : ownerRelatedCommands) {
				commandStates.put(cmd, true);
			}
		}
		if (player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin")) {
			for (String cmd : adminRelatedCommands) {
				commandStates.put(cmd, true);
			}
		}

		return commandStates;
	}

	private boolean CommandMyHorse(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + this.plugin.getDescription().getFullName()
				+ " ------------------");
		sender.sendMessage(ChatColor.AQUA + "By DogOnFire");
		sender.sendMessage("");

		sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/myhorse help" + ChatColor.AQUA
				+ " for a list of commands");

		return true;
	}

	private boolean CommandHelp(CommandSender sender) {
		if ((sender != null) && (!sender.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.help"))) {
			sender.sendMessage(ChatColor.RED + "You do not have permission for that");
			return false;
		}
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + this.plugin.getDescription().getFullName()
				+ " ------------------");
		sender.sendMessage(ChatColor.AQUA + "/myhorse" + ChatColor.WHITE + " - Basic info");
		if ((sender.isOp())
				|| (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.comehere"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse comehere <horsename>" + ChatColor.WHITE
					+ " - Teleports your selected horse to you");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.name"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse name <name>" + ChatColor.WHITE + " - Gives your selected horse a name");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.lock"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse lock <horsename>" + ChatColor.WHITE + " - Locks your horse");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.unlock"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse unlock <horsename>" + ChatColor.WHITE + " - Unlocks your horse");
		}
		if ((sender.isOp())
				|| (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.addfriend"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse addfriend" + ChatColor.WHITE + " - Add friend to your horse");
		}
		if ((sender.isOp())
				|| (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.removefriend"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse removefriend" + ChatColor.WHITE + " - Removes a friend from your horse");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.list"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse list" + ChatColor.WHITE + " - Shows a list of your owned horses");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.goto"))) {
			if (this.plugin.useHorseTeleportation) {
				sender.sendMessage(ChatColor.AQUA + "/myhorse goto <horsename>" + ChatColor.WHITE
						+ " - Teleports you to one of your owned horses");
			}
		}
		if ((sender.isOp())
				|| (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.setowner"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse setowner <ownername>" + ChatColor.WHITE
					+ " - Set a new owner for your selected horse!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.select"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse select <horsename|id>" + ChatColor.WHITE
					+ " - Selects one of your horses!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.info"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse info" + ChatColor.WHITE + " - Display info about your horse!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.sell"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse sell <price>" + ChatColor.WHITE
					+ " - Set your selected horse for sale!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.claim"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse claim" + ChatColor.WHITE + " - Claims a un-owned horse");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.goaway"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse goaway <horsename|id>" + ChatColor.WHITE + " - Sets your horse free!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.kill"))) {
			sender.sendMessage(
					ChatColor.AQUA + "/myhorse kill <horsename|id>" + ChatColor.WHITE + " - Kill the selected horse!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.spawn"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse spawn <mule|skeleton|normal|zombie> <baby>" + ChatColor.WHITE
					+ " - Spawns a horse!");
		}
		if ((sender.isOp()) || (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.chest"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse chest" + ChatColor.WHITE
					+ " - Gets the horse's inventory if it is carrying a chest");
		}
		if ((sender.isOp())
				|| (this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.removechest"))) {
			sender.sendMessage(ChatColor.AQUA + "/myhorse removechest <horsename|id>" + ChatColor.WHITE
					+ " - Removes the chest from a horse");
		}
		return true;
	}

	private boolean CommandReload(CommandSender sender) {
		if ((!sender.isOp())
				&& (!this.plugin.getPermissionsManager().hasPermission((Player) sender, "myhorse.reload"))) {
			sender.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		this.plugin.reloadSettings();
		this.plugin.loadSettings();

		this.plugin.getHorseManager().load();

		this.plugin.getHorseManager().updateHorseEntities();

		sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ": " + ChatColor.WHITE
				+ "Reloaded configuration.");
		this.plugin.log(sender.getName() + " /mh reload");

		return true;
	}

	private boolean CommandSpawn(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.spawn"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 2) || (args.length > 3)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse spawn");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse spawn <mule|donkey|skeleton|normal|zombie>");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse spawn <mule|donkey|skeleton|normal|zombie> baby");
			return false;
		}

		EntityType variant = null;

		if (args.length > 1) {
			switch (args[1].toLowerCase()) {
			case "skeleton":
				variant = EntityType.SKELETON_HORSE;
				break;
			case "mule":
				variant = EntityType.MULE;
				break;
			case "donkey":
				variant = EntityType.DONKEY;
				break;
			case "zombie":
				variant = EntityType.ZOMBIE_HORSE;
				break;
			case "normal":
				variant = EntityType.HORSE;
				break;
			default:
				player.sendMessage(ChatColor.RED + "Invalid type of horse");
				break;
			}
		}

		this.plugin.getLanguageManager().setName(variant.name());

		boolean baby = false;
		if (args.length == 2) {
			if (args[1].equals("baby")) {
				baby = true;
				this.plugin.getLanguageManager().setName("baby " + variant.name());
			}
		} else if (args.length == 3) {
			if (args[2].equals("baby")) {
				baby = true;
				this.plugin.getLanguageManager().setName("baby " + variant.name());
			}
		}

		this.plugin.getHorseManager().newHorse(player, variant, baby);

		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.SpawnedHorse, ChatColor.GREEN));

		return true;
	}

	private boolean CommandSetName(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.name"))) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPermissionForCommand,
					ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse name <horsename>");
			return false;
		}
		String horseName = args[1];
		for (int n = 2; n < args.length; n++) {
			horseName = horseName + " " + args[n];
		}
		if ((horseName.length() < 2) || (horseName.equals("null"))) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.InvalidHorseName,
					ChatColor.DARK_RED));
			return false;
		}
		if (this.plugin.getHorseManager().ownedHorseWithName(player.getName(), horseName)) {
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.AlreadyHasHorseWithThatName, ChatColor.DARK_RED));
			return false;
		}
		UUID horseIdentifier = this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId());
		if (horseIdentifier == null) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoHorseSelected,
					ChatColor.DARK_RED));
			return false;
		}
		AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);
		if (horse == null) {
			this.plugin.getHorseManager().setNameForHorse(horseIdentifier, horseName);
			this.plugin.getLanguageManager().setName(horseName);
			player.sendMessage(
					this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.SetHorseName, ChatColor.GREEN));
			return true;
		}
		this.plugin.getHorseManager().setNameForHorse(horseIdentifier, horseName);

		horse.setCustomName(this.plugin.getHorseNameColorForPlayer(player.getUniqueId()) + horseName);
		horse.setCustomNameVisible(this.plugin.alwaysShowHorseName);

		this.plugin.getLanguageManager().setName(horseName);
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.SetHorseName, ChatColor.GREEN));

		return true;
	}

	private boolean CommandLock(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.lock"))) {
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse lock");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse lock <horsename>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}
		this.plugin.getHorseManager().setLockedForHorse(horseIdentifier, true);

		this.plugin.getLanguageManager().setName(this.plugin.getHorseManager().getNameForHorse(horseIdentifier));
		player.sendMessage(this.plugin.getLanguageManager()
				.getLanguageString(LanguageManager.LANGUAGESTRING.HorseLocked, ChatColor.GREEN));

		return true;
	}

	private boolean CommandUnlock(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.unlock"))) {
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse unlock");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse unlock <horsename>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}
		this.plugin.getHorseManager().setLockedForHorse(horseIdentifier, false);

		this.plugin.getLanguageManager().setName(this.plugin.getHorseManager().getNameForHorse(horseIdentifier));
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.HorseUnlocked, ChatColor.GREEN));

		return true;
	}

	private boolean CommandComeHere(Player player, String[] args) {
		if (!this.plugin.useHorseTeleportation
				&& !(player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin"))) {
			this.plugin.getLanguageManager().setAmount("/mh comehere");
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.CommandDisabled,
					ChatColor.DARK_RED));
			return false;
		}
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.comehere"))) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPermissionForCommand,
					ChatColor.DARK_RED));
			return false;
		}
		if (args.length > 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse comehere");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse comehere <horsename>");
			return false;
		}

		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}

		AbstractHorse horse = (AbstractHorse) plugin.getHorseManager().getHorseEntity(horseIdentifier);

		if (horse == null) {
			horse = (AbstractHorse) plugin.getHorseManager().getUnloadedHorseEntity(horseIdentifier);

			if (horse == null) {
				horse = this.plugin.getHorseManager().restoreHorseEntity(horseIdentifier, player.getLocation());
				horseIdentifier = horse.getUniqueId();
				this.plugin.logDebug("Horse " + horseIdentifier.toString()
						+ " could not be found in the world and was therefore restored!");
				return true;
			}
		}

		if (!horse.teleport(player.getLocation(), TeleportCause.COMMAND)) {
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.CouldNotTeleportYourHorse, ChatColor.DARK_RED));
			return false;
		}

		this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier, player.getLocation());

		this.plugin.getLanguageManager().setName(this.plugin.getHorseManager().getNameForHorse(horseIdentifier));
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.ComeHere, ChatColor.GREEN));

		return true;
	}

	private boolean CommandGoAway(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.goaway"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse goaway");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse goaway <horsename>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}
		AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);
		if (horse == null) {
			horse = (AbstractHorse) this.plugin.getHorseManager().getUnloadedHorseEntity(horseIdentifier);
			if (horse == null) {
				horse = this.plugin.getHorseManager().restoreHorseEntity(horseIdentifier,
						this.plugin.getHorseManager().getHorseLastSelectionPosition(horseIdentifier));
				horseIdentifier = horse.getUniqueId();
				this.plugin.logDebug("Horse " + horseIdentifier.toString()
						+ " could not be found in the world and was therefore restored!");
			}
		}
		System.out.println(this.plugin.getHorseManager().isHorseCarryingChest(horseIdentifier));
		if (this.plugin.getHorseManager().isHorseCarryingChest(horseIdentifier))
			this.plugin.getHorseManager().removeCustomInventoryFromHorse(horseIdentifier, player.getLocation());

		this.plugin.getHorseManager().removeCustomInventoryFromHorse(horseIdentifier, player.getLocation());

		for (ItemStack item : horse.getInventory().getContents()) {
			if (item != null)
				player.getWorld().dropItem(player.getLocation(), item);
		}
		horse.getInventory().clear();
		horse.setTamed(false);

		this.plugin.getLanguageManager().setName(this.plugin.getHorseManager().getNameForHorse(horseIdentifier));
		player.sendMessage(ChatColor.GREEN + this.plugin.getLanguageManager()
				.getLanguageString(LANGUAGESTRING.YouSetYourHorseFree, ChatColor.GREEN));
		System.out.println(this.plugin.getHorseManager().isHorseCarryingChest(horseIdentifier));
		System.out.println(this.plugin.getHorseManager().isHorseCarryingChest(horse.getUniqueId()));
		this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, null);

		return true;
	}

	private UUID getHorseIdentifierFromArgs(Player player, String[] args) {
		UUID horseIdentifier = null;
		if (args.length == 1) {
			horseIdentifier = this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId());
			if (horseIdentifier == null) {
				player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
						.getLanguageString(LanguageManager.LANGUAGESTRING.NoHorseSelected, ChatColor.DARK_RED));
				return null;
			}
		} else {
			horseIdentifier = this.plugin.getHorseManager().getHorseByName(args[1]);

			if (horseIdentifier == null) {
				int horseIndex = 0;
				try {
					horseIndex = Integer.parseInt(args[1]) - 1;
				} catch (Exception indexException) {
					this.plugin.getLanguageManager().setName(args[1]);
					if (StringUtils.isAlphanumeric(args[1])) {
						player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager().getLanguageString(
								LanguageManager.LANGUAGESTRING.NoHorseWithSuchName, ChatColor.DARK_RED));
					} else {
						player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager().getLanguageString(
								LanguageManager.LANGUAGESTRING.NoHorseWithSuchID, ChatColor.DARK_RED));
					}
					return null;
				}
				List<UUID> horseList = null;
				if (this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin")) {
					horseList = this.plugin.getHorseManager().getAllHorses();
				} else {
					horseList = this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId());
				}
				if ((horseIndex < 0) || (horseIndex >= horseList.size())) {
					player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
							.getLanguageString(LanguageManager.LANGUAGESTRING.InvalidCommand, ChatColor.DARK_RED));
					return null;
				}
				horseIdentifier = (UUID) horseList.get(horseIndex);
			}
		}
		return horseIdentifier;
	}

	private boolean CommandKill(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.kill"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse kill");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse kill <horsename>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoHorseSelected,
					ChatColor.DARK_RED));
			return false;
		}

		AbstractHorse abstractHorse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);

		if (abstractHorse == null) {
			abstractHorse = (AbstractHorse) this.plugin.getHorseManager().getUnloadedHorseEntity(horseIdentifier);
			if (abstractHorse == null) {
				abstractHorse = this.plugin.getHorseManager().restoreHorseEntity(horseIdentifier,
						this.plugin.getHorseManager().getHorseLastSelectionPosition(horseIdentifier));
				horseIdentifier = abstractHorse.getUniqueId();
			}
		}

		for (ItemStack item : abstractHorse.getInventory()) {
			if (item != null)
				player.getWorld().dropItem(player.getLocation(), item);
		}

		abstractHorse.remove();

		String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);
		if (this.plugin.getHorseManager().isHorseCarryingChest(horseIdentifier))
			this.plugin.getHorseManager().removeCustomInventoryFromHorse(horseIdentifier, player.getLocation());
		this.plugin.getLanguageManager().setName(horseName);
		this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, null);
		this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), null);
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouKilledHorse, ChatColor.GREEN));
		return true;
	}

	private boolean CommandSetOwner(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.setowner"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse setowner <playername>");
			return false;
		}
		UUID horseIdentifier = this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId());
		if (horseIdentifier == null) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoHorseSelected, ChatColor.DARK_RED));
			return false;
		}
		String ownerName = args[1];

		Player ownerPlayer = null;

		for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
			if (offlinePlayer.getName() == args[1]) {
				ownerPlayer = offlinePlayer.getPlayer();
			}
		}

		if (ownerPlayer == null) {
			this.plugin.getLanguageManager().setPlayerName(ownerName);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.HasNeverPlayedOnTheServer, ChatColor.DARK_RED));
			return false;
		}
		int maxHorses = this.plugin.getMaximumHorsesForPlayer(ownerPlayer.getName());
		int numberOfHorses = this.plugin.getHorseManager().getHorsesForOwner(ownerPlayer.getUniqueId()).size();
		if ((maxHorses > 0) && (numberOfHorses >= maxHorses)) {
			this.plugin.getLanguageManager().setPlayerName(ownerPlayer.getName());
			this.plugin.getLanguageManager().setAmount(String.valueOf(maxHorses));
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.PlayerCannotHaveMoreHorses, ChatColor.DARK_RED));
			return false;
		}
		AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);
		String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);

		if (horse != null) {
			horse.setOwner(ownerPlayer);
			horse.setCustomName(this.plugin.getHorseNameColorForPlayer(ownerPlayer.getUniqueId()) + horseName);
			horse.setCustomNameVisible(this.plugin.alwaysShowHorseName);
		}

		this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), null);

		this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, ownerPlayer.getUniqueId());

		this.plugin.getLanguageManager().setName(horseName);
		this.plugin.getLanguageManager().setPlayerName(ownerName);

		player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouSetOwnerForHorse,
				ChatColor.GREEN));
		ownerPlayer.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouAreNewOwnerOfHorse,
				ChatColor.GREEN));

		return true;
	}

	private boolean CommandList(Player player, String[] args) {
		if (!player.isOp() && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.list"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if (args.length < 1 || args.length > 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse list");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse list <page number>");
			return false;
		}
		List<UUID> horseList = null;
		if (this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin")) {
			horseList = this.plugin.getHorseManager().getAllHorses();
		} else {
			horseList = this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId());
		}

		int totalHorses = horseList.size();
		int pageIndex = 0;
		int toIndex = 0;
		if (args.length > 1) {
			try {
				pageIndex = Integer.parseInt(args[1]);
			} catch (Exception ex) {
				player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.InvalidCommand,
						ChatColor.DARK_RED));
				return false;
			}
			if (pageIndex * 15 < horseList.size() - 1) {
				toIndex = pageIndex * 15 + 15;
				if (toIndex >= horseList.size()) {
					toIndex = horseList.size() - 1;
				}
				horseList = horseList.subList(pageIndex * 15, toIndex);
			} else {
				pageIndex = 0;
			}
		} else if (horseList.size() > 16) {
			horseList = horseList.subList(0, 15);
		}

		this.plugin.getLanguageManager().setAmount("" + totalHorses);
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YourOwnedHorsesList, ChatColor.GOLD));

		int n = pageIndex * 15 + 1;
		for (UUID horseIdentifier : horseList) {
			String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);
			if (player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin")) {
				UUID ownerId = this.plugin.getHorseManager().getOwnerForHorse(horseIdentifier);

				if (ownerId != null) {
					player.sendMessage("" + ChatColor.AQUA + n++ + ") "
							+ this.plugin.getHorseNameColorForPlayer(ownerId) + horseName + ChatColor.AQUA + " ("
							+ plugin.getServer().getOfflinePlayer(ownerId).getName() + ")");
				} else {
					this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, null);
					plugin.logDebug("Horse " + horseIdentifier + " has no owner. Freeing horse.");
				}
			} else {
				player.sendMessage("" + ChatColor.AQUA + n++ + ") "
						+ this.plugin.getHorseNameColorForPlayer(player.getUniqueId()) + horseName + ChatColor.AQUA);
			}
		}

		if ((this.plugin.getPermissionsManager().hasPermission(player, "myhorse.goto")) && (horseList.size() > 0)) {
			if (this.plugin.useHorseTeleportation) {
				this.plugin.getLanguageManager().setAmount("/myhorse goto <number>");
				player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.UseGotoCommand,
						ChatColor.AQUA));
			}
		}
		return true;
	}

	private boolean CommandGoto(Player player, String[] args) {
		if (!this.plugin.useHorseTeleportation
				&& !(player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin"))) {
			this.plugin.getLanguageManager().setAmount("/mh goto");
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.CommandDisabled,
					ChatColor.DARK_RED));
			return false;
		}
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.goto"))) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPermissionForCommand,
					ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse goto");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse goto <horsename>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}
		Location location = null;
		AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);
		try {
			location = horse.getLocation();
		} catch (NullPointerException e) {
			if (horse == null) {
				horse = (AbstractHorse) this.plugin.getHorseManager().getUnloadedHorseEntity(horseIdentifier);
				location = this.plugin.getHorseManager().getHorseLastSelectionPosition(horseIdentifier);
				if (horse == null) {
					horse = this.plugin.getHorseManager().restoreHorseEntity(horseIdentifier,
							this.plugin.getHorseManager().getHorseLastSelectionPosition(horseIdentifier));
					horseIdentifier = horse.getUniqueId();
				}
			}
		}

		player.teleport(location, TeleportCause.COMMAND);

		this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), horseIdentifier);

		return true;
	}

	private boolean CommandSelect(Player player, String[] args) {
		if (!player.isOp() && !this.plugin.getPermissionsManager().hasPermission(player, "myhorse.select")) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPermissionForCommand,
					ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse select <horsename>");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse select <id>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}
		String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);

		UUID ownerId = this.plugin.getHorseManager().getOwnerForHorse(horseIdentifier);

		if (!player.isOp() && !this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin")) {
			if (!ownerId.equals(player.getUniqueId())
					&& !this.plugin.getHorseManager().isHorseFriend(horseIdentifier, player.getUniqueId())) {
				player.sendMessage(this.plugin.getLanguageManager()
						.getLanguageString(LANGUAGESTRING.CannotUseLockedHorse, ChatColor.DARK_RED));
				return false;
			}
		}

		this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), horseIdentifier);
		this.plugin.getLanguageManager().setName(horseName);
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouSelectedHorse, ChatColor.GREEN));
		if (this.plugin.getHorseManager().isBabyHorse(horseIdentifier)) {
			this.plugin.getLanguageManager().setAmount(TimeUtils
					.parseMilisToUFString(this.plugin.getHorseManager().getBabyTimeBeforeAdult(horseIdentifier)));
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.BabyTimeBeforeAdult,
					ChatColor.GREEN));
		}

		return true;
	}

	private boolean CommandInfo(Player player, String[] args) {
		if (!player.isOp() && !this.plugin.getPermissionsManager().hasPermission(player, "myhorse.info")) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPermissionForCommand,
					ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse info");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse info <horsename>");
			return false;
		}
		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);
		if (horseIdentifier == null) {
			return false;
		}
		AbstractHorse abstractHorse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);
		if (abstractHorse == null) {
			return false;
		}
		String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);
		if (horseName == null) {
			return false;
		}
		// TODO - Give the player more information about the horse
		player.sendMessage(ChatColor.YELLOW + "------------------ " + horseName + " ------------------");

		player.sendMessage(ChatColor.AQUA + "Type: " + ChatColor.WHITE + abstractHorse.getType().name());
		if (abstractHorse.getType() == EntityType.HORSE) {
			Horse horse = (Horse) abstractHorse;
			player.sendMessage(ChatColor.AQUA + "Color: " + ChatColor.WHITE + horse.getColor().name());
		}
		player.sendMessage(ChatColor.AQUA + "Owner: " + ChatColor.WHITE + abstractHorse.getOwner().getName());

		player.sendMessage(ChatColor.AQUA + "Max Health: " + ChatColor.WHITE
				+ abstractHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		// Source: https://minecraft.gamepedia.com/Horse#Jump_strength
		player.sendMessage(ChatColor.AQUA + "Jump Strength: " + ChatColor.WHITE
				+ MathUtils.round(
						-0.1817584952 * Math.pow(abstractHorse.getJumpStrength(), 3)
						+ 3.689713992 * Math.pow(abstractHorse.getJumpStrength(), 2)
						+ 2.128599134 * abstractHorse.getJumpStrength() - 0.343930367, 2) + " blocks");
		player.sendMessage(ChatColor.AQUA + "Is Carrying Chest: " + ChatColor.WHITE
				+ this.plugin.getHorseManager().isHorseCarryingChest(horseIdentifier));
		if(!abstractHorse.isAdult()) player.sendMessage(ChatColor.AQUA + "Time before adult: " + ChatColor.WHITE + TimeUtils
				.parseMilisToUFString(this.plugin.getHorseManager().getBabyTimeBeforeAdult(horseIdentifier)));
		List<UUID> friends = this.plugin.getHorseManager().getHorseFriends(horseIdentifier);
		if (friends.size() > 0) {
			player.sendMessage(ChatColor.AQUA + "Friends: ");
			for (UUID friendId : friends) {
				player.sendMessage(ChatColor.WHITE + "- " + plugin.getServer().getOfflinePlayer(friendId).getName());
			}
		}

		return true;
	}

	private boolean CommandSell(Player player, String[] args) {
		if (this.plugin.getEconomy() == null) {
			this.plugin.getLanguageManager().setAmount("/mh sell");
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.CommandDisabled,
					ChatColor.DARK_RED));
			return false;
		}
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.sell"))) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPermissionForCommand,
					ChatColor.DARK_RED));
			return false;
		}
		if ((args.length < 1) || (args.length > 2)) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse sell <price>");
			return false;
		}

		UUID horseIdentifier = this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId());

		if (horseIdentifier == null) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoHorseSelected,
					ChatColor.DARK_RED));
			return false;
		}

		if (args.length == 1) {
			AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);

			String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);
			if (horse != null)
				horse.setCustomName(this.plugin.getHorseNameColorForPlayer(player.getUniqueId()) + horseName);

			if (this.plugin.getHorseManager().getHorsePrice(horseIdentifier) == 0) {
				this.plugin.getLanguageManager().setAmount("/mh sell <price>");
				this.plugin.getLanguageManager().setName(horseName);
				player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NoPriceOnHorse,
						ChatColor.DARK_RED));
				return false;
			}

			this.plugin.getHorseManager().setHorseForSale(horseIdentifier, 0);
			this.plugin.getLanguageManager().setName(horseName);
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YouCancelledHorseForSale, ChatColor.YELLOW));

			return false;
		}

		int sellingPrice;
		try {
			sellingPrice = Integer.parseInt(args[1]);
		} catch (Exception ex) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.InvalidCommand,
					ChatColor.DARK_RED));
			return false;
		}

		if (sellingPrice <= 0) {
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.InvalidCommand,
					ChatColor.DARK_RED));
			return false;
		}
		String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);

		AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseIdentifier);

		if (horse != null)
			horse.setCustomName(
					ChatColor.GOLD + horseName + ChatColor.RED + " " + this.plugin.getEconomy().format(sellingPrice));

		this.plugin.getHorseManager().setHorseForSale(horseIdentifier, sellingPrice);

		this.plugin.getLanguageManager().setAmount(this.plugin.getEconomy().format(sellingPrice));
		this.plugin.getLanguageManager().setName(horseName);

		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouSetHorseForSale, ChatColor.GREEN));

		this.plugin.getLanguageManager().setAmount("/myhorse sell");
		player.sendMessage(
				this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.InfoCancelHorseSale, ChatColor.AQUA));

		return true;
	}

	private boolean CommandAddFriend(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.addfriend"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse addfriend <playername>");
			return false;
		}
		UUID horseIdentifier = this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId());
		if (horseIdentifier == null) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoHorseSelected, ChatColor.DARK_RED));
			return false;
		}

		String friendName = args[1];
		UUID friendId = null;

		for (OfflinePlayer playerName : Bukkit.getOfflinePlayers()) {
			if (playerName.getName() == friendName)
				friendId = playerName.getUniqueId();
		}

		if (friendId == null) {
			this.plugin.getLanguageManager().setPlayerName(friendName);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.HasNeverPlayedOnTheServer, ChatColor.DARK_RED));
			return false;
		}

		if (this.plugin.getHorseManager().isHorseFriend(horseIdentifier, friendId)) {
			this.plugin.getLanguageManager().setPlayerName(friendName);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.AlreadyHorseFriend, ChatColor.DARK_RED));
			return false;
		}

		if (friendId.equals(player.getUniqueId())) {
			return false;
		}

		this.plugin.getHorseManager().addHorseFriend(horseIdentifier, friendId);

		this.plugin.getLanguageManager().setPlayerName(friendName);
		player.sendMessage(ChatColor.GREEN + this.plugin.getLanguageManager()
				.getLanguageString(LanguageManager.LANGUAGESTRING.YouAddedFriendToHorse, ChatColor.GREEN));

		return true;
	}

	private boolean CommandRemoveFriend(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.removefriend"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse removefriend <playername>");
			return false;
		}
		UUID horseIdentifier = this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId());
		if (horseIdentifier == null) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NoHorseSelected, ChatColor.DARK_RED));
			return false;
		}

		String friendName = args[1];

		UUID friendId = plugin.getServer().getPlayer(friendName).getUniqueId();

		if (friendId == null) {
			return false;
		}

		if (!this.plugin.getHorseManager().isHorseFriend(horseIdentifier, friendId)) {
			this.plugin.getLanguageManager().setPlayerName(friendName);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.NotHorseFriend, ChatColor.DARK_RED));
			return false;
		}
		this.plugin.getHorseManager().removeHorseFriend(horseIdentifier, friendId);

		this.plugin.getLanguageManager().setPlayerName(friendName);
		player.sendMessage(ChatColor.GREEN + this.plugin.getLanguageManager()
				.getLanguageString(LANGUAGESTRING.YouRemovedFriendToHorse, ChatColor.GREEN));

		return true;
	}

	private boolean CommandClaim(Player player, String[] args) {
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.claim"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 1) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse claim");
			return false;
		}
		if (!player.isInsideVehicle()) {
			return false;
		}

		if (!(player.getVehicle() instanceof AbstractHorse)
				|| ((player.getVehicle() instanceof Llama) || (player.getVehicle() instanceof TraderLlama))) {
			player.sendMessage(
					this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.NotAHorse, ChatColor.DARK_RED));
			return false;
		}
		AbstractHorse abstractHorse = (AbstractHorse) player.getVehicle();

		if (abstractHorse.getOwner() != null && !abstractHorse.getOwner().equals(player)) {
			this.plugin.getLanguageManager()
					.setPlayerName(Bukkit.getOfflinePlayer(abstractHorse.getOwner().getUniqueId()).getName());
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouMountedOwnedHorse,
					ChatColor.DARK_RED));
			return false;
		}

		UUID horseIdentifier = abstractHorse.getUniqueId();

		if (horseIdentifier == null)
			return false;

		if (this.plugin.getHorseManager().isHorseOwned(horseIdentifier)) {
			UUID playerId = this.plugin.getHorseManager().getOwnerForHorse(horseIdentifier);
			this.plugin.getLanguageManager().setPlayerName(plugin.getServer().getOfflinePlayer(playerId).getName());

			if (this.plugin.getHorseManager().getOwnerForHorse(horseIdentifier).equals(playerId))
				player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
						.getLanguageString(LanguageManager.LANGUAGESTRING.AlreadyOwnThatHorse, ChatColor.DARK_RED));
			else
				player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
						.getLanguageString(LANGUAGESTRING.YouCannotClaimThisHorse, ChatColor.DARK_RED));

			return false;
		}

		int maxHorses = this.plugin.getMaximumHorsesForPlayer(player.getName());
		int numberOfHorses = this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).size();
		if ((maxHorses > 0) && (numberOfHorses >= maxHorses)) {
			this.plugin.getLanguageManager().setName(player.getName());
			this.plugin.getLanguageManager().setAmount("" + maxHorses);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YouCannotHaveMoreHorses, ChatColor.DARK_RED));
			return false;
		}
		String horseName = this.plugin.getHorseManager().getNewHorseName(player.getUniqueId());

		this.plugin.getHorseManager().setNameForHorse(horseIdentifier, horseName);
		abstractHorse.playEffect(EntityEffect.LOVE_HEARTS);
		abstractHorse.setCustomName(this.plugin.getHorseNameColorForPlayer(player.getUniqueId()) + horseName);
		abstractHorse.setCustomNameVisible(this.plugin.alwaysShowHorseName);

		abstractHorse.setOwner(player);
		if (player.getVehicle() instanceof Horse) {
			Horse horse = (Horse) abstractHorse;
			this.plugin.getHorseManager().setHorseStyle(horseIdentifier, horse.getStyle());
			this.plugin.getHorseManager().setHorseColor(horseIdentifier, horse.getColor());
			this.plugin.getHorseManager().setHorseArmor(horseIdentifier,
					(horse.getInventory().getArmor() != null) ? horse.getInventory().getArmor() : null);
		}

		this.plugin.getHorseManager().setLockedForHorse(horseIdentifier, true);
		this.plugin.getHorseManager().setHorseHasSaddle(horseIdentifier,
				(abstractHorse.getInventory().getSaddle() != null) ? true : false);
		this.plugin.getHorseManager().setHorseType(horseIdentifier, abstractHorse.getType());
		this.plugin.getHorseManager().setHorseJumpStrength(horseIdentifier, abstractHorse.getJumpStrength());
		this.plugin.getHorseManager().setHorseMaxHealth(horseIdentifier,
				abstractHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		this.plugin.getHorseManager().setBabyForHorse(horseIdentifier, false);
		this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, player.getUniqueId());

		player.sendMessage(ChatColor.GREEN
				+ this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouClaimedAHorse, ChatColor.GREEN));
		if (player.getVehicle() instanceof ChestedHorse) {
			ChestedHorse chestedHorse = (ChestedHorse) player.getVehicle();
			if (chestedHorse.isCarryingChest()) {
				this.plugin.getHorseManager().setCarryingChest(chestedHorse.getUniqueId(), true);
				Inventory horseCustomInventory = Bukkit.createInventory(player, 9 * 2);
				for (ItemStack itemStack : chestedHorse.getInventory().getStorageContents()) {
					if (itemStack != null && !itemStack.getType().equals(Material.SADDLE))
						horseCustomInventory.addItem(itemStack);
				}
				this.plugin.getHorseManager().saveHorseCustomInventory(horseIdentifier, horseCustomInventory);
				chestedHorse.setCarryingChest(false);
				player.sendMessage(this.plugin.getLanguageManager()
						.getLanguageString(LANGUAGESTRING.ChestedHorseItemsMoved, ChatColor.YELLOW));
			}
		}

		if (this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).size() == 1) {
			this.plugin.getLanguageManager().setAmount("/myhorse name <horsename>");
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.UseCommandToNameYourHorse, ChatColor.AQUA));

			this.plugin.getLanguageManager().setAmount("/myhorse unlock");
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.UseCommandToUnlockYourHorse, ChatColor.AQUA));

			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.PutChestOnHorseInfo,
					ChatColor.AQUA));
		}
		return true;
	}

	private boolean CommandChest(Player player, String[] args) {

		if (!this.plugin.allowChestOnAllHorses
				&& !(player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin"))) {
			this.plugin.getLanguageManager().setAmount("/mh chest");
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.CommandDisabled,
					ChatColor.DARK_RED));
			return false;
		}
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.chest"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.chest"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}
		if (args.length != 1) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse chest");
			return false;
		}
		if (!player.isInsideVehicle()) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NotRidingAHorse, ChatColor.YELLOW));
			return false;
		}
		if (!(player.getVehicle() instanceof AbstractHorse)
				|| ((player.getVehicle() instanceof Llama) || (player.getVehicle() instanceof TraderLlama))) {
			return false;
		}
		if (!plugin.getHorseManager().isHorseCarryingChest(player.getVehicle().getUniqueId())) {
			this.plugin.getLanguageManager()
					.setName(plugin.getHorseManager().getNameForHorse(player.getVehicle().getUniqueId()));
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.HorseHasNoChest, ChatColor.DARK_RED));
			return false;
		}
		player.openInventory(plugin.getHorseManager().getHorseCustomInventory(player.getVehicle().getUniqueId()));

		return true;
	}

	private boolean CommandRemoveChest(Player player, String[] args) {
		if (!this.plugin.allowChestOnAllHorses
				&& !(player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin"))) {
			this.plugin.getLanguageManager().setAmount("/mh removechest");
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.CommandDisabled,
					ChatColor.DARK_RED));
			return false;
		}
		if ((!player.isOp()) && (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.removechest"))) {
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.NoPermissionForCommand, ChatColor.DARK_RED));
			return false;
		}

		if (args.length < 1 || args.length > 2) {
			player.sendMessage(ChatColor.RED + "Usage: /myhorse removechest");
			player.sendMessage(ChatColor.RED + "Usage: /myhorse removechest <horsename>");
			return false;
		}

		UUID horseIdentifier = getHorseIdentifierFromArgs(player, args);

		if (horseIdentifier == null) {
			return false;
		}
		if (!plugin.getHorseManager().isHorseCarryingChest(horseIdentifier)) {
			return false;
		}

		this.plugin.getHorseManager().removeCustomInventoryFromHorse(horseIdentifier, player.getLocation());
		this.plugin.getLanguageManager().setName(this.plugin.getHorseManager().getNameForHorse(horseIdentifier));

		if (!plugin.getHorseManager().isHorseCarryingChest(horseIdentifier))
			player.sendMessage(ChatColor.GREEN + this.plugin.getLanguageManager()
					.getLanguageString(LanguageManager.LANGUAGESTRING.ChestHasBeenRemoved, ChatColor.GREEN));
		else
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.ChestHasBeenRemovedFailure, ChatColor.DARK_RED));

		return true;
	}
}
