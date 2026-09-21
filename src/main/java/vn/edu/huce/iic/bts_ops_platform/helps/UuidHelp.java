package vn.edu.huce.iic.bts_ops_platform.helps;

import java.util.UUID;

public final class UuidHelp {

    private UuidHelp() {
    }

    public static UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
