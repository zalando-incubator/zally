package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.parameters.QueryParameter

/**
 * Lint for snake case for query params
 */
@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "130",
        severity = Severity.MUST,
        title = "Use snake_case (never camelCase) for Query Parameters"
)
class SnakeCaseForQueryParamsRule {

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            val swagger = adapter.swagger!!
            val result = swagger.paths.orEmpty().flatMap { (path, pathObject) ->
                pathObject.operationMap.orEmpty().flatMap { (verb, operation) ->
                    val badParams = operation.parameters.filter { it is QueryParameter && !PatternUtil.isSnakeCase(it.name) }
                    if (badParams.isNotEmpty()) listOf("$path $verb" to badParams) else emptyList()
                }
            }
            return if (result.isNotEmpty()) {
                val (paths, params) = result.unzip()
                val description = "Parameters that are not in snake_case: " + params.flatten().map { it.name }.toSet().joinToString(",")
                Violation(description, paths)
            } else null
        }
        return Violation.UNSUPPORTED_API_VERSION
    }
}
