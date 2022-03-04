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
 *   Martin Melik Merkumians - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.structuredtextcore.formatting2

import org.eclipse.xtext.formatting2.internal.AbstractTextReplacer
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.eclipse.xtext.formatting2.regionaccess.ITextSegment
import org.eclipse.xtext.formatting2.ITextReplacerContext

class KeywordCaseTextReplacer extends AbstractTextReplacer {
	
	new(IFormattableDocument document, ITextSegment region) {
		super(document, region)
	}
	
	override createReplacements(ITextReplacerContext context) {
		val replacement = region.replaceWith(region.text.toUpperCase)
		context.addReplacement(replacement)
		return context
	}
	
}