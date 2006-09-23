package uk.co.richardfearn.historyflow.subversion;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.swixml.SwingEngine;

/**
 * <p>Dialog that allows a file in a Subversion repository to be selected.</p>
 * 
 * @author Richard Fearn
 */
public class SubversionDialog implements ActionListener, DocumentListener {

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

		textUrl.getDocument().addDocumentListener(this);
		textUsername.getDocument().addDocumentListener(this);
		textPassword.getDocument().addDocumentListener(this);
		textFilePath.getDocument().addDocumentListener(this);
		
		buttonOk.addActionListener(this);
		buttonCancel.addActionListener(this);
		
		((JDialog) dialog).pack();
	}

	public void actionPerformed(ActionEvent e) {
		// Handle click on the OK button
		if (e.getSource() == buttonOk) {
			url = textUrl.getText().trim();
			username = textUsername.getText().trim();
			password = new String(textPassword.getPassword());
			filePath = textFilePath.getText().trim();
			((JDialog) dialog).dispose();
		}
		
		// Handle click on the Cancel button
		else if (e.getSource() == buttonCancel) {
			((JDialog) dialog).dispose();
		}
	}

	public void changedUpdate(DocumentEvent e) {
		setOkButtonState();
	}

	public void insertUpdate(DocumentEvent e) {
		setOkButtonState();
	}

	public void removeUpdate(DocumentEvent e) {
		setOkButtonState();
	}

	// Update enabled state of OK button
	private void setOkButtonState() {
		boolean gotUrl = textUrl.getText().trim().length() > 0;
		boolean gotFilePath = textFilePath.getText().trim().length() > 0;
		buttonOk.setEnabled(gotUrl && gotFilePath);
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

	private static final long serialVersionUID = 1L;

	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}

}
