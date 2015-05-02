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
import org.bukkit.entity.Player;

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
			if (sender instanceof Player)
				return purgeRegions((Player)sender);
			else
				return purgeRegions(null);
		}
		return false;
	}


	protected boolean purgeRegions(Player player) 
	{
		if ((player != null) && !(player.hasPermission("rocend.purgeregions")))
		{
			player.sendMessage("Do not have the permission to purge regions.");
			return true;
		}
		if (player != null)
			player.sendMessage("Purge regions files.");
		else
			System.out.println("purge regions files");
		
		Configuration conf = _plugin == null ? _test.getConfig() : _plugin.getConfig();
		
		DBManager dbManager = new DBManager();
	    
		dbManager.getConnection(conf);
		
		List<String> regionFiles = new ArrayList<String>();
		List<String> worldList = conf.get_regions_world_name();
		boolean ok = true;
		for (String world : worldList)
		{
			if (player != null)
				player.sendMessage("Purge regions for "+world);
			else
				System.out.println("- Purge regions for "+world);

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
			dbManager.close();
			Collections.sort(regionFiles);
			// complete with prefix and suffix
			List<String> regionFiles2 = new ArrayList<String>();
			for(String s : regionFiles)
			{
				regionFiles2.add("r."+s+".mca");
			}
			regionFiles = regionFiles2;
			
			System.out.println("Used regions files : "+regionFiles.size());
/*			int c = 0;
			for(String s : regionFiles)
			{
				c++;
				if (c % 10 == 0)
					System.out.println(",");
				else if (c > 1) 
					System.out.print(", ");
				System.out.print(s);
			}*/
			ok = ok && cleanUpFiles(player, regionFiles, world);
		}
		return ok;
	}
	public boolean  cleanUpFiles(Player player, List<String> regionFiles, String world)
	{

		// now search for what we seek : UNUSED regions files ...
		File f = new File("");
		f = new File(f.getAbsolutePath());
		System.out.println(f.getAbsolutePath()+" : "+f.getParent());
		File regionFile = null;
		while (regionFile == null && f.getParentFile() != null)
		{
		    File f1 = new File(f.getParentFile().getAbsolutePath()+File.separator+world+File.separator+"region");
		    if (f1.exists() && f1.isDirectory())
		    	regionFile = f1;
		    else
		    	f = f.getParentFile();
		}
		if (regionFile == null)
		{
			if (player != null)
				player.sendMessage("\nFolder not found for : "+File.separator+world+File.separator+"region in "+(new File(".")).getAbsolutePath());
			else
				System.out.println("\nFolder not found for : "+File.separator+world+File.separator+"region in "+(new File(".")).getAbsolutePath());
		}
		else
		{
			File     archiveDir = new File(regionFile.getParentFile().getAbsolutePath()+File.separator+"archive");
			if (!archiveDir.exists())
				archiveDir.mkdir();
			String[] rFiles = regionFile.list();
			
			int count = rFiles.length, toKeep = 0, toArchive=0;
			for (String rf : rFiles)
			{
				if (!regionFiles.contains(rf))
				{
					toArchive++;
					File toMove = new File(regionFile.getAbsolutePath()+File.separator+rf);
					
					toMove.renameTo(new File(archiveDir.getAbsolutePath()+File.separator+rf));
					System.out.println("* archive "+rf+" --> "+toMove);

				}
				else
					toKeep++;
			}
			String msg = (" Total "+count+" toArchive: "+toArchive+"  toKeep: "+toKeep+"  detected: "+regionFiles.size());
			if (player != null)
				player.sendMessage(msg);
			else
				System.out.println(msg);

		}
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
			int minx = (int)rs.getDouble("min_x");
			int minz = (int)rs.getDouble("min_z");
			int maxx = (int)rs.getDouble("max_x");
			int maxz = (int)rs.getDouble("max_z");
			
			int region_minx = minx >> 9; // 4 for chunck then 5 to region
			int region_minz = minz >> 9; // 4 for chunck then 5 to region
			int region_maxx = maxx >> 9; // 4 for chunck then 5 to region
			int region_maxz = maxz >> 9; // 4 for chunck then 5 to region

			if (region_minx > region_maxx)
			{
				int i = region_maxx;
				region_maxx = region_minx;
				region_minx = i; // swap
			}
			if (region_minz > region_maxz)
			{
				int i = region_maxz;
				region_maxz = region_minz;
				region_minz = i; // swap
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
