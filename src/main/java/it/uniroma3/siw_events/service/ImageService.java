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
    private final Path profileRoot = Paths.get("uploads/profile_images");
    private final Path postedImagesRoot = Paths.get("uploads/event_posted_images");

    public String saveImage(MultipartFile file) {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(uniqueFileName));
            System.out.println("PATH img: " + uniqueFileName);
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

    public String saveProfileImage(MultipartFile file) {
        try {
            if (!Files.exists(profileRoot)) {
                Files.createDirectories(profileRoot);
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.profileRoot.resolve(uniqueFileName));
            return "/uploads/profile_images/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the profile image. Error: " + e.getMessage());
        }
    }

    public void deleteProfileImage(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                Path fileToDelete = Paths.get(filePath.substring(1)); // remove leading '/'
                Files.deleteIfExists(fileToDelete);
            }
        } catch (IOException e) {
            // Log the exception
        }
    }

    public String savePostedImage(MultipartFile file) {
        try {
            if (!Files.exists(postedImagesRoot)) {
                Files.createDirectories(postedImagesRoot);
            }
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.postedImagesRoot.resolve(uniqueFileName));
            System.out.println("PATH posted img: " + uniqueFileName);
            return "/uploads/event_posted_images/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the posted image. Error: " + e.getMessage());
        }
    }
}
