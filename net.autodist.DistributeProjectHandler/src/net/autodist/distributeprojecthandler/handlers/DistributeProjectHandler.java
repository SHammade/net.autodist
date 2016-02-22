package net.autodist.distributeprojecthandler.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.*;

import net.autodist.configuration.Activator;
import net.autodist.configuration.preferences.AutoDistPreferencePage;
import net.autodist.configuration.preferences.PreferenceConstants;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
/**
 * Handler for the command net.autodist.AnnotationIdentify.commands.identifyAnnotationCommand
 * Calls the other AutoDist components 
 * @author Hammade, Retzlaff
 */
public class DistributeProjectHandler extends AbstractHandler {
	
	private static final String DEFAULT_PACKAGE_NAME = "net.autodist";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = null;		
	    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	        Object firstElement = selection.getFirstElement();
	        if (firstElement instanceof IAdaptable)
	        {
	            project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);	            
	        }
	    }
	    
		String packageName = DEFAULT_PACKAGE_NAME;
		PackageNameDialog dialog = new PackageNameDialog(window.getShell(), getPackageNames(project));
		dialog.create();
		if (dialog.open() == Window.OK) {
			packageName = dialog.getPackageName();
		}

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String rawThriftPath = store.getString(PreferenceConstants.THRIFT_PATH);
		if (rawThriftPath == null || rawThriftPath.isEmpty())
		{
			MessageDialog.openWarning(window.getShell(), "Settings for AutoDist not set!", "Please set the settings for AutoDist in the following Dialog!"); 
			
			IPreferencePage page = new AutoDistPreferencePage();
			PreferenceManager mgr = new PreferenceManager();
			IPreferenceNode node = new PreferenceNode("1", page);
			mgr.addToRoot(node);
			PreferenceDialog prefDialog = new PreferenceDialog(window.getShell(), mgr);
			prefDialog.create();
			prefDialog.setMessage(page.getTitle());
			prefDialog.setBlockOnOpen(true);
			prefDialog.open();
			if(prefDialog.getReturnCode() == Window.CANCEL)
			{
				MessageDialog.openError(window.getShell(), "Settings for AutoDist not set!", "Settings for AutoDist not set, Distribution canceled!"); 
				return null;
			}
			rawThriftPath = store.getString(PreferenceConstants.THRIFT_PATH);
		}
		
		if (project == null)
			return null;			
		DistributeProject distributeProject = new DistributeProject(project, packageName, rawThriftPath);
		DistributeProjectJob job = new DistributeProjectJob(distributeProject);
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();	
		return null;
	}
	
	private ArrayList<String> getPackageNames(IProject project) {
		ArrayList<String> packageNames = new ArrayList<>();
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IFolder sourceFolder = javaProject.getProject().getFolder("src");
			if (!sourceFolder.exists())
				throw new IllegalArgumentException("no source folder found!");
			IJavaElement[] fragments = javaProject.getPackageFragmentRoot(sourceFolder).getChildren();
			for (int j = 0; j < fragments.length; j++) {
				IPackageFragment fragment = (IPackageFragment) fragments[j];
				String name = fragment.getElementName();
				if (name.isEmpty())
					continue;
				packageNames.add(name);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		int indexOfEmptyEntry = packageNames.indexOf(new String(""));
		if (indexOfEmptyEntry != -1)
			packageNames.remove(indexOfEmptyEntry);

		if (packageNames.size() == 0)
			packageNames.add(DEFAULT_PACKAGE_NAME);

		return packageNames;
	}
}
