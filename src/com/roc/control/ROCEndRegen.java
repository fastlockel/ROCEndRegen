/*
 * Copyright (c) 2015 Nicolas BODIN
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.roc.control;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.roc.command.EndRegenCommands;
import com.roc.config.Configuration;

public class ROCEndRegen extends JavaPlugin 
{
	public static final String pluginName = "ROCEndRegen";

	protected Configuration _conf;
	
	@Override
	public void onLoad() 
	{
		saveDefaultConfig();
		_conf = new Configuration();
		_conf.load(getConfig(), getLogger());

	}

	@Override
	public void onEnable() 
	{
		onLoad();
	
		 CommandExecutor endRegenExecutor = new EndRegenCommands(this);
		 getCommand("rocend").setExecutor(endRegenExecutor);
		 getLogger().info("ROCEndRegen started : "+getDescription().getVersion());
		 if (_conf != null && _conf.shareXP())
			 getServer().getPluginManager().registerEvents(new ROCFightListener(), this);
	}
	
	@Override
	public void onDisable() 
	{
		 if (_conf != null && _conf.shareXP())
			 HandlerList.unregisterAll(this);
		_conf = null;
		getLogger().info("ROCEndRegen disabled.");

	}

	public void reload()
	{
		onLoad();
		getLogger().info("ROCEndRegen reloaded.");
	}
	public Configuration getConfig()
	{
		return _conf;
	}
}
