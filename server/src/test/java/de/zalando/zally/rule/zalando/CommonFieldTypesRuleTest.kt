package de.zalando.zally.rule.zalando

import de.zalando.zally.getOpenApiContextFromContent
import de.zalando.zally.testConfig
import io.swagger.v3.parser.util.SchemaTypeUtil.DATE_TIME_FORMAT
import io.swagger.v3.parser.util.SchemaTypeUtil.INTEGER_TYPE
import io.swagger.v3.parser.util.SchemaTypeUtil.STRING_TYPE
import io.swagger.v3.parser.util.SchemaTypeUtil.UUID_FORMAT
import io.swagger.v3.parser.util.SchemaTypeUtil.createSchema
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class CommonFieldTypesRuleTest {
    private val rule = CommonFieldTypesRule(testConfig)

    @Test
    fun `checkField should allow empty type`() {
        assertThat(rule.checkField("", createSchema(STRING_TYPE, null))).isNull()
    }

    @Test
    fun `checkField should allow non-common type`() {
        assertThat(rule.checkField("unknown", createSchema(STRING_TYPE, null))).isNull()
    }

    @Test
    fun `checkField should allow valid values of common field types`() {
        assertThat(rule.checkField("id", createSchema(STRING_TYPE, null))).isNull()
        assertThat(rule.checkField("id", createSchema(STRING_TYPE, UUID_FORMAT))).isNull()
        assertThat(rule.checkField("created", createSchema(STRING_TYPE, DATE_TIME_FORMAT))).isNull()
        assertThat(rule.checkField("modified", createSchema(STRING_TYPE, DATE_TIME_FORMAT))).isNull()
        assertThat(rule.checkField("type", createSchema(STRING_TYPE, null))).isNull()
    }

    @Test
    fun `checkField should return violation description for invalid type`() {
        assertThat(rule.checkField("id", createSchema(INTEGER_TYPE, null))).isNotNull()
    }

    @Test
    fun `checkField should allow type with format not set`() {
        assertThat(rule.checkField("", createSchema(STRING_TYPE, null))).isNull()
    }

    @Test
    fun `checkField should allow non-common field with no format set`() {
        assertThat(rule.checkField("unknown", createSchema(STRING_TYPE, null))).isNull()
    }

    @Test
    fun `checkField should allow valid formats of common fields`() {
        assertThat(rule.checkField("id", createSchema(STRING_TYPE, UUID_FORMAT))).isNull()
        assertThat(rule.checkField("created", createSchema(STRING_TYPE, DATE_TIME_FORMAT))).isNull()
        assertThat(rule.checkField("modified", createSchema(STRING_TYPE, DATE_TIME_FORMAT))).isNull()
    }

    @Test
    fun `checkField should return a violation description if field type has invalid format`() {
        assertThat(rule.checkField("id", createSchema(INTEGER_TYPE, null))).isNotNull()
    }

    @Test
    fun `checkTypesOfCommonFields should not return any violations for a minimal api`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths: {}
        """.trimIndent())

        assertThat(rule.checkTypesOfCommonFields(context)).isEmpty()
    }

    @Test
    fun `checkTypesOfCommonFields should not return any violations for a specification with non-common fields`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            components:
              schemas:
                Pet:
                  properties:
                    name:
                      type: string
            paths: {}
        """.trimIndent())

        assertThat(rule.checkTypesOfCommonFields(context)).isEmpty()
    }

    @Test
    fun `checkTypesOfCommonFields should not return any violations for a specification with valid common fields`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            components:
              schemas:
                Pet:
                  properties:
                    id:
                      type: string
            paths: {}
        """.trimIndent())

        assertThat(rule.checkTypesOfCommonFields(context)).isEmpty()
    }

    @Test
    fun `checkTypesOfCommonFields should return a violation for a specification with invalid common field in a schema`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            components:
              schemas:
                Pet:
                  properties:
                    id:
                      type: integer
            paths: {}
        """.trimIndent())

        val violations = rule.checkTypesOfCommonFields(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).containsPattern(".*expected type 'string'.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/Pet/properties/id")
    }

    @Test
    fun `checkTypesOfCommonFields should return a violation if common field has a valid type but invalid format`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            components:
              schemas:
                Pet:
                  properties:
                    modified:
                      type: string
                      format: uuid
            paths: {}
        """.trimIndent())

        val violations = rule.checkTypesOfCommonFields(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).containsPattern(".*expected format 'date-time'.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/Pet/properties/modified")
    }

    @Test
    fun `checkTypesOfCommonFields should return a violation for invalid common field embedded in path segment as response`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths:
              /pets:
                get:
                  responses:
                    200:
                      description: Lorem Ipsum
                      content:
                        application/json:
                          schema:
                            properties:
                              id:
                                type: integer
        """.trimIndent())

        val violations = rule.checkTypesOfCommonFields(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).containsPattern(".*expected type 'string'.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1pets/get/responses/200/content/application~1json/schema/properties/id")
    }

    @Test
    fun `checkTypesOfCommonFields should return a violation for invalid common field in nested objects`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            components:
              schemas:
                Pet:
                  properties:
                    address:
                      properties:
                        modified:
                          type: number
            paths: {}
        """.trimIndent())

        val violations = rule.checkTypesOfCommonFields(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).containsPattern(".*expected type 'string'.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/Pet/properties/address/properties/modified")
    }

    @Test
    fun `checkTypesOfCommonFields should also test references`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent("""
            openapi: 3.0.1
            info: {title: "Lorem Ipsum", version: "1.0.0"}
            paths:
              /pets:
                get:
                  responses:
                    200:
                      description: Lorem Ipsum
                      content:
                        application/json:
                          schema:
                            properties:
                              id:
                                "${'$'}ref": "#/components/schemas/CustomId"
            components:
              schemas:
                CustomId:
                  type: integer
                  format: int64
        """.trimIndent())

        val violations = rule.checkTypesOfCommonFields(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).containsPattern(".*expected type 'string'.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1pets/get/responses/200/content/application~1json/schema/properties/id")
    }
}
