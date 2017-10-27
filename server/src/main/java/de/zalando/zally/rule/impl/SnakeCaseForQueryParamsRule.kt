package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import io.swagger.models.parameters.QueryParameter
import org.springframework.stereotype.Component

/**
 * Lint for snake case for query params
 */
@Component
class SnakeCaseForQueryParamsRule : SwaggerRule() {
    override val title = "Use snake_case (never camelCase) for Query Parameters"
    override val url = "/#130"
    override val violationType = ViolationType.MUST
    override val code = "M011"
    override val guidelinesCode = "130"

    override fun validate(swagger: Swagger): Violation? {
        val result = swagger.paths.orEmpty().flatMap { (path, pathObject) ->
            pathObject.operationMap.orEmpty().flatMap { (verb, operation) ->
                val badParams = operation.parameters.filter { it is QueryParameter && !PatternUtil.isSnakeCase(it.name) }
                if (badParams.isNotEmpty()) listOf("$path $verb" to badParams) else emptyList()
            }
        }
        return if (result.isNotEmpty()) {
            val (paths, params) = result.unzip()
            val description = "Parameters that are not in snake_case: " + params.flatten().map { it.name }.toSet().joinToString(",")
            Violation(this, title, description, violationType, url, paths)
        } else null
    }
}
