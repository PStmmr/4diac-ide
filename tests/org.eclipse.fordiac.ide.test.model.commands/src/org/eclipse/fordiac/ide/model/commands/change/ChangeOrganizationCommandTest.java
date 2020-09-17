/*******************************************************************************
 * Copyright (c) 2020 Johannes Kepler University
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Ernst Blecha
 *     - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.model.commands.change;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.fordiac.ide.model.commands.testinfra.VersionInfoTestBase;
import org.junit.jupiter.params.provider.Arguments;

//see org.eclipse.fordiac.ide.util.ColorHelperTest.java for information on implementing tests

public class ChangeOrganizationCommandTest extends VersionInfoTestBase {

	private static final String NEW_ORGANIZATION = "new org"; //$NON-NLS-1$

	private static State executeCommand(State state) {
		state.setCommand(new ChangeOrganizationCommand(state.getVersionInfo(), NEW_ORGANIZATION));//
		assumeNotNull(state.getCommand());
		assumeTrue(state.getCommand().canExecute());
		state.getCommand().execute();
		return state;
	}

	private static void verifyState(State state, State oldState, TestFunction t) {
		t.test(state.getVersionInfo().getOrganization().equals(NEW_ORGANIZATION));
		t.test(state.getVersionInfo().getDate().equals(oldState.getVersionInfo().getDate()));
		t.test(state.getVersionInfo().getRemarks().equals(oldState.getVersionInfo().getRemarks()));
		t.test(state.getVersionInfo().getAuthor().equals(oldState.getVersionInfo().getAuthor()));
		t.test(state.getVersionInfo().getVersion().equals(oldState.getVersionInfo().getVersion()));
	}

	private static State executeCommandToNull(State state) {
		state.setCommand(new ChangeOrganizationCommand(state.getVersionInfo(), null));
		assumeNotNull(state.getCommand());
		assumeTrue(state.getCommand().canExecute());
		state.getCommand().execute();
		return state;
	}

	private static void verifyStateNull(State state, State oldState, TestFunction t) {
		t.test(state.getVersionInfo().getOrganization().equals(EMPTY));
		t.test(state.getVersionInfo().getDate().equals(oldState.getVersionInfo().getDate()));
		t.test(state.getVersionInfo().getRemarks().equals(oldState.getVersionInfo().getRemarks()));
		t.test(state.getVersionInfo().getAuthor().equals(oldState.getVersionInfo().getAuthor()));
		t.test(state.getVersionInfo().getVersion().equals(oldState.getVersionInfo().getVersion()));
	}

	// parameter creation function
	public static Collection<Arguments> data() {
		final List<ExecutionDescription<?>> executionDescriptions = List.of( //
				new ExecutionDescription<>("Change Organization", // //$NON-NLS-1$
						ChangeOrganizationCommandTest::executeCommand, //
						ChangeOrganizationCommandTest::verifyState //
				), //
				new ExecutionDescription<>("Change Organization to null", // //$NON-NLS-1$
						ChangeOrganizationCommandTest::executeCommandToNull, //
						ChangeOrganizationCommandTest::verifyStateNull //
				) //
		);

		return createCommands(executionDescriptions);
	}

}
