package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil.isApplicationJsonOrProblemJson
import de.zalando.zally.util.PatternUtil.isCustomMediaTypeWithVersioning
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class MediaTypesRule : SwaggerRule() {

    override val title = "Prefer standard media type names"
    override val url = "/#172"
    override val violationType = ViolationType.SHOULD
    override val code = "S004"
    override val guidelinesCode = "172"
    private val DESCRIPTION = "Custom media types should only be used for versioning"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().entries.flatMap { (pathName, path) ->
            path.operationMap.orEmpty().entries.flatMap { (verb, operation) ->
                val mediaTypes = ArrayList<String>() + operation.produces.orEmpty() + operation.consumes.orEmpty()
                val violatingMediaTypes = mediaTypes.filter(this::isViolatingMediaType)
                if (violatingMediaTypes.isNotEmpty()) listOf("$pathName $verb") else emptyList()
            }
        }
        return if (paths.isNotEmpty()) Violation(this, title, DESCRIPTION, violationType, url, paths) else null
    }

    private fun isViolatingMediaType(mediaType: String) =
        !isApplicationJsonOrProblemJson(mediaType) && !isCustomMediaTypeWithVersioning(mediaType)
}
