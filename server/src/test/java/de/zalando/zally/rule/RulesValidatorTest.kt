package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import kotlin.reflect.full.createInstance

class RulesValidatorTest {

    private val swaggerContent = javaClass.classLoader.getResource("fixtures/api_spp.json").readText(Charsets.UTF_8)

    @Rule(
            ruleSet = TestRuleSet::class,
            id = "TestFirstRule",
            severity = Severity.SHOULD,
            title = "First Rule"
    )
    class TestFirstRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.SHOULD)
        fun validate(adapter: ApiAdapter): List<Violation> = listOf(
                Violation("dummy1", listOf("x", "y", "z")),
                Violation("dummy2", listOf()))
    }

    @Rule(
            ruleSet = TestRuleSet::class,
            id = "TestSecondRule",
            severity = Severity.MUST,
            title = "Second Rule"
    )
    class TestSecondRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun validate(adapter: ApiAdapter, ignore: SwaggerIgnoreExtension): Violation? =
                Violation("dummy3", listOf("a"))
    }

    @Rule(
            ruleSet = TestRuleSet::class,
            id = "TestBadRule",
            severity = Severity.MUST,
            title = "Third Rule"
    )
    class TestBadRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun invalid(adapter: ApiAdapter): String = "Hello World!"

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun invalidParams(adapter: ApiAdapter, json: JsonNode, text: String): Violation? = null
    }

    @Test
    fun shouldReturnEmptyViolationsListWithoutRules() {
        val rules = emptyList<Any>()
        val validator = OpenApiRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results)
                .isEmpty()
    }

    @Test
    fun shouldReturnOneViolation() {
        val rules = listOf(TestSecondRule())
        val validator = OpenApiRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy3")
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val rules = listOf(TestFirstRule())
        val validator = OpenApiRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldSortViolationsByViolationType() {
        val rules = listOf(TestFirstRule(), TestSecondRule())
        val validator = OpenApiRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy3", "dummy1", "dummy2")
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val rules = listOf(TestFirstRule(), TestSecondRule())
        val validator = OpenApiRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(arrayOf("TestSecondRule")))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun checkReturnsStringThrowsException() {
        val rules = listOf(TestBadRule())
        assertThatThrownBy {
            val validator = OpenApiRulesValidator(rulesManager(rules))
            validator.validate(swaggerContent, RulesPolicy(arrayOf("TestCheckApiNameIsPresentRule")))
        }.hasMessage("Unsupported return type for a @Check method!: class java.lang.String")
    }

    private fun rulesManager(rules: List<Any>): RulesManager {
        return RulesManager(
                rules.mapNotNull { instance ->
                    val rule = instance::class.java.getAnnotation(Rule::class.java)
                    val ruleSet = rule?.ruleSet?.createInstance()
                    ruleSet?.let { RuleDetails(ruleSet, rule, instance) }
                }
        )
    }
}
