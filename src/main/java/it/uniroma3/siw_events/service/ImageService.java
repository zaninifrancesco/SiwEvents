package it.uniroma3.siw_events.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private final Path root = Paths.get("uploads/event_images");

    public String saveImage(MultipartFile file) {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(uniqueFileName));
            return "/uploads/event_images/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public void deleteImage(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                Path fileToDelete = Paths.get(filePath.substring(1)); // remove leading '/'
                Files.deleteIfExists(fileToDelete);
            }
        } catch (IOException e) {
            // Log the exception
        }
    }
}
