package de.zalando.zally.integration

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.zally.ZallyClient
import feign.Feign
import feign.RequestInterceptor
import feign.jackson.JacksonDecoder
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.zalando.stups.tokens.AccessTokens
import org.zalando.twintip.spring.SchemaResource

private val AUTHORIZATION_HEADER_NAME = "Authorization"
private val BEARER = "Bearer "

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories
@EnableJpaAuditing
class Application {

    @Bean
    fun forwardAccessTokenInterceptor(tokens: AccessTokens): RequestInterceptor {
        return RequestInterceptor { template ->
            val token = tokens.get("zally")
            template?.header(AUTHORIZATION_HEADER_NAME, BEARER + token)
        }
    }

    @Bean
    fun githubClient(
        @Value("\${github.oauthToken}") oauthToken: String,
        @Value("\${github.apiUrl}") apiUrl: String
    ): GitHub {
        return GitHub.connectUsingOAuth(apiUrl, oauthToken)
    }

    @Bean
    fun zallyClient(
        @Value("\${zally.apiUrl}") apiUrl: String,
        requestInterceptor: RequestInterceptor,
        jacksonObjectMapper: ObjectMapper
    ): ZallyClient {
        return Feign.builder()
                .requestInterceptor(requestInterceptor)
                .decoder(JacksonDecoder(jacksonObjectMapper))
                .target(ZallyClient::class.java, apiUrl)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
