/*******************************************************************************
 * Copyright (c) 2022 Martin Erich Jobst, Patrick Aigner
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 *   Patrick Aigner - adapted for Lua Code generation
 *******************************************************************************/
package org.eclipse.fordiac.ide.export.forte_lua.st

import java.util.Map
import org.eclipse.fordiac.ide.export.ExportException
import org.eclipse.fordiac.ide.model.libraryElement.BaseFBType
import org.eclipse.fordiac.ide.structuredtextalgorithm.stalgorithm.STAlgorithm
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static extension org.eclipse.emf.ecore.util.EcoreUtil.getRootContainer
import static extension org.eclipse.fordiac.ide.structuredtextalgorithm.util.StructuredTextParseUtil.*

@FinalFieldsConstructor
class STAlgorithmSupport extends StructuredTextSupport {
	final org.eclipse.fordiac.ide.model.libraryElement.STAlgorithm algorithm

	STAlgorithm parseResult

	override prepare() {
		if (parseResult === null && errors.empty) {
			parseResult = algorithm.parse(errors, warnings, infos)
		}
		return parseResult !== null
	}

	override generate(Map<?, ?> options) throws ExportException {
		prepare()
		parseResult?.generateStructuredTextAlgorithm
	}

	def private CharSequence generateStructuredTextAlgorithm(STAlgorithm alg) {
		if (algorithm.rootContainer instanceof BaseFBType) {
			val container = algorithm.rootContainer as BaseFBType
			return '''
				local ENV = {}
				«container.interfaceList.generateFBVariablePrefix»
				«container.internalVars.generateInternalVariablePrefix»
				«alg.body.varTempDeclarations.generateLocalVariables»
				
				«alg.body.statements.generateStatementList»
				«container.internalVars.generateInternalVariableSuffix»
				«container.interfaceList.generateFBVariableSuffix»
			'''
		} else {
			return '''
				local ENV = {}
				«alg.body.varTempDeclarations.generateLocalVariables»
				
				«alg.body.statements.generateStatementList»
			'''
		}
	}

	override getDependencies(Map<?, ?> options) {
		prepare()
		parseResult?.containedDependencies ?: emptySet
	}
}
