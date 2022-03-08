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
package org.eclipse.fordiac.ide.model.eval.st

import java.util.List
import java.util.Map
import org.eclipse.fordiac.ide.model.data.DataType
import org.eclipse.fordiac.ide.model.data.Subrange
import org.eclipse.fordiac.ide.model.eval.AbstractEvaluator
import org.eclipse.fordiac.ide.model.eval.Evaluator
import org.eclipse.fordiac.ide.model.eval.value.ArrayValue
import org.eclipse.fordiac.ide.model.eval.value.BoolValue
import org.eclipse.fordiac.ide.model.eval.value.Value
import org.eclipse.fordiac.ide.model.eval.variable.PartialVariable
import org.eclipse.fordiac.ide.model.eval.variable.Variable
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration
import org.eclipse.fordiac.ide.structuredtextalgorithm.stalgorithm.STAlgorithmBody
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STArrayAccessExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STArrayInitializerExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STAssignmentStatement
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STBinaryExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STBinaryOperator
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCaseStatement
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STContinue
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STDateAndTimeLiteral
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STDateLiteral
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STElementaryInitializerExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STExit
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STFeatureExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STForStatement
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STIfStatement
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STMemberAccessExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STMultibitPartialExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STNumericLiteral
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STRepeatStatement
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STReturn
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STStatement
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STStringLiteral
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STTimeLiteral
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STTimeOfDayLiteral
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STUnaryExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STVarDeclaration
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STWhileStatement
import org.eclipse.xtend.lib.annotations.Accessors

import static org.eclipse.fordiac.ide.model.eval.variable.VariableOperations.*

import static extension org.eclipse.fordiac.ide.model.eval.value.ValueOperations.*
import static extension org.eclipse.fordiac.ide.model.eval.variable.ArrayVariable.*

abstract class StructuredTextEvaluator extends AbstractEvaluator {

	@Accessors final String name
	final Map<String, Variable> variables

	new(String name, Iterable<Variable> variables, Evaluator parent) {
		super(parent)
		this.name = name
		this.variables = variables.toMap[getName]
	}

	override getVariables() {
		variables.unmodifiableView
	}

	def protected dispatch Value evaluate(STAlgorithmBody alg) {
		alg.trap.evaluateStructuredTextAlgorithm
		null
	}

	def protected dispatch Value evaluate(STExpression expr) {
		expr.trap.evaluateExpression
	}

	def private evaluateStructuredTextAlgorithm(STAlgorithmBody alg) {
		alg.varTempDeclarations.flatMap[varDeclarations].filter(STVarDeclaration).forEach[evaluateLocalVariable]
		try {
			alg.statements.evaluateStatementList
		} catch (StructuredTextException e) {
			// return
		}
	}

	def private void evaluateLocalVariable(STVarDeclaration variable) {
		val type = if (variable.array)
				(variable.type as DataType).newArrayType(variable.ranges.map[evaluateSubrange])
			else
				variable.type as DataType
		variables.put(variable.name,
			newVariable(variable.name, type).evaluateInitializerExpression(variable.defaultValue))
	}

	def private dispatch Variable evaluateInitializerExpression(Variable variable, Void expression) {
		variable
	}

	def private dispatch Variable evaluateInitializerExpression(Variable variable,
		STElementaryInitializerExpression expression) {
		variable.value = expression.value.evaluateExpression
		variable
	}

	def private dispatch Variable evaluateInitializerExpression(Variable variable,
		STArrayInitializerExpression expression) {
		val value = variable.value as ArrayValue
		expression.values.flatMap [ elem |
			if (elem.initExpressions.empty)
				#[elem.indexOrInitExpression.evaluateExpression]
			else {
				val initValues = elem.initExpressions.map[evaluateExpression]
				(0 ..< elem.indexOrInitExpression.evaluateExpression.asInteger).flatMap[initValues]
			}
		].forEach[initValue, index|value.getRaw(index).value = initValue]
		variable
	}

	def private void evaluateStatementList(List<STStatement> statements) {
		statements.forEach[evaluateStatement]
	}

	def private dispatch void evaluateStatement(STStatement stmt) {
		error('''The statement «stmt.eClass.name» is not supported''')
		throw new UnsupportedOperationException('''The statement «stmt.eClass.name» is not supported''')
	}

	def private dispatch void evaluateStatement(STAssignmentStatement stmt) {
		stmt.left.evaluateVariable.value = stmt.right.trap.evaluateExpression
	}

	def private dispatch void evaluateStatement(STIfStatement stmt) {
		if (stmt.condition.trap.evaluateExpression.asBoolean) {
			stmt.statements.evaluateStatementList
		} else {
			(stmt.elseifs.findFirst[condition.trap.evaluateExpression.asBoolean]?.statements ?:
				stmt.^else?.statements)?.evaluateStatementList
		}
	}

