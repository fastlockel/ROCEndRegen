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

package com.roc.config;

import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration extends YamlConfiguration 
{
	protected int _regenDelay;
	protected int _respawnDelay;
	protected long _lastRespawn;
	protected long _lastRegen;
	
	public Configuration() 
	{
		_lastRespawn = 0;
		_lastRegen = 0;
	}
	

	public void load(FileConfiguration f, Logger log) 
	{
		try 
		{
			_regenDelay = f.getInt("RegenDelay", 30);
			_respawnDelay = f.getInt("RespawnDelay", 30);
		} 
		catch (Exception e) 
		{
			log.warning("Configuration file could not be found "
					+ f.getName());
		}
	}
	
	public boolean canRespawn()
	{
		if ((System.currentTimeMillis() - _lastRespawn)<(_respawnDelay*60000L))
		{
			return false;
		}
		return true;
	}
	public int getRespawnDelay()
	{
		if ((System.currentTimeMillis() - _lastRespawn)<(_respawnDelay*60000L))
		{
			return 1+(int)((_respawnDelay*60000L - System.currentTimeMillis() +_lastRespawn)/60000);
		}
		return 0;
	}

	public boolean canRegen()
	{
		if ((System.currentTimeMillis() - _lastRegen)<(_regenDelay*60000L))
		{
			return false;
		}
		return true;
	}
	
	public int getRegenDelay()
	{
		if ((System.currentTimeMillis() - _lastRegen)<(_regenDelay*60000L))
		{
			return 1+(int)((_regenDelay*60000L - System.currentTimeMillis() +_lastRegen)/60000);
		}
		return 0;
	}
	public void respawn()
	{
		_lastRespawn = System.currentTimeMillis();
	}
	public void regen()
	{
		_lastRegen = System.currentTimeMillis();
	}
}
