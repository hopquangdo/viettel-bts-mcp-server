package vn.edu.huce.iic.bts_ops_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.excel-import")
public record AppExcelImportProperties(
        int maxImportRows,
        int maxPreviewRows,
        int batchSize
) {
    public AppExcelImportProperties {
        if (maxImportRows < 1) {
            maxImportRows = 100_000;
        }
        if (maxPreviewRows < 1) {
            maxPreviewRows = 500;
        }
        if (batchSize < 1) {
            batchSize = 500;
        }
    }
}
