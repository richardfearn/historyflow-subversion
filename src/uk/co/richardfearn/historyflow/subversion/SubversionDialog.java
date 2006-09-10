package uk.co.richardfearn.historyflow.subversion;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * <p>Dialog that allows a file in a Subversion repository to be selected.</p>
 * 
 * @author Richard Fearn
 */
public class SubversionDialog extends JDialog implements ActionListener, DocumentListener {

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

	public SubversionDialog(JFrame parent, String defaultUrl, String defaultUsername, String defaultFilePath) throws Exception {
		super(parent, "Subversion file", true);

		setLayout(new GridLayout(5, 2));

		// Create text fields and labels
		JLabel labelUrl = new JLabel("URL:");
		textUrl = new JTextField(defaultUrl, 20);
		JLabel labelUsername = new JLabel("Username:");
		textUsername = new JTextField(defaultUsername, 20);
		JLabel labelPassword = new JLabel("Password:");
		textPassword = new JPasswordField(20);
		JLabel labelFilePath = new JLabel("Path to file:");
		textFilePath = new JTextField(defaultFilePath, 20);

		// Create buttons
		buttonOk = new JButton("OK");
		buttonOk.setEnabled(false);
		buttonCancel = new JButton("Cancel");

		// Add text fields and labels
		getContentPane().add(labelUrl);
		getContentPane().add(textUrl);
		getContentPane().add(labelUsername);
		getContentPane().add(textUsername);
		getContentPane().add(labelPassword);
		getContentPane().add(textPassword);
		getContentPane().add(labelFilePath);
		getContentPane().add(textFilePath);

		// Add buttons
		getContentPane().add(buttonOk);
		getContentPane().add(buttonCancel);

		// Set up listeners on text fields and buttons
		textUrl.getDocument().addDocumentListener(this);
		textUsername.getDocument().addDocumentListener(this);
		textPassword.getDocument().addDocumentListener(this);
		textFilePath.getDocument().addDocumentListener(this);
		buttonOk.addActionListener(this);
		buttonCancel.addActionListener(this);

		// Set size and position
		setSize(400, 200);
		setLocationRelativeTo(parent);
	}

	public void actionPerformed(ActionEvent e) {
		// Handle click on the OK button
		if (e.getSource() == buttonOk) {
			url = textUrl.getText().trim();
			username = textUsername.getText().trim();
			password = new String(textPassword.getPassword());
			filePath = textFilePath.getText().trim();
			dispose();
		}
		
		// Handle click on the Cancel button
		else if (e.getSource() == buttonCancel) {
			dispose();
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
		boolean gotUsername = textUsername.getText().trim().length() > 0;
		boolean gotPassword = textPassword.getPassword().length > 0;
		boolean gotFilePath = textFilePath.getText().trim().length() > 0;
		buttonOk.setEnabled(gotUrl && gotUsername && gotPassword && gotFilePath);
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

}
