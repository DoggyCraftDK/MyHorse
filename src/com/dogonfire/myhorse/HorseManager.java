package com.dogonfire.myhorse;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.TraderLlama;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HorseManager {
	private MyHorse plugin = null;
	private FileConfiguration horsesConfig = null;
	private File horsesConfigFile = null;
	private Random random = new Random();

	HorseManager(MyHorse plugin) {
		this.plugin = plugin;
	}

	public void load() {
		if (this.horsesConfigFile == null) {
			this.horsesConfigFile = new File(this.plugin.getDataFolder(), "horses.yml");
		}
		this.horsesConfig = YamlConfiguration.loadConfiguration(this.horsesConfigFile);
		if (this.horsesConfig == null) {
			this.plugin.log("Error loading horses.yml! This plugin will NOT work.");
			return;
		}
		this.plugin.log("Loaded " + this.horsesConfig.getKeys(false).size() + " horses.");
	}

	public void save() {
		if ((this.horsesConfig == null) || (this.horsesConfigFile == null)) {
			return;
		}
		try {
			this.horsesConfig.save(this.horsesConfigFile);
		} catch (Exception ex) {
			this.plugin.log("Could not save config to " + this.horsesConfigFile + ": " + ex.getMessage());
		}
	}

	private FileConfiguration getHorsesConfig() {
		return this.horsesConfig;
	}

	private void setHorsesConfig(String path, Object value) {
		getHorsesConfig().set(path, value);
		this.save();
	}

	public void newHorse(Player player, EntityType type, boolean baby) {
		AbstractHorse horse;
		try {
			horse = (AbstractHorse) player.getLocation().getWorld().spawnEntity(player.getLocation(), type);
			if (baby)
				((Ageable) horse).setBaby();
		} catch (NullPointerException e) {
			player.sendMessage(ChatColor.RED + type.name() + " is not a valid horsetype");
		}
	}

	public boolean ownedHorseWithName(String ownerName, String name) {
		for (String horseIdentifierString : getHorsesConfig().getKeys(false)) {
			String horseOwnerName = this.horsesConfig.getString(horseIdentifierString + ".Owner");
			if ((horseOwnerName != null) && (horseOwnerName.equals(ownerName))) {
				String horseName = getHorsesConfig().getString(horseIdentifierString + ".Name");
				if (horseName.equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getNewHorseName(UUID playerId) {
		boolean noRandomNamesLeft = false;
		String[] names = { "Snowwhite", "Blackie", "Misty", "Ghostrunner", "Sleipner", "Binky", "Starlight", "Stormy",
				"Silver", "Snowfire", "Luna", "Lucky", "Prince", "Spirit", "Coco", "Romeo", "King", "Buttercup", "Rose",
				"Thunder", "Lightning", "Titan", "Beast", "Firestarter", "Spitfire", "Tornado", "Dreamrunner", "Nova",
				"Shadow", "Cookie", "Maria", "Thunderhoof", "Mirage", "Neptune", "Athena", "Calypso", "Nitro", "Diana",
				"Electra", "Kira", "April", "Aurora", "Angelfire", "Rainbow", "Ranger", "Nirvana", "Tomcat", "Treasure",
				"Tyson", "Pearl", "Pilgrim", "Playboy", "Popcorn", "Majesty" };
		List<String> horseNames = getHorsesForOwner(playerId).stream().map(e -> getNameForHorse(e))
				.collect(Collectors.toList());

		if (horseNames.size() >= names.length) {
			noRandomNamesLeft = true;
			for (int i = 0; i < names.length; i++) {
				if (!horseNames.contains(names[i])) {
					noRandomNamesLeft = false;
					break;
				}
			}
		}

		String name = names[this.random.nextInt(names.length)];

		while (horseNames.contains(name) && !noRandomNamesLeft) {
			name = names[this.random.nextInt(names.length)];
		}
		if (noRandomNamesLeft)
			name = horseNames.get(random.nextInt(horseNames.size())) + random.nextInt(100);

		return name;
	}

	public boolean isHorseLocked(UUID horseIdentifier) {
		return getHorsesConfig().getBoolean(horseIdentifier.toString() + ".Locked");
	}

	public void setLockedForHorse(UUID horseIdentifier, boolean locked) {
		setHorsesConfig(horseIdentifier.toString() + ".Locked", locked);
	}

	/**
	 * @ @param horseIdentifier
	 * @return the amount seconds left
	 */
	public long getBabyTimeBeforeAdult(UUID horseIdentifier) {
		return ((getHorseBirthDate(horseIdentifier).getTime()
				+ TimeUnit.SECONDS.toMillis(this.plugin.amountOfSecondsBeforeAdult)) - System.currentTimeMillis());
	}

	public Date getHorseBirthDate(UUID horseIdentifier) {
		try {
			return new SimpleDateFormat("HH:mm:ss dd-MM-yyyy")
					.parse(getHorsesConfig().getString(horseIdentifier.toString() + ".BirthDate"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setNameForHorse(UUID horseIdentifier, String name) {
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		setHorsesConfig(horseIdentifier.toString() + ".Name", name);
		setHorsesConfig(horseIdentifier.toString() + ".BirthDate", formatter.format(thisDate));
	}

	/**
	 * 
	 * @param horseIdentifier
	 * @return the entity or null if the entity couldn't be found in loaded
	 * chunks
	 */
	public Entity getHorseEntity(UUID horseIdentifier) {
		// Start by searching in loaded chunks
		for (World world : plugin.getServer().getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if ((entity instanceof AbstractHorse)
						&& !((entity instanceof Llama) || (entity instanceof TraderLlama))) {
					if (entity.getUniqueId().equals(horseIdentifier)) {
						return entity;
					}
				}
			}
		}
		return null;
	}

	public Entity getUnloadedHorseEntity(UUID horseIdentifier) {
		Chunk horseChunk = getHorseLastSelectionPosition(horseIdentifier).getChunk();

		if (horseChunk.load()) {
			for (Entity entity : horseChunk.getEntities()) {
				if ((entity instanceof AbstractHorse)
						&& !((entity instanceof Llama) || (entity instanceof TraderLlama))) {
					if (entity.getUniqueId().equals(horseIdentifier)) {
						plugin.logDebug("Chunks loaded: " + horseChunk.getWorld().getLoadedChunks().length + " | "
								+ horseChunk.getWorld().getName());
						return entity;
					}
				}
			}
		}
		return null;
	}

	public void updateHorseEntities() {
		for (UUID id : getAllHorses()) {
			AbstractHorse horseEntity = (AbstractHorse) getHorseEntity(id);
			if (horseEntity != null) {
				horseEntity.setCustomName(
						this.plugin.getHorseNameColorForPlayer(getOwnerForHorse(id)) + getNameForHorse(id));

				if (!isBabyHorse(id)) {
					horseEntity.setAgeLock(false);
					horseEntity.setAdult();
				}
			}
		}
	}

	public void setHorseHasSaddle(UUID horseIdentifier, boolean hasSaddle) {
		setHorsesConfig(horseIdentifier.toString() + ".hasSaddle", hasSaddle);
	}

	public boolean getHorseHasSaddle(UUID horseIdentifier) {
		return getHorsesConfig().getBoolean(horseIdentifier.toString() + ".hasSaddle");
	}

	public void setHorseArmor(UUID horseIdentifier, ItemStack armor) {
		setHorsesConfig(horseIdentifier.toString() + ".Armor", armor);
	}

	public ItemStack getHorseArmor(UUID horseIdentifier) {
		return (ItemStack) getHorsesConfig().get(horseIdentifier.toString() + ".Armor");
	}

	public UUID getHorseByName(String horseName) {
		for (String horseIdentifierString : getHorsesConfig().getKeys(false)) {
			String name = getHorsesConfig().getString(horseIdentifierString + ".Name");
			if ((name != null) && (name.equalsIgnoreCase(horseName))) {
				return UUID.fromString(horseIdentifierString);
			}
		}
		return null;
	}

	public UUID getOwnerForHorse(UUID horseIdentifier) {
		String ownerString = getHorsesConfig().getString(horseIdentifier.toString() + ".Owner");
		UUID ownerId = null;

		try {
			ownerId = UUID.fromString(ownerString);
		} catch (Exception ex) {
			return null;
		}

		return ownerId;
	}

	public void setCarryingChest(UUID horseIdentifier, boolean isCarryingChest) {
		setHorsesConfig(horseIdentifier.toString() + ".IsCarryingChest", isCarryingChest);
	}

	public void removeCustomInventoryFromHorse(UUID horseIdentifier, Location dropLocation) {
		for (ItemStack item : getHorseCustomInventory(horseIdentifier).getContents()) {
			if (item != null) {
				dropLocation.getWorld().dropItem(dropLocation, item);
			}
		}
		ItemStack chest = new ItemStack(Material.CHEST, 1);
		dropLocation.getWorld().dropItem(dropLocation, chest);

		setHorsesConfig(horseIdentifier.toString() + ".IsCarryingChest", false);
		setHorsesConfig(horseIdentifier.toString() + ".Inventory", null);
	}

	public void saveHorseCustomInventory(UUID horseIdentifier, Inventory inventory) {
		if (inventory.getContents() != null) {
			for (int i = 0; i < inventory.getStorageContents().length; i++) {
				setHorsesConfig(horseIdentifier.toString() + ".Inventory." + String.valueOf(i), inventory.getItem(i));
			}
		}
	}

	public Inventory getHorseCustomInventory(UUID horseIdentifier) {
		int amount = 9;
		if (getHorseEntity(horseIdentifier) instanceof ChestedHorse)
			amount *= 2;

		Inventory inventory = Bukkit.createInventory(Bukkit.getPlayer(getOwnerForHorse(horseIdentifier)), amount,
				plugin.getHorseNameColorForPlayer(getOwnerForHorse(horseIdentifier))
						+ plugin.getHorseManager().getNameForHorse(horseIdentifier) + "'s Inventory");
		try {
			Map<String, Object> config = getHorsesConfig()
					.getConfigurationSection(horseIdentifier.toString() + ".Inventory").getValues(true);
			for (String index : config.keySet()) {
				inventory.setItem(Integer.parseInt(index), (ItemStack) config.get(index));
			}
			return inventory;
		} catch (Exception e) {
			return inventory;
		}
	}

	public boolean isBabyHorse(UUID horseIdentifier) {
		return getHorsesConfig().getBoolean(horseIdentifier.toString() + ".isBaby");
	}

	public void setBabyForHorse(UUID horseIdentifier, boolean baby) {
		setHorsesConfig(horseIdentifier.toString() + ".isBaby", baby);
	}

	public boolean isHorseCarryingChest(UUID horseIdentifier) {
		return getHorsesConfig().getBoolean(horseIdentifier.toString() + ".IsCarryingChest");
	}

	public String getNameForHorse(UUID horseIdentifier) {
		return getHorsesConfig().getString(horseIdentifier.toString() + ".Name");
	}

	public boolean isHorseOwned(UUID horseIdentifier) {
		return getHorsesConfig().contains(horseIdentifier.toString() + ".Owner");
	}

	public void setHorseColor(UUID horseIdentifier, Horse.Color color) {
		setHorsesConfig(horseIdentifier.toString() + ".Color", color.name());
	}

	public Horse.Color getHorseColor(UUID horseIdentifier) {
		return Horse.Color.valueOf(getHorsesConfig().getString(horseIdentifier.toString() + ".Color"));
	}

	public void setOwnerForHorse(UUID horseIdentifier, UUID ownerId) {
		if (ownerId == null) {
			setHorsesConfig(horseIdentifier.toString(), null);
			AbstractHorse horse = (AbstractHorse) getHorseEntity(horseIdentifier);

			if (horse != null) {
				if (horse instanceof ChestedHorse) {
					ChestedHorse chestedHorse = (ChestedHorse) getHorseEntity(horseIdentifier);
					chestedHorse.setCarryingChest(false);
				}
				horse.setCustomName(null);
				horse.setCustomNameVisible(false);
				horse.setOwner(null);
				horse.setTamed(false);
			}
		} else {
			AbstractHorse horse = (AbstractHorse) getHorseEntity(horseIdentifier);
			if (horse != null) {
				OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(ownerId);

				horse.setOwner(player);
				horse.setTamed(true);
			}

			setHorsesConfig(horseIdentifier.toString() + ".Owner", ownerId.toString());
			setHorsesConfig(horseIdentifier.toString() + ".IsCarryingChest", false);
		}
	}

	public AbstractHorse restoreHorseEntity(UUID oldHorseUUID, Location spawnLocation) {
		AbstractHorse restoredHorse = (AbstractHorse) spawnLocation.getWorld().spawnEntity(spawnLocation,
				getHorseType(oldHorseUUID));

		restoredHorse.setTamed(true);

		UUID restoredHorseUUID = restoredHorse.getUniqueId();

		setOwnerForHorse(restoredHorseUUID, getOwnerForHorse(oldHorseUUID));

		setHorseType(restoredHorseUUID, getHorseType(oldHorseUUID));

		if (isBabyHorse(oldHorseUUID))
			restoredHorse.setBaby();
		setBabyForHorse(restoredHorseUUID, isBabyHorse(oldHorseUUID));

		if (getHorseHasSaddle(oldHorseUUID))
			restoredHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		setHorseHasSaddle(restoredHorseUUID, getHorseHasSaddle(oldHorseUUID));

		this.plugin.getHorseManager().setNameForHorse(restoredHorseUUID, getNameForHorse(oldHorseUUID));

		if (getHorseType(oldHorseUUID).equals(EntityType.HORSE)) {
			Horse horse = (Horse) restoredHorse;
			horse.setStyle(getHorseStyle(oldHorseUUID));
			horse.setColor(getHorseColor(oldHorseUUID));
			horse.getInventory().setArmor(getHorseArmor(oldHorseUUID));
			setHorseStyle(restoredHorseUUID, getHorseStyle(oldHorseUUID));
			setHorseColor(restoredHorseUUID, getHorseColor(oldHorseUUID));
			setHorseArmor(restoredHorseUUID, getHorseArmor(oldHorseUUID));
		}

		setHorseJumpStrength(restoredHorseUUID, getHorseJumpStrength(oldHorseUUID));
		setHorseMaxHealth(restoredHorseUUID, getHorseMaxHealth(oldHorseUUID));

		restoredHorse.setCustomName(
				this.plugin.getHorseNameColorForPlayer(getOwnerForHorse(oldHorseUUID)) + getNameForHorse(oldHorseUUID));
		restoredHorse.setCustomNameVisible(this.plugin.alwaysShowHorseName);
		if (getHorsePrice(oldHorseUUID) != 0) {
			setHorseForSale(restoredHorseUUID, getHorsePrice(oldHorseUUID));
			restoredHorse.setCustomName(ChatColor.GOLD + getNameForHorse(oldHorseUUID) + ChatColor.RED + " "
					+ this.plugin.getEconomy().format(getHorsePrice(oldHorseUUID)));
		}
		if (isHorseCarryingChest(oldHorseUUID)) {
			setCarryingChest(restoredHorseUUID, isHorseCarryingChest(oldHorseUUID));
			saveHorseCustomInventory(restoredHorseUUID, getHorseCustomInventory(oldHorseUUID));
		}

		setOwnerForHorse(oldHorseUUID, null);
		return restoredHorse;
	}

	public void setHorseStyle(UUID horseIdentifier, Horse.Style style) {
		setHorsesConfig(horseIdentifier.toString() + ".Style", style.name());
	}

	public Horse.Style getHorseStyle(UUID horseIdentifier) {
		return Horse.Style.valueOf(getHorsesConfig().getString(horseIdentifier.toString() + ".Style"));
	}

	public void setHorseType(UUID horseIdentifier, EntityType type) {
		setHorsesConfig(horseIdentifier.toString() + ".Type", type.name());
	}

	public EntityType getHorseType(UUID horseIdentifier) {
		
		return EntityType.valueOf(getHorsesConfig().getString(horseIdentifier.toString() + ".Type"));
	}

	public void setHorseJumpStrength(UUID horseIdentifier, double value) {
		setHorsesConfig(horseIdentifier.toString() + ".JumpStrength", value);
	}

	public double getHorseJumpStrength(UUID horseIdentifier) {
		return (double) getHorsesConfig().get(horseIdentifier.toString() + ".JumpStrength");
	}

	public void setHorseMaxHealth(UUID horseIdentifier, double value) {
		setHorsesConfig(horseIdentifier.toString() + ".MaxHealth", value);
	}

	public double getHorseMaxHealth(UUID horseIdentifier) {
		return (double) getHorsesConfig().get(horseIdentifier.toString() + ".MaxHealth");
	}

	public List<UUID> getAllHorses() {
		List<UUID> horseList = new ArrayList<>();
		for (String horseIdentifierString : getHorsesConfig().getKeys(false)) {
			UUID horseIdentifier = null;
			try {
				horseIdentifier = UUID.fromString(horseIdentifierString);
			} catch (Exception ex) {
				continue;
			}
			horseList.add(horseIdentifier);
		}
		return horseList;
	}

	public List<UUID> getHorsesForOwner(UUID ownerId) {
		List<UUID> horseList = new ArrayList<UUID>();
		for (String horseIdentifierString : getHorsesConfig().getKeys(false)) {
			String horseOwnerString = getHorsesConfig().getString(horseIdentifierString + ".Owner");
			UUID horseOwnerId = null;

			try {
				horseOwnerId = UUID.fromString(horseOwnerString);
			} catch (Exception ex) {
				continue;
			}

			if (horseOwnerId.equals(ownerId)) {
				UUID horseIdentifier = null;

				try {
					horseIdentifier = UUID.fromString(horseIdentifierString);
				} catch (Exception ex) {
					continue;
				}

				horseList.add(horseIdentifier);
			}
		}
		return horseList;
	}

	public void setHorseLastSelectionPosition(UUID horseIdentifier, Location location) {
		setHorsesConfig(horseIdentifier + ".LastSelection.X", location.getBlockX());
		setHorsesConfig(horseIdentifier + ".LastSelection.Y", location.getBlockY());
		setHorsesConfig(horseIdentifier + ".LastSelection.Z", location.getBlockZ());
		setHorsesConfig(horseIdentifier + ".LastSelection.World", location.getWorld().getName());
	}

	public Location getHorseLastSelectionPosition(UUID horseIdentifier) {
		Location location = null;
		String worldName = this.horsesConfig.getString(horseIdentifier + ".LastSelection.World");
		double x = getHorsesConfig().getInt(horseIdentifier + ".LastSelection.X");
		double y = getHorsesConfig().getInt(horseIdentifier + ".LastSelection.Y");
		double z = getHorsesConfig().getInt(horseIdentifier + ".LastSelection.Z");
		try {
			World world = this.plugin.getServer().getWorld(worldName);

			location = new Location(world, x, y, z);
		} catch (Exception ex) {
			this.plugin.log(ex.getMessage());
			return null;
		}
		return location;
	}

	public void setHorseForSale(UUID horseIdentifier, int sellingPrice) {
		if (sellingPrice <= 0) {
			setHorsesConfig(horseIdentifier + ".Price", null);
		} else {
			setHorsesConfig(horseIdentifier + ".Price", sellingPrice);
		}
	}

	public int getHorsePrice(UUID horseIdentifier) {
		return getHorsesConfig().getInt(horseIdentifier + ".Price");
	}

	public UUID newHorse(String playerName, LivingEntity entity) {
		AbstractHorse horse = (AbstractHorse) entity;

		setHorsesConfig(horse.getUniqueId() + ".Name", "Horsy");

		if (horse instanceof Horse) {
			Horse horse1 = (Horse) entity;
			setHorsesConfig(horse.getUniqueId() + ".Color", horse1.getColor().name());
			setHorsesConfig(horse.getUniqueId() + ".Style", horse1.getStyle().name());
		}
		setHorsesConfig(horse.getUniqueId() + ".MaxHealth",
				horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		setHorsesConfig(horse.getUniqueId() + ".JumpStrength", horse.getJumpStrength());

		return horse.getUniqueId();
	}

	public List<UUID> getHorseFriends(UUID horseIdentifier) {
		List<String> friends = getHorsesConfig().getStringList(horseIdentifier + ".Friends");
		List<UUID> friendList = new ArrayList<UUID>();

		if (friends == null) {
			return null;
		}

		UUID friendId = null;

		for (String friend : friends) {
			try {
				friendId = UUID.fromString(friend);
			} catch (Exception ex) {
				continue;
			}

			friendList.add(friendId);
		}

		return friendList;
	}

	public void addHorseFriend(UUID horseIdentifier, UUID playerId) {
		List<String> friendList = getHorsesConfig().getStringList(horseIdentifier + ".Friends");
		UUID friendId = null;

		for (String friendString : friendList) {
			try {
				friendId = UUID.fromString(friendString);
			} catch (Exception ex) {
				continue;
			}

			if (friendId.equals(playerId)) {
				return;
			}
		}

		friendList.add(playerId.toString());

		setHorsesConfig(horseIdentifier + ".Friends", friendList);
	}

	public void removeHorseFriend(UUID horseIdentifier, UUID playerId) {
		List<String> friendList = getHorsesConfig().getStringList(horseIdentifier + ".Friends");
		UUID friendId = null;

		for (String friendString : friendList) {
			try {
				friendId = UUID.fromString(friendString);
			} catch (Exception ex) {
				continue;
			}

			if (friendId.equals(playerId)) {
				friendList.remove(playerId.toString());
				setHorsesConfig(horseIdentifier + ".Friends", friendList);
				return;
			}
		}
	}

	public void clearHorseFriends(UUID horseIdentifier) {
		setHorsesConfig(horseIdentifier + ".Friends", null);
	}

	public boolean isHorseFriend(UUID horseIdentifier, UUID playerId) {
		List<String> friendList = getHorsesConfig().getStringList(horseIdentifier + ".Friends");
		UUID friendId = null;

		for (String friendString : friendList) {
			try {
				friendId = UUID.fromString(friendString);
			} catch (Exception ex) {
				continue;
			}

			if (friendId.equals(playerId)) {
				return true;
			}
		}

		return false;
	}
}
