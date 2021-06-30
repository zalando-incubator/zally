package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.zalando.zally.core.ApiValidator
import org.zalando.zally.core.RulesPolicy
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.util.resourceToString

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class ApiReviewRepositoryTest {

    @Autowired
    private lateinit var apiReviewRepository: ApiReviewRepository

    @Autowired
    private lateinit var rulesValidator: ApiValidator

    @Before
    fun setUp() {
        apiReviewRepository.deleteAll()
    }

    @Test
    fun shouldReturnResultsWithoutCustomLabels() {
        val givenRequest = ApiDefinitionRequest(
            apiDefinition = resourceToString("fixtures/openapi3_petstore.json")
        )
        val givenApiReview = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = rulesValidator.validate(givenRequest.apiDefinition!!, RulesPolicy(listOf()))
        )

        apiReviewRepository.save(givenApiReview)

        val actual = apiReviewRepository.findLatestApiReviews()
        assertThat(actual.size).isEqualTo(1)
    }

    @Test
    fun shouldOnlyReturnOneResultWhenMultipleReviewsExistWithSameName() {
        val givenLabels = mapOf(Pair("custom", "label"))
        val givenRequest = ApiDefinitionRequest(
            apiDefinition = resourceToString("fixtures/openapi3_petstore.json"),
            customLabels = givenLabels
        )

        val givenApiReview1 = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = rulesValidator.validate(givenRequest.apiDefinition!!, RulesPolicy(listOf()))
        )
        apiReviewRepository.save(givenApiReview1)

        val givenApiReview2 = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = rulesValidator.validate(givenRequest.apiDefinition!!, RulesPolicy(listOf()))
        )
        apiReviewRepository.save(givenApiReview2)

        val actual = apiReviewRepository.findLatestApiReviews(listOf("label"))

        assertThat(actual.size).isEqualTo(1)
    }

    @Test
    fun shouldReturnResultsWithSameNameButDifferentLabelValues() {
        val givenRequest = ApiDefinitionRequest(
            apiDefinition = resourceToString("fixtures/openapi3_petstore.json"),
            customLabels = mapOf(Pair("custom", "label"))
        )

        val givenApiReview1 = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = rulesValidator.validate(givenRequest.apiDefinition!!, RulesPolicy(listOf()))
        )
        apiReviewRepository.save(givenApiReview1)

        val givenRequest2 = ApiDefinitionRequest(
            apiDefinition = resourceToString("fixtures/openapi3_petstore.json"),
            customLabels = mapOf(Pair("custom", "otherlabel"))
        )
        val givenApiReview2 = ApiReview(
            givenRequest2,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = rulesValidator.validate(givenRequest.apiDefinition!!, RulesPolicy(listOf()))
        )
        apiReviewRepository.save(givenApiReview2)

        val actual = apiReviewRepository.findLatestApiReviews()

        assertThat(actual.size).isEqualTo(2)
    }

    @Test
    fun shouldPersistCustomLabels() {
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
        apiReviewRepository.save(givenApiReview)

        val actual = apiReviewRepository.findLatestApiReviews(listOf("label"))

        assertThat(actual.size).isEqualTo(1)
        assertThat(actual[0].customLabels).containsAllEntriesOf(givenLabels)
    }
}
