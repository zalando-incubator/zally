package de.zalando.zally.integration

import de.zalando.zally.integration.mock.EmbeddedPostgresqlConfiguration
import de.zalando.zally.integration.mock.GithubMock
import de.zalando.zally.integration.mock.JadlerRule
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers.containsString
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [Application::class, EmbeddedPostgresqlConfiguration::class])
@ActiveProfiles("test")
@SqlGroup(
    Sql("/sql/validations-get-test-data.sql"),
    Sql("/sql/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
)
class ReportControllerIntegrationTest {
    companion object {
        @ClassRule
        @JvmField
        val githubServer = JadlerRule(GithubMock(JadlerMocker(JdkStubHttpServer(8088)))) {
            it.mockGet("/user", "json/github-user-response.json") // required for app start
        }
    }

    @Autowired
    lateinit var restTemplate: WebTestClient

    // todo verify html?

    @Test
    fun shouldReturnExistingValidation() {
        // TODO fix tests
        restTemplate.get().uri("/reports/100").exchange().expectStatus().isOk
            .expectBody(String::class.java).value(containsString("https://github.com/myUserName/zally"))
            /*.andExpect(
                content().string(containsString("100"))
            )
            .andExpect(
                content().string(containsString("SHOULD"))
            )
            .andExpect(
                content().string(containsString("description: Zalando&#39;s API Linter"))
            )*/
    }

    @Test
    fun shouldReturnNotFoundIfReportsDoesNotExist() {
        restTemplate.perform(get("/reports/555").accept(MediaType.TEXT_HTML_VALUE))
            .andExpect(status().isNotFound)
    }
}
