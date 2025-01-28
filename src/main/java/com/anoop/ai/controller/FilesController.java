package com.anoop.ai.controller;

import com.anoop.ai.model.FileInfo;
import com.anoop.ai.model.ResponseMessage;
import com.anoop.ai.services.FilesStorageService;
import java.io.IOException;
import java.net.MalformedURLException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "/v1/files")
public class FilesController {
  @Autowired private FilesStorageService storageService;

  @GetMapping
  public Flux<FileInfo> getListFiles() throws IOException {
    Flux<FileInfo> fileInfos =
        Flux.fromStream(storageService.loadAll())
            .map(
                path -> {
                  String filename = path.getFileName().toString();
                  return new FileInfo(filename, "");
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
      @RequestPart("file") Mono<FilePart> filePartMono, @PathVariable(name = "id") String userId) {
    return filePartMono.flatMap(
        filePart -> {
          try {
            return storageService.save(filePart, userId);
          } catch (Exception e) {
            log.error("Error: ", e);
            String message =
                "Could not upload the file: " + filePart.filename() + ". Error: " + e.getMessage();
            return Mono.just(new ResponseMessage(message));
          }
        });
  }

  @DeleteMapping("/all")
  public Mono<Void> deleteAll() {
    storageService.deleteAll();
    return Mono.empty();
  }
}
