/*******************************************************************************
 * Copyright (c) 2024 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.debug.ui.view.editparts;

import org.eclipse.fordiac.ide.debug.EvaluatorDebugTarget;
import org.eclipse.fordiac.ide.model.eval.variable.Variable;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;

/*
 *  meta model class for marking inner values for the editpart factory
 */
public class InnerValueEntity extends InterfaceValueEntity {

	public InnerValueEntity(final IInterfaceElement ie, final Variable<?> variable,
			final EvaluatorDebugTarget debugTarget) {
		super(ie, variable, debugTarget);
		// nothing special to be done
	}

}
