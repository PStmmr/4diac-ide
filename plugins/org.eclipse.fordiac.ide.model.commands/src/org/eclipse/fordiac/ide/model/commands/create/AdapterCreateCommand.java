/*******************************************************************************
 * Copyright (c) 2012 - 2017 TU Wien ACIN, Profactor GmbH, fortiss GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl, Gerhard Ebenhofer, Monika Wenger
 *       - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.model.commands.create;

import org.eclipse.fordiac.ide.model.libraryElement.AdapterDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterFB;
import org.eclipse.fordiac.ide.model.libraryElement.CompositeFBType;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementFactory;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;

public class AdapterCreateCommand extends FBCreateCommand {

	public AdapterCreateCommand(final int x, final int y, final AdapterDeclaration adapterDecl, final FBType parent) {
		super(getFBNetwork(parent), LibraryElementFactory.eINSTANCE.createAdapterFB(), x, y);
		getAdapterFB().setTypeEntry(adapterDecl.getType().getTypeEntry());
		getAdapterFB().setAdapterDecl(adapterDecl);
		adapterDecl.setAdapterFB(getAdapterFB());
	}

	private AdapterFB getAdapterFB() {
		return (AdapterFB) getElement();
	}

	@Override
	protected InterfaceList getTypeInterfaceList() {
		return getAdapterFB().getType().getInterfaceList();
	}

	@Override
	protected void checkName() {
		// for adapters we already have a correct name and do not want to change that. Also for basics, simples, and
		// SIFBs we are not in an fbnetwork and name checking wouldn't even work.
	}

	private static FBNetwork getFBNetwork(final FBType parent) {
		if (parent instanceof CompositeFBType && !(parent instanceof SubAppType)) {
			return ((CompositeFBType) parent).getFBNetwork();
		}
		return null;
	}

}
