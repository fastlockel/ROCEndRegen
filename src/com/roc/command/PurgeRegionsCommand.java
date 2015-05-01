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

public class PurgeRegionsCommand 
{
	ROCEndRegen _plugin;
	ROCEndRegenTest _test;
	
	public PurgeRegionsCommand(ROCEndRegen plugin)
	{
		_plugin = plugin;
	}
	public PurgeRegionsCommand(ROCEndRegenTest plugin)
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

		if (args[0].equalsIgnoreCase("purgeregions"))
		{
			purgeRegions();
			return true;
		}
		return false;
	}


	protected boolean purgeRegions() 
	{
		System.out.println("purge regions files");
		
		Configuration conf = _plugin == null ? _test.getConfig() : _plugin.getConfig();
		
		DBManager dbManager = new DBManager();
	    
		dbManager.getConnection(conf);
		
		List<String> regionFiles = new ArrayList<String>();
		String world = conf.get_regions_world_name();
		
		if (conf.get_regions_world_query_points() != null)
		{
			List<String> queries = conf.get_regions_world_query_points();
			for(String query : queries)
			{
				try
				{
					ResultSet rs = dbManager.executeQuery(query, new String[] {world});
					
					List<String> current = decodeRegionPoints(rs);
					
					for(String s : current)
					{
						if (!regionFiles.contains(s))
							regionFiles.add(s);
					}
					rs.close();
					dbManager.closeStatement();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		if (conf.get_regions_world_query_areas() != null)
		{
			List<String> queries = conf.get_regions_world_query_areas();
			
			for(String query : queries)
			{
				try
				{
					ResultSet rs = dbManager.executeQuery(query, new String[] {world});
					
					List<String> current = decodeRegionAreas(rs);
					
					for(String s : current)
					{
						if (!regionFiles.contains(s))
							regionFiles.add(s);
					}
					rs.close();
					dbManager.closeStatement();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		Collections.sort(regionFiles);
		System.out.println("Used regions files : "+regionFiles.size());
		int c = 0;
		for(String s : regionFiles)
		{
			c++;
			if (c % 10 == 0)
				System.out.println(",");
			else if (c > 1) 
				System.out.print(", ");
			System.out.print(s);
		}
		// now search for what we seek : UNUSED regions files ...
		
		return true;
	}
	
	/**
	 * Extract region file extensions from points x,z (blocs)
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	public List<String> decodeRegionPoints(ResultSet rs) throws Exception
	{
		List<String> resu = new ArrayList<String>();
		int lastx=-10000000, lastz=-10000000;// speedup
		while (rs.next())
		{
			//Object obj = rs.getObject(1);
			int x = (int)rs.getDouble("x");
			int z = (int)rs.getDouble("z");
			
			int region_x = x >> 9; // 4 for chunck then 5 to region
			int region_z = z >> 9; // 4 for chunck then 5 to region
			
	
			if (region_x != lastx && region_z != lastz) // speeds up if query ordered by properly
			{
				lastx = region_x;
				lastz = region_z;
				StringBuffer sb = new StringBuffer();
				
				sb.append(region_x).append('.').append(region_z);
				
				if (!resu.contains(sb.toString()))
					resu.add(sb.toString());
			}
		}
		
		return resu;
	}
	
	/**
	 * Extract region file extensions from areas xmin,xmax,zmin,zmax (blocs)
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	public List<String> decodeRegionAreas(ResultSet rs) throws Exception
	{
		List<String> resu = new ArrayList<String>();
		while (rs.next())
		{
			int minx = (int)rs.getDouble("minx");
			int minz = (int)rs.getDouble("minz");
			int maxx = (int)rs.getDouble("maxx");
			int maxz = (int)rs.getDouble("maxz");
			
			int region_minx = minx >> 9; // 4 for chunck then 5 to region
			int region_minz = minz >> 9; // 4 for chunck then 5 to region
			int region_maxx = maxx >> 9; // 4 for chunck then 5 to region
			int region_maxz = maxz >> 9; // 4 for chunck then 5 to region

			if (region_minx > region_maxx)
			{
				int i = region_maxx;
				region_maxx = region_minx;
				region_minx = i; // spwap
			}
			if (region_minz > region_maxz)
			{
				int i = region_maxz;
				region_maxz = region_minz;
				region_minz = i; // spwap
			}
			
			for (int region_x = region_minx; region_x <= region_maxx; region_x++)
			{
				for (int region_z = region_minz; region_z <= region_maxz; region_z++)
				{
					StringBuffer sb = new StringBuffer();
					
					sb.append(region_x).append('.').append(region_z);
					
					if (!resu.contains(sb.toString()))
						resu.add(sb.toString());
				}
			}
			
		}
		return resu;
	}
}
