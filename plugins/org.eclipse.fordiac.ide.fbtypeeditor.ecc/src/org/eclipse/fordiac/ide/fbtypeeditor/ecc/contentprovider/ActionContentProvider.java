/*******************************************************************************
 * Copyright (c) 2015 - 2017 fortiss GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl, Monika Wenger
 *     - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.fbtypeeditor.ecc.contentprovider;

import org.eclipse.fordiac.ide.model.libraryElement.BasicFBType;
import org.eclipse.fordiac.ide.model.libraryElement.ECAction;
import org.eclipse.fordiac.ide.model.libraryElement.ECState;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class ActionContentProvider implements ITreeContentProvider {
	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ECState) {
			return ((ECState) inputElement).getECAction().toArray();
		}
		if (inputElement instanceof ECAction) {
			return ((ECAction) inputElement).getECState().getECC().getBasicFBType().getAlgorithm().toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof ECState && null != ((ECState) parentElement).getECAction()) {
			return ((ECState) parentElement).getECAction().toArray();
		}
		if (parentElement instanceof BasicFBType) {
			return ((BasicFBType) parentElement).getAlgorithm().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof ECState) {
			return ((ECState) element).getECC();
		}
		if ((element instanceof ECAction) && (null != ((ECAction) element).getECState())) {
			return ((ECAction) element).getECState().getECC().getBasicFBType();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof ECState) {
			return !((ECState) element).getECAction().isEmpty();
		}
		if (element instanceof ECAction) {
			return !((ECAction) element).getECState().getECC().getBasicFBType().getAlgorithm().isEmpty();
		}
		return false;
	}
}
