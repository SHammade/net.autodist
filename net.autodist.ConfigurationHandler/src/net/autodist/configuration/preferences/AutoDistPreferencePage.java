package net.autodist.configuration.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;

import net.autodist.configuration.Activator;

import org.eclipse.ui.IWorkbench;

public class AutoDistPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public AutoDistPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("General Settings for AutoDist:");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {		
		addField(new FileFieldEditor(PreferenceConstants.THRIFT_PATH, 
				"&Full path of the thrift-binary:", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		
	}
	
}