package vn.edu.huce.iic.bts_ops_platform.mcp.infrastructure.events;

import java.util.UUID;

public record AppEventMetadata(
        UUID actorId,
        String actorName,
        String requestId,
        String clientIp,
        String source) {}
