package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import io.swagger.models.auth.OAuth2Definition

class UsePasswordFlowWithOauth2Rule : SwaggerRule() {
    override val title = "Set Flow to Password When Using OAuth 2.0"
    override val url = "/#104"
    override val violationType = ViolationType.MUST
    override val code = "M017"
    override val guidelinesCode = "104"

    override fun validate(swagger: Swagger): Violation? {
        val definitionsWithoutPasswordFlow = swagger
                .securityDefinitions
                .orEmpty()
                .values
                .filter { it.type?.toLowerCase() == "oauth2" }
                .filter { (it as OAuth2Definition).flow != "password" }

        return if (definitionsWithoutPasswordFlow.any())
            Violation(this, title, "OAuth2 security definitions should use password flow", violationType, url, emptyList())
        else null
    }
}
