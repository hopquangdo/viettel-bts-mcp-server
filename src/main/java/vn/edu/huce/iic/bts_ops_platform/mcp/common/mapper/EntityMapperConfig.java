package vn.edu.huce.iic.bts_ops_platform.mcp.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Cấu hình MapStruct dùng chung — khai báo trên mỗi module mapper:
 * {@code @Mapper(config = EntityMapperConfig.class)}
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EntityMapperConfig {}
