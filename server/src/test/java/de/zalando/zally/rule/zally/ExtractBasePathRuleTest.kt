package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.swaggerWithPaths
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtractBasePathRuleTest {
    val DESC_PATTERN = "All paths start with prefix '%s'. This prefix could be part of base path."

    private val rule = ExtractBasePathRule(ZallyRuleSet())

    @Test
    fun validateEmptyPath() {
        assertThat(rule.validate(Swagger())).isNull()
    }

    @Test
    fun simplePositiveCase() {
        val swagger = swaggerWithPaths("/orders/{order_id}", "/orders/{updates}", "/merchants")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun singlePathShouldPass() {
        val swagger = swaggerWithPaths("/orders/{order_id}")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun simpleNegativeCase() {
        val swagger = swaggerWithPaths(
            "/shipment/{shipment_id}",
            "/shipment/{shipment_id}/status",
            "/shipment/{shipment_id}/details"
        )
        assertThat(rule.validate(swagger))
                .hasFieldOrPropertyWithValue("description", DESC_PATTERN.format("/shipment"))
                .hasFieldOrPropertyWithValue("paths", listOf<String>())
    }

    @Test
    fun multipleResourceNegativeCase() {
        val swagger = swaggerWithPaths(
            "/queue/models/configs/{config-id}",
            "/queue/models/",
            "/queue/models/{model-id}",
            "/queue/models/summaries"
        )
        assertThat(rule.validate(swagger))
                .hasFieldOrPropertyWithValue("description", DESC_PATTERN.format("/queue/models"))
                .hasFieldOrPropertyWithValue("paths", listOf<String>())
    }

    @Test
    fun shouldMatchWholeSubresource() {
        val swagger = swaggerWithPaths(
            "/api/{api_id}/deployments",
            "/api/{api_id}/",
            "/applications/{app_id}",
            "/applications/"
        )
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val swagger = getFixture("api_tinbox.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }
}
