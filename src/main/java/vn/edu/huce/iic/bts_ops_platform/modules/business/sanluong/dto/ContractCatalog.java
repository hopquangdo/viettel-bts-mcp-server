package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ContractCatalog(
        List<ContractWorkItem> items,
        Set<UUID> congViecIds,
        Set<UUID> chiTietLeafIds) {
}
