package com.dogonfire.myhorse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

public class LanguageManager {
	private MyHorse plugin;
	private FileConfiguration languageConfig = null;
	private Random random = new Random();
	private String amount;
	private String name;
	private String playerName;
	private String authorName;
	private String languageName;

	private void downloadLanguageFile(String fileName) throws IOException {
		BufferedInputStream in = new BufferedInputStream(
				new URL("https://raw.githubusercontent.com/DogOnFire/MyHorse/master/lang/" + fileName).openStream());

		FileOutputStream fos = new FileOutputStream(this.plugin.getDataFolder() + "/lang/" + fileName);

		BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

		byte[] data = new byte[1024];

		int x = 0;
		while ((x = in.read(data, 0, 1024)) >= 0) {
			bout.write(data, 0, x);
		}
		bout.close();

		in.close();
	}

	private boolean loadLanguageFile(String fileName) {
		File languageConfigFile = new File(this.plugin.getDataFolder() + "/lang/" + fileName);
		if (!languageConfigFile.exists()) {
			return false;
		}
		try {
			this.languageConfig = new YamlConfiguration();
			this.languageConfig.loadFromString(Files.toString(languageConfigFile, Charset.forName("UTF-8")));
		} catch (Exception e) {
			this.plugin.log("Could not load data from " + languageConfigFile + ": " + e.getMessage());
		}
		this.languageName = this.languageConfig.getString("Version.Name");
		this.authorName = this.languageConfig.getString("Version.Author");

		this.plugin.logDebug("Loaded " + this.languageConfig.getString("Version.Name") + " by "
				+ this.languageConfig.getString("Version.Author") + " version "
				+ this.languageConfig.getString("Version.Version"));

		return true;
	}

	public void load() {
		File directory = new File(this.plugin.getDataFolder() + "/lang");
		if (!directory.exists()) {
			System.out.println("Creating language file directory '/lang'...");

			boolean result = directory.mkdir();
			if (result) {
				this.plugin.logDebug("Language directory created");
			} else {
				this.plugin.logDebug("Could not create language directory!");
				return;
			}
		}
		String languageFileName = this.plugin.language + ".yml";
		if (!loadLanguageFile(languageFileName)) {
			this.plugin.log("Could not load " + languageFileName + " from the /lang folder.");
			if (this.plugin.downloadLanguageFile) {
				this.plugin.log("Downloading " + languageFileName + " from DogOnFire...");
				try {
					downloadLanguageFile(languageFileName);
				} catch (Exception ex) {
					this.plugin.log("Could not download " + languageFileName + " language file from DogOnFire: "
							+ ex.getMessage());
					return;
				}
				if (!loadLanguageFile(languageFileName)) {
					this.plugin.log("Could not load " + languageFileName + "!");
				}
			} else {
				this.plugin.log(
						"Will NOT download from DogOnFire. Please place a valid language file in your /lang folder!");
			}
		}
	}

	public String getLanguageString(LANGUAGESTRING type, ChatColor color) {
		List<String> strings = this.languageConfig.getStringList(type.name());
		if (strings.size() == 0) {
			this.plugin.log("No language strings found for " + type.name() + "!");
			return type.name() + " MISSING";
		}
		String text = (String) strings.toArray()[this.random.nextInt(strings.size())];

		return parseString(text, color);
	}

	LanguageManager(MyHorse p) {
		this.plugin = p;
	}

	public String getAuthor() {
		return this.authorName;
	}

	public String getLanguageName() {
		return this.languageName;
	}

	public String parseString(String id, ChatColor color) {
		String string = color + id.replaceAll("&([0-9a-f])", "§$1");

		if (string.contains("$ServerName")) {
			string = string.replace("$ServerName", ChatColor.GOLD + this.plugin.serverName + color);
		}
		if (string.contains("$Name")) {
			string = string.replace("$Name", ChatColor.GOLD + this.name + color);
		}
		if (string.contains("$PlayerName")) {
			string = string.replace("$PlayerName", ChatColor.GOLD + this.playerName + color);
		}
		if (string.contains("$Amount")) {
			string = string.replace("$Amount", ChatColor.GOLD + this.amount + color);
		}
		return string;
	}

	public String parseStringForBook(String id) {
		String string = id;
		if (string.contains("$ServerName")) {
			string = string.replace("$ServerName", this.plugin.serverName);
		}
		if (string.contains("$Name")) {
			string = string.replace("$Name", this.name);
		}
		if (string.contains("$Amount")) {
			string = string.replace("$Amount", this.amount);
		}
		if (string.contains("$PlayerName")) {
			string = string.replace("PlayerName", this.playerName);
		}
		return string;
	}

	public String getPlayerName() {
		return this.playerName;
	}

	public void setPlayerName(String name) {
		if (this.playerName == null) {
			this.plugin.logDebug("WARNING: Setting null playerName");
		}
		this.playerName = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (name == null) {
			this.plugin.logDebug("WARNING: Setting null name");
		}
		this.name = name;
	}

	public String getAmount() {
		return this.amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public static enum LANGUAGESTRING {
		ComeHere, HorsesWasPurged, YouPutAChestOnHorse, HorseHasNoChest, ChestHasBeenRemoved, HasNeverPlayedOnTheServer,
		ChestHasBeenRemovedFailure, NotAHorse, YourHorseHasGrownUp, BabyTimeBeforeAdult, NotRidingAHorse,
		NoPriceOnHorse, AlreadyOwnThatHorse, ChestedHorseItemsMoved, NoHorseWithSuchName, NoHorseWithSuchID,
		AlreadyHasHorseWithThatName, YouCancelledHorseForSale, CouldNotTeleportYourHorse, YouCannotClaimThisHorse,
		YouDoNotHaveEnoughMoneyToBuyHorse, YouMountedOwnedHorse, PlayerCannotHaveMoreHorses, YouCannotHaveMoreHorses,
		PutChestOnHorseInfo, NotHorseFriend, CannotUseNameTags, CommandDisabled, AlreadyHorseFriend,
		YouAddedFriendToHorse, YouRemovedFriendToHorse, InvalidHorseName, InfoCancelHorseSale, PlayerBoughtYourHorse,
		AreYouSureYouWantToBuyHorse, DoYouWishToClaim, YouClaimedAHorse, CannotOpenLockedHorse, YourHorseIsFarAway,
		YouSetHorseForSale, CannotLeashLockedHorse, UseGotoCommand, YourOwnedHorsesList, YouAreNewOwnerOfHorse,
		YouSetOwnerForHorse, ThatPlayerIsNotOnline, YouKilledHorse, YouSetYourHorseFree, AreYouSureBuyHorse,
		YouBoughtHorse, YouOlsHorse, YouSelectedHorse, CannotUseLockedHorse, HorseLocked, HorseUnlocked, YouTamedAHorse,
		UseCommandToNameYourHorse, UseCommandToUnlockYourHorse, UseCommandToSellYourHorse, YourHorseDied,
		NoHorseSelected, SpawnedHorse, SetHorseName, NoMoreHorsesAllowed, NoPermissionForCommand, InvalidCommand;
	}
}
