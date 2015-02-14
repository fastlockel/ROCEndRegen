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

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.roc.control.ROCEndRegen;

public class EndRegenCommands implements CommandExecutor 
{
	ROCEndRegen _plugin;
	
	public EndRegenCommands(ROCEndRegen plugin)
	{
		_plugin = plugin;
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
		return false;
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
				((Player)sender).getWorld().spawnEntity(new Location(((Player)sender).getWorld(), x+20, y+20, z+20), EntityType.ENDER_DRAGON);
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
		sender.sendMessage("Regen not implemented yet.");
	 
		_plugin.getConfig().regen();
		return true;
	}
}
