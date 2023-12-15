package com.example.demowithtests.web.document;

import com.example.demowithtests.domain.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;


public interface DocumentResource {

    Document createDocument(Document document);

    Document getDocumentById(Integer id);
}
