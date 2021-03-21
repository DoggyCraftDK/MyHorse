package com.dogonfire.myhorse.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HorseGrownUpEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private UUID horseIdentifier;
	
	public HorseGrownUpEvent(UUID horseIdentifier) 
	{
		this.horseIdentifier = horseIdentifier;
	}
	
	@Override
	public boolean isCancelled() 
	{
		
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) 
	{
		this.isCancelled = isCancelled;
	}

	@Override
	public HandlerList getHandlers() 
	{
	    return handlers;
	}

	public static HandlerList getHandlerList() 
	{
	    return handlers;
	}
	
	public UUID getHorseId() 
	{
		return this.horseIdentifier;
	}
}
