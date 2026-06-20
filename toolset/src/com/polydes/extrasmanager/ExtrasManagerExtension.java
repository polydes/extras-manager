package com.polydes.extrasmanager;

import java.awt.*;

import javax.swing.*;

import com.polydes.extrasmanager.data.FileEditor;

import stencyl.app.comp.datatypes.filepath.FilePathEditor;
import stencyl.app.comp.dg.DialogPanel;
import stencyl.app.comp.propsheet.DialogPanelWrapper;
import stencyl.app.comp.propsheet.PropertiesSheetSupport;
import stencyl.app.ext.OptionsAddon;
import stencyl.app.ext.OptionsPanel;
import stencyl.app.lnf.Theme;
import stencyl.app.sys.Mime.BasicType;
import stencyl.core.ext.ProjectDataSupport;
import stencyl.core.ext.app.AppExtension;
import stencyl.core.lib.IProject;
import stencyl.sw.app.ext.ExtensionCP;

public class ExtrasManagerExtension extends AppExtension
{
	private final ProjectDataSupport<ProjectExtrasManager> projectExtrasManager =
			new ProjectDataSupport<>(this, ProjectExtrasManager::new);

	@Override
	public void onLoad()
	{
		super.onLoad();

		FileEditor.typeProgramMap.put(BasicType.TEXT, readStringProp("textEditorPath", null));
		FileEditor.typeProgramMap.put(BasicType.IMAGE, readStringProp("imageEditorPath", null));

		projectExtrasManager.onLoad();
		
		getAddons().setAddon(ExtensionCP.EXTENSION_CP_OPTIONS_ADDONS, (OptionsAddon) ExtrasManagerExtension.this::getOptions);

//		requestFolderOwnership(this, dataFolderName);
	}

	@Override
	public void onUnload()
	{
		super.onUnload();

		projectExtrasManager.onUnload();
	}

	/*
	 * Happens when a game is saved.
	 */
	@Override
	public void onGameSave(IProject project)
	{
		projectExtrasManager.onGameSave(project);
	}

	/*
	 * Happens when a game is opened.
	 */
	@Override
	public void onGameOpened(IProject project)
	{
		projectExtrasManager.onGameOpened(project);
	}
	
	/*
	 * Happens when a game is closed.
	 */
	@Override
	public void onGameClosed(IProject project)
	{
		projectExtrasManager.onGameClosed(project);
	}

	public OptionsPanel getOptions()
	{
		return new OptionsPanel()
		{
			PropertiesSheetSupport sheet;
			
			@Override
			public void init()
			{
				DialogPanel panel = new DialogPanel(Theme.BG_COLOR);
				
				sheet = new PropertiesSheetSupport(new DialogPanelWrapper(panel), properties);
				
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
