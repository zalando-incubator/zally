package org.zalando.zally.ruleset.zalando

import org.intellij.lang.annotations.Language
import org.junit.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions

class UseJsonPayloadRuleTest {

    private val rule = UseJsonPayload()

    @Test
    fun `return violation if a request contains non JSON format`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            paths:
              /create:
                post:
                  summary: Create a pet
                  requestBody:
                      content:
                        application/custom-format:
                          schema:
                            type: object
                            properties:
                              req-prop:
                                type: string          
                  responses:
                    '201':
                      description: Null response
                    default:
                      description: unexpected error
              
            
        """.trimIndent()
        )

        val violations = rule.validatePayload(context)
        ZallyAssertions.assertThat(violations).hasSize(1)
    }

    @Test
    fun `return violation if a response contains non JSON format`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            paths:
              /list:
                get:
                  summary: Get data
                  responses:
                    '201':
                      description: Null response
                    default:
                      description: unexpected error
                      content:
                        application/pdf:
                          schema:
                            type: object
                            properties:
                              prop1:
                                type: string
              
            
        """.trimIndent()
        )

        val violations = rule.validatePayload(context)
        ZallyAssertions.assertThat(violations).hasSize(1)
    }

    @Test
    fun `return no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            paths:
              /create:
                post:
                  summary: Create a pet
                  requestBody:
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              req-prop:
                                type: string          
                  responses:
                    '201':
                      description: Null response
                    default:
                      description: unexpected error            
              /list:
                get:
                  summary: Get data
                  responses:
                    '201':
                      description: Null response
                    default:
                      description: unexpected error
                      content:
                        application/json:
                          schema:
                            type: object
                            properties:
                              prop1:
                                type: string
              
            
        """.trimIndent()
        )

        val violations = rule.validatePayload(context)
        ZallyAssertions.assertThat(violations).isEmpty()
    }
}
