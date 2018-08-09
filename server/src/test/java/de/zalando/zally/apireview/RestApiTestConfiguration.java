package de.zalando.zally.apireview;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zally.rule.TestRuleSet;
import de.zalando.zally.rule.api.Check;
import de.zalando.zally.rule.api.Context;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.api.Violation;
import de.zalando.zally.rule.zalando.UseOpenApiRule;
import de.zalando.zally.util.ast.JsonPointers;
import org.assertj.core.util.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Configuration
public class RestApiTestConfiguration {

    @Bean
    @Primary
    @Profile("test")
    public Collection<Object> rules() {
        return Arrays.asList(
                new TestCheckIsOpenApi3(),
                new TestCheckAlwaysReport3MustViolations(),
                new TestUseOpenApiRule()
        );
    }

    /** Rule used for testing */
    @Rule(
            ruleSet = TestRuleSet.class,
            id = "TestCheckIsOpenApi3",
            severity = Severity.MUST,
            title = "TestCheckIsOpenApi3"
    )
    public static class TestCheckIsOpenApi3 {

        @Check(severity = Severity.MUST)
        public Violation validate(JsonNode json) {
            if (!"3.0.0".equals(json.path("openapi").textValue())) {
                return new Violation("TestCheckIsOpenApi3", JsonPointer.compile("/openapi"));
            }
            return null;
        }
    }

    /** Rule used for testing */
    @Rule(
            ruleSet = TestRuleSet.class,
            id = "TestCheckAlwaysReport3MustViolations",
            severity = Severity.MUST,
            title = "TestCheckAlwaysReport3MustViolations"
    )
    public static class TestCheckAlwaysReport3MustViolations {

        @Check(severity = Severity.MUST)
        public Iterable<Violation> validate(JsonNode json) {
            return Arrays.asList(
                    new Violation("TestCheckAlwaysReport3MustViolations #1", JsonPointers.empty()),
                    new Violation("TestCheckAlwaysReport3MustViolations #2", JsonPointers.empty()),
                    new Violation("TestCheckAlwaysReport3MustViolations #3", JsonPointers.empty())
            );
        }
    }

    @Rule(
        ruleSet = TestRuleSet.class,
        id = UseOpenApiRule.id,
        severity = Severity.MUST,
        title = "TestUseOpenApiRule"
    )
    public static class TestUseOpenApiRule {

        @Check(severity = Severity.HINT)
        public Iterable<Violation> validate(Context context) {
            return Lists.emptyList();
        }
    }
}
