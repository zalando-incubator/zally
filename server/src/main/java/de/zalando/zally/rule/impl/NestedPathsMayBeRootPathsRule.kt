package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil.isPathVariable
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class NestedPathsMayBeRootPathsRule : SwaggerRule() {
    override val title = "Consider Using (Non-) Nested URLs"
    override val url = "/#145"
    override val violationType = ViolationType.MAY
    override val code = "C001"
    override val guidelinesCode = "145"
    private val DESCRIPTION = "Nested paths / URLs may be top-level resource"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filter {
            val pathSegments = it.split("/".toRegex())
            // we are only interested in paths that have sub-resource followed by a param: /path1/{param1}/path2/{param2}
            pathSegments.size > 4 && isPathVariable(pathSegments.last())
        }
        return if (paths.isNotEmpty()) Violation(this, title, DESCRIPTION, violationType, url, paths) else null
    }
}
