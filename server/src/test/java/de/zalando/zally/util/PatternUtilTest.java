package de.zalando.zally.util;

import org.junit.Test;

import static de.zalando.zally.util.PatternUtil.hasTrailingSlash;
import static de.zalando.zally.util.PatternUtil.hasVersionInUrl;
import static de.zalando.zally.util.PatternUtil.isCamelCase;
import static de.zalando.zally.util.PatternUtil.isHyphenated;
import static de.zalando.zally.util.PatternUtil.isHyphenatedCamelCase;
import static de.zalando.zally.util.PatternUtil.isHyphenatedPascalCase;
import static de.zalando.zally.util.PatternUtil.isKebabCase;
import static de.zalando.zally.util.PatternUtil.isLowerCaseAndHyphens;
import static de.zalando.zally.util.PatternUtil.isPascalCase;
import static de.zalando.zally.util.PatternUtil.isPathVariable;
import static de.zalando.zally.util.PatternUtil.isSnakeCase;
import static de.zalando.zally.util.PatternUtil.isVersion;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for patterns utility
 */
public class PatternUtilTest {

    @Test
    public void checkHasTrailingSlash() {
        assertTrue(hasTrailingSlash("blah/"));
        assertFalse(hasTrailingSlash("blah"));
    }

    @Test
    public void checkIsLowerCaseAndHyphens() {
        assertTrue(isLowerCaseAndHyphens("a-b-c"));
        assertTrue(isLowerCaseAndHyphens("abc"));
        assertFalse(isLowerCaseAndHyphens("A-B-C"));
    }

    @Test
    public void checkIsPathVariable() {
        assertTrue(isPathVariable("{test}"));
        assertFalse(isPathVariable("{}"));
        assertFalse(isPathVariable(" { } "));
        assertFalse(isPathVariable("abc"));
        assertFalse(isPathVariable("{test"));
        assertFalse(isPathVariable("test}"));
    }

    @Test
    public void checkIsCamelCase() {
        assertTrue(isCamelCase("testCase"));
        assertFalse(isCamelCase("TestCase"));
    }

    @Test
    public void checkIsPascalCase() {
        assertTrue(isPascalCase("TestCase"));
        assertFalse(isPascalCase("testCase"));
    }

    @Test
    public void checkIsHyphenatedCamelCase() {
        assertTrue(isHyphenatedCamelCase("test-Case"));
        assertFalse(isHyphenatedCamelCase("Test-Case"));
        assertFalse(isHyphenatedCamelCase("testCase"));
        assertFalse(isHyphenatedCamelCase("TestCase"));
    }

    @Test
    public void checkIsHyphenatedPascalCase() {
        assertTrue(isHyphenatedPascalCase("Test-Case"));
        assertTrue(isHyphenatedPascalCase("X-Flow-Id"));
        assertFalse(isHyphenatedPascalCase("test-Case"));
        assertFalse(isHyphenatedPascalCase("TestCase"));
        assertFalse(isHyphenatedPascalCase("testCase"));
    }

    @Test
    public void checkIsSnakeCase() {
        assertTrue(isSnakeCase("test_case"));
        assertTrue(isSnakeCase("test"));
        assertFalse(isSnakeCase("TestCase"));
        assertFalse(isSnakeCase("Test_Case"));
        assertFalse(isSnakeCase(""));
        assertFalse(isSnakeCase("_"));
        assertFalse(isSnakeCase("customer-number"));
        assertFalse(isSnakeCase("_customer_number"));
        assertFalse(isSnakeCase("CUSTOMER_NUMBER"));
    }

    @Test
    public void checkIsKebabCase() {
        assertTrue(isKebabCase("test-case"));
        assertFalse(isKebabCase("test-Case"));
        assertFalse(isKebabCase("testCase"));
    }

    @Test
    public void checkIsHyphenated() {
        // uncontroversial positive cases
        assertTrue(isHyphenated("A"));
        assertTrue(isHyphenated("low"));
        assertTrue(isHyphenated("Aa"));
        assertTrue(isHyphenated("A-A"));
        assertTrue(isHyphenated("X-Auth-2.0"));
        assertTrue(isHyphenated("This-Is-Some-Hyphenated-String"));
        assertTrue(isHyphenated("this-is-other-hyphenated-string"));

        // uncontroversial negative cases
        assertFalse(isHyphenated("Sorry no hyphens here"));
        assertFalse(isHyphenated("a--a"));

        // issue 572
        assertTrue(isHyphenated("X-RateLimit-Reset"));

        // the following are all perfectly valid single-term 'hypenated' headers
        assertTrue(isHyphenated("aA"));
        assertTrue(isHyphenated("AA"));
        assertTrue(isHyphenated("CamelCaseIsNotAcceptableAndShouldBeIllegal"));
    }

    @Test
    public void checkHasVersionInUrl() {
        assertTrue(hasVersionInUrl("path/to/v1"));
        assertTrue(hasVersionInUrl("path/to/v1/"));
        assertFalse(hasVersionInUrl("path/to"));
    }

    @Test
    public void checkGenericIsVersion() {
        assertFalse(isVersion("*"));
        assertFalse(isVersion("1"));
        assertFalse(isVersion("1.2"));
        assertFalse(isVersion("12.3"));
        assertTrue(isVersion("1.2.3"));
        assertFalse(isVersion("1.23"));
        assertTrue(isVersion("1.2.34"));
        assertTrue(isVersion("123.456.789"));
        assertFalse(isVersion("1.2.*"));
        assertFalse(isVersion("1.*"));
        assertFalse(isVersion("a"));
        assertFalse(isVersion("1.a"));
        assertFalse(isVersion("*.1"));
        assertFalse(isVersion("1.*.2"));
    }
}
