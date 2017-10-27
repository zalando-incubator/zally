package de.zalando.zally.rule.impl

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LimitNumberOfResourcesRule(@Autowired rulesConfig: Config) : SwaggerRule() {
    override val title = "Limit number of Resources"
    override val url = "/#146"
    override val violationType = ViolationType.SHOULD
    override val code = "S002"
    override val guidelinesCode = "146"
    private val pathCountLimit = rulesConfig.getConfig(name).getInt("paths_count_limit")

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty()
        val pathsCount = paths.size
        return if (pathsCount > pathCountLimit) {
            Violation(this, title, "Number of paths $pathsCount is greater than $pathCountLimit",
                    violationType, url, paths.keys.toList())
        } else null
    }
}
