package fr.simple.edm.controller;

import fr.simple.edm.domain.EdmAggregationItem;
import fr.simple.edm.domain.EdmAutoTidySuggestion;
import fr.simple.edm.domain.EdmDocumentFile;
import fr.simple.edm.domain.EdmDocumentSearchResultWrapper;
import fr.simple.edm.service.EdmAggregationsService;
import fr.simple.edm.service.EdmDocumentService;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class EdmDocumentController {

    @Inject
    private EdmDocumentService edmDocumentService;

    @Inject
    private EdmAggregationsService edmAggregationsService;

    @RequestMapping(value = "/document", params = {"q"})
    @ResponseBody
    public EdmDocumentSearchResultWrapper search(@RequestParam(value = "q") String pattern) {
        log.debug("Searched pattern : '{}'", pattern);
        return edmDocumentService.search(pattern);
    }

    @RequestMapping(value = "/document/suggest", params = {"q"})
    @ResponseBody
    public List<EdmDocumentFile> getSuggestions(@RequestParam(value = "q") String pattern) {
        log.debug("Suggestions pattern : '{}'", pattern);
        return edmAggregationsService.getSuggestions(pattern);
    }

    @RequestMapping(value = "/document/top_terms")
    @ResponseBody
    public List<EdmAggregationItem> getTerms(@RequestParam(value = "q", defaultValue = "") String pattern) {
        log.debug("Get relative terms for pattern : '{}'", pattern);
        return edmAggregationsService.getTopTerms(pattern);
    }

    @RequestMapping(value = "/document/aggregations")
    @ResponseBody
    public Map<String, List<EdmAggregationItem>> getAggregations(@RequestParam(value = "q", defaultValue = "") String pattern) {
        log.debug("Get relative terms for pattern : '{}'", pattern);
        return edmAggregationsService.getAggregations(pattern);
    }

    @RequestMapping(value = "/files", params = {"docId"})
    @ResponseBody
    public FileSystemResource getFile(@RequestParam(value = "docId") String docId, HttpServletResponse response) throws NotFoundException, IOException {
        log.debug("Downloading file : '{}'", docId);

        // document does not exists or access is not allowed
        EdmDocumentFile edmDocumentFile = edmDocumentService.findOne(docId);
        if (edmDocumentFile == null) {
            throw new NotFoundException(docId);
        }

        Path filePath = Paths.get(edmDocumentFile.getNodePath());
        File file = new File(edmDocumentFile.getNodePath());

        response.setContentType(Files.probeContentType(filePath));
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        return new FileSystemResource(file);
    }

    /**
     * @param file
     *  A text file which contains document you want to tidy
     * @return
     */
    @RequestMapping(value = "/document/suggestTidy", method = RequestMethod.POST)
    @ResponseBody
    public EdmAutoTidySuggestion suggestTidyForFile(@RequestPart("file") MultipartFile file) {
        log.debug("Wanna get suggestion for file : {}", file.getOriginalFilename());
        return edmDocumentService.getTidySuggestions(file);
    }

}
