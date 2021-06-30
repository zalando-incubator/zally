package org.zalando.zally.statistic

import com.fasterxml.jackson.core.JsonPointer
import io.micrometer.core.instrument.MeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.zalando.zally.apireview.ApiReview
import org.zalando.zally.apireview.ApiReviewRepository
import org.zalando.zally.configuration.ReviewMetricsProperties
import org.zalando.zally.core.Result
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.util.readApiDefinition
import org.zalando.zally.util.resourceToString
import java.net.URI

@RunWith(MockitoJUnitRunner::class)
class ReviewMetricsTest {

    @InjectMocks
    private lateinit var reviewMetrics: ReviewMetrics

    @Mock
    private lateinit var reviewMetricsProperties: ReviewMetricsProperties

    @Mock
    private lateinit var apiReviewRepository: ApiReviewRepository

    @Mock
    private lateinit var meterRegistry: MeterRegistry

    @Before
    fun setUp() {
        Mockito.`when`(reviewMetricsProperties.namePrefix).thenReturn("zally_")
        Mockito.`when`(reviewMetricsProperties.filterLabels).thenReturn(listOf())
    }

    @Test
    fun shouldInitializeStatisticsReferenceMap() {
        val givenRequest = readApiDefinition("fixtures/openapi3_petstore.json")
        val givenApiReview = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = emptyList()
        )

        val givenListOfStatistics = listOf(givenApiReview)
        Mockito.`when`(apiReviewRepository.findLatestApiReviews()).thenReturn(givenListOfStatistics)

        reviewMetrics.updateMetrics()

        assertThat(reviewMetrics.statisticsReferences.size).isEqualTo(givenListOfStatistics.size)
        reviewMetrics.statisticsReferences.forEach { reference ->
            reference.metricPair.forEach {
                assertThat(it.metricValue.get()).isEqualTo(0L)
            }
        }
    }

    @Test
    fun shouldUpdateExistingReferencesWhenUpdatingMetricsValues() {
        // first we make sure we hold a reference to some review statistics
        val givenRequest = readApiDefinition("fixtures/openapi3_petstore.json")
        val mustViolation = Result(
            "id",
            URI("/"),
            "violation",
            "something is not right",
            Severity.MUST,
            JsonPointer.empty()
        )
        val givenApiReview = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = listOf(mustViolation)
        )

        Mockito.`when`(apiReviewRepository.findLatestApiReviews()).thenReturn(listOf(givenApiReview))

        reviewMetrics.updateMetrics()

        val actual = reviewMetrics.statisticsReferences[0].metricPair.first { it.metricName == MetricName.MUST_VIOLATIONS }
        assertThat(actual.metricValue).hasValue(1L)

        // now we return a new result without violations for the same API and verify the reference is updated
        val givenNewApiReview = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = emptyList()
        )
        Mockito.`when`(apiReviewRepository.findLatestApiReviews()).thenReturn(listOf(givenNewApiReview))

        reviewMetrics.updateMetrics()

        assertThat(actual.metricValue).hasValue(0L)
    }

    @Test
    fun shouldInitializeNewMetricWhenLabelsAreDifferent() {
        val givenLabels = mapOf(Pair("custom", "label"))
        val givenRequest = ApiDefinitionRequest(
            apiDefinition = resourceToString("fixtures/openapi3_petstore.json"),
            customLabels = givenLabels
        )
        val givenApiReview = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = emptyList()
        )

        val givenLabels2 = mapOf(Pair("custom", "otherlabel"))
        val givenRequest2 = ApiDefinitionRequest(
            apiDefinition = resourceToString("fixtures/openapi3_petstore.json"),
            customLabels = givenLabels2
        )
        val givenApiReview2 = ApiReview(
            givenRequest2,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = emptyList()
        )

        val givenListOfStatistics = listOf(givenApiReview, givenApiReview2)
        Mockito.`when`(apiReviewRepository.findLatestApiReviews()).thenReturn(givenListOfStatistics)

        reviewMetrics.updateMetrics()

        assertThat(reviewMetrics.statisticsReferences.size).isEqualTo(givenListOfStatistics.size)
    }
}
