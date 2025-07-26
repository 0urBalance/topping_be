package org.balanceus.topping.application.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.MenuImage;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreImage;
import org.balanceus.topping.domain.repository.MenuImageRepository;
import org.balanceus.topping.domain.repository.StoreImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final StoreImageRepository storeImageRepository;
    private final MenuImageRepository menuImageRepository;

    @Value("${app.upload.dir:src/main/resources/static}")
    private String uploadDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png"
    );

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;

    public List<String> uploadStoreImages(Store store, MultipartFile[] files, StoreImage.ImageType imageType) {
        List<String> uploadedPaths = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                validateImageFile(file);
                String imagePath = saveImageFile(file, "stores", store.getUuid().toString());
                
                StoreImage storeImage = new StoreImage();
                storeImage.setStore(store);
                storeImage.setImagePath(imagePath);
                storeImage.setOriginalFilename(file.getOriginalFilename());
                storeImage.setImageType(imageType);
                storeImage.setFileSize(file.getSize());
                storeImage.setContentType(file.getContentType());
                storeImage.setDisplayOrder(getNextDisplayOrder(store));
                
                storeImageRepository.save(storeImage);
                uploadedPaths.add(imagePath);
                
                log.info("Successfully uploaded store image: {} for store: {}", imagePath, store.getUuid());
                
            } catch (Exception e) {
                log.error("Failed to upload image for store: {}", store.getUuid(), e);
                throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }
        
        return uploadedPaths;
    }

    public List<String> uploadMenuImages(Menu menu, MultipartFile[] files, MenuImage.ImageType imageType) {
        List<String> uploadedPaths = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                validateImageFile(file);
                String imagePath = saveImageFile(file, "products", menu.getUuid().toString());
                
                MenuImage menuImage = new MenuImage();
                menuImage.setMenu(menu);
                menuImage.setImagePath(imagePath);
                menuImage.setOriginalFilename(file.getOriginalFilename());
                menuImage.setImageType(imageType);
                menuImage.setFileSize(file.getSize());
                menuImage.setContentType(file.getContentType());
                menuImage.setDisplayOrder(getNextDisplayOrder(menu));
                
                menuImageRepository.save(menuImage);
                uploadedPaths.add(imagePath);
                
                log.info("Successfully uploaded menu image: {} for menu: {}", imagePath, menu.getUuid());
                
            } catch (Exception e) {
                log.error("Failed to upload image for menu: {}", menu.getUuid(), e);
                throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }
        
        return uploadedPaths;
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. JPG, JPEG, PNG 파일만 업로드 가능합니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        }
    }

    private String saveImageFile(MultipartFile file, String category, String entityId) throws IOException {
        // Create directory structure: /image/{category}/{entityId}/
        String relativePath = String.format("/image/%s/%s", category, entityId);
        Path directoryPath = Paths.get(uploadDir, relativePath);
        
        // Create directories if they don't exist
        Files.createDirectories(directoryPath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
        
        // Full file path
        Path filePath = directoryPath.resolve(uniqueFilename);
        String webPath = relativePath + "/" + uniqueFilename;
        
        // Process and save image
        BufferedImage processedImage = processImage(file);
        ImageIO.write(processedImage, extension, filePath.toFile());
        
        return webPath;
    }

    private BufferedImage processImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("이미지 파일을 읽을 수 없습니다.");
        }
        
        // Resize if necessary
        if (originalImage.getWidth() > MAX_WIDTH || originalImage.getHeight() > MAX_HEIGHT) {
            return resizeImage(originalImage, MAX_WIDTH, MAX_HEIGHT);
        }
        
        return originalImage;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth, newHeight;
        
        if (originalWidth > originalHeight) {
            newWidth = Math.min(maxWidth, originalWidth);
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newHeight = Math.min(maxHeight, originalHeight);
            newWidth = (int) (newHeight * aspectRatio);
        }
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private int getNextDisplayOrder(Store store) {
        return (int) storeImageRepository.countByStore(store);
    }

    private int getNextDisplayOrder(Menu menu) {
        return (int) menuImageRepository.countByMenu(menu);
    }

    public void deleteStoreImage(UUID imageId) {
        try {
            // First, get the image details before deletion
            StoreImage storeImage = storeImageRepository.findByUuid(imageId)
                    .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
            
            // Delete the physical file
            deletePhysicalFile(storeImage.getImagePath());
            
            // Delete the database record
            storeImageRepository.deleteByUuid(imageId);
            
            log.info("Successfully deleted store image: {} (path: {})", imageId, storeImage.getImagePath());
        } catch (Exception e) {
            log.error("Failed to delete store image: {}", imageId, e);
            throw new RuntimeException("이미지 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteMenuImage(UUID imageId) {
        try {
            // First, get the image details before deletion
            MenuImage menuImage = menuImageRepository.findByUuid(imageId)
                    .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));
            
            // Delete the physical file
            deletePhysicalFile(menuImage.getImagePath());
            
            // Delete the database record
            menuImageRepository.deleteByUuid(imageId);
            
            log.info("Successfully deleted menu image: {} (path: {})", imageId, menuImage.getImagePath());
        } catch (Exception e) {
            log.error("Failed to delete menu image: {}", imageId, e);
            throw new RuntimeException("이미지 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Delete physical file from filesystem
     */
    private void deletePhysicalFile(String imagePath) {
        try {
            // Sanitize the path to prevent directory traversal attacks
            if (imagePath == null || imagePath.trim().isEmpty()) {
                log.warn("Empty image path provided for deletion");
                return;
            }

            // Ensure the path starts with /image/ to prevent unauthorized deletions
            if (!imagePath.startsWith("/image/")) {
                log.warn("Invalid image path for deletion: {}", imagePath);
                throw new SecurityException("Invalid image path");
            }

            // Convert web path to filesystem path
            String filePath = uploadDir + imagePath;
            Path fileToDelete = Paths.get(filePath).normalize();
            Path uploadPath = Paths.get(uploadDir).normalize();

            // Ensure the file is within the upload directory (additional security check)
            if (!fileToDelete.startsWith(uploadPath)) {
                log.warn("Path traversal attempt detected: {}", imagePath);
                throw new SecurityException("Path traversal attempt detected");
            }

            // Delete the file if it exists
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                log.info("Successfully deleted physical file: {}", fileToDelete);
            } else {
                log.warn("Physical file not found for deletion: {}", fileToDelete);
            }
        } catch (SecurityException e) {
            throw e; // Re-throw security exceptions
        } catch (Exception e) {
            log.error("Failed to delete physical file: {}", imagePath, e);
            // Don't throw exception for file deletion errors to avoid blocking database cleanup
        }
    }
}