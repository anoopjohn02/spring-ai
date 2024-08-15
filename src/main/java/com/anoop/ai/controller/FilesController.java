package com.anoop.ai.controller;

import com.anoop.ai.model.FileInfo;
import com.anoop.ai.model.ResponseMessage;
import com.anoop.ai.services.FilesStorageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/v1/files")
public class FilesController {
    @Autowired
    private FilesStorageService storageService;

    @GetMapping
    public Flux<FileInfo> getListFiles() throws IOException {
        Flux<FileInfo> fileInfos = Flux.fromStream(storageService.loadAll())
                .map(path -> {
                    String filename = path.getFileName().toString();
                    String url = MvcUriComponentsBuilder
                            .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
                    return new FileInfo(filename, url);
                });
        return fileInfos;
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public Mono<Resource> getFile(@PathVariable String filename) throws MalformedURLException {
        Resource file = storageService.load(filename);
        return Mono.just(file);
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseMessage> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable(name = "id") String userId) {
        String message = "";
        try {
            storageService.save(file, userId);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return Mono.just(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return Mono.just(new ResponseMessage(message));
        }
    }

    @DeleteMapping("/all")
    public Mono<Void> deleteAll() {
        storageService.deleteAll();
        return Mono.empty();
    }
}
