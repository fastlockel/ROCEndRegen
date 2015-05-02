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

import java.io.File;
import java.io.InputStream;

import com.roc.command.EndRegenCommands;
import com.roc.config.Configuration;

public class ROCEndRegenTest 
{
	public static final String pluginName = "ROCEndRegen";

	protected Configuration _conf;
	
	public void onLoad() 
	{
		//saveDefaultConfig();
		_conf = new Configuration();
		File f = new File("../config_test.yml");
		
		try
		{
			if (f.exists())
				_conf.load(f);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		_conf.load(_conf, null);

	}
	public static void main(String[] args)
	{
		ROCEndRegenTest test = new ROCEndRegenTest();
		test.onLoad();
		
		EndRegenCommands cmds = new EndRegenCommands(test);
		
		cmds.onCommand(null, null, null, new String[] {"PurgePS"});
		cmds.onCommand(null, null, null, new String[] {"PurgeRegions"});
	}
	public Configuration getConfig()
	{
		return _conf;
	}
}
