package de.zalando.zally.rule.impl

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PluralizeResourceNamesRule(@Autowired rulesConfig: Config) : SwaggerRule() {
    override val title = "Pluralize Resource Names"
    override val url = "/#134"
    override val violationType = ViolationType.SHOULD
    override val code = "S008"
    override val guidelinesCode = "134"
    private val DESC_PATTERN = "Resources %s are singular (but we are not sure)"
    private val allowedPrefixes = rulesConfig.getConfig(name).getStringList("whitelist_prefixes")

    override fun validate(swagger: Swagger): Violation? {
        val res = swagger.paths?.keys?.flatMap { path ->
            val allParts = path.split("/".toRegex())
            val partsToCheck = if (allParts.size > 1 && allowedPrefixes.contains(allParts.first())) allParts.drop(1)
            else allParts

            partsToCheck.filter { s -> !s.isEmpty() && !PatternUtil.isPathVariable(s) && !isPlural(s) }
                .map { it to path }
        }
        return if (res != null && res.isNotEmpty()) {
            val desc = res.map { "'${it.first}'" }.toSet().joinToString(", ")
            val paths = res.map { it.second }
            Violation(this, title, String.format(DESC_PATTERN, desc), violationType, url, paths)
        } else null
    }
}
