package com.polydes.extrasmanager.app;

import java.awt.*;

import javax.swing.*;

import com.polydes.extrasmanager.ProjectExtrasManager;
import com.polydes.extrasmanager.app.pages.MainPage;

public class MainEditor extends JPanel
{
	public final ProjectExtrasManager manager;
	
	private MainPage page;
	
	public static final Color SIDEBAR_COLOR = new Color(62, 62, 62);
	
	public MainEditor(ProjectExtrasManager manager)
	{
		super(new BorderLayout());
		
		this.manager = manager;
		page = new MainPage(manager);
		
		add(page);
	}
	
	public void disposePages()
	{
		page.dispose();
		page = null;
	}

	public void gameSaved()
	{
		revalidate();
		repaint();
	}
}
