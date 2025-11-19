package com.groom.manvsclass.controller;

import com.groom.manvsclass.service.SuggestionUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/suggestions")
@RequiredArgsConstructor
public class AdminSuggestionController {

    private final SuggestionUploadService suggestionUploadService;

    /**
     * Endpoint per caricare un file di testo con i suggerimenti di una classe.
     * Formato supportato: una riga per suggerimento con "DIFFICOLTA;testo".
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSuggestions(@RequestParam("className") String className,
                                                  @RequestParam("file") MultipartFile file) {
        suggestionUploadService.uploadSuggestionsFromFile(className, file);
        return ResponseEntity.accepted().build();
    }
}
