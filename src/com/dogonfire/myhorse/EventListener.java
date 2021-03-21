package com.dogonfire.myhorse;

import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.dogonfire.myhorse.LanguageManager.LANGUAGESTRING;
import com.dogonfire.myhorse.events.HorseGrownUpEvent;

public class EventListener implements Listener {
	private MyHorse plugin;

	EventListener(MyHorse p) {
		this.plugin = p;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		this.plugin.getOwnerManager().setLastLogin(event.getPlayer().getUniqueId(), new Date());
		if ((this.plugin.useUpdateNotifications) && ((event.getPlayer().isOp())
				|| (this.plugin.getPermissionsManager().hasPermission(event.getPlayer(), "myhorse.updates")))) {
			this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin,
					new UpdateNotifier(this.plugin, event.getPlayer()));
		}
	}

	public void onEntityPortalEvent(EntityPortalEvent event) {
		if (!(event.getEntity() instanceof AbstractHorse)
				|| ((event.getEntity() instanceof Llama) || (event.getEntity() instanceof TraderLlama))) {
			return;
		}
		UUID horseIdentifier = event.getEntity().getUniqueId();
		if (!this.plugin.getHorseManager().isHorseOwned(horseIdentifier)) {
			return;
		}
		if (!this.plugin.isAllowedInWorld(event.getEntity().getWorld())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Entity entity : event.getChunk().getEntities()) {
			if ((entity instanceof AbstractHorse) && !((entity instanceof Llama) || (entity instanceof TraderLlama))) {
				AbstractHorse abstractHorse = (AbstractHorse) entity;
				if (this.plugin.getHorseManager().isHorseOwned(entity.getUniqueId())) {
					if (plugin.getHorseManager().getHorsePrice(abstractHorse.getUniqueId()) == 0) {
						abstractHorse.setCustomName(this.plugin.getHorseNameColorForPlayer(
								plugin.getHorseManager().getOwnerForHorse(abstractHorse.getUniqueId()))
								+ plugin.getHorseManager().getNameForHorse(abstractHorse.getUniqueId()));
					} else {
						abstractHorse.setCustomName(ChatColor.GOLD
								+ this.plugin.getHorseManager().getNameForHorse(abstractHorse.getUniqueId())
								+ ChatColor.RED + " " + this.plugin.getEconomy().format(
										this.plugin.getHorseManager().getHorsePrice(abstractHorse.getUniqueId())));
					}

					if (!this.plugin.getHorseManager().isBabyHorse(entity.getUniqueId())) {
						abstractHorse.setAdult();
					}
					abstractHorse.setOwner(Bukkit
							.getOfflinePlayer(plugin.getHorseManager().getOwnerForHorse(abstractHorse.getUniqueId())));
					abstractHorse.setCustomNameVisible(this.plugin.alwaysShowHorseName);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (!this.plugin.useHorseTeleportation) {
			return;
		}
		for (Entity entity : event.getChunk().getEntities()) {
			if (entity instanceof AbstractHorse || !((entity instanceof Llama) || (entity instanceof TraderLlama))) {
				UUID horseIdentifier = entity.getUniqueId();
				if (this.plugin.getHorseManager().isHorseOwned(horseIdentifier)) {
					this.plugin.logDebug(
							"Saved horse onChunkUnload | " + plugin.getHorseManager().getNameForHorse(horseIdentifier));

					this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier, entity.getLocation());

				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (event.getPlayer().getVehicle() == null) {
			return;
		}
		Vehicle vehicle = (Vehicle) event.getPlayer().getVehicle();
		if (!(vehicle instanceof AbstractHorse) || ((vehicle instanceof Llama) || (vehicle instanceof TraderLlama))) {
			return;
		}
		if (!this.plugin.isAllowedInWorld(vehicle.getWorld())) {
			return;
		}
		UUID horseIdentifier = vehicle.getUniqueId();

		vehicle.eject();

		this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier, vehicle.getLocation());
	}
	
	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		if (!(event.getExited() instanceof Player)) {
			return;
		}
		if (!(event.getVehicle() instanceof AbstractHorse)
				|| ((event.getVehicle() instanceof Llama) || (event.getVehicle() instanceof TraderLlama))) {
			return;
		}
		if (!this.plugin.isAllowedInWorld(event.getExited().getWorld())) {
			return;
		}
		UUID horseIdentifier = event.getVehicle().getUniqueId();
		if (!this.plugin.getHorseManager().isHorseOwned(horseIdentifier)) {
			return;
		}
		this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier, event.getExited().getLocation());
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getEntered().getType() != EntityType.PLAYER) {
			return;
		}
		Player player = (Player) event.getEntered();
		if (!(player.getVehicle() instanceof AbstractHorse)
				|| ((player.getVehicle() instanceof Llama) || (player.getVehicle() instanceof TraderLlama))) {
			this.plugin.logDebug(event.getVehicle().getType().name());
			return;
		}
		AbstractHorse horse = (AbstractHorse) event.getVehicle();
		if (!this.plugin.isAllowedInWorld(event.getEntered().getWorld())) {
			return;
		}

		if (horse.getOwner() != null && this.plugin.getHorseManager().getOwnerForHorse(horse.getUniqueId()) == null) {
			return;
		}

		if (player.isSneaking()) {
			event.setCancelled(true);
			return;
		}

		UUID horseIdentifier = event.getVehicle().getUniqueId();

		String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);
		if (horseName == null) {
			this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, null);
			if (this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).size() == 0) {
				this.plugin.getLanguageManager().setAmount("/mh claim");
				player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.DoYouWishToClaim,
						ChatColor.AQUA));
			}
			return;
		}
		this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier, event.getEntered().getLocation());
		if (player.isOp() || this.plugin.getPermissionsManager().hasPermission(player, "myhorse.bypass.mount")
				|| this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin")) {
			this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), horseIdentifier);
			this.plugin.getLanguageManager().setName(horseName);

			player.sendMessage(ChatColor.AQUA + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YouSelectedHorse, ChatColor.GREEN));

			return;
		}

		UUID ownerId = this.plugin.getHorseManager().getOwnerForHorse(horseIdentifier);

		if (this.plugin.getHorseManager().isHorseLocked(horseIdentifier)) {
			if (!this.plugin.getHorseManager().isHorseFriend(horseIdentifier, player.getUniqueId())) {
				this.plugin.logDebug("VehicleEnter(): Not a horse friend");

				if (!ownerId.equals(player.getUniqueId())) {
					this.plugin.getLanguageManager().setName(player.getName());
					player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
							.getLanguageString(LANGUAGESTRING.CannotUseLockedHorse, ChatColor.DARK_RED));

					event.setCancelled(true);

					return;
				}
				this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), horseIdentifier);
				this.plugin.getLanguageManager().setName(horseName);
				player.sendMessage(ChatColor.AQUA + this.plugin.getLanguageManager()
						.getLanguageString(LANGUAGESTRING.YouSelectedHorse, ChatColor.GREEN));

				return;
			}
			this.plugin.getLanguageManager().setPlayerName(plugin.getServer().getOfflinePlayer(ownerId).getName());
			this.plugin.getLanguageManager().setName(horseName);
			player.sendMessage(ChatColor.AQUA + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YouMountedOwnedHorse, ChatColor.GREEN));

		}
	}

	@EventHandler
	public void onEntityTameEvent(EntityTameEvent event) {
		if (!(event.getEntity() instanceof AbstractHorse)
				|| ((event.getEntity() instanceof Llama) || (event.getEntity() instanceof TraderLlama))) {
			return;
		}
		if (!this.plugin.isAllowedInWorld(event.getEntity().getWorld())) {
			return;
		}
		if (this.plugin.getHorseManager().isHorseOwned(event.getEntity().getUniqueId())) {
			return;
		}
		Player player = this.plugin.getServer().getPlayer(event.getOwner().getName());

		int maxHorses = this.plugin.getMaximumHorsesForPlayer(player.getName());
		int numberOfHorses = this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).size();
		if ((maxHorses > 0) && (numberOfHorses >= maxHorses)) {
			this.plugin.getLanguageManager().setAmount("" + maxHorses);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YouCannotHaveMoreHorses, ChatColor.DARK_RED));
			event.setCancelled(true);
			return;
		}
		UUID horseIdentifier = this.plugin.getHorseManager().newHorse(event.getOwner().getName(), event.getEntity());
		String horseName;
		do {
			horseName = this.plugin.getHorseManager().getNewHorseName(player.getUniqueId());
		} while (this.plugin.getHorseManager().ownedHorseWithName(event.getOwner().getName(), horseName));
		this.plugin.getHorseManager().setNameForHorse(horseIdentifier, horseName);

		AbstractHorse abstractHorse = (AbstractHorse) event.getEntity();

		abstractHorse.setCustomName(this.plugin.getHorseNameColorForPlayer(event.getOwner().getUniqueId()) + horseName);
		abstractHorse.setCustomNameVisible(this.plugin.alwaysShowHorseName);

		this.plugin.getHorseManager().setLockedForHorse(horseIdentifier, true);
		this.plugin.getHorseManager().setBabyForHorse(horseIdentifier, false);
		this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, event.getOwner().getUniqueId());
		this.plugin.getHorseManager().setHorseHasSaddle(horseIdentifier,
				(abstractHorse.getInventory().getSaddle() != null) ? true : false);
		if (abstractHorse instanceof Horse) {
			Horse horse = (Horse) event.getEntity();
			this.plugin.getHorseManager().setHorseStyle(horseIdentifier, horse.getStyle());
			this.plugin.getHorseManager().setHorseColor(horseIdentifier, horse.getColor());
			this.plugin.getHorseManager().setHorseArmor(horseIdentifier, horse.getInventory().getArmor());
		}
		this.plugin.getHorseManager().setHorseType(horseIdentifier, abstractHorse.getType());
		this.plugin.getHorseManager().setHorseJumpStrength(horseIdentifier, abstractHorse.getJumpStrength());
		this.plugin.getHorseManager().setHorseMaxHealth(horseIdentifier,
				abstractHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		if (player != null) {
			this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(), horseIdentifier);

			player.sendMessage(
					this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YouTamedAHorse, ChatColor.GREEN));
			if(this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).size() > 1) {
				return;
			}
			this.plugin.getLanguageManager().setAmount("/myhorse name <horsename>");
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.UseCommandToNameYourHorse, ChatColor.AQUA));

			this.plugin.getLanguageManager().setAmount("/myhorse unlock");
			player.sendMessage(this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.UseCommandToUnlockYourHorse, ChatColor.AQUA));

			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.PutChestOnHorseInfo,
					ChatColor.AQUA));

			if (plugin.economyEnabled
					&& (player.isOp() || plugin.getPermissionsManager().hasPermission(player, "myhorse.sell"))) {
				this.plugin.getLanguageManager().setAmount("/myhorse sell <price>");
				player.sendMessage(this.plugin.getLanguageManager()
						.getLanguageString(LANGUAGESTRING.UseCommandToSellYourHorse, ChatColor.AQUA));
			}
		}
		this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier, event.getEntity().getLocation());
	}

	@EventHandler
	public void onBreedEvent(EntityBreedEvent event) {
		if (!(event.getMother() instanceof AbstractHorse)
				|| ((event.getMother() instanceof Llama) || (event.getMother() instanceof TraderLlama))) {
			return;
		}
		if (!(plugin.getHorseManager().isHorseOwned(event.getMother().getUniqueId())
				&& plugin.getHorseManager().isHorseOwned(event.getFather().getUniqueId()))) {
			return;
		}
		Player player = (Player) Bukkit
				.getOfflinePlayer(plugin.getHorseManager().getOwnerForHorse(event.getMother().getUniqueId()));
		AbstractHorse mother = (AbstractHorse) event.getMother();
		AbstractHorse father = (AbstractHorse) event.getFather();
		if (!plugin.getHorseManager().getOwnerForHorse(mother.getUniqueId())
				.equals(plugin.getHorseManager().getOwnerForHorse(father.getUniqueId()))) {
			event.setCancelled(true);
			return;
		}
		int maxHorses = this.plugin.getMaximumHorsesForPlayer(player.getName());
		int numberOfHorses = this.plugin.getHorseManager().getHorsesForOwner(player.getUniqueId()).size();
		if ((maxHorses > 0) && (numberOfHorses >= maxHorses)) {
			event.getEntity().remove();
			mother.setBreed(true);
			father.setBreed(true);
			this.plugin.getLanguageManager().setName(player.getName());
			this.plugin.getLanguageManager().setAmount("" + maxHorses);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YouCannotHaveMoreHorses, ChatColor.DARK_RED));
			return;
		}

		event.getEntity().remove();
		event.setExperience(0);
		AbstractHorse babyHorse = (AbstractHorse) event.getMother().getWorld().spawnEntity(mother.getLocation(),
				mother.getType());

		plugin.getHorseManager().setOwnerForHorse(babyHorse.getUniqueId(), player.getUniqueId());
		this.plugin.getHorseManager().setHorseHasSaddle(babyHorse.getUniqueId(), true);

		if (mother instanceof Horse) {
			Horse horse = (Horse) babyHorse;
			this.plugin.getHorseManager().setHorseColor(babyHorse.getUniqueId(), horse.getColor());
			this.plugin.getHorseManager().setHorseStyle(babyHorse.getUniqueId(), horse.getStyle());
		}

		babyHorse.setBaby();
		babyHorse.setAgeLock(true);

		String name = plugin.getHorseManager().getNewHorseName(player.getUniqueId());
		babyHorse.setCustomName(this.plugin.getHorseNameColorForPlayer(player.getUniqueId()) + name);
		plugin.getHorseManager().setNameForHorse(babyHorse.getUniqueId(), name);

		babyHorse.setCustomNameVisible(this.plugin.alwaysShowHorseName);
		babyHorse.setOwner(player);
		plugin.getHorseManager().setBabyForHorse(babyHorse.getUniqueId(), true);
		plugin.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, plugin.timer, 0, 20L * 2L);
	}

	@EventHandler
	public void onHorseInteract(PlayerInteractEntityEvent event) {
		// To make sure that the player also can ride a skeleton- and
		// zombiehorse
		if ((event.getRightClicked() instanceof AbstractHorse)
				&& !((event.getRightClicked() instanceof Llama) || (event.getRightClicked() instanceof TraderLlama))) {
			AbstractHorse entity = (AbstractHorse) event.getRightClicked();
			if (entity.isAdult())
				entity.addPassenger(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onHorseGrownUp(HorseGrownUpEvent event) {
		UUID horseId = event.getHorseId();
		AbstractHorse horse = (AbstractHorse) this.plugin.getHorseManager().getHorseEntity(horseId);
		if (horse == null)
			return;
		horse.setAgeLock(false);
		horse.setAdult();
		Player player = (Player) Bukkit.getOfflinePlayer(this.plugin.getHorseManager().getOwnerForHorse(horseId));
		this.plugin.getLanguageManager().setName(this.plugin.getHorseManager().getNameForHorse(horseId));
		player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.YourHorseHasGrownUp,
				ChatColor.GREEN));
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if (!(event.getEntity() instanceof SkeletonHorse || event.getEntity() instanceof ZombieHorse)) {
			return;
		}

	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void OnSignChange(SignChangeEvent event) {

	}

	@EventHandler
	public void onPlayerHangingBreak(HangingBreakEvent event) {
		if (event.getEntity().getType() != EntityType.LEASH_HITCH) {
			return;
		}
		for (Entity nearbyEntity : event.getEntity().getNearbyEntities(5.0D, 3.0D, 5.0D)) {
			if (!(nearbyEntity instanceof AbstractHorse)
					|| ((nearbyEntity instanceof Llama) || (nearbyEntity instanceof TraderLlama))) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() == null) {
			return;
		}

		if (event.getRightClicked().getType() == EntityType.LEASH_HITCH) {
			for (Entity nearbyEntity : event.getPlayer().getNearbyEntities(5.0D, 3.0D, 5.0D)) {
				if (!(nearbyEntity instanceof AbstractHorse)
						|| ((nearbyEntity instanceof Llama) || (nearbyEntity instanceof TraderLlama))) {
					AbstractHorse horse = (AbstractHorse) nearbyEntity;
					if (horse.isLeashed()) {
						if (this.plugin.getHorseManager().isHorseOwned(horse.getUniqueId())) {
							UUID ownerId = this.plugin.getHorseManager().getOwnerForHorse(horse.getUniqueId());
							if (!ownerId.equals(event.getPlayer().getUniqueId())) {
								if (this.plugin.getHorseManager().isHorseFriend(horse.getUniqueId(),
										event.getPlayer().getUniqueId())) {
									return;
								}

								this.plugin.getLanguageManager()
										.setName(plugin.getServer().getOfflinePlayer(ownerId).getName());
								event.getPlayer().sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
										.getLanguageString(LANGUAGESTRING.CannotOpenLockedHorse, ChatColor.DARK_RED));
								event.setCancelled(true);
							}
						}
					}
				}
			}
			return;
		}

		if (!(event.getRightClicked() instanceof AbstractHorse)
				|| ((event.getRightClicked() instanceof Llama) || (event.getRightClicked() instanceof TraderLlama))) {
			return;
		}

		AbstractHorse horse = (AbstractHorse) event.getRightClicked();

		if (!this.plugin.isAllowedInWorld(event.getPlayer().getWorld())) {
			return;
		}

		Player player = event.getPlayer();

		UUID horseIdentifier = event.getRightClicked().getUniqueId();

		UUID ownerId = this.plugin.getHorseManager().getOwnerForHorse(horseIdentifier);
		if (ownerId == null) {
			return;
		}

		// Buy & Sell
		this.plugin.getHorseManager().setHorseLastSelectionPosition(horseIdentifier,
				event.getRightClicked().getLocation());
		if (this.plugin.getEconomy() != null) {
			if (!ownerId.equals(player.getUniqueId())) {
				int price = this.plugin.getHorseManager().getHorsePrice(horseIdentifier);
				if (price > 0) {
					String horseName = this.plugin.getHorseManager().getNameForHorse(horseIdentifier);
					this.plugin.getLanguageManager().setPlayerName(player.getName());
					this.plugin.getLanguageManager().setName(horseName);
					this.plugin.getLanguageManager().setAmount(this.plugin.getEconomy().format(price));

					if (!this.plugin.getOwnerManager().isBuying(player.getUniqueId(), horseIdentifier)) {
						if (this.plugin.getEconomy().has(player, price)) {
							player.sendMessage(this.plugin.getLanguageManager()
									.getLanguageString(LANGUAGESTRING.AreYouSureYouWantToBuyHorse, ChatColor.AQUA));
							this.plugin.getOwnerManager().setBuying(player.getUniqueId(), horseIdentifier, true);
						} else {
							this.plugin.getLanguageManager().setAmount(this.plugin.getEconomy().format(price));
							this.plugin.getLanguageManager()
									.setPlayerName(this.plugin.getServer().getOfflinePlayer(ownerId).getName());
							this.plugin.getLanguageManager().setName(horseName);
							player.sendMessage(this.plugin.getLanguageManager().getLanguageString(
									LANGUAGESTRING.YouDoNotHaveEnoughMoneyToBuyHorse, ChatColor.DARK_RED));
						}
					} else {
						Player ownerPlayer = this.plugin.getServer().getPlayer(ownerId);

						horse.setCustomName(this.plugin.getHorseNameColorForPlayer(player.getUniqueId()) + horseName);
						horse.setOwner(player);

						this.plugin.getOwnerManager().setBuying(player.getUniqueId(), horseIdentifier, false);
						this.plugin.getHorseManager().setHorseForSale(horseIdentifier, 0);

						this.plugin.getHorseManager().setLockedForHorse(horseIdentifier, true);
						this.plugin.getHorseManager().setOwnerForHorse(horseIdentifier, player.getUniqueId());

						this.plugin.getHorseManager().clearHorseFriends(horseIdentifier);

						if (ownerPlayer != null) {
							this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(ownerId, null);
							ownerPlayer.sendMessage(this.plugin.getLanguageManager()
									.getLanguageString(LANGUAGESTRING.PlayerBoughtYourHorse, ChatColor.GREEN));
						}

						Player ownerName = (Player) this.plugin.getServer().getOfflinePlayer(ownerId);

						this.plugin.getLanguageManager().setAmount("" + price);
						this.plugin.getLanguageManager().setName(horseName);
						this.plugin.getLanguageManager().setPlayerName(ownerName.getName());
						player.sendMessage(this.plugin.getLanguageManager()
								.getLanguageString(LANGUAGESTRING.YouBoughtHorse, ChatColor.GREEN));

						this.plugin.getEconomy().withdrawPlayer(player, price);
						this.plugin.getEconomy().depositPlayer(ownerName, price);
					}
					event.setCancelled(true);

					return;
				}
			}
		}
		// Nametag prevention
		if (player.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)
				|| player.getInventory().getItemInOffHand().getType().equals(Material.NAME_TAG)) {
			event.setCancelled(true);
			this.plugin.getLanguageManager().setAmount("/mh name <name>");
			player.sendMessage(this.plugin.getLanguageManager().getLanguageString(LANGUAGESTRING.CannotUseNameTags,
					ChatColor.RED));
		}

		// Lead protection
		if (player.getInventory().getItemInMainHand() != null
				|| player.getInventory().getItemInOffHand().getType() != null) {
			if (player.getInventory().getItemInMainHand().getType() == Material.LEAD
					|| player.getInventory().getItemInOffHand().getType() == Material.LEAD) {
				if (this.plugin.getPermissionsManager().hasPermission(player, "myhorse.bypass.leash")) {
					return;
				}
				if (this.plugin.getHorseManager().isHorseLocked(horseIdentifier)) {
					if (!ownerId.equals(player.getUniqueId())
							&& !this.plugin.getHorseManager().isHorseFriend(horseIdentifier, player.getUniqueId())) {
						String ownerName = this.plugin.getServer().getOfflinePlayer(ownerId).getName();
						this.plugin.getLanguageManager().setName(ownerName);
						player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
								.getLanguageString(LANGUAGESTRING.CannotLeashLockedHorse, ChatColor.DARK_RED));

						event.setCancelled(true);

						return;
					}
				}
			}
		}

		if (player.getInventory().getItemInMainHand().getType().equals(Material.SADDLE)) {
			this.plugin.getHorseManager().setHorseHasSaddle(horseIdentifier, true);
		}

		// Inventory protection
		if (player.isSneaking()) {
			if (!ownerId.equals(player.getUniqueId())) {
				if (this.plugin.getHorseManager().isHorseLocked(horseIdentifier)) {
					if (!this.plugin.getPermissionsManager().hasPermission(player, "myhorse.bypass.chest")) {
						String ownerName = this.plugin.getServer().getOfflinePlayer(ownerId).getName();
						this.plugin.getLanguageManager().setName(ownerName);
						player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
								.getLanguageString(LANGUAGESTRING.CannotOpenLockedHorse, ChatColor.DARK_RED));

						event.setCancelled(true);

						return;
					}
				}
			}
			if (!this.plugin.getHorseManager().isBabyHorse(horseIdentifier)) {
				if (this.plugin.allowChestOnAllHorses || (player.isOp()
						|| this.plugin.getPermissionsManager().hasPermission(player, "myhorse.admin"))) {
					if (!this.plugin.getHorseManager().isHorseCarryingChest(horseIdentifier)) {
						if ((player.getInventory().getItemInMainHand() != null)) {
							if (player.getInventory().getItemInMainHand().getType() == Material.CHEST) {
								event.setCancelled(true);
								this.plugin.getHorseManager().setCarryingChest(horseIdentifier, true);
								this.plugin.getLanguageManager()
										.setName(this.plugin.getHorseManager().getNameForHorse(horseIdentifier));
								player.getInventory().getItemInMainHand()
										.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
								player.sendMessage(this.plugin.getLanguageManager()
										.getLanguageString(LANGUAGESTRING.YouPutAChestOnHorse, ChatColor.GREEN));

								if (horse instanceof ChestedHorse) {
									ChestedHorse chestedHorse = (ChestedHorse) horse;
									chestedHorse.setCarryingChest(false);
								}
								return;
							}
						}
					} else {
						event.setCancelled(true);
						this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(player.getUniqueId(),
								horseIdentifier);
						player.openInventory(plugin.getHorseManager().getHorseCustomInventory(horseIdentifier));
						return;
					}
				}
			}
			return;
		}
	}

	@EventHandler
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof AbstractHorse) {
			AbstractHorse abstractHorse = (AbstractHorse) event.getInventory().getHolder();
			UUID horseIdentifier = abstractHorse.getUniqueId();
			if (abstractHorse.getInventory().getStorageContents()[0] != null) {
				this.plugin.getHorseManager().setHorseHasSaddle(horseIdentifier, true);
			} else {
				this.plugin.getHorseManager().setHorseHasSaddle(horseIdentifier, false);
			}
			if (event.getInventory().getHolder() instanceof Horse) {
				if (abstractHorse.getInventory().getStorageContents()[1] != null) {
					this.plugin.getHorseManager().setHorseArmor(horseIdentifier,
							abstractHorse.getInventory().getStorageContents()[1]);
				} else {
					this.plugin.getHorseManager().setHorseArmor(horseIdentifier, null);
				}
			}
		}
		if (event.getInventory().getHolder() instanceof Player) {
			Player player = (Player) event.getInventory().getHolder();

			if (plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId()) == null) {
				return;
			}
			if (event.getInventory() == null) {
				return;
			}
			// TODO - Check if working properly
			this.plugin.getHorseManager().saveHorseCustomInventory(
					plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(player.getUniqueId()),
					event.getInventory());
		}
	}

	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof AbstractHorse)
				|| ((event.getEntity() instanceof Llama) || (event.getEntity() instanceof TraderLlama))) {
			return;
		}
		if (!this.plugin.isAllowedInWorld(event.getEntity().getWorld())) {
			return;
		}
		if (!this.plugin.getHorseManager().isHorseOwned(event.getEntity().getUniqueId())) {
			return;
		}
		if (this.plugin.horseDamageDisabled) {
			if (this.plugin.isDamageProtection(event.getCause())) {
				event.setCancelled(true);
			}
		}
	}

	// TODO - Check if this works
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof AbstractHorse)
				|| ((event.getEntity() instanceof Llama) || (event.getEntity() instanceof TraderLlama))) {
			return;
		}
		if (!this.plugin.isAllowedInWorld(event.getEntity().getWorld())) {
			return;
		}
		if (!this.plugin.getHorseManager().isHorseOwned(event.getEntity().getUniqueId())) {
			return;
		}

		UUID ownerId = this.plugin.getHorseManager().getOwnerForHorse(event.getEntity().getUniqueId());
		if (ownerId == null) {
			return;
		}

		String horseName = this.plugin.getHorseManager().getNameForHorse(event.getEntity().getUniqueId());

		Player player = this.plugin.getServer().getPlayer(ownerId);
		if (player != null) {
			this.plugin.getLanguageManager().setAmount(event.getEntity().getLastDamageCause().getCause().name());
			this.plugin.getLanguageManager().setName(horseName);
			player.sendMessage(ChatColor.RED + this.plugin.getLanguageManager()
					.getLanguageString(LANGUAGESTRING.YourHorseDied, ChatColor.DARK_RED));
		}
		if (this.plugin.getHorseManager().isHorseCarryingChest(event.getEntity().getUniqueId()))
			this.plugin.getHorseManager().removeCustomInventoryFromHorse(event.getEntity().getUniqueId(),
					event.getEntity().getLocation());
		if (this.plugin.getOwnerManager().getCurrentHorseIdentifierForPlayer(ownerId)
				.equals(event.getEntity().getUniqueId()))
			this.plugin.getOwnerManager().setCurrentHorseIdentifierForPlayer(ownerId, null);
		this.plugin.getHorseManager().setOwnerForHorse(event.getEntity().getUniqueId(), null);
	}
}
