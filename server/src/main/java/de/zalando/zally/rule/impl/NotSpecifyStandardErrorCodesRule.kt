package de.zalando.zally.rule.impl

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NotSpecifyStandardErrorCodesRule(@Autowired rulesConfig: Config) : SwaggerRule() {
    override val title = "Not Specify Standard Error Codes"
    override val url = "/#151"
    override val violationType = ViolationType.HINT
    override val code = "H002"
    override val guidelinesCode = "151"
    private val description = "Not Specify Standard Error Status Codes Like 400, 404, 503 " +
            "Unless They Have Another Meaning Or Special Implementation/Contract Detail"

    private val standardErrorStatusCodes = rulesConfig.getConfig(name)
            .getIntList("standard_error_codes").toSet()

    override fun validate(swagger: Swagger): Violation? {

        val paths = swagger.paths.orEmpty().flatMap { pathEntry ->
            pathEntry.value.operationMap.orEmpty().flatMap { opEntry ->
                opEntry.value.responses.orEmpty().flatMap { responseEntry ->
                    val httpCode = responseEntry.key.toIntOrNull()
                    if (isStandardErrorCode(httpCode)) {
                        listOf("${pathEntry.key} ${opEntry.key} ${responseEntry.key}")
                    } else emptyList()
                }
            }
        }

        return if (paths.isNotEmpty()) Violation(this, title, description, violationType, url, paths) else null
    }

    private fun isStandardErrorCode(httpStatusCode: Int?): Boolean {
        return httpStatusCode in standardErrorStatusCodes
    }

}
