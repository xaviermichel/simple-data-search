package fr.simple.edm.crawler.bridge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fr.simple.edm.domain.EdmCategory;
import fr.simple.edm.domain.EdmDocumentFile;
import fr.simple.edm.domain.EdmNodeType;
import fr.simple.edm.domain.EdmSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdmConnector {

    public void saveEdmDocument(String server, EdmDocumentFile doc, File file) throws IOException {
        doc.setEdmNodeType(EdmNodeType.DOCUMENT);
        doc.setFileContent(Files.readAllBytes(file.toPath()));

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(server + "/document", doc, EdmDocumentFile.class);
    }

    public void notifyStartCrawling(String server, String source) throws ClientProtocolException, IOException {
        HttpGet request = new HttpGet(server + "/crawl/start?source=" + sanitizeSourceName(source));
        HttpClient client = HttpClientBuilder.create().build();
        client.execute(request);
    }

    public void notifyEndOfCrawling(String server, String source) throws ClientProtocolException, IOException {
        HttpGet request = new HttpGet(server + "/crawl/stop?source=" + sanitizeSourceName(source));
        HttpClient client = HttpClientBuilder.create().build();
        client.execute(request);
    }

    private String sanitizeSourceName(String sourceName) {
        return sourceName.replaceAll(" ", "-");
    }

    public String getIdFromSourceBySourceName(String server, String sourceName, String categoryId) {
        // get Node
        RestTemplate restTemplate = new RestTemplate();
        EdmSource result = restTemplate.getForObject(server + "/source/name/{sourceName}", EdmSource.class, sourceName);

        // if exits, nothing to do !
        if (result.getId() != null && ! result.getId().isEmpty()) {
            return result.getId();
        }

        // else, we have to create it
        EdmSource directory = new EdmSource();
        directory.setDescription("");
        directory.setName(sourceName);
        directory.setParentId(categoryId);
        ResponseEntity<EdmSource> createdSource = restTemplate.postForEntity(server + "/source", directory, EdmSource.class);
        return createdSource.getBody().getId();
    }

    public String getIdFromCategoryByCategoryName(String server, String categoryName) {
        // get Node
        RestTemplate restTemplate = new RestTemplate();
        EdmCategory result = restTemplate.getForObject(server + "/category/name/{categoryName}", EdmCategory.class, categoryName);

        // if exits, nothing to do !
        if (result.getId() != null && ! result.getId().isEmpty()) {
            return result.getId();
        }

        // else, we have to create it
        EdmCategory category = new EdmCategory();
        category.setDescription("");
        category.setName(categoryName);
        ResponseEntity<EdmCategory> createdSource = restTemplate.postForEntity(server + "/category", category, EdmCategory.class);
        return createdSource.getBody().getId();
    }
}
