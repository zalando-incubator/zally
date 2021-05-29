package org.zalando.zally.ruleset.zalando

import io.swagger.v3.oas.models.media.Content
import org.zalando.zally.core.MediaType
import org.zalando.zally.core.util.allOperations
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "167",
    severity = Severity.MUST,
    title = "Use JSON payload as interchange format"
)
class UseJsonPayload {

    val description = "Payload should be a JSON. Actual:"

    @Check(severity = Severity.MUST)
    fun validatePayload(context: Context): List<Violation> {

        val validateContent: (Content) -> List<Violation> = { content ->
            content.filter { (key, _) -> key != MediaType.APPLICATION_JSON.type }
                .map { context.violation("$description ${it.key}", it.value) }
        }

        val allOperations = context.api.paths.flatMap { (_, path) -> path.allOperations().filterNotNull() }

        val requestViolations =
            allOperations.filter { it.requestBody?.content != null }.flatMap { validateContent(it.requestBody.content) }

        val responseViolations = allOperations
            .filter { it.responses.isNotEmpty() }
            .flatMap { it.responses.entries }
            .mapNotNull { it.value.content }
            .flatMap(validateContent)

        return requestViolations + responseViolations
    }
}
