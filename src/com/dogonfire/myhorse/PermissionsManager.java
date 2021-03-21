package com.dogonfire.myhorse;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionsManager
{
	
	private String		pluginName		= "null";
	private MyHorse		plugin;
	private Permission	vaultPermission	= null;
	private Chat		vaultChat		= null;

	public PermissionsManager(MyHorse plugin)
	{
		this.plugin = plugin;

		if (plugin.vaultEnabled)
		{
			vaultPermission = plugin.getPermissions();
			vaultChat = plugin.getChat();
		}
	}

	public void load()
	{
		// Nothing
	}

	public Plugin getPlugin()
	{
		return this.plugin;
	}

	public String getPermissionPluginName()
	{
		return this.pluginName;
	}

	public boolean hasPermission(Player player, String node)
	{
		if (this.plugin.vaultEnabled)
		{
			return vaultPermission.has(player, node);
		}
		else
		{
			return player.hasPermission(node);
		}
	}

	public boolean isGroup(String groupName)
	{
		if (this.plugin.vaultEnabled)
		{
			for (String group : vaultPermission.getGroups())
			{
				if (group.contains(groupName))
					return true;
			}
		}
		return false;
	}
	
	public String[] getGroups()
	{
		if (this.plugin.vaultEnabled)
		{
			return vaultPermission.getGroups();
		}
		return null;
	}
	
	public String getGroup(String playerName)
	{
		if (this.plugin.vaultEnabled)
		{			
			for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) 
			{
				if(offlinePlayer.getName().equals(playerName)) 
				{
					return vaultPermission.getPrimaryGroup(null, offlinePlayer);
				}
			}
		}
		return "";
	}

	public String getPrefix(String playerName)
	{
		if (this.plugin.vaultEnabled)
		{
			Player player = plugin.getServer().getPlayer(playerName);
			return vaultChat.getPlayerPrefix(player);
		}
		return "";
	}

	public void setGroup(String playerName, String groupName)
	{
		if (this.plugin.vaultEnabled)
		{
			Player player = plugin.getServer().getPlayer(playerName);
			vaultPermission.playerAddGroup(player, groupName);
		}
	}
}
