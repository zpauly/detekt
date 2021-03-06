package io.gitlab.arturbosch.detekt.rules.complexity

import io.gitlab.arturbosch.detekt.api.CodeSmellThresholdRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Metric
import io.gitlab.arturbosch.detekt.api.ThresholdedCodeSmell
import io.gitlab.arturbosch.detekt.rules.isUsedForNesting
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenExpression

/**
 * @author Artur Bosch
 */
class ComplexMethod(config: Config = Config.empty, threshold: Int = 10) : CodeSmellThresholdRule("ComplexMethod", config, threshold) {

	override fun visitNamedFunction(function: KtNamedFunction) {
		val mcc = MccVisitor().visit(function)
		if (mcc > threshold) {
			addFindings(ThresholdedCodeSmell(id, severity, Entity.Companion.from(function), Metric("MCC", mcc, threshold)))
		}
	}

	internal class MccVisitor : DetektVisitor() {

		private var mcc = 1

		private fun inc() {
			mcc++
		}

		fun visit(function: KtNamedFunction): Int {
			mcc = 1
			super.visitNamedFunction(function)
			return mcc
		}

		override fun visitIfExpression(expression: KtIfExpression) {
			inc()
			super.visitIfExpression(expression)
		}

		override fun visitLoopExpression(loopExpression: KtLoopExpression) {
			inc()
			super.visitLoopExpression(loopExpression)
		}

		override fun visitWhenExpression(expression: KtWhenExpression) {
			inc()
			super.visitWhenExpression(expression)
		}

		override fun visitTryExpression(expression: KtTryExpression) {
			inc()
			super.visitTryExpression(expression)
		}

		override fun visitCallExpression(expression: KtCallExpression) {
			if (expression.isUsedForNesting()) {
				val lambdaArguments = expression.lambdaArguments
				if (lambdaArguments.size > 0) {
					val lambdaArgument = lambdaArguments[0]
					lambdaArgument.getLambdaExpression().bodyExpression?.let {
						inc()
					}
				}
			}
			super.visitCallExpression(expression)
		}
	}
}