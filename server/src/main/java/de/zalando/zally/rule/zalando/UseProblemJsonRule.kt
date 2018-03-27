package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.*
import io.swagger.models.properties.ObjectProperty
import io.swagger.models.properties.RefProperty

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "176",
        severity = Severity.MUST,
        title = "Use Problem JSON"
)
class UseProblemJsonRule {
    private val description = "Operations Should Return Problem JSON When Any Problem Occurs During Processing " +
            "Whether Caused by Client Or Server"
    private val requiredFields = setOf("title", "status")

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? =
            if (adapter.isV2()) {
                validateV2(adapter.swagger!!)
            } else Violation.UNSUPPORTED_API_VERSION


    fun validateV2(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().flatMap { pathEntry ->
            pathEntry.value.operationMap.orEmpty().filter { it.key.shouldContainPayload() }.flatMap { opEntry ->
                opEntry.value.responses.orEmpty().flatMap { responseEntry ->
                    val httpCode = responseEntry.key.toIntOrNull()
                    if (httpCode in 400..599 && !isValidProblemJson(swagger, responseEntry.value, opEntry.value)) {
                        listOf("${pathEntry.key} ${opEntry.key} ${responseEntry.key}")
                    } else emptyList()
                }
            }
        }

        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }

    private fun isValidProblemJson(swagger: Swagger, response: Response, operation: Operation) =
            isProblemJson(swagger, response) && producesJson(swagger, operation)

    private fun isProblemJson(swagger: Swagger, response: Response): Boolean {
        val schema = response.schema
        val properties = when (schema) {
            is RefProperty -> getProperties(swagger, swagger.definitions?.get((response.schema as RefProperty).simpleRef))
            is ObjectProperty -> schema.properties?.keys.orEmpty()
            else -> emptySet<String>()
        }
        return properties.containsAll(requiredFields)
    }

    private fun getProperties(swagger: Swagger, definition: Model?): Set<String> {
        return when (definition) {
            is ComposedModel -> definition.allOf.orEmpty().flatMap { getProperties(swagger, it) }.toSet()
            is RefModel -> getProperties(swagger, swagger.definitions[definition.simpleRef])
            else -> definition?.properties?.keys.orEmpty()
        }
    }

    private fun producesJson(swagger: Swagger, operation: Operation) =
            if (operation.produces.orEmpty().isEmpty()) {
                swagger.produces.orEmpty().containsJson()
            } else {
                operation.produces.containsJson()
            }

    // support for application/json also with set charset e.g. "application/json; charset=utf-8"
    private fun List<String>.containsJson() =
            any { it.startsWith("application/json") }

    private fun HttpMethod.shouldContainPayload(): Boolean =
            name.toLowerCase() !in listOf("head", "options")
}
