package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.CaseChecker
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "118",
    severity = Severity.MUST,
    title = "Property Names Must be ASCII snake_case"
)
class SnakeCaseInPropNameRule(@Autowired config: Config) {
    private val description = "Property name has to be snake_case"

    private val checker = CaseChecker.load(config)

    @Check(severity = Severity.MUST)
    fun checkPropertyNames(context: Context): List<Violation> =
        checker.checkPropertyNames(context).map { Violation(description, it.pointer!!) }
}
