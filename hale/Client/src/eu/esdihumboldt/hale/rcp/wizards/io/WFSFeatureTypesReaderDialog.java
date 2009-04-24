/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                  01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */
package eu.esdihumboldt.hale.rcp.wizards.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * FIXME Add Type description.
 * 
 * @author Thorsten Reitz 
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public class WFSFeatureTypesReaderDialog 
	extends Dialog {
	
	private final static Logger _log = Logger.getLogger(WFSFeatureTypesReaderDialog.class);

	URL result;
	
	public WFSFeatureTypesReaderDialog(Shell parent, int style) {
		super(parent, style);
	}
	
	public WFSFeatureTypesReaderDialog(Shell parent, String title) {
		super(parent, 0);
		this.setText(title);
	}

	/**
	 * @see org.eclipse.swt.widgets.Dialog
	 * @return any Object.
	 */
	public URL open () {
		Shell parent = super.getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(400, 400);
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
		shell.setText(super.getText());
		
		this.createControls(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return result;
	}

	private void createControls(Shell shell) {
		_log.debug("Creating Controls");
		
		// Create Fields for URL entry.
		Composite c = new Composite(shell, SWT.NONE);
		c.setLayout(new GridLayout());
		c.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		Group urlDefinitionArea = new Group(c, SWT.NONE);
		urlDefinitionArea.setText("Enter the URL of your WFS");
		urlDefinitionArea.setLayoutData( new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 2;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		urlDefinitionArea.setLayout(fileSelectionLayout);
		
		// Host + Port
		Label hostPortLabel = new Label(urlDefinitionArea, SWT.NONE);
		hostPortLabel.setText("Host + Port:");
		hostPortLabel.setToolTipText("Enter the hostname and the port of the " +
				"WFS you want to query here.");
		final Text hostPortText = new Text (urlDefinitionArea, SWT.BORDER | SWT.SINGLE);
		hostPortText.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		hostPortText.setText("http://staging-esdi-humboldt.igd.fraunhofer.de:8080");
		hostPortText.setEditable(true);
		hostPortText.addListener (SWT.FocusOut, new Listener () {
			public void handleEvent (Event e) {
				String string = hostPortText.getText();
				try {
					new URL(string);
				}
				catch (Exception ex) {
					_log.warn("Exception occured in parsing " + string 
							+ " to URL: " + ex.getMessage());
					e.doit = false;
					return;
				}
			}
		});
		
		// Application Path
		Label applicationPathLabel = new Label(urlDefinitionArea, SWT.NONE);
		applicationPathLabel.setText("WFS path:");
		applicationPathLabel.setToolTipText("Enter the path to the " +
				"WFS application you want to query here.");
		final Text applicationPathText = new Text (urlDefinitionArea, SWT.BORDER | SWT.SINGLE);
		applicationPathText.setEditable(true);
		applicationPathText.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		applicationPathText.setText("/geoserver/ows?service=WFS");
		
		// Protocol Version & Type
		Label protocolVersionLabel = new Label(urlDefinitionArea, SWT.NONE);
		protocolVersionLabel.setText("WFS Version and Protocol Type:");
		protocolVersionLabel.setToolTipText("Select one of the offered combinations of service version and protocol.");
		final Combo combo = new Combo (urlDefinitionArea, SWT.READ_ONLY);
		combo.setItems (new String [] {"1.1.0, HTTP GET", "1.0.0 XML POST", "1.1.0 XML POST"});
		combo.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		// Validation Group
		Group urlValidationArea = new Group(c, SWT.NONE);
		urlValidationArea.setText("Validate your WFS settings");
		urlValidationArea.setLayoutData( new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		GridLayout urlValidationLayout = new GridLayout();
		urlValidationLayout.numColumns = 1;
		urlValidationLayout.makeColumnsEqualWidth = false;
		urlValidationArea.setLayout(urlValidationLayout);
		
		Composite urlValidationStatusArea = new Composite(urlValidationArea, SWT.NONE);
		urlValidationArea.setLayoutData( new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		GridLayout urlValidationStatusLayout = new GridLayout();
		urlValidationStatusLayout.numColumns = 2;
		urlValidationStatusLayout.makeColumnsEqualWidth = false;
		urlValidationStatusArea.setLayout(urlValidationStatusLayout);
		
		Button testUrl = new Button(urlValidationStatusArea, SWT.PUSH);
		testUrl.setText("Validate settings");
		final Label currentStatusLabel = new Label(urlValidationStatusArea, SWT.NONE);
		currentStatusLabel.setText("No Validation performed yet.");
		
		final Text testResultText = new Text(urlValidationArea, SWT.BORDER);
		testResultText.setLayoutData( new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		
		testUrl.addListener(SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				URL url = null;
				try {
					url = GetCapabilititiesRetriever.buildURL(
							hostPortText.getText(),
							applicationPathText.getText(),
							combo.getSelectionIndex());
				} catch (Exception e1) {
					currentStatusLabel.setText("Validation FAILED.");
					testResultText.setText("Capabilities URL could not " +
							"be built: " + e1.getMessage());
				}
				if (url != null) {
					String result = null;
					try {
						result = GetCapabilititiesRetriever.readFromUrl(url);
						testResultText.setText(result);
					} catch (IOException e1) {
						currentStatusLabel.setText("Validation FAILED.");
						testResultText.setText("Capabilities document " +
								"could not be read: " + e1.getMessage());
					}
					
					String url_uri_string = null;
					try {
						url_uri_string = url.toURI().toString();
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
					}
					
					if (url_uri_string != null && GetCapabilititiesRetriever.validate(url_uri_string)) {
						int count = GetCapabilititiesRetriever.countOccurences(
								result, "FeatureType") / 2;
						currentStatusLabel.setText("Validation OK - " 
								+ count + " FeatureTypes!");
					}
					else {
						currentStatusLabel.setText("Validation FAILED.");
						testResultText.setText("Capabiltities were retrieved, " 
								+ "but validation failed.");
					}
				}
			}
		});
		
		
		// Cancel/Finish buttons
		Composite buttons = new Composite(c, SWT.BOTTOM);
		buttons.setLayoutData( new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		GridLayout buttonsLayout = new GridLayout();
		buttonsLayout.numColumns = 2;
		buttonsLayout.makeColumnsEqualWidth = true;
		buttons.setLayout(buttonsLayout);
		Button cancel = new Button(buttons, SWT.CANCEL);
		cancel.setAlignment(SWT.RIGHT_TO_LEFT);
		cancel.setText("Cancel");
		Button finish = new Button(buttons, SWT.SAVE);
		finish.setAlignment(SWT.RIGHT_TO_LEFT);
		finish.setText("Use this WFS");
		
		// finish drawing
		urlDefinitionArea.moveAbove(null);
	}

}
