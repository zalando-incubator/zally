package de.zalando.zally.rule.zally

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers

/**
 * Rule highlighting that x-zally-ignore should be used sparingly
 */
@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "H002",
    severity = Severity.HINT,
    title = "Avoid using x-zally-ignore extension."
)
class AvoidXZallyIgnoreRule {

    private val xZallyIgnore = "x-zally-ignore"

    /**
     * Check the model doesn't use x-zally-ignore
     * @param root JsonNode root of the spec model
     * @return Violation iff x-zally-ignore is in use
     */
    @Check(severity = Severity.HINT)
    fun validate(root: JsonNode): List<Violation> = validateTree(JsonPointers.EMPTY, root)

    private fun validateTree(pointer: JsonPointer, node: JsonNode): List<Violation> =
        when {
            node.isArray -> validateArrayNode(pointer, node)
            node.isObject -> validateObjectNode(pointer, node)
            else -> emptyList()
        }

    private fun validateArrayNode(pointer: JsonPointer, node: JsonNode): List<Violation> =
        node.asSequence().toList().mapIndexed { index, childNode ->
            val childPointer = pointer.append(JsonPointer.compile("/$index"))
            validateTree(childPointer, childNode)
        }.flatten()

    private fun validateObjectNode(pointer: JsonPointer, node: JsonNode): List<Violation> =
        node.fields().asSequence().toList().flatMap { (name, childNode) ->
            val childPointer = pointer.append(JsonPointers.escape(name))
            when (name) {
                xZallyIgnore -> validateXZallyIgnore(childPointer, childNode)
                else -> validateTree(childPointer, childNode)
            }
        }

    private fun validateXZallyIgnore(pointer: JsonPointer, node: JsonNode): List<Violation> =
        listOf(Violation(
            when {
                node.isArray -> node.joinToString(
                    prefix = "Ignores rules ",
                    separator = ", ",
                    transform = JsonNode::asText
                )
                node.isValueNode -> "Invalid ignores, expected list but found single value $node"
                else -> "Invalid ignores, expected list but found $node"
            }, pointer
        ))
}
