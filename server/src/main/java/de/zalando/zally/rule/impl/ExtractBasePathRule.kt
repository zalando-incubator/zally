package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class ExtractBasePathRule : SwaggerRule() {

    override val title = "Base path can be extracted"
    override val url = "/naming/Naming.html"
    override val violationType = ViolationType.HINT
    override val code = "H001"
    // TODO: Provide guidelines code
    override val guidelinesCode = ""
    private val DESC_PATTERN = "All paths start with prefix '%s'. This prefix could be part of base path."

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys
        if (paths.size < 2) {
            return null
        }
        val commonPrefix = paths.reduce { s1, s2 -> findCommonPrefix(s1, s2) }
        return if (commonPrefix.isNotEmpty())
            Violation(this, title, DESC_PATTERN.format(commonPrefix), violationType, url, emptyList())
        else null
    }

    private fun findCommonPrefix(s1: String, s2: String): String {
        val parts1 = s1.split("/")
        val parts2 = s2.split("/")
        val (commonParts, _) = parts1.zip(parts2).takeWhile { (t1, t2) -> !t1.startsWith('{') && t1 == t2 }.unzip()
        return commonParts.joinToString("/")
    }
}
