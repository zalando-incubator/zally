package org.zalando.zally.apireview

import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.dto.ApiDefinitionResponse
import org.zalando.zally.dto.ViolationDTO
import org.zalando.zally.exception.ApiReviewNotFoundException
import org.zalando.zally.exception.InaccessibleResourceUrlException
import org.zalando.zally.exception.MissingApiDefinitionException
import org.zalando.zally.core.ApiValidator
import org.zalando.zally.core.RulesPolicy
import org.zalando.zally.rule.api.Severity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@CrossOrigin
@RestController
class ApiViolationsController(
    private val rulesValidator: ApiValidator,
    private val apiDefinitionReader: ApiDefinitionReader,
    private val apiReviewRepository: ApiReviewRepository,
    private val serverMessageService: ServerMessageService,
    private val configPolicy: RulesPolicy
) {

    @ResponseBody
    @PostMapping("/api-violations")
    fun validate(
        @RequestBody(required = true) request: ApiDefinitionRequest,
        @RequestHeader(value = "User-Agent", required = false) userAgent: String?,
        @RequestHeader(value = "Authorization", required = false) authorization: String?,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<ApiDefinitionResponse> {
        val apiDefinition = retrieveApiDefinition(request)

        val requestPolicy = retrieveRulesPolicy(request)

        val violations = rulesValidator.validate(apiDefinition, requestPolicy, authorization)
        val review = ApiReview(request, userAgent.orEmpty(), apiDefinition, violations)
        apiReviewRepository.save(review)

        val location = uriBuilder
            .path("/api-violations/{externalId}")
            .buildAndExpand(review.externalId)
            .toUri()

        return ResponseEntity
            .status(HttpStatus.OK)
            .location(location)
            .body(buildApiDefinitionResponse(review))
    }

    @ResponseBody
    @GetMapping("/api-violations/{externalId}")
    fun getExistingViolationResponse(
        @PathVariable(value = "externalId") externalId: UUID
    ): ApiDefinitionResponse {
        val review = apiReviewRepository.findByExternalId(externalId) ?: throw ApiReviewNotFoundException()

        return buildApiDefinitionResponse(review)
    }

    private fun retrieveRulesPolicy(request: ApiDefinitionRequest): RulesPolicy = request.ignoreRules
        .let { configPolicy.withMoreIgnores(request.ignoreRules) }

    private fun retrieveApiDefinition(request: ApiDefinitionRequest): String = try {
        apiDefinitionReader.read(request)
    } catch (e: MissingApiDefinitionException) {
        apiReviewRepository.save(ApiReview(request, "", ""))
        throw e
    } catch (e: InaccessibleResourceUrlException) {
        apiReviewRepository.save(ApiReview(request, "", ""))
        throw e
    }

    private fun buildApiDefinitionResponse(review: ApiReview): ApiDefinitionResponse = ApiDefinitionResponse(
        externalId = review.externalId,
        message = serverMessageService.serverMessage(review.userAgent),
        violations = review.ruleViolations
            .sortedBy(RuleViolation::type)
            .map {
                ViolationDTO(
                    it.ruleTitle,
                    it.description,
                    it.type,
                    it.ruleUrl,
                    listOfNotNull(it.locationPointer),
                    it.locationPointer,
                    it.locationLineStart,
                    it.locationLineEnd
                )
            },
        violationsCount = listOf(
            Severity.MUST to review.mustViolations,
            Severity.SHOULD to review.shouldViolations,
            Severity.MAY to review.mayViolations,
            Severity.HINT to review.hintViolations
        ).map { it.first.name.toLowerCase() to it.second }.toMap(),
        apiDefinition = review.apiDefinition
    )
}
