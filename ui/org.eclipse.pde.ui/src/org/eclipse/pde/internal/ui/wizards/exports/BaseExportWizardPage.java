/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.exports;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;

public abstract class BaseExportWizardPage extends ExportWizardPage  {
	private static final String S_JAR_FORMAT = "exportUpdate"; //$NON-NLS-1$
	private static final String S_EXPORT_DIRECTORY = "exportDirectory";	 //$NON-NLS-1$
	private static final String S_EXPORT_SOURCE="exportSource"; //$NON-NLS-1$
	private static final String S_MULTI_PLATFORM="multiplatform"; //$NON-NLS-1$
	private static final String S_DESTINATION = "destination"; //$NON-NLS-1$
	private static final String S_ZIP_FILENAME = "zipFileName"; //$NON-NLS-1$
	private static final String S_SAVE_AS_ANT = "saveAsAnt"; //$NON-NLS-1$
	private static final String S_ANT_FILENAME = "antFileName"; //$NON-NLS-1$
		
	protected Button fDirectoryButton;
	protected Combo fDirectoryCombo;
	private Button fBrowseDirectory;
	
	protected Button fArchiveFileButton;
	protected Combo fArchiveCombo;
	private Button fBrowseFile;
	
	private Button fIncludeSource;

	private Button fMultiPlatform;

	private Combo fAntCombo;
	private Button fBrowseAnt;
	private Button fSaveAsAntButton;
	private String fZipExtension = ".zip"; //$NON-NLS-1$
	private Button fJarButton;

	
	public BaseExportWizardPage(String name) {
		super(name);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = getVerticalSpacing();
		container.setLayout(layout);
		
		createTopSection(container);
		createExportDestinationSection(container);
		createOptionsSection(container);
		
		Dialog.applyDialogFont(container);
		
		// load settings
		IDialogSettings settings = getDialogSettings();
		initializeTopSection();
		initializeExportOptions(settings);
		initializeDestinationSection(settings);
		pageChanged();
		hookListeners();
		setControl(container);
		hookHelpContext(container);
	}
    
    protected int getVerticalSpacing() {
        return 5;
    }
	
	protected abstract void createTopSection(Composite parent);
	
	protected abstract void initializeTopSection();
	
	private void createExportDestinationSection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(PDEUIMessages.ExportWizard_destination); 
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fArchiveFileButton = new Button(group, SWT.RADIO);
		fArchiveFileButton.setText(PDEUIMessages.ExportWizard_archive); 
		
		fArchiveCombo = new Combo(group, SWT.BORDER);
		fArchiveCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fBrowseFile = new Button(group, SWT.PUSH);
		fBrowseFile.setText(PDEUIMessages.ExportWizard_browse); 
		fBrowseFile.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(fBrowseFile);		

		fDirectoryButton = new Button(group, SWT.RADIO);
		fDirectoryButton.setText(PDEUIMessages.ExportWizard_directory); 

