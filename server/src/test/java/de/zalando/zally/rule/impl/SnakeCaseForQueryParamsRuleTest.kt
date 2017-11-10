package de.zalando.zally.rule.impl

import de.zalando.zally.getFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SnakeCaseForQueryParamsRuleTest {

    private val validSwagger = getFixture("snakeCaseForQueryParamsValid.json")
    private val invalidSwaggerWithLocalParam = getFixture("snakeCaseForQueryParamsInvalidLocalParam.json")
    private val invalidSwaggerWIthInternalRef = getFixture("snakeCaseForQueryParamsInvalidInternalRef.json")
    private val invalidSwaggerWithExternalRef = getFixture("snakeCaseForQueryParamsInvalidExternalRef.json")

    @Test
    fun shouldFindNoViolations() {
        assertThat(SnakeCaseForQueryParamsRule().validate(validSwagger)).isNull()
    }

    @Test
    fun shouldFindViolationsInLocalRef() {
        val result = SnakeCaseForQueryParamsRule().validate(invalidSwaggerWithLocalParam)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }

    @Test
    fun shouldFindViolationsInInternalRef() {
        val result = SnakeCaseForQueryParamsRule().validate(invalidSwaggerWIthInternalRef)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }

    @Test
    fun shouldFindViolationsInExternalRef() {
        val result = SnakeCaseForQueryParamsRule().validate(invalidSwaggerWithExternalRef)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }
}
