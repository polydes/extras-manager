package com.polydes.extrasmanager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.polydes.extrasmanager.app.MainEditor;
import com.polydes.extrasmanager.data.FileEditor;
import com.polydes.extrasmanager.data.FileUpdateWatcher;
import com.polydes.extrasmanager.io.FileOperations;

import stencyl.app.comp.datatypes.filepath.FilePathEditor;
import stencyl.app.comp.dg.DialogPanel;
import stencyl.app.comp.propsheet.DialogPanelWrapper;
import stencyl.app.comp.propsheet.PropertiesSheetStyle;
import stencyl.app.comp.propsheet.PropertiesSheetSupport;
import stencyl.app.ext.OptionsAddon;
import stencyl.app.ext.OptionsPanel;
import stencyl.app.ext.PageAddon;
import stencyl.app.sys.Mime.BasicType;
import stencyl.core.api.pnodes.HierarchyModel;
import stencyl.core.ext.addon.AddonContributor;
import stencyl.core.ext.addon.ContributorAddonData;
import stencyl.core.ext.app.AppExtension;
import stencyl.core.io.FileHelper;
import stencyl.core.lib.IProject;
import stencyl.core.lib.ProjectManager;
import stencyl.core.sys.FileMonitor;
import stencyl.core.sys.SysFile;
import stencyl.core.sys.SysFolder;
import stencyl.sw.app.center.GameLibrary;
import stencyl.sw.app.ext.ExtensionCP;
import stencyl.sw.core.lib.game.Game;

public class ExtrasManagerExtension extends AppExtension
{
	public static class ExtrasManager
	{
		private final IProject project;
		private final HierarchyModel<SysFile,SysFolder> model;
		private final FileUpdateWatcher updateWatcher;
		
		private final String gameDir;
		private final String extrasDir;
		
		private MainEditor mainEditor;
		
		private AddonContributor projectAddons;

		public ExtrasManager(IProject project)
		{
			this.project = project;
			Game game = (Game) project;
			
			gameDir = project.getLocation();
			extrasDir = gameDir + "extras/";
			File extrasFile = new File(extrasDir);

			if(!extrasFile.exists())
				extrasFile.mkdir();

			model = FileMonitor.getExtrasModel(game);

			String extensionId = ExtrasManagerExtension.get().getManifest().id;
			File templatesFile = new File(project.getFiles().getExtensionGameDataLocation(extensionId), "templates");

			if(!templatesFile.exists())
			{
				templatesFile.mkdir();
				loadDefaults(templatesFile);
			}

			FileOperations.templatesFile = templatesFile;

			updateWatcher = new FileUpdateWatcher(model);
			
			projectAddons = new AddonContributor()
			{
				final ContributorAddonData data = new ContributorAddonData(new HashMap<>());
				
				@Override
				public String getAddonContributorId()
				{
					return "app-"+_instance.getManifest().id;
				}

				@Override
				public ContributorAddonData getAddonData()
				{
					return data;
				}

				@Override
				public boolean hasInitializedAddonData()
				{
					return true;
				}
			};
			
			projectAddons.setAddon(GameLibrary.DASHBOARD_SIDEBAR_PAGE_ADDONS, (PageAddon) this::getEditor);
			
			project.getAddonManager().addDataForContributor(projectAddons);
		}

		public IProject getProject()
		{
			return project;
		}

		public HierarchyModel<SysFile,SysFolder> getModel()
		{
			return model;
		}
		
		public MainEditor getEditor()
		{
			if(mainEditor == null)
				mainEditor = new MainEditor(this);
			return mainEditor;
		}
		
		public void save()
		{
			if(mainEditor != null)
				mainEditor.gameSaved();
		}
		
		public void close()
		{
			project.getAddonManager().removeDataForContributor(projectAddons);
			updateWatcher.dispose();
			if(mainEditor != null)
				mainEditor.disposePages();
		}
	}
	
	private Map<IProject, ExtrasManager> projectExtrasManager = new HashMap<>();
	
	private static ExtrasManagerExtension _instance;
	
	public static ExtrasManagerExtension get()
	{
		return _instance;
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
		
		_instance = this;

		FileEditor.typeProgramMap.put(BasicType.TEXT, readStringProp("textEditorPath", null));
		FileEditor.typeProgramMap.put(BasicType.IMAGE, readStringProp("imageEditorPath", null));
		
		for(IProject project : ProjectManager.getOpenedProjects())
		{
			projectExtrasManager.put(project, new ExtrasManager(project));
		}
		
		setAddon(ExtensionCP.EXTENSION_CP_OPTIONS_ADDONS, (OptionsAddon) ExtrasManagerExtension.this::getOptions);

//		requestFolderOwnership(this, dataFolderName);
	}

	@Override
	public void onUnload()
	{
		super.onUnload();

		for(IProject project : ProjectManager.getOpenedProjects())
		{
			projectExtrasManager.remove(project).close();
		}
	}

	/*
	 * Happens when a game is saved.
	 */
	@Override
	public void onGameSave(IProject project)
	{
		var manager = projectExtrasManager.get(project);
		if(manager != null)
			manager.save();
	}

	/*
	 * Happens when a game is opened.
	 */
	@Override
	public void onGameOpened(IProject project)
	{
		projectExtrasManager.put(project, new ExtrasManager(project));
	}
	
	public static void loadDefaults(File templates)
	{
		try
		{
			FileHelper.writeStringToFile
			(
				new File(templates, "File.txt").getAbsolutePath(),
				""
			);
			FileHelper.writeToPNG
			(
				new File(templates, "Image.png").getAbsolutePath(),
				new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
			);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * Happens when a game is closed.
	 */
	@Override
	public void onGameClosed(IProject project)
	{
		var manager = projectExtrasManager.remove(project);
		if(manager != null)
			manager.close();
	}

	public OptionsPanel getOptions()
	{
		return new OptionsPanel()
		{
			PropertiesSheetSupport sheet;
			
			@Override
			public void init()
			{
				DialogPanel panel = new DialogPanel(PropertiesSheetStyle.DARK.pageBg);
				
				sheet = new PropertiesSheetSupport(new DialogPanelWrapper(panel), PropertiesSheetStyle.DARK, properties);
				
				sheet.build()
					.header("Options")
					.field("textEditorPath").label("Text Editor")._editor(FilePathEditor.BUILDER).add()
					.field("imageEditorPath").label("Image Editor")._editor(FilePathEditor.BUILDER).add()
					.finish();
				
				panel.addFinalRow(new JLabel());
				add(panel, BorderLayout.CENTER);
			}

			@Override
			public void onPressedOK()
			{
				FileEditor.typeProgramMap.put(BasicType.BINARY, readStringProp("textEditorPath", null));
				FileEditor.typeProgramMap.put(BasicType.TEXT, readStringProp("textEditorPath", null));
				FileEditor.typeProgramMap.put(BasicType.IMAGE, readStringProp("imageEditorPath", null));
				sheet.dispose();
				sheet = null;
			}

			@Override
			public void onPressedCancel()
			{
				sheet.revertChanges();
				sheet.dispose();
				sheet = null;
			}
			
			@Override
			public void onShown()
			{
			}
		};
	}
}
