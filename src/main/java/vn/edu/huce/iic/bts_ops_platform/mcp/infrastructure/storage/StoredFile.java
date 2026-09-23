package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.storage;

public record StoredFile(
        String relativePath,
        String storedFileName,
        String originalFileName,
        long size,
        String contentType,
        String extension
) {}
