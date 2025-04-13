package com.akida.ecommerce.servicesImpl;

import com.akida.ecommerce.exceptions.StorageException;
import com.akida.ecommerce.models.Category;
import com.akida.ecommerce.models.Image;
import com.akida.ecommerce.models.Product;
import com.akida.ecommerce.services.StorageService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;


@Service
@Slf4j
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation;
    private final String baseUrl;
    private final String productDir;
    private final String categoryDir;
    private final int productThumbWidth;
    private final int productThumbHeight;
    private final int categoryThumbWidth;
    private final int categoryThumbHeight;

    @Autowired
    public FileSystemStorageService(
            @Value("${app.storage.base-dir}") String baseDir,
            @Value("${app.storage.base-url}") String baseUrl,
            @Value("${app.storage.product.dir}") String productDir,
            @Value("${app.storage.category.dir}") String categoryDir,
            @Value("${app.storage.product.thumbnail-width}") int productThumbWidth,
            @Value("${app.storage.product.thumbnail-height}") int productThumbHeight,
            @Value("${app.storage.category.thumbnail-width}") int categoryThumbWidth,
            @Value("${app.storage.category.thumbnail-height}") int categoryThumbHeight) {

        this.rootLocation = Paths.get(baseDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        this.productDir = productDir;
        this.categoryDir = categoryDir;
        this.productThumbWidth = productThumbWidth;
        this.productThumbHeight = productThumbHeight;
        this.categoryThumbWidth = categoryThumbWidth;
        this.categoryThumbHeight = categoryThumbHeight;

        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve(productDir));
            Files.createDirectories(rootLocation.resolve(categoryDir));
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public Image storeProductImage(MultipartFile file, Product product) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);

        // Store original image
        Path productDirPath = rootLocation.resolve(productDir);
        Path targetLocation = productDirPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Generate thumbnail
        String thumbnailFilename = "thumb_" + uniqueFilename;
        Path thumbnailLocation = productDirPath.resolve(thumbnailFilename);
        generateThumbnail(file, thumbnailLocation, productThumbWidth, productThumbHeight);

        // Create Image entity
        Image image = new Image();
        image.setFileName(originalFilename);
        image.setFilePath(productDir + "/" + uniqueFilename);
        image.setFileType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setThumbnailPath(productDir + "/" + thumbnailFilename);
        image.setProduct(product);

        return image;
    }

    @Override
    public Image storeCategoryImage(MultipartFile file, Category category) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);

        // Store original image
        Path categoryDirPath = rootLocation.resolve(categoryDir);
        Path targetLocation = categoryDirPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Generate thumbnail
        String thumbnailFilename = "thumb_" + uniqueFilename;
        Path thumbnailLocation = categoryDirPath.resolve(thumbnailFilename);
        generateThumbnail(file, thumbnailLocation, categoryThumbWidth, categoryThumbHeight);

        // Create Image entity
        Image image = new Image();
        image.setFileName(originalFilename);
        image.setFilePath(categoryDir + "/" + uniqueFilename);
        image.setFileType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setThumbnailPath(categoryDir + "/" + thumbnailFilename);
        image.setCategory(category);

        return image;
    }

    private void generateThumbnail(MultipartFile file, Path targetLocation, int width, int height) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            Thumbnails.of(inputStream)
                    .size(width, height)
                    .outputFormat("jpg")
                    .toFile(targetLocation.toFile());
        }
    }

    @Override
    public Resource loadAsResource(String filePath) throws FileNotFoundException {
        try {

            // Resolve path
            Path file = rootLocation.resolve(filePath).normalize();

            // Verify path is within root location (security check)
            if (!file.startsWith(rootLocation.toAbsolutePath())) {
                throw new SecurityException("Cannot access files outside root directory");
            }

            // Check file existence directly
            if (!Files.exists(file)) {
                log.error("File does not exist at: {}", file.toAbsolutePath());
                throw new FileNotFoundException("File not found: " + filePath);
            }

            // Check readability
            if (!Files.isReadable(file)) {
                throw new FileNotFoundException("No read permission for: " + filePath);
            }

            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Invalid path: " + filePath);
        } catch (IOException e) {
            throw new FileNotFoundException("Error accessing: " + filePath);
        }
    }

    @Override
    public void deleteImage(Image image) throws IOException {
        // Delete main image
        Path mainImagePath = rootLocation.resolve(image.getFilePath());
        Files.deleteIfExists(mainImagePath);

        // Delete thumbnail
        if (image.getThumbnailPath() != null) {
            Path thumbnailPath = rootLocation.resolve(image.getThumbnailPath());
            Files.deleteIfExists(thumbnailPath);
        }
    }

    @Override
    public String getImageUrl(String filePath) {
        return baseUrl + "/" + filePath;
    }

    // Helper methods
    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename).toLowerCase();
    }
}