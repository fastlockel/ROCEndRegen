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
package com.roc.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.roc.control.ROCEndRegen;
import com.roc.control.ROCEndRegenTest;
import com.roc.control.ROCFightListener;
import com.roc.control.RegenInterface;

public class EndRegenCommands implements CommandExecutor 
{
	ROCEndRegen _plugin;
	ROCEndRegenTest _test;
	
	public EndRegenCommands(ROCEndRegen plugin)
	{
		_plugin = plugin;
	}
	public EndRegenCommands(ROCEndRegenTest plugin)
	{
		_test = plugin;
	}
	@Override
	public boolean onCommand(
			CommandSender sender,
			Command command,
			String label,
			String[] args)
	{
		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("respawn"))
			return respawn(sender);
		else if (args[0].equalsIgnoreCase("regen"))
			return regen(sender);
		else if (args[0].equalsIgnoreCase("reload"))
			return reload(sender);
		else if (args[0].equalsIgnoreCase("egg"))
			return egg(sender);
		else if (args[0].equalsIgnoreCase("purgeregions"))
		{
			PurgeRegionsCommand cmd;
			
			if (_plugin == null)
				cmd = new PurgeRegionsCommand(_test);
			else
				cmd = new PurgeRegionsCommand(_plugin);
				
			
			cmd.execute(sender, command, label, args);
		}
		return false;
	}


	protected boolean egg(CommandSender sender) 
	{
		if ((sender instanceof Player) && !((Player)sender).hasPermission("rocend.reload"))
		{
			sender.sendMessage("Do not have the permission to invoke egg.");
			return true;
		}
		ROCFightListener listener = new ROCFightListener();
		
		if (sender instanceof Player)
		{
			listener.dropEgg((Player)sender);
		}
		else
			sender.sendMessage("Must be ingame to drop egg.");

	
		return true;
	}

	protected boolean reload(CommandSender sender) 
	{
		if ((sender instanceof Player) && !((Player)sender).hasPermission("rocend.reload"))
		{
			sender.sendMessage("Do not have the permission to reload.");
			return true;
		}
		_plugin.reload();
		sender.sendMessage("ROCEndRegen reloaded.");

		return true;
	}

	protected boolean respawn(CommandSender sender) 
	{
		if ((sender instanceof Player) && !((Player)sender).hasPermission("rocend.respawn"))
		{
			sender.sendMessage("Do not have the permission to reload.");
			return true;
		}

		if ((sender instanceof Player) && !(_plugin.getConfig().canRespawn()))
		{
			sender.sendMessage("Too early to respawn the enderdragon. Wait "+_plugin.getConfig().getRespawnDelay() +" min. and try again.");
			return true;
		}

/*		List<World> worldList = sender.getServer().getWorlds();
		
		for (World w : worldList)
		{
			List<EnderDragon> dragons = new ArrayList<EnderDragon>(w.getEntitiesByClass(EnderDragon.class));
		
			if (dragons != null && dragons.size() > 0)
			{
				_plugin.getLogger().warning("Already has an enderdragon");
				return true; // already there
			}
		}
	*/
		if (sender instanceof Player)
		{
			if (((Player)sender).getWorld().getEnvironment() == Environment.THE_END)
			{
				//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon enderdragon ~20 ~40 ~20");
				Location playerLocation = ((Player)sender).getLocation();
				int x = playerLocation.getBlockX();
				int y = playerLocation.getBlockY();
				int z = playerLocation.getBlockZ();
				World theEnd = ((Player)sender).getWorld();
				List<EnderDragon> list = new ArrayList<EnderDragon>();
				for (Entity e : theEnd.getEntities()) 
				{
					if (e instanceof EnderDragon) 
					{
						list.add(((EnderDragon) e));
						ROCFightListener.clear();
					}
				}
				for (EnderDragon e : list)
				{
					sender.sendMessage("Enderdragon removed.");
					((EnderDragon) e).damage(10000000D);
					((EnderDragon) e).remove();
				}
				ROCFightListener.clear();
				theEnd.spawnEntity(new Location(((Player)sender).getWorld(), x+20, y+20, z+20), EntityType.ENDER_DRAGON);
				sender.sendMessage("Enderdragon respawned.");
				_plugin.getConfig().respawn();
			}
			else
			{
				sender.sendMessage("You must be in END world to spawn the enderdragon.");
			}

		}
		else 
			sender.sendMessage("Respawn available in game only and not from console.");
				 
		
		return true;
	}
	
	protected boolean regen(CommandSender sender) 
	{
		if ((sender instanceof Player) && !((Player)sender).hasPermission("rocend.regen"))
		{
			sender.sendMessage("Do not have the permission to regen.");
			return true;
		}

		if ((sender instanceof Player) && !(_plugin.getConfig().canRegen()))
			return true;

		//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "summon enderdragon ~20 ~40 ~20");
		if ((sender instanceof Player) && ((Player)sender).getWorld().getEnvironment() == Environment.THE_END)
		{
			
			Plugin worldEditPlugin = _plugin.getServer().getPluginManager().getPlugin("WorldEdit");
			
			if (worldEditPlugin != null)
			{
				try
				{
					RegenInterface we = (RegenInterface)Class.forName("com.roc.control.WEControl").newInstance();
				
					we.regen(((Player)sender).getWorld());
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage("Regen not supported when Worldedit plugin is not found.");
		}		 
		_plugin.getConfig().regen();
		return true;
	}
}
