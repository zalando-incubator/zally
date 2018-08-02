package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.exception.MissingApiDefinitionException;
import de.zalando.zally.exception.UnaccessibleResourceUrlException;
import de.zalando.zally.util.JadlerUtil;
import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.emptyList;
import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

public class ApiDefinitionReaderTest {

    private static final String APPLICATION_X_YAML_VALUE = "application/x-yaml";

    private final String contentInJson = "{\"swagger\":\"2.0\"}";

    private ApiDefinitionReader reader;

    @Before
    public void setUp() {
        initJadlerUsing(new JdkStubHttpServer());
        reader = new ApiDefinitionReader(new RestTemplate());
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test(expected = MissingApiDefinitionException.class)
    public void shouldThrowMissingApiDefinitionExceptionWhenDefinitionIsNotFound() {
        reader.read(new ApiDefinitionRequest());
    }

    @Test
    public void shouldReturnStringWhenApiDefinitionIsFound() {
        ApiDefinitionRequest request = new ApiDefinitionRequest(contentInJson, null, "http://zalando.de", emptyList());
        String result = reader.read(request);
        assertEquals(contentInJson, result);
    }

    @Test
    public void shouldReadJsonSwaggerDefinitionFromUrl() {
        String url = JadlerUtil.stubResource("test.json", contentInJson);
        String result = reader.read(ApiDefinitionRequest.Factory.fromUrl(url));
        assertEquals(contentInJson, result);
    }

    @Test
    public void shouldReadYamlSwaggerDefinitionFromUrl() {
        String contentInYaml = "swagger: \"2.0\"";
        String url = JadlerUtil.stubResource("test.yaml", contentInYaml, APPLICATION_X_YAML_VALUE);
        String result = reader.read(ApiDefinitionRequest.Factory.fromUrl(url));

        assertEquals(contentInYaml, result);
    }

    @Test
    public void shouldPreferRawSpecification () {
        String rawYaml = "raw: yaml";
        ApiDefinitionRequest request = new ApiDefinitionRequest("{\"some\": \"json\"", rawYaml, "http://zalando.de", emptyList());
        String result = reader.read(request);
        assertEquals(rawYaml, result);
    }

    @Test
    public void shouldRetryLoadingOfUrlIfEndsWithSpecialEncodedCharacters() {
        String url = JadlerUtil.stubResource("test.json", contentInJson);
        String result = reader.read(ApiDefinitionRequest.Factory.fromUrl(url + "%3D%3D"));
        assertEquals(contentInJson, result);
    }

    @Test(expected = UnaccessibleResourceUrlException.class)
    public void shouldErrorBadRequestWhenDefinitionFromUrlUnsuccessful() {
        String url = JadlerUtil.stubResource("test.json", "", HttpStatus.UNAUTHORIZED.value(), APPLICATION_JSON_VALUE);

        reader.read(ApiDefinitionRequest.Factory.fromUrl(url));
    }

    @Test(expected = UnaccessibleResourceUrlException.class)
    public void shouldErrorBadRequestWhenDefinitionFromUrlWrongContentType() {
        String url = JadlerUtil.stubResource("test.json", "", HttpStatus.OK.value(), TEXT_HTML_VALUE);

        reader.read(ApiDefinitionRequest.Factory.fromUrl(url));
    }
}
