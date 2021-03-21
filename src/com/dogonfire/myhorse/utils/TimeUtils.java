package com.dogonfire.myhorse.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils 
{
	// TODO - Fine for now, but can be made more clean
	public static String parseMilisToUFString(long milis)
	{
		long total  	= milis / 1000;
        long seconds   	= total % 60;
        long minutes  	= total % 3600 / 60; 
        long hours  	= total / 3600 % 24;
		if(TimeUnit.MILLISECONDS.toDays(milis) > 0) 
		{
			if(hours == 0)
			{
				return String.format("%s %s", 
					TimeUnit.MILLISECONDS.toDays(milis), 
					(TimeUnit.MILLISECONDS.toDays(milis) == 1) ? "minute" : "minutes");
			}
			return String.format("%s %s and %s %s", 
					TimeUnit.MILLISECONDS.toDays(milis),
					(TimeUnit.MILLISECONDS.toDays(milis) == 1) ? "day" : "days",
					hours, 
					(hours == 1) ? "hour" : "hours");
		}
		else if(TimeUnit.MILLISECONDS.toHours(milis) > 0) 
		{
			if(minutes == 0)
			{
				return String.format("%s %s",
						TimeUnit.MILLISECONDS.toHours(milis),
						(TimeUnit.MILLISECONDS.toHours(milis) == 1) ? "hour" : "hours");
					
			}
			return String.format("%s %s and %s %s", 
					TimeUnit.MILLISECONDS.toHours(milis),
					(TimeUnit.MILLISECONDS.toHours(milis) == 1) ? "hour" : "hours",
					minutes,
					(minutes == 1) ? "minute" : "minutes");
		}
		else if(TimeUnit.MILLISECONDS.toMinutes(milis) > 0) 
		{
			if(seconds == 0)
			{
				return String.format("%s %s", 
					TimeUnit.MILLISECONDS.toMinutes(milis), 
					(TimeUnit.MILLISECONDS.toMinutes(milis) == 1) ? "minute" : "minutes");
			}
			return String.format("%s %s and %s %s", 
					TimeUnit.MILLISECONDS.toMinutes(milis), 
					(TimeUnit.MILLISECONDS.toMinutes(milis) == 1) ? "minute" : "minutes",
					seconds,
					(seconds == 1) ? "second" : "seconds");
		}
		else if(TimeUnit.MILLISECONDS.toSeconds(milis) > 0) 
		{
			return String.format("%s %s", 
					TimeUnit.MILLISECONDS.toSeconds(milis),
					(TimeUnit.MILLISECONDS.toSeconds(milis) == 1) ? "second" : "seconds");
		}
		return "0 seconds";
	}
}
