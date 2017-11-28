package de.zalando.zally.rule.zalando

import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Swagger
import io.swagger.models.parameters.QueryParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QueryParameterCollectionFormatRuleTest {

    private val rule = QueryParameterCollectionFormatRule(ZalandoRuleSet())

    @Test
    fun negativeCaseCollectionFormatNotSupported() {
        val swagger = Swagger().apply {
            parameters = mapOf("test" to QueryParameter().apply { name = "test"; type = "array"; collectionFormat = "notSupported" })
        }

        val result = rule.validate(swagger)!!
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", "CollectionFormat should be one of: [csv, multi]")
                .hasFieldOrPropertyWithValue("paths", listOf("parameters test"))
    }

    @Test
    fun negativeCaseCollectionFormatNotSupportedFromPath() {
        val paramList = listOf(QueryParameter().apply { name = "test"; type = "array"; collectionFormat = "notSupported" })
        val swagger = Swagger().apply {
            paths = mapOf("/apis" to Path().apply { get = Operation().apply { parameters = paramList } })
        }

        val result = rule.validate(swagger)!!
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", "CollectionFormat should be one of: [csv, multi]")
                .hasFieldOrPropertyWithValue("paths", listOf("/apis test"))
    }

    @Test
    fun negativeCaseCollectionFormatNull() {
        val swagger = Swagger().apply {
            parameters = mapOf("test" to QueryParameter().apply { name = "test"; type = "array"; collectionFormat = null })
        }

        val result = rule.validate(swagger)!!
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", "CollectionFormat should be one of: [csv, multi]")
                .hasFieldOrPropertyWithValue("paths", listOf("parameters test"))
    }

    @Test
    fun negativeCaseCollectionFormatNullFromPath() {
        val paramList = listOf(QueryParameter().apply { name = "test"; type = "array"; collectionFormat = null })
        val swagger = Swagger().apply {
            paths = mapOf("/apis" to Path().apply { get = Operation().apply { parameters = paramList } })
        }

        val result = rule.validate(swagger)!!
        assertThat(result)
                .hasFieldOrPropertyWithValue("description", "CollectionFormat should be one of: [csv, multi]")
                .hasFieldOrPropertyWithValue("paths", listOf("/apis test"))
    }

    @Test
    fun positiveCaseCsv() {
        val swagger = Swagger().apply {
            parameters = mapOf("test" to QueryParameter().apply { name = "test"; type = "array"; collectionFormat = "csv" })
        }

        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseCsvFromPath() {
        val paramList = listOf(QueryParameter().apply { name = "test"; type = "array"; collectionFormat = "csv" })
        val swagger = Swagger().apply {
            paths = mapOf("/apis" to Path().apply { get = Operation().apply { parameters = paramList } })
        }

        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseMulti() {
        val swagger = Swagger().apply {
            parameters = mapOf("test" to QueryParameter().apply { name = "test"; type = "array"; collectionFormat = "multi" })
        }

        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseMultiFromPath() {
        val paramList = listOf(QueryParameter().apply { name = "test"; type = "array"; collectionFormat = "multi" })
        val swagger = Swagger().apply {
            paths = mapOf("/apis" to Path().apply { get = Operation().apply { parameters = paramList } })
        }

        assertThat(rule.validate(swagger)).isNull()
    }
}
