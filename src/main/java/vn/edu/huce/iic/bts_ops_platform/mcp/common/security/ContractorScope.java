package vn.edu.huce.iic.bts_ops_platform.mcp.common.security;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public record ContractorScope(Set<UUID> hopDongIds, Set<UUID> hopDongDoiTuongIds) {

    public static ContractorScope empty() {
        return new ContractorScope(Set.of(), Set.of());
    }

    public boolean isEmpty() {
        return hopDongIds.isEmpty() && hopDongDoiTuongIds.isEmpty();
    }

    public Set<UUID> hopDongIds() {
        return hopDongIds == null ? Set.of() : Collections.unmodifiableSet(hopDongIds);
    }

    public Set<UUID> hopDongDoiTuongIds() {
        return hopDongDoiTuongIds == null ? Set.of() : Collections.unmodifiableSet(hopDongDoiTuongIds);
    }
}
