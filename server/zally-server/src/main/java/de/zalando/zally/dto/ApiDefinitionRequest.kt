package de.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.zalando.zally.util.JsonRawValueDeserializer

data class ApiDefinitionRequest(

    @JsonRawValue
    @JsonDeserialize(using = JsonRawValueDeserializer::class)
    val apiDefinition: String? = null,

    val apiDefinitionString: String? = null,

    val apiDefinitionUrl: String? = null,

    val ignoreRules: List<String> = emptyList()
) {
    /** for java invocations: it doesn't have overloaded constructors */
    companion object Factory {

        fun fromJson(json: String) = ApiDefinitionRequest(json)

        fun fromUrl(url: String) = ApiDefinitionRequest(apiDefinitionUrl = url)
    }
}
