package de.zalando.zally.rule.zalando

import com.google.common.collect.Sets
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Operation
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import io.swagger.models.auth.OAuth2Definition

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "104",
        severity = Severity.MUST,
        title = "Secure Endpoints with OAuth 2.0"
)
class SecureWithOAuth2Rule {
    private val description = "Every endpoint must be secured by OAuth2 properly"

    @Check(severity = Severity.MUST)
    fun checkSecurityDefinitions(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            val swagger = adapter.swagger!!
            val hasOAuth = swagger.securityDefinitions.orEmpty().values.any { it.type?.toLowerCase() == "oauth2" }
            val containsHttpScheme = swagger.schemes.orEmpty().contains(Scheme.HTTP)
            return if (!hasOAuth) {
                Violation("No OAuth2 security definitions found", emptyList())
            } else if (containsHttpScheme) {
                Violation("OAuth2 should be only used together with https", emptyList())
            } else {
                null
            }
        }
        return Violation.UNSUPPORTED_API_VERSION
    }

    @Check(severity = Severity.SHOULD)
    fun checkPasswordFlow(adapter: ApiAdapter): Violation? =
            if (adapter.isV2()) {
                checkPasswordFlowV2(adapter.swagger!!)
            } else {
                Violation.UNSUPPORTED_API_VERSION
            }

    private fun checkPasswordFlowV2(swagger: Swagger): Violation? {
        val definitionsWithoutPasswordFlow = swagger
                .securityDefinitions
                .orEmpty()
                .values
                .filter { it.type?.toLowerCase() == "oauth2" }
                .filter { (it as OAuth2Definition).flow != "application" }

        return if (definitionsWithoutPasswordFlow.any())
            Violation("OAuth2 security definitions should use application flow", emptyList())
        else null
    }

    @Check(severity = Severity.MUST)
    fun checkUsedScopes(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            return checkUsedScopesV2(adapter.swagger!!)
        }
        return Violation.UNSUPPORTED_API_VERSION
    }

    private fun checkUsedScopesV2(swagger: Swagger): Violation? {
        val definedScopes = getDefinedScopes(swagger)
        val hasTopLevelScope = hasTopLevelScope(swagger, definedScopes)
        val paths = swagger.paths.orEmpty().entries.flatMap { (pathKey, path) ->
            path.operationMap.orEmpty().entries.map { (method, operation) ->
                val actualScopes = extractAppliedScopes(operation)
                val undefinedScopes = Sets.difference(actualScopes, definedScopes)
                val unsecured = undefinedScopes.size == actualScopes.size && !hasTopLevelScope
                val msg = when {
                    undefinedScopes.isNotEmpty() ->
                        "undefined scopes: " + undefinedScopes.map { "'${it.second}'" }.joinToString(", ")
                    unsecured ->
                        "no valid OAuth2 scope"
                    else -> null
                }
                if (msg != null) "$pathKey $method has $msg" else null
            }.filterNotNull()
        }
        return if (!paths.isEmpty()) {
            Violation(description, paths)
        } else null
    }

    // get the scopes from security definition
    private fun getDefinedScopes(swagger: Swagger): Set<Pair<String, String>> =
            swagger.securityDefinitions.orEmpty().entries.flatMap { (group, def) ->
                (def as? OAuth2Definition)?.scopes.orEmpty().keys.map { scope -> group to scope }
            }.toSet()

    // Extract all oauth2 scopes applied to the given operation into a simple list
    private fun extractAppliedScopes(operation: Operation): Set<Pair<String, String>> =
            operation.security?.flatMap { groupDefinition ->
                groupDefinition.entries.flatMap { (group, scopes) ->
                    scopes.map { group to it }
                }
            }.orEmpty().toSet()

    private fun hasTopLevelScope(swagger: Swagger, definedScopes: Set<Pair<String, String>>): Boolean =
            swagger.security?.any { securityRequirement ->
                securityRequirement.requirements.entries.any { (group, scopes) ->
                    scopes.any { scope -> (group to scope) in definedScopes }
                }
            } ?: false
}
