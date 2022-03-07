/*******************************************************************************
 * Copyright (c) 2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.export.forte_ng.st

import java.util.Map
import org.eclipse.fordiac.ide.export.ExportException
import org.eclipse.fordiac.ide.export.forte_ng.ForteNgExportFilter
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STVarDeclaration
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STVarInputDeclarationBlock
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STVarOutputDeclarationBlock
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STVarTempDeclarationBlock
import org.eclipse.fordiac.ide.structuredtextfunctioneditor.stfunction.STFunction
import org.eclipse.fordiac.ide.structuredtextfunctioneditor.stfunction.STFunctionSource
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor
class STFunctionSupport extends StructuredTextSupport {
	final STFunctionSource function

	override prepare(Map<?, ?> options) {
		return true
	}

	override generate(Map<?, ?> options) throws ExportException {
		prepare(options)
		if (options.get(ForteNgExportFilter.OPTION_HEADER) == Boolean.TRUE)
			function.generateStructuredTextFunctionHeader
		else
			function.generateStructuredTextFunctionImpl
	}

	def private CharSequence generateStructuredTextFunctionHeader(STFunctionSource func) '''
		«FOR definition : func.functions.filter(STFunction)»
			«definition.generateStructuredTextFunctionDeclaration»;
			
		«ENDFOR»
	'''

	def private CharSequence generateStructuredTextFunctionImpl(STFunctionSource func) '''
		«FOR definition : func.functions.filter(STFunction)»
			«definition.generateStructuredTextFunctionDeclaration» {
			  «definition.generateStructuredTextFunctionBody»
			}
		«ENDFOR»
	'''

	def private CharSequence generateStructuredTextFunctionDeclaration(STFunction func) //
	'''«func.returnType?.generateTypeName ?: "void"» «func.generateFeatureName»(«func.generateStructuredTextFunctionInputs»«func.generateStructuredTextFunctionOutputs»)'''

	def private CharSequence generateStructuredTextFunctionInputs(STFunction func) //
	'''«FOR param : func.varDeclarations.filter(STVarInputDeclarationBlock).flatMap[varDeclarations].filter(STVarDeclaration) SEPARATOR ", "»«param.generateTypeName» «param.generateFeatureName»«ENDFOR»'''

	def private CharSequence generateStructuredTextFunctionOutputs(STFunction func) //
	'''«FOR param : func.varDeclarations.filter(STVarOutputDeclarationBlock).flatMap[varDeclarations].filter(STVarDeclaration) BEFORE "," SEPARATOR ", "»«param.generateTypeName» &«param.generateFeatureName»«ENDFOR»'''

	def private CharSequence generateStructuredTextFunctionBody(STFunction func) '''
		«func.varDeclarations.generateLocalVariables(false)»
		«func.varDeclarations.filter(STVarTempDeclarationBlock).generateLocalVariables(true)»
		
		«func.code.generateStatementList»
	'''

	override getDependencies(Map<?, ?> options) {
		prepare(options)
		if (options.get(ForteNgExportFilter.OPTION_HEADER) == Boolean.TRUE)
			(function.functions.map[returnType].filterNull + function.functions.flatMap [
				varDeclarations.filter[it instanceof STVarInputDeclarationBlock || it instanceof STVarOutputDeclarationBlock]
			].flatMap[varDeclarations].map[type]).toSet
		else
			function.containedDependencies
	}
}
