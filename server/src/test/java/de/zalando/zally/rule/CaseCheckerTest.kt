package de.zalando.zally.rule

import org.assertj.core.api.Assertions
import org.junit.Test

class CaseCheckerTest {

    private val upper = "[A-Z]+".toRegex()
    private val lower = "[a-z]+".toRegex()
    private val white = "white".toRegex()
    private val cases = sortedMapOf(
        "lower" to lower,
        "UPPER" to upper
    )
    private val check = CaseChecker.CaseCheck(lower, listOf(white))
    private val cut = CaseChecker(cases, check)

    @Test
    fun `check with matching input returns null`() {
        Assertions
            .assertThat(cut.check("PREFIX", "PREFIXES", check, "lower"))
            .isNull()
    }

    @Test
    fun `check with unmatched input returns message`() {
        Assertions
            .assertThat(cut.check("PREFIX", "PREFIXES", check, "012345"))
            .isEqualTo("PREFIX '012345' does not match lower ([a-z]+)")
    }

    @Test
    fun `check with unmatched inputs returns message`() {
        Assertions
            .assertThat(cut.check("PREFIX", "PREFIXES", check, "012", "345"))
            .isEqualTo("PREFIXES '012', '345' do not match lower ([a-z]+)")
    }

    @Test
    fun `check with mismatched input returns message with suggestions`() {
        Assertions
            .assertThat(cut.check("PREFIX", "PREFIXES", check, "INPUT"))
            .isEqualTo("PREFIX 'INPUT' does not match lower ([a-z]+) but seems to be UPPER")
    }

    @Test
    fun `check with mismatched inputs returns message with suggestions`() {
        Assertions
            .assertThat(cut.check("PREFIX", "PREFIXES", check, "IN", "PUT"))
            .isEqualTo("PREFIXES 'IN', 'PUT' do not match lower ([a-z]+) but seems to be UPPER")
    }
}