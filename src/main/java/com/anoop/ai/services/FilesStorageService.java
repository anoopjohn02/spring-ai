package com.anoop.ai.services;

import com.anoop.ai.data.vector.DocumentService;
import com.anoop.ai.model.ResponseMessage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilesStorageService {

  private final DocumentService documentService;

  private final ResourceLoader resourceLoader;

  private final Path root = Paths.get("uploads");

  @PostConstruct
  public void init() throws IOException {
    Files.createDirectories(root);
  }

  public Mono<ResponseMessage> save(FilePart filePart, String userId) throws IOException {
    try {
      if (!root.toFile().exists()) {
        root.toFile().mkdirs();
      }
      Path path = this.root.resolve(filePart.filename());
      Mono<ResponseMessage> status =
          filePart
              .transferTo(path)
              .then(
                  Mono.fromCallable(
                      () -> {
                        Resource resource = new FileSystemResource(path.toFile());
                        documentService.loadSingleDocument(resource, userId);
                        return new ResponseMessage(
                            "File uploaded successfully: " + filePart.filename());
                      }));
      return status;
    } catch (Exception e) {
      if (e instanceof FileAlreadyExistsException) {
        throw new RuntimeException("A file of that name already exists.");
      }
      log.error("Error: ", e);
      throw e;
    }
  }

  public Resource load(String filename) throws MalformedURLException {
    Path file = root.resolve(filename);
    Resource resource = new UrlResource(file.toUri());

    if (resource.exists() || resource.isReadable()) {
      return resource;
    } else {
      throw new RuntimeException("Could not read the file!");
    }
  }

  public void deleteAll() {
    try {
      List<Boolean> deleteVector =
          loadAll()
              .map(path -> documentService.deleteByFile(path.getFileName().toString()))
              .collect(Collectors.toList());
      if (Arrays.asList(deleteVector).contains(Boolean.valueOf(false))) {
        throw new RuntimeException("Deletion failed!");
      }
      FileSystemUtils.deleteRecursively(root.toFile());
    } catch (Exception exception) {
      throw new RuntimeException("Could not delete files!");
    }
  }

  public Stream<Path> loadAll() throws IOException {
    return Files.walk(this.root, 1)
        .filter(path -> !path.equals(this.root))
        .map(this.root::relativize);
  }
}
