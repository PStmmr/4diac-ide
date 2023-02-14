/*******************************************************************************
 * Copyright (c) 2023 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Dunja Životin - extracted out of the now AbstractSelectionButton class,
 *   took project finding from the search plug-in.
 *******************************************************************************/
package org.eclipse.fordiac.ide.model.ui.nat;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fordiac.ide.model.data.DataType;
import org.eclipse.fordiac.ide.model.data.StructuredType;
import org.eclipse.fordiac.ide.model.libraryElement.AutomationSystem;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.typelibrary.TypeLibrary;
import org.eclipse.fordiac.ide.model.typelibrary.TypeLibraryManager;
import org.eclipse.fordiac.ide.model.ui.Messages;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.part.FileEditorInput;

public class TypeSelectionTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		final TypeNode elementaries = new TypeNode(Messages.DataTypeDropdown_Elementary_Types);
		final TypeNode structures = new TypeNode(Messages.DataTypeDropdown_STRUCT_Types);

		if (inputElement instanceof HashMap<?,?>) {
			
			final HashMap<String, List<String>> map = (HashMap<String, List<String>>) inputElement;
			
			TypeLibrary typeLib = TypeLibraryManager.INSTANCE.getTypeLibrary(getCurrentProject());
			List<DataType> dataTypes = typeLib.getDataTypeLibrary().getDataTypesSorted();
			List<StructuredType> structuredTypes = typeLib.getDataTypeLibrary().getStructuredTypesSorted();
			
			
			map.forEach((key, val) -> {
				val.forEach(value -> {
					if (key.equals(Messages.DataTypeDropdown_Elementary_Types)) {
						Optional<DataType> type = dataTypes.stream()
								.filter(dataType -> dataType.getName().equals(value))
								.findFirst();
						final TypeNode newNode = new TypeNode(type.get().getName(), type.get());
						elementaries.addChild(newNode);
					} else if (key.equals(Messages.DataTypeDropdown_STRUCT_Types)) {
						Optional<StructuredType> type = structuredTypes.stream()
								.filter(structType -> structType.getName().equals(value))
								.findFirst();
						if (type.isPresent()) {
							if(null != type.get().getTypeEntry()) {
								final String parentPath = type.get().getTypeEntry().getFile().getParent()
										.getProjectRelativePath().toOSString();
								createSubdirectories(structures, type.get(), parentPath);
							} else {
								final TypeNode runtimeNode = new TypeNode(type.get().getName(), type.get());
								runtimeNode.setParent(structures);
								structures.addChild(runtimeNode);
							}
						}
					}
				});
			});
		}

		if (elementaries.getChildren().isEmpty()) {
			return structures.getChildren().toArray();
		} else if (structures.getChildren().isEmpty()) {
			return elementaries.getChildren().toArray();
		}

		return new TypeNode[] { elementaries, structures };
	}

	private void createSubdirectories(TypeNode node, final StructuredType structuredType,
			final String parentPath) {
		// split up the path in subdirectories
		final String[] paths = parentPath.split("\\\\"); //$NON-NLS-1$

		// start after Type Library
		for (int i = 1; i < paths.length; i++) {
			final TypeNode current = new TypeNode(paths[i]);
			// check if we already have a parent node
			final int index = node.getChildren().indexOf(current);
			if (-1 != index) {
				node = node.getChildren().get(index);
			} else {
				current.setParent(node);
				node.addChild(current);
				node = current;
			}
		}
		final TypeNode actualType = new TypeNode(structuredType.getName(), structuredType);
		actualType.setParent(node);
		node.addChild(actualType);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TypeNode) {
			return ((TypeNode) parentElement).getChildren().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof TypeNode) {
			return ((TypeNode) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof TypeNode) {
			return !((TypeNode) element).getChildren().isEmpty();
		}
		return false;
	}
	
	private static IProject getCurrentProject() {
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProject project = getProjectFromActiveEditor(page);
		if (project == null) {
			project = getProjectFromProjectExplorerSelction(page);
		}
		return project;
	}

	private static IProject getProjectFromActiveEditor(final IWorkbenchPage page) {
		final IEditorPart openEditor = page.getActiveEditor();
		if (openEditor != null) {
			final IEditorInput editorInput = openEditor.getEditorInput();
			if (editorInput instanceof FileEditorInput) {
				return ((FileEditorInput) editorInput).getFile().getProject();
			}
		}
		return null;
	}

	private static IProject getProjectFromProjectExplorerSelction(final IWorkbenchPage page) {
		final IViewPart view = page.findView("org.eclipse.fordiac.ide.systemmanagement.ui.systemexplorer"); //$NON-NLS-1$

		if (view instanceof CommonNavigator) {
			final ISelection selection = ((CommonNavigator) view).getCommonViewer().getSelection();
			if (selection instanceof StructuredSelection && !((StructuredSelection) selection).isEmpty()) {
				Object selElement = ((StructuredSelection) selection).getFirstElement();
				if (selElement instanceof EObject) {
					selElement = getFileForModel((EObject) selElement);
				}
				if (selElement instanceof IResource) {
					return ((IResource) selElement).getProject();
				}
			}
		}

		return null;
	}
	
	private static IFile getFileForModel(final EObject sel) {
		final EObject root = EcoreUtil.getRootContainer(sel);
		if (root instanceof AutomationSystem) {
			return ((AutomationSystem) root).getSystemFile();
		} else if (root instanceof FBType) {
			return ((FBType) root).getTypeEntry().getFile();
		}
		return null;
	}

}