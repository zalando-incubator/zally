package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class NoProtocolInHostRule : SwaggerRule() {
    override val title = "Host should not contain protocol"
    // TODO: Provide URL
    override val url = ""
    override val violationType = ViolationType.MUST
    override val code = "M008"
    // TODO: Provide guidelines code
    override val guidelinesCode = ""
    private val desc = "Information about protocol should be placed in schema. Current host value '%s' violates this rule"

    override fun validate(swagger: Swagger): Violation? {
        val host = swagger.host.orEmpty()
        return if ("://" in host)
            Violation(this, title, desc.format(host), violationType, url, emptyList())
        else null
    }
}
