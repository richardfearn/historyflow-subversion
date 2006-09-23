package uk.co.richardfearn.historyflow.subversion;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.swixml.SwingEngine;

/**
 * <p>Dialog that allows a file in a Subversion repository to be selected.</p>
 * 
 * @author Richard Fearn
 */
public class SubversionDialog implements ActionListener, KeyListener {

	private String url;

	private String username;

	private String password;

	private String filePath;

	private JTextField textUrl;

	private JTextField textUsername;

	private JPasswordField textPassword;

	private JTextField textFilePath;

	private JButton buttonOk;

	private JButton buttonCancel;

	private Component dialog;
	
	private static final String DIALOG_XML_FILE = "dialog.xml";
	
	public SubversionDialog(JFrame parent, String defaultUrl, String defaultUsername, String defaultFilePath) throws Exception {
		URL url = getClass().getResource(DIALOG_XML_FILE);
		dialog = new SwingEngine(this).render(url);
		
		// Fill in values, and set OK button state accordingly
		textUrl.setText(defaultUrl);
		textUsername.setText(defaultUsername);
		textFilePath.setText(defaultFilePath);
		setOkButtonState();

		textUrl.addKeyListener(this);
		textUsername.addKeyListener(this);
		textPassword.addKeyListener(this);
		textFilePath.addKeyListener(this);
		
		buttonOk.addActionListener(this);
		buttonCancel.addActionListener(this);
		
		((JDialog) dialog).pack();
	}
	
	private void okPressed() {
		url = textUrl.getText().trim();
		username = textUsername.getText().trim();
		password = new String(textPassword.getPassword());
		filePath = textFilePath.getText().trim();
		((JDialog) dialog).dispose();
	}

	public void actionPerformed(ActionEvent e) {
		// Handle click on the OK button
		if (e.getSource() == buttonOk) {
			okPressed();
		}
		
		// Handle click on the Cancel button
		else if (e.getSource() == buttonCancel) {
			((JDialog) dialog).dispose();
		}
	}

	// Determine validity of what's been entered
	private boolean isOk() {
		boolean gotUrl = textUrl.getText().trim().length() > 0;
		boolean gotFilePath = textFilePath.getText().trim().length() > 0;
		return (gotUrl && gotFilePath);
	}

	// Update enabled state of OK button
	private void setOkButtonState() {
		buttonOk.setEnabled(isOk());
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}

	public void keyPressed(KeyEvent e) {
		setOkButtonState();
	}

	public void keyReleased(KeyEvent e) {
		setOkButtonState();
		if (e.getKeyCode() == KeyEvent.VK_ENTER && isOk()) {
			okPressed();
		}
	}

	public void keyTyped(KeyEvent e) {
		setOkButtonState();
	}

	private static final long serialVersionUID = 1L;

}
