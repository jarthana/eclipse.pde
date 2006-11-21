/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.pde.internal.ui.editor.cheatsheet.comp;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.internal.core.icheatsheet.comp.ICompCSConstants;
import org.eclipse.pde.internal.core.icheatsheet.comp.ICompCSModel;
import org.eclipse.pde.internal.core.icheatsheet.comp.ICompCSObject;
import org.eclipse.pde.internal.core.util.PDETextHelper;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * CompCSPage
 *
 */
public class CompCSPage extends PDEFormPage implements IModelChangedListener {

	public static final String PAGE_ID = "compCSPage"; //$NON-NLS-1$

	private CompCSBlock fBlock;
	
	/**
	 * @param editor
	 */
	public CompCSPage(FormEditor editor) {
		super(editor, PAGE_ID, PDEUIMessages.SimpleCSPage_0);

		fBlock = new CompCSBlock(this);
	}

	// TODO: MP: LOW: CompCS: Clean-up and reuse externalized strings
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		// Bug: Two veritical scrollbars appear when resizing the editor
		// vertically
		// Note: Scrolled form #1 created here
		ScrolledForm form = managedForm.getForm();
		// Set page title
		ICompCSModel model = (ICompCSModel)getModel();
		// TODO: MP: LOW: CompCS:  Create formatted error page
		if ((model == null) || 
				(model.isLoaded() == false)) {
			throw new RuntimeException(PDEUIMessages.SimpleCSPage_1);
		}
		
		String title = PDETextHelper.translateReadText(model.getCompCS()
				.getFieldName());
		if (title.length() > 0) {
			form.setText(title);
		} else {
			form.setText(PDEUIMessages.SimpleCSPage_0);
		}
		// Create the masters details block
		// Note: Scrolled form #2 created here
		fBlock.createContent(managedForm);
		// Force the selection in the masters tree section to load the 
		// proper details section
		fBlock.getMastersSection().fireSelection();
		// Register this page to be informed of model change events
		model.addModelChangedListener(this);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormPage#dispose()
	 */
	public void dispose() {
		
		ICompCSModel compCSModel = (ICompCSModel)getModel();
		if (compCSModel != null) {
			compCSModel.removeModelChangedListener(this);
		}
		super.dispose();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.core.IModelChangedListener#modelChanged(org.eclipse.pde.core.IModelChangedEvent)
	 */
	public void modelChanged(IModelChangedEvent event) {
		
		if (event.getChangeType() == IModelChangedEvent.CHANGE) {
			Object[] objects = event.getChangedObjects();
			ICompCSObject object = (ICompCSObject) objects[0];
			if (object == null) {
				// Ignore
			} else if (object.getType() == ICompCSConstants.TYPE_COMPOSITE_CHEATSHEET) {
				String changeProperty = event.getChangedProperty();
				if ((changeProperty != null)
						&& changeProperty
								.equals(ICompCSConstants.ATTRIBUTE_NAME)) {
					// Has to be a String if the property is a title
					// Update the form page title
					getManagedForm().getForm().setText(
							PDETextHelper.translateReadText((String) event
									.getNewValue()));
				}
			}
		}
		// Inform the block
		fBlock.modelChanged(event);
	}

	/**
	 * @return
	 */
	public ISelection getSelection() {
		return fBlock.getSelection();
	}
	
}
