package de.zalando.zally.rule.zalando

import de.zalando.zally.getOpenApiContextFromContent
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class JsonProblemAsDefaultResponseRuleTest {

    val rule = JsonProblemAsDefaultResponseRule()

    @Test
    fun `checkContainsDefaultResponse should return violation if default response is not set`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths:
              '/pets':
                get:
                  responses:
                    200:
                      description: Lorem Ipsum
        """.trimIndent())

        val violations = rule.checkContainsDefaultResponse(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*has to contain the default response.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1pets/get")
    }

    @Test
    fun `checkDefaultResponseIsProblemJson should return violation if not problem json is set as default response`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths:
              '/pets':
                get:
                  responses:
                    default:
                      ${'$'}ref: 'https://some.other.schema'
        """.trimIndent())

        val violations = rule.checkDefaultResponseIsProblemJson(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*problem json has to be used as default response.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1pets/get")
    }

    @Test
    fun `checkDefaultResponseIsProblemJson should not return violation if problem json is set as default response`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths:
              '/pets':
                get:
                  responses:
                    default:
                      ${'$'}ref: 'https://zalando.github.io/problem/schema.yaml#/Problem'
        """.trimIndent())

        val violations = rule.checkDefaultResponseIsProblemJson(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `(checkDefaultResponseIsProblemJson|checkContainsDefaultResponse) should not return violation for empty specification`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths: {}
        """)

        assertThat(rule.checkContainsDefaultResponse(context)).isEmpty()
        assertThat(rule.checkDefaultResponseIsProblemJson(context)).isEmpty()
    }
}
