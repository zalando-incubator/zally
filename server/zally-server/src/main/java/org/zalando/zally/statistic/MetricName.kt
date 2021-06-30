package org.zalando.zally.statistic

enum class MetricName(val value: String) {

    NUMBER_OF_ENDPOINTS("number_of_endpoints_total"),
    MUST_VIOLATIONS("must_violations_total"),
    SHOULD_VIOLATIONS("should_violations_total"),
    MAY_VIOLATIONS("may_violations_total"),
    HINT_VIOLATIONS("hint_violations_total")
}
