package com.polydes.extrasmanager.app;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import stencyl.app.comp.ButtonBarFactory;
import stencyl.app.comp.GroupButton;
import stencyl.app.comp.dg.StencylDialog;
import stencyl.app.lnf.Theme;
import stencyl.core.loc.LanguagePack;

public class FileRenameDialog extends StencylDialog
{
	/*-------------------------------------*\
	 * Globals
	\*-------------------------------------*/ 

	private static LanguagePack lang = LanguagePack.get();

	public static final int WIDTH = 240;
	public static final int HEIGHT = 140;
	
	private String result;
	private JPanel panel;
	private JTextArea text;
	
	private AbstractButton okButton;	
	
	/*-------------------------------------*\
	 * Constructor
	\*-------------------------------------*/ 

	public FileRenameDialog(JFrame owner, File model)
	{
		super
		(
			owner, 
			"Rename File", 
			WIDTH, HEIGHT, 
			false
		);
		
		result = model.getName();
		
		add(createContentPanel(), BorderLayout.CENTER);
		
		setVisible(true);
	}
	
	/*-------------------------------------*\
	 * Construct UI
	\*-------------------------------------*/ 

	@Override
	public JComponent createContentPanel()
	{
		text = new JTextArea(1, 5);
		text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));	
		text.setText(result);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		
		panel = new JPanel(new BorderLayout());
		panel.add(text, BorderLayout.CENTER);
		panel.setBackground(Theme.EDITOR_BG_COLOR);
		
		text.getDocument().addDocumentListener
		(
			new DocumentListener()
			{
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					okButton.setEnabled(text.getDocument().getLength() > 0);
				}
				
				@Override
				public void removeUpdate(DocumentEvent e)
				{
					okButton.setEnabled(text.getDocument().getLength() > 0);
				}
				
				@Override
				public void changedUpdate(DocumentEvent e)
				{
					
				}
			}
		);
		
		text.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				
			}
			
			@Override
			public void keyReleased(KeyEvent e)
			{
				
			}
			
			@Override
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if(text.getDocument().getLength() > 0)
					{
						result = text.getText();
						setVisible(false);
					}
					else
						e.consume();
				}
			}
		});
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
		p.setBackground(Theme.EDITOR_BG_COLOR);
		p.add(panel, BorderLayout.CENTER);
		
		return p;
	}

	@Override
	public JPanel createButtonPanel() 
	{
		okButton = new GroupButton(0);
		JButton cancelButton = new GroupButton(0);

		okButton.setAction
		(
			new AbstractAction(lang.get("globals.savechanges"))
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					result = text.getText();
					setVisible(false);
				}
			}
		);

		cancelButton.setAction
		(
			new AbstractAction(lang.get("globals.cancel")) 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					cancel();
				}
			}
		);

		return ButtonBarFactory.createButtonBar
		(
			this,
			new AbstractButton[] {okButton, cancelButton},
			0
		);
	}
	
	public String getString()
	{
		return result;
	}
	
	@Override
	public void cancel()
	{
		result = null;
		setVisible(false);
	}
}