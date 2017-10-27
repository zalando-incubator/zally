package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class KebabCaseInPathSegmentsRule : SwaggerRule() {

    override val title = "Lowercase words with hyphens"
    override val url = "/#129"
    override val violationType = ViolationType.MUST
    override val code = "M007"
    override val guidelinesCode = "129"
    private val description = "Use lowercase separate words with hyphens for path segments"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filterNot {
            val pathSegments = it.split("/").filter { it.isNotEmpty() }
            pathSegments.filter { !PatternUtil.isPathVariable(it) && !PatternUtil.isLowerCaseAndHyphens(it) }.isEmpty()
        }
        return if (paths.isNotEmpty()) Violation(this, title, description, violationType, url, paths) else null
    }
}
