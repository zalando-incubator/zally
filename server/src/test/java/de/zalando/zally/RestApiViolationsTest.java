package de.zalando.zally;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zally.exception.MissingApiDefinitionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import de.zalando.zally.rules.InvalidApiSchemaRule;
import de.zalando.zally.rules.Rule;
import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static net.jadler.Jadler.onRequest;
import static net.jadler.Jadler.port;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "zally.message=Test message")
public class RestApiViolationsTest extends RestApiBaseTest {
    @Before
    public void setUp() {
        initJadlerUsing(new JdkStubHttpServer());
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void shouldValidateGivenApiDefinition() throws IOException {
        ResponseEntity<JsonNode> responseEntity = sendRequest(
                new ObjectMapper().readTree(ResourceUtils.getFile("src/test/resources/fixtures/api_spp.json")));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode rootObject = responseEntity.getBody();
        assertThat(rootObject.has("violations")).isTrue();

        JsonNode violations = rootObject.get("violations");
        assertThat(violations).hasSize(2);
        assertThat(violations.get(0).get("title").asText()).isEqualTo("dummy1");
        assertThat(violations.get(1).get("title").asText()).isEqualTo("dummy2");

        String message = rootObject.get("message").asText();
        assertThat(message).isEqualTo("Test message");
    }

    @Test
    public void shouldReturnCounters() throws IOException {
        ResponseEntity<JsonNode> responseEntity = sendRequest(
                new ObjectMapper().readTree(ResourceUtils.getFile("src/test/resources/fixtures/api_spp.json")));
        JsonNode rootObject = responseEntity.getBody();

        JsonNode counters = rootObject.get("violations_count");
        assertThat(counters.get("must").asInt()).isEqualTo(1);
        assertThat(counters.get("should").asInt()).isEqualTo(0);
        assertThat(counters.get("could").asInt()).isEqualTo(0);
        assertThat(counters.get("hint").asInt()).isEqualTo(1);
    }

    @Test
    public void shouldReturnMetricsOfFoundViolations() throws IOException {
        ResponseEntity<JsonNode> responseEntity = sendRequest(
                new ObjectMapper().readTree(ResourceUtils.getFile("src/test/resources/fixtures/api_spp.json")));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> metricsResponse = restTemplate.getForEntity("http://localhost:" + managementPort + "/metrics", JsonNode.class);
        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("JSON: " + metricsResponse.getBody().toString());
        JsonNode rootObject = metricsResponse.getBody();
        assertThat(rootObject.has("counter.api-reviews.requested")).isTrue();
        assertThat(rootObject.has("counter.api-reviews.processed")).isTrue();
        assertThat(rootObject.has("histogram.api-reviews.violations.count")).isTrue();
        assertThat(rootObject.has("histogram.api-reviews.violations.type.must.count")).isTrue();
        assertThat(rootObject.has("histogram.api-reviews.violations.rule.checkapinameispresentrule.count")).isTrue();
    }

    @Test
    public void shouldRespondWithBadRequestOnMalformedJson() throws IOException {
        RequestEntity requestEntity = RequestEntity
                .post(URI.create(getUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"malformed\": \"dummy\"");
        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldRespondWithProblemJsonOnMalformedJson() throws IOException {
        RequestEntity requestEntity = RequestEntity
                .post(URI.create(getUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"malformed\": \"dummy\"");

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        assertThat(responseEntity.getHeaders().getContentType().toString()).isEqualTo("application/problem+json");
        assertThat(responseEntity.getBody().has("title")).isTrue();
        assertThat(responseEntity.getBody().has("status")).isTrue();
        assertThat(responseEntity.getBody().has("detail")).isTrue();
    }

    @Test
    public void shouldRespondWithBadRequestWhenApiDefinitionFieldIsMissing() throws IOException {
        RequestEntity requestEntity = RequestEntity
                .post(URI.create(getUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"my_api\": \"dummy\"}");

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody().get("title").asText()).isEqualTo("Bad Request");
        assertThat(responseEntity.getBody().get("detail").asText()).isEqualTo(MissingApiDefinitionException.MESSAGE);
    }

    @Test
    public void shouldRespondWithViolationWhenApiDefinitionFieldIsNotValidSwaggerDefinition() throws IOException {
        RequestEntity requestEntity = RequestEntity
                .post(URI.create(getUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"api_definition\": \"no swagger definition\"}");

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode rootObject = responseEntity.getBody();
        assertThat(rootObject.has("violations")).isTrue();

        JsonNode violations = rootObject.get("violations");
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).get("title").asText()).isEqualTo(new InvalidApiSchemaRule().getTitle());
    }

    @Test
    public void shouldReadJsonSpecificationFromUrl() throws Exception {
        final String definitionUrl = getLocalUrl(
                "src/test/resources/fixtures/api_spp.json", MediaType.APPLICATION_JSON.toString());

        final RequestEntity requestEntity = RequestEntity
                .post(URI.create(getUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"api_definition_url\": \"" + definitionUrl + "\"}");
        final ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final JsonNode rootObject = responseEntity.getBody();
        final JsonNode violations = rootObject.get("violations");
        assertThat(violations).hasSize(2);
        assertThat(violations.get(0).get("title").asText()).isEqualTo("dummy1");
        assertThat(violations.get(1).get("title").asText()).isEqualTo("dummy2");
    }

    @Test
    public void shouldReadYamlSpecificationFromUrl() throws Exception {
        final String definitionUrl = getLocalUrl(
                "src/test/resources/fixtures/api_spa.yaml", MediaType.APPLICATION_JSON.toString());

        final RequestEntity requestEntity = RequestEntity
                .post(URI.create(getUrl()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"api_definition_url\": \"" + definitionUrl + "\"}");
        final ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(requestEntity, JsonNode.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final JsonNode rootObject = responseEntity.getBody();

        final JsonNode violations = rootObject.get("violations");
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0 ).get("title").asText()).isEqualTo("dummy2");
    }

    private String getLocalUrl(final String resourceFilePath, final String contentType) throws Exception {
        final File file = ResourceUtils.getFile(resourceFilePath);
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        final String content = reader.lines().collect(Collectors.joining("\n"));

        final String fileName = Paths.get(resourceFilePath).getFileName().toString();
        final String remotePath = "/" + fileName;
        final String url = "http://localhost:" + port() + remotePath;

        onRequest()
                .havingMethodEqualTo("GET")
                .havingPathEqualTo(remotePath)
                .respond()
                .withStatus(200)
                .withHeader("Content-Type", contentType)
                .withBody(content);

        return url;
    }

}