	def private dispatch void evaluateStatement(STCaseStatement stmt) {
		val value = stmt.selector.trap.evaluateExpression;
		(stmt.cases.findFirst[conditions.exists[trap.evaluateExpression == value]]?.statements ?:
			stmt.^else?.statements)?.evaluateStatementList
	}

	def private dispatch void evaluateStatement(STForStatement stmt) {
		val variable = variables.get(stmt.variable.name)
		// from
		variable.value = stmt.from.trap.evaluateExpression
		// to
		val to = stmt.to.evaluateExpression
		// by
		val by = stmt.by?.evaluateExpression ?: 1.wrapValue(variable.type)
		// direction?
		if (by >= variable.type.defaultValue) {
			try {
				while (variable.value <= to) {
					try {
						stmt.statements.evaluateStatementList
					} catch (ContinueException e) {
						// continue
					}
					(stmt.by ?: stmt.from).trap
					variable.value = variable.value + by
				}
			} catch (ExitException e) {
				// break
			}
		} else {
			try {
				while (variable.value >= to) {
					try {
						stmt.statements.evaluateStatementList
					} catch (ContinueException e) {
						// continue
					}
					stmt.by.trap
					variable.value = variable.value + by
				}
			} catch (ExitException e) {
				// break
			}
		}
	}

	def private dispatch void evaluateStatement(STWhileStatement stmt) {
		try {
			while (stmt.condition.trap.evaluateExpression.asBoolean) {
				try {
					stmt.statements.evaluateStatementList
				} catch (ContinueException e) {
					// continue
				}
			}
		} catch (ExitException e) {
			// break
		}
	}

	def private dispatch void evaluateStatement(STRepeatStatement stmt) {
		try {
			do {
				try {
					stmt.statements.evaluateStatementList
				} catch (ContinueException e) {
					// continue
				}
			} while (!stmt.condition.trap.evaluateExpression.asBoolean);
		} catch (ExitException e) {
			// break
		}
	}

	def private dispatch void evaluateStatement(STContinue stmt) { throw new ContinueException(stmt.trap) }

	def private dispatch void evaluateStatement(STReturn stmt) { throw new ReturnException(stmt.trap) }

	def private dispatch void evaluateStatement(STExit stmt) { throw new ExitException(stmt.trap) }

	def private dispatch Value evaluateExpression(STExpression expr) {
		error('''The expression «expr.eClass.name» is not supported''')
		throw new UnsupportedOperationException('''The expression «expr.eClass.name» is not supported''')
	}

	def private dispatch Value evaluateExpression(STBinaryExpression expr) {
		switch (expr.op) {
			case ADD:
				expr.left.evaluateExpression + expr.right.evaluateExpression
			case SUB:
				expr.left.evaluateExpression - expr.right.evaluateExpression
			case MUL:
				expr.left.evaluateExpression * expr.right.evaluateExpression
			case DIV:
				expr.left.evaluateExpression / expr.right.evaluateExpression
			case MOD:
				expr.left.evaluateExpression % expr.right.evaluateExpression
			case POWER:
				expr.left.evaluateExpression ** expr.right.evaluateExpression
			case AND,
			case AMPERSAND:
				switch (left: expr.left.evaluateExpression) {
					BoolValue case left.boolValue: expr.right.evaluateExpression
					BoolValue: BoolValue.FALSE
					default: left.bitwiseAnd(expr.right.evaluateExpression)
				}
			case OR:
				switch (left: expr.left.evaluateExpression) {
					BoolValue case !left.boolValue: expr.right.evaluateExpression
					BoolValue: BoolValue.TRUE
					default: left.bitwiseOr(expr.right.evaluateExpression)
				}
			case XOR:
				switch (left: expr.left.evaluateExpression) {
					BoolValue: BoolValue.toBoolValue(left.boolValue.xor(expr.right.evaluateExpression.asBoolean))
					default: left.bitwiseXor(expr.right.evaluateExpression)
				}
			case EQ:
				BoolValue.toBoolValue(expr.left.evaluateExpression == expr.right.evaluateExpression)
			case NE:
				BoolValue.toBoolValue(expr.left.evaluateExpression != expr.right.evaluateExpression)
			case LT:
				BoolValue.toBoolValue(expr.left.evaluateExpression < expr.right.evaluateExpression)
			case LE:
				BoolValue.toBoolValue(expr.left.evaluateExpression <= expr.right.evaluateExpression)
			case GT:
				BoolValue.toBoolValue(expr.left.evaluateExpression > expr.right.evaluateExpression)
			case GE:
				BoolValue.toBoolValue(expr.left.evaluateExpression >= expr.right.evaluateExpression)
			default: {
				error('''The operator «expr.op» is not supported''')
				throw new UnsupportedOperationException('''The operator «expr.op» is not supported''')
			}
		}
	}

