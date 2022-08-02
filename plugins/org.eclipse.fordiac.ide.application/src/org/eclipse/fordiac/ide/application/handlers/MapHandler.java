/*******************************************************************************
 * Copyright (c) 2022 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Michael Oberlehner, Daniel Lindhuber - initial API and implementation and/or initial documentation
 *   Alois Zoitl - fixed the system retrieval and did some clean-up
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.handlers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fordiac.ide.application.Messages;
import org.eclipse.fordiac.ide.model.commands.change.MapToCommand;
import org.eclipse.fordiac.ide.model.libraryElement.AutomationSystem;
import org.eclipse.fordiac.ide.model.libraryElement.Device;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.Resource;
import org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper;
import org.eclipse.fordiac.ide.ui.imageprovider.FordiacImage;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class MapHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		final List<FBNetworkElement> fbelements = HandlerHelper.getSelectedFBNElements(selection);
		final AutomationSystem system = getSystem(editor);

		if (system != null && !fbelements.isEmpty()) {
			final CompoundCommand mapCommands = openDialog(editor.getSite().getShell(), system, fbelements);
			if (!mapCommands.isEmpty()) {
				HandlerHelper.getCommandStack(editor).execute(mapCommands);
			}
		}

		return Status.OK_STATUS;
	}

	private static CompoundCommand openDialog(final Shell shell, final AutomationSystem system,
			final List<FBNetworkElement> fbelements) {
		final CompoundCommand mapCommands = new CompoundCommand();

		// save system for the auto select with one resource
		final ElementTreeSelectionDialog dialog = createDialog(shell, system);
		if (Window.OK == dialog.open()) {
			final Object firstResult = dialog.getFirstResult();
			if (firstResult instanceof Resource) {
				fbelements.forEach(fb -> {
					final Command cmd = MapToCommand.createMapToCommand(fb, (Resource) firstResult);
					if (cmd.canExecute()) {
						mapCommands.add(cmd);
					}
				});
			}
		}
		return mapCommands;
	}

	private static ElementTreeSelectionDialog createDialog(final Shell shell, final AutomationSystem system) {
		final ITreeContentProvider treeProvider = createTreeContentProvider();
		final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell, createTreeLabelProvider(),
				treeProvider) {
			@Override
			public int open() {
				final Object[] elements = treeProvider.getElements(system);
				// if only one resource exists, default select it
				if (elements.length == 1) {
					setResult(Arrays.asList(elements));
					return Window.OK;
				}
				return super.open();
			}
		};

		dialog.setInput(system);
		dialog.setTitle(Messages.MapToDialog_Title);
		dialog.setMessage(Messages.MapToDialog_Message);
		dialog.setHelpAvailable(false);
		dialog.setAllowMultiple(false);

		return dialog;
	}


	private static LabelProvider createTreeLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if(element instanceof Resource) {
					return ((Resource) element).getDevice().getName() + "." //$NON-NLS-1$
							+((Resource)element).getName();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {
				return FordiacImage.ICON_RESOURCE.getImage();
			}
		};
	}

	private static ITreeContentProvider createTreeContentProvider() {
		return new ITreeContentProvider() {

			@Override
			public boolean hasChildren(final Object element) {
				return false;
			}

			@Override
			public Object getParent(final Object element) {
				if (element instanceof EObject) {
					return ((EObject) element).eContainer();
				}
				return null;
			}

			@Override
			public Object[] getElements(final Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				if (parentElement instanceof AutomationSystem) {
					final AutomationSystem system = (AutomationSystem) parentElement;
					return system.getSystemConfiguration().getDevices().stream()
							.flatMap(dev -> dev.getResource().stream())
							.toArray(Resource[]::new);
				}

				if (parentElement instanceof Device) {
					final Device device = (Device) parentElement;
					return device.getResource().toArray();
				}
				return new Object[0];
			}
		};
	}

	@Override
	public void setEnabled(final Object evaluationContext) {
		final IEditorPart editor = (IEditorPart) HandlerUtil.getVariable(evaluationContext,
				ISources.ACTIVE_EDITOR_NAME);
		final FBNetwork network = editor.getAdapter(FBNetwork.class);
		setBaseEnabled(isValidSelection(evaluationContext) && isMapable(network) && hasDevices(network));
	}

	private static boolean isValidSelection(final Object evaluationContext) {
		final ISelection selection = (ISelection) HandlerUtil.getVariable(evaluationContext,
				ISources.ACTIVE_CURRENT_SELECTION_NAME);
		return !HandlerHelper.getSelectedFBNElements(selection).isEmpty();
	}

	private static boolean hasDevices(final FBNetwork network) {
		final AutomationSystem system = network.getAutomationSystem();
		return system != null && !system.getSystemConfiguration().getDevices().isEmpty();
	}

	public static boolean isMapable(final FBNetwork network) {
		return ((network != null) && !(network.isSubApplicationNetwork() || network.isCFBTypeNetwork()
				|| network.eContainer() instanceof Resource));
	}

	private static AutomationSystem getSystem(final IEditorPart editor) {
		final FBNetwork fbNetwork = HandlerHelper.getFBNetwork(editor);
		return fbNetwork.getAutomationSystem();
	}

}