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

import java.util.List;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration extends YamlConfiguration 
{
	protected int _regenDelay;
	protected int _respawnDelay;
	protected long _lastRespawn;
	protected long _lastRegen;
	protected boolean _shareXP;
	protected boolean _mysql_use;
	protected String _mysql_host;
	protected int _mysql_port;
	protected String _mysql_database;
	protected String _mysql_user;
	protected String _mysql_password;
	
	protected List<String> _regions_world_name;
	protected boolean _regions_world_purge;
	protected boolean _regions_world_archive;
	protected List<String> _regions_world_query_points;
	protected List<String> _regions_world_query_areas;
	
	protected int _ps_day_inactivity;
	protected List<String> _ps_inactivity_queries;
	
	public Configuration() 
	{
		_lastRespawn = 30;
		_lastRegen = 30;
		_shareXP = true;;
		_mysql_use = false;
		_regions_world_purge=false;
		_regions_world_archive=true;
	}
	

	public void load(FileConfiguration f, Logger log) 
	{
		try 
		{
			_regenDelay = f.getInt("RegenDelay", 30);
			_respawnDelay = f.getInt("RespawnDelay", 30);
			_shareXP = f.getBoolean("ShareXP", true);
			_mysql_use = f.getBoolean("mysql.use", false);
			if (_mysql_use)
			{
				_mysql_host = f.getString("mysql.host", "localhost");
			    _mysql_port = f.getInt("mysql.port", 3306);
				_mysql_database = f.getString("mysql.database", "minecraft");
				_mysql_user = f.getString("mysql.user", "minecraft");
				_mysql_password = f.getString("mysql.password", "minecraftPassword");
				
				_regions_world_name = (List<String>)f.getList("regions.world.name");
				_regions_world_purge = f.getBoolean("regions.world.purge", false);
				_regions_world_archive = f.getBoolean("regions.world.archive", true);
			}
			if (_regions_world_purge)
			{
				_regions_world_query_points = (List<String>)f.getList("regions.world.query_points");
				_regions_world_query_areas = (List<String>)f.getList("regions.world.query_areas");
			}
			_ps_day_inactivity = f.getInt("preciousstones.day-inactivity", 3306);
			_ps_inactivity_queries = (List<String>)f.getList("preciousstones.queries");
			
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
	
	public boolean shareXP()
	{
		return _shareXP;
	}


	public int get_regenDelay() {
		return _regenDelay;
	}


	public boolean is_mysql_use() {
		return _mysql_use;
	}


	public String get_mysql_host() {
		return _mysql_host;
	}


	public int get_mysql_port() {
		return _mysql_port;
	}


	public String get_mysql_database() {
		return _mysql_database;
	}


	public String get_mysql_user() {
		return _mysql_user;
	}


	public String get_mysql_password() {
		return _mysql_password;
	}


	public List<String> get_regions_world_name() {
		return _regions_world_name;
	}


	public boolean is_regions_world_purge() {
		return _regions_world_purge;
	}


	public boolean is_regions_world_archive() {
		return _regions_world_archive;
	}


	public List<String> get_regions_world_query_points() {
		return _regions_world_query_points;
	}


	public List<String> get_regions_world_query_areas() {
		return _regions_world_query_areas;
	}
	
	public List<String> getInactivityQueries() {
		return _ps_inactivity_queries;
	}
	
	public int getDayInactivity() {
		return _ps_day_inactivity;
	}
	
}
