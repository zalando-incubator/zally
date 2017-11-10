package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil.isVersion
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class VersionInInfoSectionRule : SwaggerRule() {
    override val title = "Provide version information"
    override val url = "/#116"
    override val violationType = ViolationType.SHOULD
    override val code = "S009"
    override val guidelinesCode = "116"
    private val DESCRIPTION = "Only the documentation, not the API itself, needs version information. It should be in the " +
        "format MAJOR.MINOR.DRAFT."

    override fun validate(swagger: Swagger): Violation? {
        val version = swagger.info?.version
        val desc = when {
            version == null -> "Version is missing"
            !isVersion(version) -> "Specified version has incorrect format: $version"
            else -> null
        }
        return desc?.let { Violation(this, title, "$DESCRIPTION $it", violationType, url, emptyList()) }
    }
}
