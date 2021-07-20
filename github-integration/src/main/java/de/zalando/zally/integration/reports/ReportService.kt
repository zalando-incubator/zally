package de.zalando.zally.integration.reports

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.github.PullRequestEvent
import de.zalando.zally.integration.validation.ValidationNotFoundException
import de.zalando.zally.integration.validation.ValidationRepository
import de.zalando.zally.integration.zally.ApiDefinitionResponse
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val validationRepository: ValidationRepository,
    private val jsonObjectMapper: ObjectMapper
) {

    fun getReport(id: Long): Report {
        val validation = validationRepository.findById(id)
            .orElseThrow { ValidationNotFoundException("Not found Validation with id $id") }
        val pullRequest = jsonObjectMapper.readValue(validation.pullRequestInfo, PullRequestEvent::class.java)

        return Report(validation.id, pullRequest, validation.apiValidations.map {
            val apiDefinitionResponse = jsonObjectMapper.readValue(it.violations, ApiDefinitionResponse::class.java)
            DefinitionValidationResult(it.fileName, it.apiDefinition, apiDefinitionResponse)
        })
    }
}

data class Report(
    val id: Long?,
    val pullRequestEvent: PullRequestEvent?,
    val validationResults: List<DefinitionValidationResult>
)

data class DefinitionValidationResult(
    val fileName: String?,
    val apiDefinition: String?,
    val apiDefinitionResponse: ApiDefinitionResponse?
)
