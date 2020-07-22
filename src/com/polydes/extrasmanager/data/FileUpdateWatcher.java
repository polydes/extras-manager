package com.polydes.extrasmanager.data;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.polydes.common.nodes.HierarchyModel;
import com.polydes.common.nodes.HierarchyRepresentation;
import com.polydes.common.sys.Mime;
import com.polydes.common.sys.SysFile;
import com.polydes.common.sys.SysFolder;

import stencyl.sw.SW;
import stencyl.sw.Session;
import stencyl.sw.app.gamecontroller.GameInterfaceServer;
import stencyl.sw.app.gamecontroller.GameInterfaceServer.AssetType;

public class FileUpdateWatcher implements HierarchyRepresentation<SysFile,SysFolder>
{
	final HierarchyModel<SysFile, SysFolder> model;
	
	public FileUpdateWatcher(HierarchyModel<SysFile, SysFolder> model)
	{
		this.model = model;
		model.addRepresentation(this);
	}
	
	public void dispose()
	{
		model.removeRepresentation(this);
	}
	
	private static Pattern bsPattern = Pattern.compile(Matcher.quoteReplacement("\\"));
	private static String fs(String s)
	{
		return bsPattern.matcher(s).replaceAll("/");
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if(!Session.instance().isGCIAutoupdateEnabled())
		{
			return;
		}
		
		if(evt.getSource() instanceof SysFile && evt.getPropertyName().equals(SysFile.STATE))
		{
			SysFile modified = (SysFile) evt.getSource();
			
			String extrasPath = ((SysFile) model.getRootBranch()).getFile().getAbsolutePath();
			String modifiedPath = modified.getFile().getAbsolutePath();
			String assetPath = "assets/data/" + fs(modifiedPath.substring(extrasPath.length() + 1));
			
			GameInterfaceServer server = SW.get().getGameInterfaceServer();
			
			if(!server.getClients().isEmpty())
			{
				try
				{
					byte[] toSend = Files.readAllBytes(modified.getFile().toPath());
					
					AssetType assetType = null;
					switch(Mime.getType(modified.getFile()))
					{
						case TEXT: assetType = AssetType.TEXT; break;
						case IMAGE: assetType = AssetType.IMAGE; break;
						case AUDIO: assetType = AssetType.SOUND; break;
						default: assetType = AssetType.BINARY; break;
					}
					
					server.sendAsset(assetPath, assetType, toSend);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void itemAdded(SysFolder folder, SysFile item, int position)
	{
	}

	@Override
	public void itemRemoved(SysFolder folder, SysFile item, int oldPosition)
	{
	}
}
