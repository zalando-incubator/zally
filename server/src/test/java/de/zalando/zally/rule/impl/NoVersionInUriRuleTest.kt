package de.zalando.zally.rule.impl

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoVersionInUriRuleTest {

    val expectedViolation = Violation(
            NoVersionInUriRule(),
            "Do Not Use URI Versioning",
            "basePath attribute contains version number",
            ViolationType.MUST,
            NoVersionInUriRule().url,
            emptyList())

    @Test
    fun returnsViolationsWhenVersionIsInTheBeginingOfBasePath() {
        val swagger = Swagger().apply { basePath = "/v1/tests" }
        assertThat(NoVersionInUriRule().validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationsWhenVersionIsInTheMiddleOfBasePath() {
        val swagger = Swagger().apply { basePath = "/api/v1/tests" }
        assertThat(NoVersionInUriRule().validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationsWhenVersionIsInTheEndOfBasePath() {
        val swagger = Swagger().apply { basePath = "/api/v1" }
        assertThat(NoVersionInUriRule().validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationsWhenVersionIsBig() {
        val swagger = Swagger().apply { basePath = "/v1024/tests" }
        assertThat(NoVersionInUriRule().validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsEmptyViolationListWhenNoVersionFoundInURL() {
        val swagger = Swagger().apply { basePath = "/violations/" }
        assertThat(NoVersionInUriRule().validate(swagger)).isNull()
    }

    @Test
    fun returnsEmptyViolationListWhenBasePathIsNull() {
        val swagger = Swagger()
        assertThat(NoVersionInUriRule().validate(swagger)).isNull()
    }
}
