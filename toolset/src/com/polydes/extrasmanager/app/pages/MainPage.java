package com.polydes.extrasmanager.app.pages;

import java.awt.*;

import javax.swing.*;

import com.polydes.extrasmanager.ExtrasManagerExtension.ExtrasManager;
import com.polydes.extrasmanager.data.ExtrasNodeCreator;
import com.polydes.extrasmanager.data.FilePreviewer;

import stencyl.app.api.nodes.HierarchyModelInterface;
import stencyl.app.comp.MiniSplitPane;
import stencyl.app.comp.UI;
import stencyl.app.comp.filelist.TreePage;
import stencyl.core.api.pnodes.HierarchyModel;
import stencyl.core.sys.FileMonitor;
import stencyl.core.sys.SysFile;
import stencyl.core.sys.SysFolder;
import stencyl.sw.app.sys.SysFileUiProvider;

public class MainPage extends JPanel
{
	public static final int DEFAULT_SPLITPANE_WIDTH = 180;
	public static final Color BG_COLOR = new Color(43, 43, 43);

	protected JComponent currView;
	public JPanel navwindow;
	public JPanel navbar;
	public JPanel sidebar;
	
	public TreePage<SysFile,SysFolder> treePage;
	
	protected MiniSplitPane splitPane;
	
	public void dispose()
	{
		removeAll();
		
		treePage.dispose();
		treePage = null;
		
		currView = null;
		navwindow = null;
		navbar = null;
		sidebar = null;
		
		splitPane.removeAll();
		splitPane = null;
		
		FilePreviewer.endPreview();
	}
	
	public MainPage(ExtrasManager manager)
	{
		super(new BorderLayout());

		HierarchyModel<SysFile,SysFolder> model = manager.getModel();
		HierarchyModelInterface<SysFile, SysFolder> extrasModelInterface = new HierarchyModelInterface<>(model);
		
		FileMonitor monitor = FileMonitor.getMonitor(manager.getProject());
		extrasModelInterface.setNodeCreator(new ExtrasNodeCreator(monitor, extrasModelInterface));
		
		SysFileUiProvider uiProvider = new SysFileUiProvider(extrasModelInterface);
		
		treePage = new TreePage<>(extrasModelInterface);
		treePage.setNodeIconProvider(uiProvider);
		treePage.setNodeViewProvider(uiProvider);
		treePage.setInnerCellSize(92, 80);
		treePage.setIconSize(80, 80);
		
		treePage.getTree().getTree().setRootVisible(true);
		treePage.getTree().setListEditEnabled(true);
//		treePage.getTree().disableButtonBar();
		
		JScrollPane treescroller = UI.createScrollPane(treePage.getTree());
		treescroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		navwindow = new JPanel(new BorderLayout());
		navwindow.add(currView = treePage, BorderLayout.CENTER);
		
		revalidate();
		repaint();
		
		splitPane = new MiniSplitPane();
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treescroller);
		splitPane.setRightComponent(navwindow);
		
		add(splitPane);
		
		splitPane.setDividerLocation(DEFAULT_SPLITPANE_WIDTH);
	}
}