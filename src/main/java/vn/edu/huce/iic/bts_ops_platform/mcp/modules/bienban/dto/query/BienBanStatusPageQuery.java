package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.query;

import java.util.List;

/** Repository page result for the status listing. */
public record BienBanStatusPageQuery(List<BienBanStatusItemQuery> items, long totalItems) {
    public BienBanStatusPageQuery {
        items = List.copyOf(items);
    }
}