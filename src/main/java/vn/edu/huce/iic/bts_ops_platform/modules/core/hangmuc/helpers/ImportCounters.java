package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.helpers;

import java.util.List;

public final class ImportCounters {

    public final int created;
    public final int updated;
    public final int skipped;
    public final List<String> errors;

    private ImportCounters(int created, int updated, int skipped, List<String> errors) {
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.errors = errors;
    }

    public static ImportCounters created(int count) {
        return new ImportCounters(count, 0, 0, List.of());
    }

    public static ImportCounters updated(int count) {
        return new ImportCounters(0, count, 0, List.of());
    }

    public static ImportCounters skipped(int count) {
        return new ImportCounters(0, 0, count, List.of());
    }

    public static ImportCounters error(String message) {
        return new ImportCounters(0, 0, 1, List.of(message));
    }
}
