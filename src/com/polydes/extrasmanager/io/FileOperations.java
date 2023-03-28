package com.polydes.extrasmanager.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.polydes.extrasmanager.data.FileClipboard;

import stencyl.app.comp.dg.YesNoQuestionDialog;
import stencyl.core.api.Choice;
import stencyl.core.io.FileHelper;
import stencyl.core.sys.FileMonitor;
import stencyl.core.sys.SysFileOperations;

//TODO: Move this to IO and move interface stuff to somewhere in app.

public class FileOperations
{
	public static File templatesFile;

	public static File[] getTemplates()
	{
		return templatesFile.listFiles();
	}

	public static void copy(List<File> files)
	{
		FileClipboard.clear();
		for(File f : files)
			FileClipboard.add(f);
		FileClipboard.op = FileClipboard.COPY;
	}

	public static void cut(List<File> files)
	{
		FileClipboard.clear();
		for(File f : files)
			FileClipboard.add(f);
		FileClipboard.op = FileClipboard.CUT;
	}

	public static void paste(File targetParent)
	{
		for(File f : FileClipboard.list())
		{
			try
			{
				String newName = SysFileOperations.getUnusedName(f.getName(), targetParent);
				File target = new File(targetParent, newName);
				if(f.isDirectory())
				{
					ArrayList<File> exclude = new ArrayList<File>();
					exclude.add(target);
					FileHelper.copyDirectory(f, target, exclude);
				}
				else
					FileUtils.copyFile(f, target);

				if(FileClipboard.op == FileClipboard.CUT)
					FileHelper.delete(f);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		};

		FileClipboard.clear();
		FileMonitor.refreshMonitorsWithFile(targetParent.getAbsolutePath());
	}

	public static void deleteFiles(List<File> files)
	{
		if(files.isEmpty()) return;
		
		Choice result = YesNoQuestionDialog.showYesNoPrompt("Delete Files", "Are you sure you want to delete the selected files?");
		if(result == Choice.YES)
		{
			for(Object o : files)
				FileHelper.delete((File) o);
			
			FileMonitor.refreshMonitorsWithFile(files.get(0).getAbsolutePath());
		}
	}

	public static void renameFiles(List<File> files, String newName)
	{
		if(files.isEmpty()) return;
		
		for(File f : files)
		{
			File parent = f.getParentFile();
			String name = SysFileOperations.getUnusedName(newName, parent);
			f.renameTo(new File(parent, name));
		}

		FileMonitor.refreshMonitorsWithFile(files.get(0).getAbsolutePath());
	}

	public static List<File> asFiles(Object[] a)
	{
		ArrayList<File> files = new ArrayList<File>(a.length);
		for(Object o : a)
			files.add((File) o);
		return files;
	}
}
