package com.akida.ecommerce.controller;

import com.akida.ecommerce.serviceimpl.FileSystemStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/secured/images")
@RequiredArgsConstructor
public class ImageController {

    private final FileSystemStorageService storageService;



    @GetMapping("/byFileName")
    public ResponseEntity<Resource> serveImage(@RequestParam("fileName") String fileName) {
        try {
            Resource file = storageService.loadAsResource(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/jpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


}