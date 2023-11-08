package com.polydes.extrasmanager.data;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.polydes.extrasmanager.app.FileRenameDialog;

import stencyl.app.sys.Mime;
import stencyl.app.sys.Mime.BasicType;
import stencyl.core.sys.FileMonitor;
import stencyl.sw.app.main.SW;

public class FileEditor
{
	public static HashMap<BasicType, String> typeProgramMap = new HashMap<>();
	
	public static void edit(File f)
	{
		String exec = typeProgramMap.get(Mime.getType(f));
		try
		{
			if(exec == null || exec.length() <= 2)
			{
				try
				{
					Desktop.getDesktop().edit(f);
				}
				catch(Exception ex)
				{
					try
					{
						Desktop.getDesktop().open(f);
					}
					catch(Exception ex2)
					{
						ex2.printStackTrace();
					}
				}
			}
			else
				Runtime.getRuntime().exec(new String[] {exec, f.getAbsolutePath()});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void rename(File file)
	{
		FileRenameDialog dg = new FileRenameDialog(SW.get(), file);
		String result = dg.getString();
		if(result != null)
		{
			File renameTo = new File(file.getParentFile(), result);
			file.renameTo(renameTo);
			FileMonitor.refreshMonitorsWithFile(renameTo.getAbsolutePath());
		}
	}
}
