package de.zalando.zally.rule.impl

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SnakeCaseInPropNameRule(@Autowired rulesConfig: Config) : SwaggerRule() {
    override val title = "snake_case property names"
    override val url = "/#118"
    override val violationType = ViolationType.MUST
    override val code = "M012"
    override val guidelinesCode = "118"
    private val description = "Property names must be snake_case: "

    private val whitelist = rulesConfig.getStringList(SnakeCaseInPropNameRule::class.simpleName + ".whitelist").toSet()

    override fun validate(swagger: Swagger): Violation? {
        val result = swagger.getAllJsonObjects().flatMap { (def, path) ->
            val badProps = def.keys.filterNot { PatternUtil.isSnakeCase(it) || whitelist.contains(it) }
            if (badProps.isNotEmpty()) listOf(badProps to path) else emptyList()
        }
        return if (result.isNotEmpty()) {
            val (props, paths) = result.unzip()
            val properties = props.flatten().toSet().joinToString(", ")
            Violation(this, title, description + properties, violationType, url, paths)
        } else null
    }
}
