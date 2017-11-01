package de.zalando.zally.rule

import de.zalando.zally.getFixture
import de.zalando.zally.swaggerWithHeaderParams
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HyphenateHttpHeadersRuleTest {

    @Test
    fun simplePositiveCase() {
        val swagger = swaggerWithHeaderParams("Right-Name")
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun simplePositiveCamelCase() {
        // CamelCaseName IS a valid 'hypenated' header, it just has a single term
        val swagger = swaggerWithHeaderParams("CamelCaseName")
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun mustAcceptValuesFromWhitelist() {
        val swagger = swaggerWithHeaderParams("ETag", "X-Trace-ID")
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun emptySwaggerShouldPass() {
        val swagger = Swagger()
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val swagger = getFixture("api_tinbox.yaml")
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun issue572RateLimitHeadersAreAccepted() {
        val swagger = swaggerWithHeaderParams("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")
        assertThat(HyphenateHttpHeadersRule(testConfig).validate(swagger)).isNull()
    }
}
