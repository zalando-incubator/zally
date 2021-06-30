package org.zalando.zally.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport
import org.zalando.stups.oauth2.spring.security.expression.ExtendedOAuth2WebSecurityExpressionHandler
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices

@Configuration
@EnableResourceServer
@Profile("production")
@Import(SecurityProblemSupport::class)
class OAuthConfiguration : ResourceServerConfigurerAdapter() {

    @Value("\${spring.oauth2.resource.tokenInfoUri}")
    private lateinit var tokenInfoUri: String

    @Autowired
    private lateinit var problemSupport: SecurityProblemSupport

    @Throws(Exception::class)
    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources.expressionHandler(ExtendedOAuth2WebSecurityExpressionHandler())
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .httpBasic().disable()
            .requestMatchers().antMatchers("/**")
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .regexMatchers("/health/?").permitAll()
            .regexMatchers(
                "/metrics/?(\\?.*)?",
                "/api-violations/?([a-z0-9-]+/?)?(\\?)?",
                "/supported-rules/?(\\?.*)?",
                "/review-statistics/?(\\?.*)?",
                "/prometheus/?(\\?.*)?"
            )
            .access("#oauth2.hasScope('uid')")
            .antMatchers("**").denyAll()

        http
            .exceptionHandling()
            .authenticationEntryPoint(problemSupport)
            .accessDeniedHandler(problemSupport)
    }

    @Bean
    fun customResourceTokenServices(): ResourceServerTokenServices {
        return TokenInfoResourceServerTokenServices(tokenInfoUri)
    }
}
