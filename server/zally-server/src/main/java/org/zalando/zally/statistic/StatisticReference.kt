package org.zalando.zally.statistic

import java.util.concurrent.atomic.AtomicLong

data class StatisticReference(val statisticName: String, val metricPair: List<MetricPair>, val customLabels: Map<String, String>) {

    fun metricValueFor(metricName: MetricName) =
        this.metricPair[this.metricPair.indexOfFirst { p -> p.metricName == metricName }].metricValue

    companion object Factory {
        fun of(statistic: ReviewStatisticsByName) = StatisticReference(
            statistic.name,
            listOf(
                MetricPair(MetricName.NUMBER_OF_ENDPOINTS, AtomicLong(statistic.numberOfEndpoints)),
                MetricPair(MetricName.MUST_VIOLATIONS, AtomicLong(statistic.mustViolations)),
                MetricPair(MetricName.SHOULD_VIOLATIONS, AtomicLong(statistic.shouldViolations)),
                MetricPair(MetricName.MAY_VIOLATIONS, AtomicLong(statistic.mayViolations)),
                MetricPair(MetricName.HINT_VIOLATIONS, AtomicLong(statistic.hintViolations))
            ),
            statistic.customLabels
        )
    }
}
