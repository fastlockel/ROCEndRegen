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

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.roc.config.Configuration;
import com.roc.control.ROCEndRegen;
import com.roc.control.ROCEndRegenTest;
import com.roc.data.DBManager;

public class PurgePSCommand 
{
	ROCEndRegen _plugin;
	ROCEndRegenTest _test;
	
	public PurgePSCommand(ROCEndRegen plugin)
	{
		_plugin = plugin;
	}
	public PurgePSCommand(ROCEndRegenTest plugin)
	{
		_test = plugin;
	}

	public boolean execute(
			CommandSender sender,
			Command command,
			String label,
			String[] args)
	{
		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("purgeps"))
		{
			purgePS();
			return true;
		}
		return false;
	}


	protected boolean purgePS() 
	{
		Configuration conf = _plugin == null ? _test.getConfig() : _plugin.getConfig();
		
		int  days = conf.getDayInactivity();

		if (days < 1)
			return false;
		System.out.println("purge PS data older than "+days+"d");
		
		DBManager dbManager = new DBManager();
	    
		dbManager.getConnection(conf);
		
		List<String> worldList = conf.get_regions_world_name();
		List<String> queries   = conf.getInactivityQueries();
		boolean ok = true;
		long expTime = System.currentTimeMillis() - (days+1)*24L*3600000L;
		for (String world : worldList)
		{
				for(String query : queries)
				{
					try
					{
						List<Object> params = new ArrayList<Object>();
						int p = query.indexOf("world=?");
						if (p > 0)
						{
							params.add(world);
							p = p+"world=?".length()+1;
						}
						else
							p=0;
						
						p = query.indexOf("?", p);
						if (p > 0)
						{
							params.add(expTime);
							p = query.indexOf("?", p+1);
							if (p > 0)
								params.add(expTime);
						}
						int n = dbManager.executeUpdate(query,params);
						
						dbManager.closeStatement();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		}
		return ok;
	}
}
