package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "154",
        severity = Severity.SHOULD,
        title = "Explicitly define the Collection Format of Query Parameters"
)
class QueryParameterCollectionFormatRule {
    private val formatsAllowed = listOf("csv", "multi")
    private val violationDescription = "CollectionFormat should be one of: $formatsAllowed"

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            val swagger = adapter.swagger!!
            fun Collection<Parameter>?.extractInvalidQueryParam(path: String) =
                    orEmpty().filterIsInstance<QueryParameter>()
                            .filter { it.`type` == "array" && it.collectionFormat !in formatsAllowed }
                            .map { path to it.name }

            val fromParams = swagger.parameters.orEmpty().values.extractInvalidQueryParam("parameters")
            val fromPaths = swagger.paths.orEmpty().entries.flatMap { (name, path) ->
                path.parameters.extractInvalidQueryParam(name) + path.operations.flatMap { operation ->
                    operation.parameters.extractInvalidQueryParam(name)
                }
            }

            val allHeaders = fromParams + fromPaths
            val paths = allHeaders
                    .map { "${it.first} ${it.second}" }
                    .distinct()

            return if (paths.isNotEmpty()) createViolation(paths) else null
        }
        return Violation.UNSUPPORTED_API_VERSION
    }

    fun createViolation(paths: List<String>): Violation {
        return Violation(violationDescription, paths)
    }
}
