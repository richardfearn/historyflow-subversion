package uk.co.richardfearn.historyflow.subversion;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import cue.historyflow.api.ContentType;
import cue.historyflow.api.DocumentProviderException;
import cue.historyflow.api.IDocumentHistory;
import cue.historyflow.api.IHistoryProvider;
import cue.historyflow.api.IPreferences;
import cue.historyflow.api.IProgressDisplay;
import cue.historyflow.api.IVersion;
import cue.historyflow.api.impl.DocumentHistoryImpl;
import cue.historyflow.api.impl.VersionImpl;

/**
 * <p>A history provider for History Flow that allows files in a Subversion
 * repository to be analysed.</p>
 * 
 * @author Richard Fearn
 */
public class SubversionHistoryProvider implements IHistoryProvider {

	private static final Map NULL_MAP = null;

	private static final Preferences PREFS = Preferences.userNodeForPackage(SubversionHistoryProvider.class);

	private static final String PREF_URL = "url";

	private static final String PREF_USERNAME = "username";

	private static final String PREF_FILEPATH = "filepath";

	private static final String DEFAULT_URL = "svn://localhost/path/to/svnroot";

	private static final String DEFAULT_USERNAME = "username";

	private static final String DEFAULT_FILEPATH = "path/to/file";

	// Text shown for this plugin on the File menu
	public String getActionText() {
		return "Subversion file...";
	}

	// Returns the revisions of the file
	public IDocumentHistory getHistory(IPreferences prefs, IProgressDisplay progressDisplay) throws DocumentProviderException {
		// Read the preferences
		String url = PREFS.get(PREF_URL, DEFAULT_URL);
		String username = PREFS.get(PREF_USERNAME, DEFAULT_USERNAME);
		String filePath = PREFS.get(PREF_FILEPATH, DEFAULT_FILEPATH);

		// Display the dialog
		SubversionDialog d;
		try {
			d = new SubversionDialog(progressDisplay.getOwningFrame(), url, username, filePath);
			d.setVisible(true);
		} catch (Exception e) {
			throw new DocumentProviderException("Unable to display file selection dialog box", e);
		}

		// Code blocks here until dialog is closed

		// Read the values (if any) that were entered into the dialog
		url = d.getUrl();
		username = d.getUsername();
		String password = d.getPassword();
		filePath = d.getFilePath();

		// If the Cancel button was pressed, go no further
		if (url == null) {
			return null;
		}

		// Save the new preferences
		PREFS.put(PREF_URL, url);
		PREFS.put(PREF_USERNAME, username);
		PREFS.put(PREF_FILEPATH, filePath);
		try {
			PREFS.flush();
		} catch (BackingStoreException e) {
			throw new DocumentProviderException("Couldn't save preferences", e);
		}

		// Read the revisions
		IVersion[] revisions = null;
		try {
			revisions = getRevisions(url, username, password, filePath, progressDisplay);
		} catch (Exception e) {
			throw new DocumentProviderException("Couldn't read revisions", e);
		}

		// Create the document history. Assume that the file being analysed is
		// plain text
		IDocumentHistory docHistory = new DocumentHistoryImpl(filePath, url, revisions, ContentType.PLAINTEXT);
		return docHistory;
	}

	/**
	 * <p>Retrieves the revisions of a file in a Subversion repository.</p>
	 * 
	 * @param url the Subversion repository URL
	 * @param username the Subversion username
	 * @param password the Subversion password
	 * @param filePath the path to the file
	 * @param progressDisplay the History Flow progress window
	 * 
	 * @return an array of {@link IVersion} objects
	 * 
	 * @throws Exception
	 */
	private IVersion[] getRevisions(String url, String username, String password, String filePath, IProgressDisplay progressDisplay) throws Exception {
		// List of revisions
		List<IVersion> versions = new Vector<IVersion>();

		// Set up the repository factory
		SVNRepositoryFactoryImpl.setup();

		// Obtain the repository
		progressDisplay.setMessage("Obtaining repository");
		SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));

		// Create an authentication manager
		progressDisplay.setMessage("Creating authentication manager");
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
		repository.setAuthenticationManager(authManager);

		// Find the latest revision number
		long latestRevision = repository.getLatestRevision();
		progressDisplay.setMessage("Latest revision is " + latestRevision);

		// Get all the revisions for the file
		Collection revisions = repository.getFileRevisions(filePath, null, 0, latestRevision);
		progressDisplay.setMessage("File has " + revisions.size());

		// The revision objects don't specify the times when the file was
		// changed, because Subversion revisions apply to the repository as a
		// whole, not to individual files. We therefore need to create a map
		// showing when each revision was committed
		progressDisplay.setMessage("Determining date of each revision");
		Map<Long, Date> revisionDates = new HashMap<Long, Date>();
		Collection logEntries = repository.log(new String[] { filePath }, null, 0, latestRevision, false, false);
		for (Object o : logEntries) {
			SVNLogEntry logEntry = (SVNLogEntry) o;
			revisionDates.put(logEntry.getRevision(), logEntry.getDate());
		}

		// Get the list of revision numbers for the file
		long[] revisionNumbers = getRevisionList(revisions);

		// Read each revision. The file may have had different names in the
		// past, so we have to determine what its name was for each revision
		// in which it was changed
		Map entries = repository.getLocations(filePath, NULL_MAP, latestRevision, revisionNumbers);
		for (long revision : revisionNumbers) {
			progressDisplay.setMessage("Retrieving revision " + revision);

			// Find what the file's location was in this revision
			SVNLocationEntry locEntry = (SVNLocationEntry) entries.get(revision);

			// Read the revision
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			repository.getFile(locEntry.getPath(), revision, null, baos);
			baos.close();

			// Create the Version object for this revision
			VersionImpl dv = new VersionImpl();
			dv.setName("revision " + revision);
			dv.setComment("revision " + revision);
			dv.setDate(revisionDates.get(revision));
			dv.setContent(new String(baos.toByteArray()));
			dv.setAuthor("rich");
			versions.add(dv);
		}

		return versions.toArray(new IVersion[] {});
	}

	/**
	 * <p>Extracts the revision numbers from a {@link Collection} of
	 * revisions.</p>
	 * 
	 * @param revisions the revisions
	 * 
	 * @return the revision numbers
	 */
	private static long[] getRevisionList(Collection revisions) {
		long[] revisionNumbers = new long[revisions.size()];
		int i = -1;
		for (Object o : revisions) {
			SVNFileRevision revision = (SVNFileRevision) o;
			i++;
			revisionNumbers[i] = revision.getRevision();
		}
		return revisionNumbers;
	}

}
