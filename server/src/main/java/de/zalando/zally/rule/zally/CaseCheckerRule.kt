package de.zalando.zally.rule.zally

import com.typesafe.config.Config
import de.zalando.zally.rule.CaseChecker
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "M010",
    severity = Severity.MUST,
    title = "Check case of various terms"
)
class CaseCheckerRule(@Autowired config: Config) {

    private val checker = CaseChecker.load(config)

    @Check(severity = Severity.MUST)
    fun checkPropertyNames(context: Context): List<Violation> =
        checker.checkPropertyNames(context)

    @Check(severity = Severity.MUST)
    fun checkPathParameterNames(context: Context): List<Violation> =
        checker.checkPathParameterNames(context)

    @Check(severity = Severity.MUST)
    fun checkQueryParameterNames(context: Context): List<Violation> =
        checker.checkQueryParameterNames(context)

    @Check(severity = Severity.MUST)
    fun checkHeaderNames(context: Context): List<Violation> =
        checker.checkHeadersNames(context)

    @Check(severity = Severity.MUST)
    fun checkPathSegments(context: Context): List<Violation> =
        checker.checkPathSegments(context)
}
