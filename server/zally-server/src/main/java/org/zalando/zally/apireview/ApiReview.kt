package org.zalando.zally.apireview

import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import org.zalando.zally.core.Result
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.rule.api.Severity
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.CollectionTable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.MapKeyColumn
import javax.persistence.OneToMany
import javax.persistence.CascadeType

@Suppress("unused")
@Entity
class ApiReview(
    request: ApiDefinitionRequest,
    val userAgent: String = "",
    @Suppress("CanBeParameter") val apiDefinition: String,
    violations: List<Result> = emptyList(),
    val name: String? = OpenApiHelper.extractApiName(apiDefinition),
    val apiId: String? = OpenApiHelper.extractApiId(apiDefinition),
    @Column(nullable = false) @Type(
        type = "org.jadira.usertype.dateandtime.threeten.PersistentOffsetDateTime",
        parameters = [Parameter(name = "javaZone", value = "UTC")]
    ) val created: OffsetDateTime = Instant.now().atOffset(ZoneOffset.UTC),
    @Column(nullable = false)
    val day: LocalDate? = created.toLocalDate(),
    val numberOfEndpoints: Int = EndpointCounter.count(apiDefinition)
) : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    val externalId: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val jsonPayload: String = request.toString()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "custom_label_mapping",
        joinColumns = [JoinColumn(name = "api_review_id", referencedColumnName = "id")]
    )
    @MapKeyColumn(name = "label_name")
    @Column(name = "label_value", nullable = false)
    val customLabels: Map<String, String> = request.customLabels

    @Column(nullable = false, name = "successfulProcessed")
    val isSuccessfulProcessed: Boolean = apiDefinition.isNotBlank()

    @OneToMany(mappedBy = "apiReview", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val ruleViolations: List<RuleViolation> = violations.map { RuleViolation(this, it) }

    val mustViolations: Int = countViolations(Severity.MUST)
    val shouldViolations: Int = countViolations(Severity.SHOULD)
    val mayViolations: Int = countViolations(Severity.MAY)
    val hintViolations: Int = countViolations(Severity.HINT)

    private fun countViolations(severity: Severity) =
        ruleViolations.stream().filter { r -> r.type === severity }.count().toInt()
}