		fDirectoryCombo = new Combo(group, SWT.BORDER);
		fDirectoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fBrowseDirectory = new Button(group, SWT.PUSH);
		fBrowseDirectory.setText(PDEUIMessages.ExportWizard_browse); 
		fBrowseDirectory.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(fBrowseDirectory);
	}
	
	protected Composite createOptionsSection(Composite parent) {
		Group comp = new Group(parent, SWT.NONE);
		comp.setText(PDEUIMessages.ExportWizard_options); 
		comp.setLayout(new GridLayout(3, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
									
		fIncludeSource = new Button(comp, SWT.CHECK);
		fIncludeSource.setText(PDEUIMessages.ExportWizard_includeSource); 
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		fIncludeSource.setLayoutData(gd);
		
        if (addJARFormatSection()) {
    		fJarButton = new Button(comp, SWT.CHECK);
    		fJarButton.setText(getJarButtonText());
    		gd = new GridData();
    		gd.horizontalSpan = 3;
    		fJarButton.setLayoutData(gd);
    		fJarButton.addSelectionListener(new SelectionAdapter() {
    			public void widgetSelected(SelectionEvent e) {
    				getContainer().updateButtons();
    			}
    		});
        }
        
        if (addMultiplatformSection()) {
			fMultiPlatform = new Button(comp, SWT.CHECK);
			fMultiPlatform.setText(PDEUIMessages.ExportWizard_multi_platform);
			gd = new GridData();
			gd.horizontalSpan = 3;
			fMultiPlatform.setLayoutData(gd);
		}
        
		if (addAntSection())
            createAntSection(comp);
		return comp;
	}
	
	protected void createAntSection(Composite comp) {
		fSaveAsAntButton = new Button(comp, SWT.CHECK);
		fSaveAsAntButton.setText(PDEUIMessages.ExportWizard_antCheck); 
		
		fAntCombo = new Combo(comp, SWT.NONE);
		fAntCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fBrowseAnt = new Button(comp, SWT.PUSH);
		fBrowseAnt.setText(PDEUIMessages.ExportWizard_browse2); 
		fBrowseAnt.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(fBrowseAnt);		
	}
	
	protected abstract String getJarButtonText();
	
	protected void toggleDestinationGroup(boolean useDirectory) {
		fArchiveCombo.setEnabled(!useDirectory);
		fBrowseFile.setEnabled(!useDirectory);
		fDirectoryCombo.setEnabled(useDirectory);
		fBrowseDirectory.setEnabled(useDirectory);
	}
	
	protected void hookListeners() {
		fArchiveFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = fArchiveFileButton.getSelection();
				fArchiveCombo.setEnabled(enabled);
				fBrowseFile.setEnabled(enabled);
				fDirectoryCombo.setEnabled(!enabled);
				fBrowseDirectory.setEnabled(!enabled);
				pageChanged();
				pageUpdate(enabled);
			}}
		);
			
 		fBrowseFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chooseFile(fArchiveCombo, "*" + fZipExtension); //$NON-NLS-1$
			}
		});
		
		fArchiveCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});
		
		fArchiveCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				pageChanged();
			}
		});
		
		fDirectoryCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pageChanged();
			}
		});
		
		fDirectoryCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				pageChanged();
			}
		});
		
		fBrowseDirectory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chooseDestination();
			}
		});
		
        if (addAntSection()) {
            fSaveAsAntButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    fAntCombo.setEnabled(fSaveAsAntButton.getSelection());
                    fBrowseAnt.setEnabled(fSaveAsAntButton.getSelection());
                    pageChanged();
                }}
            );
            
     		fBrowseAnt.addSelectionListener(new SelectionAdapter() {
    			public void widgetSelected(SelectionEvent e) {
    				chooseFile(fAntCombo, "*.xml"); //$NON-NLS-1$
    			}
    		});
    		
    		fAntCombo.addSelectionListener(new SelectionAdapter() {
    			public void widgetSelected(SelectionEvent e) {
    				pageChanged();
    			}
    		});
    		
    		fAntCombo.addModifyListener(new ModifyListener() {
    			public void modifyText(ModifyEvent e) {
    				pageChanged();
    			}
    		});	
        }

        if (addMultiplatformSection()) {
			fMultiPlatform.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					pageChanged();
				}
			});
		}
	}



	private void chooseFile(Combo combo, String filter) {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		String path = fArchiveCombo.getText();
		if (path.trim().length() == 0)
			path = PDEPlugin.getWorkspace().getRoot().getLocation().toString();
		dialog.setFileName(path);
		dialog.setFilterExtensions(new String[] {filter});
		String res = dialog.open();
		if (res != null) {
			if (combo.indexOf(res) == -1)
				combo.add(res, 0);
			combo.setText(res);
		}
	}
	
	private void chooseDestination() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		String path = fDirectoryCombo.getText();
		if (path.trim().length() == 0)
			path = PDEPlugin.getWorkspace().getRoot().getLocation().toString();
		dialog.setFilterPath(path);
		dialog.setText(PDEUIMessages.ExportWizard_dialog_title); 
		dialog.setMessage(PDEUIMessages.ExportWizard_dialog_message); 
		String res = dialog.open();
		if (res != null) {
			if (fDirectoryCombo.indexOf(res) == -1)
				fDirectoryCombo.add(res, 0);
			fDirectoryCombo.setText(res);
		}
	}

	protected void pageUpdate(boolean archive) {
	}
	protected void pageChanged() {
		if (fJarButton != null) {
			fJarButton.setEnabled(isEnableJarButton());
		}
	}
	
	protected String validateBottomSections() {
		String message = null;
		if (isButtonSelected(fArchiveFileButton) && fArchiveCombo.getText().trim().length() == 0) {
			message = PDEUIMessages.ExportWizard_status_nofile; 
		}
		if (isButtonSelected(fDirectoryButton) && fDirectoryCombo.getText().trim().length() == 0) {
			message = PDEUIMessages.ExportWizard_status_nodirectory; 
		}
		if (isButtonSelected(fSaveAsAntButton) && fAntCombo.getText().trim().length() == 0) {
			message = PDEUIMessages.ExportWizard_status_noantfile; 
		}
		return message;
	}
	
	protected boolean isEnableJarButton(){
		return true;
	}

	private boolean isButtonSelected(Button button) {
		return button != null && !button.isDisposed() && button.getSelection();
	}

	protected void initializeExportOptions(IDialogSettings settings) {		
		fIncludeSource.setSelection(settings.getBoolean(S_EXPORT_SOURCE));
        if (fJarButton != null)
            fJarButton.setSelection(getInitialJarButtonSelection(settings));
        if (addAntSection()) {
    		fSaveAsAntButton.setSelection(settings.getBoolean(S_SAVE_AS_ANT));
    		initializeCombo(settings, S_ANT_FILENAME, fAntCombo);
    		fAntCombo.setEnabled(fSaveAsAntButton.getSelection());
    		fBrowseAnt.setEnabled(fSaveAsAntButton.getSelection());
        }
        if (addMultiplatformSection()) {
    		fMultiPlatform.setSelection(settings.getBoolean(S_MULTI_PLATFORM));
        }
	}
	
	protected boolean getInitialJarButtonSelection(IDialogSettings settings){
        return settings.getBoolean(S_JAR_FORMAT);
	}
	
	protected void initializeDestinationSection(IDialogSettings settings) {
		boolean useDirectory = settings.getBoolean(S_EXPORT_DIRECTORY);
		fDirectoryButton.setSelection(useDirectory);	
		fArchiveFileButton.setSelection(!useDirectory);
		toggleDestinationGroup(useDirectory);
		initializeCombo(settings, S_DESTINATION, fDirectoryCombo);
		initializeCombo(settings, S_ZIP_FILENAME, fArchiveCombo);
	}
	
	protected void initializeCombo(IDialogSettings settings, String key, Combo combo) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < 6; i++) {
			String curr = settings.get(key + String.valueOf(i));
			if (curr != null && !list.contains(curr)) {
				list.add(curr);
			}
		}
		String text = combo.getText();
		if (text.length() > 0)
			list.add(0, text);
		
		String[] items = (String[])list.toArray(new String[list.size()]);
		combo.setItems(items);
		if (items.length > 0)
			combo.setText(items[0]);
		else
			combo.setText(""); //$NON-NLS-1$
	}

	public void saveSettings() {
		IDialogSettings settings = getDialogSettings();	
        if (fJarButton != null)
            settings.put(S_JAR_FORMAT, fJarButton.getSelection());
        if (fMultiPlatform != null)
            settings.put(S_MULTI_PLATFORM, fMultiPlatform.getSelection());
        
		settings.put(S_EXPORT_DIRECTORY, fDirectoryButton.getSelection());		
		settings.put(S_EXPORT_SOURCE, fIncludeSource.getSelection());
		
        if (fSaveAsAntButton != null)
            settings.put(S_SAVE_AS_ANT, fSaveAsAntButton.getSelection());
		
		saveCombo(settings, S_DESTINATION, fDirectoryCombo);
		saveCombo(settings, S_ZIP_FILENAME, fArchiveCombo);
        if (fAntCombo != null)
            saveCombo(settings, S_ANT_FILENAME, fAntCombo);
	}
	
	protected void saveCombo(IDialogSettings settings, String key, Combo combo) {
		if (combo.getText().trim().length() > 0) {
			settings.put(key + String.valueOf(0), combo.getText().trim());
			String[] items = combo.getItems();
			int nEntries = Math.min(items.length, 5);
			for (int i = 0; i < nEntries; i++) {
				settings.put(key + String.valueOf(i + 1), items[i].trim());
			}
		}	
	}

	public boolean doExportSource() {
		return fIncludeSource.getSelection();
	}
	
	public boolean doExportToDirectory() {
		return fDirectoryButton.getSelection();
	}
	
	public boolean useJARFormat() {
		return fJarButton != null && fJarButton.isEnabled() && fJarButton.getSelection();
	}
	
    public boolean doMultiPlatform(){
    	return fMultiPlatform!=null && fMultiPlatform.getSelection();
    }
	public String getFileName() {
		if (fArchiveFileButton.getSelection()) {
			String path = fArchiveCombo.getText();
			if (path != null && path.length() > 0) {
				String fileName = new Path(path).lastSegment();
				if (!fileName.endsWith(fZipExtension)) { 
					fileName += fZipExtension;
				}
				return fileName;
			}
		}
		return null;
	}
	
	public String getDestination() {
		if (fArchiveFileButton.getSelection()) {
			String path = fArchiveCombo.getText();
			if (path != null && path.length() > 0) {
				path = new Path(path).removeLastSegments(1).toOSString();
				return new File(path).getAbsolutePath();
			}
			return ""; //$NON-NLS-1$
		}
		
		if (fDirectoryCombo == null || fDirectoryCombo.isDisposed())
			return ""; //$NON-NLS-1$
		
		File dir = new File(fDirectoryCombo.getText().trim());			
		return dir.getAbsolutePath();
	}
	
	protected abstract void hookHelpContext(Control control);
		
	public boolean doGenerateAntFile() {
		return fSaveAsAntButton != null && fSaveAsAntButton.getSelection();
	}
	
	public String getAntBuildFileName() {
		return fAntCombo != null ? fAntCombo.getText().trim() : ""; //$NON-NLS-1$
	}
	
	public IWizardPage getNextPage() {
		IWizardPage crossPlatformPage = getWizard().getPage("environment"); //$NON-NLS-1$
		if (crossPlatformPage != null && doMultiPlatform()) {
			return crossPlatformPage;

		}
		IWizardPage advancedPage = getWizard().getPage("feature-sign"); //$NON-NLS-1$
		if (advancedPage == null)
			advancedPage = getWizard().getPage("plugin-sign"); //$NON-NLS-1$
		if (advancedPage != null && isEnableJarButton() && useJARFormat()) {
			return advancedPage;
		}
		
		return null;
	}
    
    protected boolean addAntSection() {
        return true;
    }
    
    protected boolean addJARFormatSection() {
        return true;
    }

    protected boolean addMultiplatformSection() {
		FeatureModelManager manager = PDECore.getDefault()
				.getFeatureModelManager();
		IFeatureModel model = manager
				.findFeatureModel("org.eclipse.platform.launchers"); //$NON-NLS-1$
		return model != null;
	}
    
    protected String getDestinationText() {
    	if (isDirectoryDest())
    		return fDirectoryCombo.getText();
    	return fArchiveCombo.getText();
    }
    
    protected boolean isDirectoryDest() {
    	return fDirectoryButton.getSelection();
    }
}
