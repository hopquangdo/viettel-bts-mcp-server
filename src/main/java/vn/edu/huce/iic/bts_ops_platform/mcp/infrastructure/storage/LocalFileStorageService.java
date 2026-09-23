package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.config.AppStorageProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService {

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> IMAGE_CATEGORIES = Set.of("san-luong", "vuong-mac");

    private final AppStorageProperties storageProperties;

    public StoredFile store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new AppException(FileErrorCode.FILE_UPLOAD_EMPTY, "File tải lên trống");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new AppException(FileErrorCode.FILE_UPLOAD_INVALID, "Tên file không hợp lệ");
        }

        String extension = extractExtension(originalName);
        Set<String> allowedExtensions = resolveAllowedExtensions(category);
        if (!allowedExtensions.contains(extension)) {
            throw new AppException(
                    FileErrorCode.FILE_UPLOAD_INVALID,
                    resolveInvalidExtensionMessage(category, allowedExtensions));
        }

        YearMonth yearMonth = YearMonth.now(ZoneOffset.UTC);
        String storedFileName = UUID.randomUUID() + "_" + sanitizeFileName(originalName);
        Path baseDir = Path.of(storageProperties.localBasePath()).toAbsolutePath().normalize();
        Path targetDir = baseDir
                .resolve(category)
                .resolve(String.valueOf(yearMonth.getYear()))
                .resolve(String.format("%02d", yearMonth.getMonthValue()));

        try {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedFileName);
            file.transferTo(targetFile);

            String prefix = storageProperties.publicUrlPrefix().replaceAll("/+$", "");
            String relativePath = prefix + "/" + category + "/"
                    + yearMonth.getYear() + "/"
                    + String.format("%02d", yearMonth.getMonthValue()) + "/"
                    + storedFileName;

            return new StoredFile(
                    relativePath,
                    storedFileName,
                    originalName,
                    file.getSize(),
                    file.getContentType(),
                    extension.toUpperCase(Locale.ROOT));
        } catch (IOException e) {
            throw new AppException(FileErrorCode.FILE_UPLOAD_FAILED, "Không lưu được file: " + e.getMessage());
        }
    }

    public Path resolveRelativePath(String relativePath) {
        String prefix = storageProperties.publicUrlPrefix().replaceAll("/+$", "");
        String normalized = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        if (!normalized.startsWith(prefix + "/")) {
            throw new AppException(FileErrorCode.FILE_UPLOAD_INVALID, "Đường dẫn file không hợp lệ");
        }
        String subPath = normalized.substring(prefix.length() + 1);
        Path baseDir = Path.of(storageProperties.localBasePath()).toAbsolutePath().normalize();
        Path resolved = baseDir.resolve(subPath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new AppException(FileErrorCode.FILE_UPLOAD_INVALID, "Đường dẫn file không hợp lệ");
        }
        return resolved;
    }

    private static String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    private static Set<String> resolveAllowedExtensions(String category) {
        if (category != null && IMAGE_CATEGORIES.contains(category.toLowerCase(Locale.ROOT))) {
            return IMAGE_EXTENSIONS;
        }
        return DOCUMENT_EXTENSIONS;
    }

    private static String resolveInvalidExtensionMessage(String category, Set<String> allowedExtensions) {
        if (category != null && IMAGE_CATEGORIES.contains(category.toLowerCase(Locale.ROOT))) {
            return "Chỉ hỗ trợ file ảnh .jpg, .jpeg, .png, .webp";
        }
        return "Chỉ hỗ trợ file .pdf, .doc, .docx, .xls, .xlsx";
    }
}
