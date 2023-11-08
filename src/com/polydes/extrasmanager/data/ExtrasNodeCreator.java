package com.polydes.extrasmanager.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import stencyl.app.api.nodes.HierarchyModelInterface;
import stencyl.app.api.nodes.NodeCreator;
import stencyl.app.sys.FileRenderer;
import stencyl.core.io.FileHelper;
import stencyl.core.sys.FileMonitor;
import stencyl.core.sys.SysFile;
import stencyl.core.sys.SysFileOperations;
import stencyl.core.sys.SysFolder;

public class ExtrasNodeCreator implements NodeCreator<SysFile,SysFolder>
{
	FileMonitor monitor;
	HierarchyModelInterface<SysFile,SysFolder> model;
	
	public ExtrasNodeCreator(FileMonitor monitor, HierarchyModelInterface<SysFile,SysFolder> model)
	{
		this.monitor = monitor;
		this.model = model;
	}
	
	@Override
	public ArrayList<CreatableNodeInfo> getCreatableNodeList(SysFolder creationBranch)
	{
		ArrayList<CreatableNodeInfo> list = new ArrayList<>();
		for(File f : com.polydes.extrasmanager.io.FileOperations.getTemplates())
		{
			if(f.getName().equals("Thumbs.db"))
				continue;
			list.add(new CreatableNodeInfo(f.getName(), f, FileRenderer.fetchMiniIcon(f)));
		}
		return list;
	}

	@Override
	public SysFile createNode(CreatableNodeInfo selected, String nodeName, SysFolder sfolder, int insertPosition)
	{
		File folder = sfolder.getFile();
		
		if(selected.name.equals("Folder"))
		{
			String name = SysFileOperations.getUnusedName("New Folder", folder);
			File f = new File(folder, name);
			f.mkdir();
		}
		else
		{
			nodeName = nodeName.substring(0, nodeName.indexOf(" ", nodeName.lastIndexOf('.')));
			File template = (File) selected.data;
			
			String ext = SysFileOperations.getNameParts(template.getName())[1];
			if(!nodeName.endsWith(ext))
				nodeName += ext;
			
			nodeName = SysFileOperations.getUnusedName(nodeName, folder);
			try
			{
				FileUtils.copyFile(template, new File(folder, nodeName));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		monitor.refresh();
		
		return null;
	}
	
	ArrayList<NodeAction<SysFile>> actions = new ArrayList<>(List.of(
		new NodeAction<>("Rename", null, (file) -> FileEditor.rename(file.getFile())),
		new NodeAction<>("Edit", null, (file) -> FileEditor.edit(file.getFile())),
		new NodeAction<>("Delete", null, (file) -> {
			FileHelper.delete(file.getFile());
			monitor.refresh();
		})
	));
	
	@Override
	public ArrayList<NodeAction<SysFile>> getNodeActions(SysFile[] targets)
	{
		return actions;
	}

	@Override
	public void editNode(SysFile dataItem)
	{
		
	}

	@Override
	public void nodeRemoved(SysFile toRemove)
	{
		
	}

	@Override
	public boolean attemptRemove(List<SysFile> toRemove)
	{
		return true;
	}
}