	def private dispatch Value evaluateExpression(STUnaryExpression expr) {
		switch (expr.op) {
			case PLUS:
				+expr.expression.evaluateExpression
			case MINUS:
				-expr.expression.evaluateExpression
			case NOT:
				switch (value: expr.expression.evaluateExpression) {
					BoolValue: BoolValue.toBoolValue(!value.boolValue)
					default: value.bitwiseNot
				}
			default: {
				error('''The operator «expr.op» is not supported''')
				throw new UnsupportedOperationException('''The operator «expr.op» is not supported''')
			}
		}
	}

	def private dispatch Value evaluateExpression(STNumericLiteral expr) {
		expr.value.wrapValue(expr.resultType as DataType)
	}

	def private dispatch Value evaluateExpression(STStringLiteral expr) {
		expr.value.wrapValue(expr.resultType as DataType)
	}

	def private dispatch Value evaluateExpression(STDateLiteral expr) {
		expr.value.wrapValue(expr.resultType as DataType)
	}

	def private dispatch Value evaluateExpression(STTimeLiteral expr) {
		expr.value.wrapValue(expr.resultType as DataType)
	}

	def private dispatch Value evaluateExpression(STTimeOfDayLiteral expr) {
		expr.value.wrapValue(expr.resultType as DataType)
	}

	def private dispatch Value evaluateExpression(STDateAndTimeLiteral expr) {
		expr.value.wrapValue(expr.resultType as DataType)
	}

	def private dispatch Value evaluateExpression(STFeatureExpression expr) {
		switch (feature: expr.feature) {
			VarDeclaration:
				variables.get(feature.name).value
			STVarDeclaration:
				variables.get(feature.name).value
			default: {
				error('''The feature «feature.eClass.name» is not supported''')
				throw new UnsupportedOperationException('''The feature «feature.eClass.name» is not supported''')
			}
		}
	}

	def private dispatch Value evaluateExpression(STMemberAccessExpression expr) {
		expr.member.evaluateExpression(expr.receiver.evaluateExpression)
	}

	def private dispatch Value evaluateExpression(STArrayAccessExpression expr) {
		val receiver = expr.receiver.evaluateExpression as ArrayValue
		val index = expr.index.map[evaluateExpression.asInteger].toList
		receiver.get(index).value
	}

	def private dispatch Value evaluateExpression(STExpression expr, Value receiver) {
		error('''The expression «expr.eClass.name» is not supported''')
		throw new UnsupportedOperationException('''The expression «expr.eClass.name» is not supported''')
	}

	def private dispatch Value evaluateExpression(STMultibitPartialExpression expr, Value receiver) {
		receiver.partial(expr.resultType as DataType,
			if(expr.expression !== null) expr.expression.evaluateExpression.asInteger else expr.index.intValueExact)
	}

	def private dispatch Variable evaluateVariable(STExpression expr) {
		error('''The lvalue expression «expr.eClass.name» is not supported''')
		throw new UnsupportedOperationException('''The lvalue expression «expr.eClass.name» is not supported''')
	}

	def private dispatch Variable evaluateVariable(STFeatureExpression expr) {
		switch (feature: expr.feature) {
			VarDeclaration:
				variables.get(feature.name)
			STVarDeclaration:
				variables.get(feature.name)
			default: {
				error('''The feature «feature.eClass.name» is not supported''')
				throw new UnsupportedOperationException('''The feature «feature.eClass.name» is not supported''')
			}
		}
	}

	def private dispatch Variable evaluateVariable(STMemberAccessExpression expr) {
		expr.member.evaluateVariable(expr.receiver.evaluateVariable)
	}

	def private dispatch Variable evaluateVariable(STArrayAccessExpression expr) {
		val receiver = expr.receiver.evaluateExpression as ArrayValue
		val index = expr.index.map[evaluateExpression.asInteger].toList
		receiver.get(index)
	}

	def private dispatch Variable evaluateVariable(STExpression expr, Variable receiver) {
		error('''The expression «expr.eClass.name» is not supported''')
		throw new UnsupportedOperationException('''The lvalue expression «expr.eClass.name» is not supported''')
	}

	def private dispatch Variable evaluateVariable(STMultibitPartialExpression expr, Variable receiver) {
		new PartialVariable(receiver, expr.resultType as DataType,
			if(expr.expression !== null) expr.expression.evaluateExpression.asInteger else expr.index.intValueExact)
	}

	def private Subrange evaluateSubrange(STExpression expr) {
		switch (expr) {
			STBinaryExpression case expr.op === STBinaryOperator.RANGE:
				newSubrange(expr.left.evaluateExpression.asInteger, expr.right.evaluateExpression.asInteger)
			default:
				newSubrange(0, expr.evaluateExpression.asInteger)
		}
	}

	static class StructuredTextException extends Exception {
		new(STStatement statement) {
			super(statement.eClass.name)
		}
	}

	static class ContinueException extends StructuredTextException {
		new(STStatement statement) {
			super(statement)
		}
	}

	static class ReturnException extends StructuredTextException {
		new(STStatement statement) {
			super(statement)
		}
	}

	static class ExitException extends StructuredTextException {
		new(STStatement statement) {
			super(statement)
		}
	}
}
