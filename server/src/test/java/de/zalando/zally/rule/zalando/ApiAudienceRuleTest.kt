package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions
import org.junit.Test

class ApiAudienceRuleTest {

    private val rule = ApiAudienceRule(testConfig)

    @Test
    fun correctApiAudienceIsSet() {
        val swagger = withAudience("company-internal")

        Assertions.assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun incorrectAudienceIsSet() {
        val swagger = withAudience("not-existing-audience")

        val violation = rule.validate(ApiAdapter(swagger, OpenAPI()))!!

        Assertions.assertThat(violation.paths).hasSameElementsAs(listOf("/info/x-audience"))
        Assertions.assertThat(violation.description).matches(".*doesn't match.*")
    }

    @Test
    fun noApiAudienceIsSet() {
        val violation = rule.validate(ApiAdapter(Swagger(), OpenAPI()))!!

        Assertions.assertThat(violation.paths).hasSameElementsAs(listOf("/info/x-audience"))
        Assertions.assertThat(violation.description).matches(".*Audience must be provided.*")
    }

    private fun withAudience(apiAudience: String): Swagger {
        val root = JsonNodeFactory.instance.objectNode()
        root.putObject("info")
            .put("x-audience", apiAudience)
        return SwaggerParser().read(root)
    }
}
