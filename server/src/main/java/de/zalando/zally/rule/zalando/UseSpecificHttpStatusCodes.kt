package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Operation
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "150",
        severity = Severity.SHOULD,
        title = "Use Specific HTTP Status Codes"
)
class UseSpecificHttpStatusCodes(@Autowired rulesConfig: Config) {
    private val description = "Operations should use specific HTTP status codes"

    private val allowedStatusCodes = rulesConfig
            .getConfig("${javaClass.simpleName}.allowed_codes").entrySet()
            .map { (key, config) -> (key to config.unwrapped() as List<String>) }.toMap()

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? {
        return adapter.withVersion2 { swagger ->
            val badPaths = swagger.paths.orEmpty().flatMap { path ->
                path.value.operationMap.orEmpty().flatMap { getNotAllowedStatusCodes(path.key, it) }
            }
            if (badPaths.isNotEmpty()) Violation(description, badPaths) else null
        }
    }

    private fun getNotAllowedStatusCodes(path: String, entry: Map.Entry<HttpMethod, Operation>): List<String> {
        val statusCodes = entry.value.responses.orEmpty().keys.toList()
        val allowedCodes = getAllowedStatusCodes(entry.key)
        val notAllowedCodes = statusCodes.filter { !allowedCodes.contains(it) }
        return notAllowedCodes.map { "$path ${entry.key.name} $it" }
    }

    private fun getAllowedStatusCodes(httpMethod: HttpMethod): List<String> {
        return allowedStatusCodes.getOrDefault(httpMethod.name.toLowerCase(), emptyList()) +
                allowedStatusCodes.getOrDefault("all", emptyList())
    }
}
