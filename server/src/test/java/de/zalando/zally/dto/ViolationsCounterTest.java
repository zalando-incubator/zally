package de.zalando.zally.dto;

import de.zalando.zally.rule.AvoidTrailingSlashesRule;
import de.zalando.zally.rule.Violation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;


public class ViolationsCounterTest {
    private static void assertCounters(Map<ViolationType, Integer> expectedCounters, List<Violation> violations)
            throws AssertionError {

        final ViolationsCounter counter = new ViolationsCounter(violations);
        for (ViolationType violationType : expectedCounters.keySet()) {
            assertEquals(expectedCounters.get(violationType), counter.getCounter(violationType));
        }
    }

    @Test
    public void returnsZerosWhenViolationListIsEmpty() {
        final List<Violation> violations = new ArrayList<>();
        final Map<ViolationType, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(ViolationType.MUST, 0);
        expectedCounters.put(ViolationType.SHOULD, 0);
        expectedCounters.put(ViolationType.MAY, 0);
        expectedCounters.put(ViolationType.HINT, 0);

        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsMustViolations() {
        final List<Violation> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
                (i) -> violations.add(generateViolation(ViolationType.MUST))
        );
        final Map<ViolationType, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(ViolationType.MUST, 5);
        expectedCounters.put(ViolationType.SHOULD, 0);
        expectedCounters.put(ViolationType.MAY, 0);
        expectedCounters.put(ViolationType.HINT, 0);

        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsShouldViolations() {
        final List<Violation> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
                (i) -> violations.add(generateViolation(ViolationType.SHOULD))
        );
        final Map<ViolationType, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(ViolationType.MUST, 0);
        expectedCounters.put(ViolationType.SHOULD, 5);
        expectedCounters.put(ViolationType.MAY, 0);
        expectedCounters.put(ViolationType.HINT, 0);
        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsMayViolations() {
        final List<Violation> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
                (i) -> violations.add(generateViolation(ViolationType.MAY))
        );
        final Map<ViolationType, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(ViolationType.MUST, 0);
        expectedCounters.put(ViolationType.SHOULD, 0);
        expectedCounters.put(ViolationType.MAY, 5);
        expectedCounters.put(ViolationType.HINT, 0);

        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsHintViolations() {
        final List<Violation> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
                (i) -> violations.add(generateViolation(ViolationType.HINT))
        );
        final Map<ViolationType, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(ViolationType.MUST, 0);
        expectedCounters.put(ViolationType.SHOULD, 0);
        expectedCounters.put(ViolationType.MAY, 0);
        expectedCounters.put(ViolationType.HINT, 5);

        assertCounters(expectedCounters, violations);
    }

    private Violation generateViolation(ViolationType violationType) {
        return new Violation(
                new AvoidTrailingSlashesRule(),
                "Test Name",
                "Test Description",
                violationType,
                "https://www.example.com/",
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}
