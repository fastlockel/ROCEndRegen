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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class ROCFightListener implements Listener 
{
	public static Map<UUID, Damage> _damageTracker = new HashMap<UUID, Damage>();

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent e) 
	{
		if (e.getEntity() instanceof EnderDragon) 
		{
			final double damageCount = e.getDamage(); // Amount of life taken
			UUID id = null; // look for origin
			if (e.getDamager() instanceof Player) 
			{
				Player player = (Player) e.getDamager();
				id = player.getUniqueId();
			}
			else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) 
			{
				Projectile p = (Projectile) e.getDamager();
				id = ((Player) p.getShooter()).getUniqueId();
			}
			if (id != null)
			{
				synchronized (_damageTracker) 
				{
					Damage damage  = (Damage)_damageTracker.get(id);
					
					if (damage == null)
					{
						damage = new Damage();
						
						damage.setPlayerId(id);
						_damageTracker.put(id, damage); // store new player record
					}
					damage.setDamage(damage.getDamage()+damageCount);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityCreatePortal(EntityCreatePortalEvent e) 
	{
		if (e.getEntity() instanceof EnderDragon) 
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEnderDragonDie(EntityDeathEvent e) 
	{
		if (e.getEntity() instanceof EnderDragon) 
		{
			//go throuh players and find the most active and total damages
			double totalDamage = 0D;
			double maxDamage = 0D;
			UUID   maxDamagerId = null;
			synchronized (_damageTracker) 
			{
				for(Damage d : _damageTracker.values())
				{
					totalDamage += d.getDamage();
					if (maxDamage < d.getDamage())
					{
						maxDamagerId = d.getPlayerId();
					}
				}
				Player p = Bukkit.getServer().getPlayer(maxDamagerId);
				
				if (p != null && p.isOnline())
				{
					World world = e.getEntity().getLocation().getWorld();
					
					world.dropItemNaturally(p.getLocation(), new ItemStack(Material.DRAGON_EGG));
					
					if (_damageTracker.values().size() > 1)
						p.sendMessage("Good fight, you are the best killer and got the enderdragon egg.");
					else
						p.sendMessage("You have received the enderdragon egg.");
				}
				if (totalDamage > 0)
				{
					double totalXP = 12000;
					
					for(Damage d : _damageTracker.values())
					{
						int playerXP = (int) (totalXP * d.getDamage()/totalDamage);
						
						p = Bukkit.getServer().getPlayer(d.getPlayerId());
						
						if (p.isOnline())
						{
							p.giveExp(playerXP);
							p.sendMessage("Good fight, you have received "+playerXP+" experience.");
						}
					}
				}
				e.setDroppedExp(0);
			}
		}
	}

	protected class Damage
	{
		UUID    _playerId;
		double    _damage=0;
		
		public final UUID getPlayerId() 
		{
			return _playerId;
		}
		public final void setPlayerId(UUID playerId) 
		{
			_playerId = playerId;
		}
		public final double getDamage() 
		{
			return _damage;
		}
		public final void setDamage(double damage) 
		{
			_damage = damage;
		}
	}
}
