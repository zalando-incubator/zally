package org.zalando.zally.statistic

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.zalando.zally.apireview.ApiReviewRepository
import org.zalando.zally.configuration.ReviewMetricsProperties

@Component
class ReviewMetrics(private val apiReviewRepository: ApiReviewRepository, private val meterRegistry: MeterRegistry, private val properties: ReviewMetricsProperties) {

    final val statisticsReferences = mutableListOf<StatisticReference>()

    @Scheduled(fixedDelayString = "\${metrics.review.fixed-delay:300000}", initialDelay = 10000)
    fun updateMetrics() {
        LOG.debug("Updating metrics for review statistics")
        when {
            properties.filterLabels.isEmpty() -> {
                apiReviewRepository.findLatestApiReviews()
            }
            else -> {
                apiReviewRepository.findLatestApiReviews(properties.filterLabels)
            }
        }
            .map(ReviewStatisticsByName.Factory::of)
            .forEach { statistic ->
                statisticsReferences
                    .filter { it.statisticName == statistic.name }
                    .filter { it.customLabels == statistic.customLabels }
                    .map { reference ->
                        LOG.debug("Updating metrics reference values for review statistic ${statistic.name} " +
                            "with labels ${statistic.customLabels}")
                        reference.metricValueFor(MetricName.NUMBER_OF_ENDPOINTS).set(statistic.numberOfEndpoints)
                        reference.metricValueFor(MetricName.MUST_VIOLATIONS).set(statistic.mustViolations)
                        reference.metricValueFor(MetricName.SHOULD_VIOLATIONS).set(statistic.shouldViolations)
                        reference.metricValueFor(MetricName.MAY_VIOLATIONS).set(statistic.mayViolations)
                        reference.metricValueFor(MetricName.HINT_VIOLATIONS).set(statistic.hintViolations)
                    }
                    .ifEmpty {
                        LOG.debug("Creating new metrics reference for review statistic ${statistic.name} " +
                            "with labels ${statistic.customLabels}")
                        val reference = StatisticReference.of(statistic)
                        statisticsReferences.add(reference)
                        registerGaugeMetric(reference)
                    }
            }
    }

    private fun registerGaugeMetric(reference: StatisticReference) {
        // micrometer does not like names with spaces so we replace whitespaces with underscores
        val snakeCasedApiName = reference.statisticName.replace(Regex("\\p{Zs}+"), "_").toLowerCase()
        val tags = reference.customLabels.entries.map { Tag.of(it.key, it.value) }.toMutableList()
        tags.add(Tag.of("api_name", snakeCasedApiName))
        reference.metricPair.forEach { metricPair ->
            val metricName = "${properties.namePrefix}${metricPair.metricName.value}"
            Gauge
                .builder(metricName, metricPair.metricValue, { it.toDouble() })
                .tags(tags)
                .register(meterRegistry)
            LOG.debug("Registered micrometer gauge $metricName for api $snakeCasedApiName with tags $tags")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewMetrics::class.java)
    }
}
