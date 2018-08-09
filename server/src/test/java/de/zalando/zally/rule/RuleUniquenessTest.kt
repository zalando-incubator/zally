package de.zalando.zally.rule

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class RuleUniquenessTest {

    @Autowired
    lateinit var rules: RulesManager

    @Test
    fun ruleIdsShouldBeUnique() {
        val duplicatedCodes = rules.rules
                .filterNot { it.rule.title == "TestUseOpenApiRule" }
                .groupBy { it.rule.id }
                .filterValues { it.size > 1 }

        assertThat(duplicatedCodes)
                .hasToString("{}")
    }

    @Test
    fun ruleTitlesShouldBeUnique() {
        val duplicatedCodes = rules.rules
                .filterNot { it.rule.title == "TestUseOpenApiRule" }
                .groupBy { it.rule.title }
                .filterValues { it.size > 1 }

        assertThat(duplicatedCodes)
                .hasToString("{}")
    }
}
