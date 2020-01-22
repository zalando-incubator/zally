package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.core.ContentParseResult
import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.core.RulesManager
import de.zalando.zally.rule.api.Context

/**
 * This validator validates a given OpenAPI definition based
 * on set of rules.
 */
class ContextRulesValidator(
    rules: RulesManager,
    private val defaultContextFactory: DefaultContextFactory
) : RulesValidator<Context>(rules) {

    override fun parse(content: String, authorization: String?): ContentParseResult<Context> {
        // first try to parse an OpenAPI (version 3+)
        return when (val parsedAsOpenApi = defaultContextFactory.parseOpenApiContext(content, authorization)) {
            is ContentParseResult.NotApplicable ->
                // if content was no OpenAPI, try to parse a Swagger (version 2)
                defaultContextFactory.parseSwaggerContext(content)
            else ->
                parsedAsOpenApi
        }
    }

    override fun ignore(root: Context, pointer: JsonPointer, ruleId: String) = root.isIgnored(pointer, ruleId)
}
