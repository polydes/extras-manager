package com.polydes.extrasmanager;

import com.polydes.extrasmanager.app.MainEditor;
import com.polydes.extrasmanager.data.FileUpdateWatcher;
import com.polydes.extrasmanager.io.FileOperations;

import stencyl.app.ext.PageAddon;
import stencyl.app.ext.PageAddon.ExtensionPageAddon;
import stencyl.core.api.pnodes.HierarchyModel;
import stencyl.core.ext.ProjectDataSupport.ProjectData;
import stencyl.core.ext.app.AppExtension;
import stencyl.core.io.FileHelper;
import stencyl.core.lib.IProject;
import stencyl.core.sys.FileMonitor;
import stencyl.core.sys.SysFile;
import stencyl.core.sys.SysFolder;
import stencyl.sw.app.center.GameLibrary;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProjectExtrasManager extends ProjectData
{
    private final HierarchyModel<SysFile, SysFolder> model;
    private final FileUpdateWatcher updateWatcher;

    private MainEditor mainEditor;

    public ProjectExtrasManager(IProject project, AppExtension extension) {
        super(project, extension);

        String extrasDir = project.getLocation("extras");
        File extrasFile = new File(extrasDir);

        if (!extrasFile.exists())
            extrasFile.mkdir();

        model = FileMonitor.getExtrasModel(project);

        String extensionId = extension.getInfo().getID();
        File templatesFile = new File(project.getFiles().getExtensionGameDataLocation(extensionId), "templates");

//		if(!templatesFile.exists())
//		{
//			templatesFile.mkdir();
//			loadDefaults(templatesFile);
//		}

        FileOperations.templatesFile = templatesFile;

        updateWatcher = new FileUpdateWatcher(model);

        PageAddon extrasSidebarPage = new ExtensionPageAddon(extension) {
            @Override
            public JPanel getPage() {
                return getEditor();
            }
        };

        projectAddons.setAddon(GameLibrary.DASHBOARD_SIDEBAR_PAGE_ADDONS, extrasSidebarPage);
    }

    public HierarchyModel<SysFile, SysFolder> getModel() {
        return model;
    }

    public MainEditor getEditor() {
        if (mainEditor == null)
            mainEditor = new MainEditor(this);
        return mainEditor;
    }

    @Override
    public void save() {
        if (mainEditor != null)
            mainEditor.gameSaved();
    }

    @Override
    public void close() {
        updateWatcher.dispose();
        if (mainEditor != null)
            mainEditor.disposePages();
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
}
