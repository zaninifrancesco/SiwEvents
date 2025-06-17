package it.uniroma3.siw_events.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path profileImageStorageLocation;
    private final Path eventImageStorageLocation;
    private final String profileImagesDirName;
    private final String eventImagesDirName;

    public FileStorageService(
            @Value("${app.upload-dir}") String uploadDir,
            @Value("${app.upload-dir.profile-images}") String profileImagesDir,
            @Value("${app.upload-dir.event-images}") String eventImagesDir) {

        this.profileImagesDirName = profileImagesDir;
        this.eventImagesDirName = eventImagesDir;

        Path baseUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.profileImageStorageLocation = baseUploadPath.resolve(profileImagesDir).normalize();
        this.eventImageStorageLocation = baseUploadPath.resolve(eventImagesDir).normalize();

        try {
            Files.createDirectories(this.profileImageStorageLocation);
            Files.createDirectories(this.eventImageStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossibile creare le directory di upload.", ex);
        }
    }

    private String storeFile(MultipartFile file, Path storageLocation, String subDirName) {
        if (file == null || file.isEmpty()) {
            return null; // Nessun file da salvare
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            // Genera un nome file univoco per evitare collisioni
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Verifica che il file non contenga caratteri dannosi (es. ../)
            if (fileName.contains("..")) {
                throw new RuntimeException("Nome file non valido: " + originalFileName);
            }

            Path targetLocation = storageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Ritorna il percorso relativo con slash iniziale per salvarlo nel DB (es. /profile_images/nomefile.jpg)
            // Assicura l'uso di forward slashes per compatibilità web
            return "/" + Paths.get(subDirName, fileName).toString().replace("\\", "/");
        } catch (IOException ex) {
            throw new RuntimeException("Impossibile salvare il file " + originalFileName + ". Riprova!", ex);
        }
    }

    public String storeProfileImage(MultipartFile file) {
        return storeFile(file, this.profileImageStorageLocation, this.profileImagesDirName);
    }

    public String storeEventImage(MultipartFile file) {
        return storeFile(file, this.eventImageStorageLocation, this.eventImagesDirName);
    }

    // Eventuali metodi per eliminare file potrebbero essere aggiunti qui
    // public void deleteFile(String relativePath) { ... }
}
