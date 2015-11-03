package fr.simple.edm.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EdmDocumentSearchResultDto {

    private EdmDocumentFileDto edmDocument;
    
    private String highlightedName;
    
    private String highlightedDescription;
    
    private String highlightedFileContentMatching;
    
    private String highlightedNodePath;
    
}
