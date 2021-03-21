package com.dogonfire.myhorse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UpdateNotifier implements Runnable
{
	final MyHorse	plugin;
	Player			player;

	public UpdateNotifier(MyHorse plugin, Player player)
	{
		this.plugin = plugin;
		this.player = player;
	}

	public void run()
	{
		if (this.player.isOnline())
		{
			UpdateChecker updateChecker = new UpdateChecker();

			String latestVersionName = updateChecker.getLatestVersionName();
			if (latestVersionName == null)
			{
				this.plugin.log("Could not get latest version name!");
				return;
			}

			int thisVersionNumber;

			try
			{
				thisVersionNumber = Integer.parseInt(this.plugin.getDescription().getVersion().replace(".", ""));
			}
			catch (NumberFormatException e)
			{
				this.plugin.log("Could not parse this plugin version number (from " + this.plugin.getDescription().getVersion() + ")");
				return;
			}

			int bukkitVersionNumber;

			try
			{
				bukkitVersionNumber = Integer.parseInt(latestVersionName.substring(8, 12).replace(".", ""));
			}
			catch (NumberFormatException e)
			{
				this.plugin.log("Could not parse latest version number (from " + latestVersionName + ")");
				return;
			}

			try
			{
				if (thisVersionNumber < bukkitVersionNumber)
				{
					this.player.sendMessage(ChatColor.AQUA + "There is a new update for MyHorse available: " + ChatColor.GOLD + latestVersionName + ChatColor.AQUA + " for " + ChatColor.GOLD
							+ updateChecker.getLatestVersionGameVersion());
					this.player.sendMessage(ChatColor.AQUA + "Download it at " + ChatColor.GOLD + updateChecker.getLatestVersionLink());
				}
			}
			catch (NumberFormatException e)
			{
				this.plugin.log("Could not compare version numbers!");
			}
		}
	}
}
