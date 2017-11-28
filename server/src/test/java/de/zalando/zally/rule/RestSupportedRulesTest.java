package de.zalando.zally.rule;

import de.zalando.zally.apireview.RestApiBaseTest;
import de.zalando.zally.dto.RuleDTO;
import de.zalando.zally.dto.ViolationType;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.util.ErrorResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@TestPropertySource(properties = "zally.ignoreRules=166,145")
public class RestSupportedRulesTest extends RestApiBaseTest {

    private static final List<String> IGNORED_RULES = Arrays.asList("166", "145");

    @Autowired
    private List<Rule> implementedRules;

    @Test
    public void testRulesCount() {
        assertThat(getSupportedRules().size()).isEqualTo(implementedRules.size());
    }

    @Test
    public void testRulesOrdered() {
        final List<RuleDTO> rules = getSupportedRules();
        for(int i=1;i<rules.size();++i) {
            final ViolationType prev = rules.get(i - 1).getType();
            final ViolationType next = rules.get(i).getType();
            assertTrue("Item #" + i + " is out of order:\n" +
                    rules.stream().map(Object::toString).collect(joining("\n")),
                    prev.compareTo(next)<=0);
        }
    }

    @Test
    public void testRulesFields() {
        for (RuleDTO rule : getSupportedRules()) {
            assertThat(rule.getCode()).isNotEmpty();
            assertThat(rule.getTitle()).isNotEmpty();
            assertThat(rule.getType()).isNotNull();
            assertThat(rule.getUrl()).isNotNull();
        }
    }

    @Test
    public void testIsActiveFlag() {
        for (RuleDTO rule : getSupportedRules()) {
            assertThat(rule.getActive()).isEqualTo(!IGNORED_RULES.contains(rule.getCode()));
        }
    }

    @Test
    public void testFilterByType() {

        final int expectedCount = implementedRules.size();

        final int mustCount = getSupportedRules("MuSt", null).size();
        assertThat(mustCount).isLessThan(expectedCount);

        final int shouldCount = getSupportedRules("ShOuLd", null).size();
        assertThat(shouldCount).isLessThan(expectedCount);

        final int mayCount = getSupportedRules("MaY", null).size();
        assertThat(mayCount).isLessThan(expectedCount);

        final int couldCount = getSupportedRules("CoUlD", null).size();
        assertThat(couldCount).isLessThan(expectedCount);

        final int hintCount = getSupportedRules("HiNt", null).size();
        assertThat(hintCount).isLessThan(expectedCount);

        final int actualCount = mustCount + shouldCount + mayCount + couldCount + hintCount;
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Test
    public void testReturnsForUnknownType() {
        ResponseEntity<ErrorResponse> response = getSupportedRules("TOPKEK", null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo(APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody().getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
        assertThat(response.getBody().getStatus()).isNotEmpty();
        assertThat(response.getBody().getDetail()).isNotEmpty();
    }

    @Test
    public void testFilterByActiveTrue() {
        List<RuleDTO> rules = getSupportedRules(null, true);
        assertThat(rules.size()).isEqualTo(implementedRules.size() - IGNORED_RULES.size());
    }

    @Test
    public void testFilterByActiveFalse() {
        List<RuleDTO> rules = getSupportedRules(null, false);
        assertThat(rules.size()).isEqualTo(IGNORED_RULES.size());
    }
}
