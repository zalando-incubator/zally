package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.util.ast.ReverseAst
import io.swagger.util.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonRulesValidator(@Autowired rules: RulesManager) : RulesValidator<JsonNode>(rules) {
    private var ast: ReverseAst? = null

    override fun parse(content: String): ContentParseResult<JsonNode> {
        return try {
            val json = ObjectTreeReader().read(content)
            ast = createAst(json)
            ContentParseResult.Success(json)
        } catch (e: Exception) {
            ContentParseResult.NotApplicable()
        }
    }

    private fun createAst(json: JsonNode) = try {
        Json.mapper().convertValue(json, Map::class.java)
    } catch (e: Exception) {
        null
    }?.let {
        ReverseAst.fromObject(it).build()
    }

    override fun ignore(root: JsonNode, pointer: JsonPointer, ruleId: String) = ast?.isIgnored(pointer, ruleId) ?: false
}
