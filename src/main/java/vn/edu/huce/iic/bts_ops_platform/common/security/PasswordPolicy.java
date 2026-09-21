package vn.edu.huce.iic.bts_ops_platform.common.security;

/**
 * Chính sách mật khẩu tập trung (ATTT PL06.1#4 / PL02#26): tối thiểu 8 ký tự và phải có đủ
 * chữ HOA, chữ thường, chữ số. Dùng chung cho mọi DTO đặt/đổi mật khẩu qua {@code @Pattern}.
 * Không siết ký tự đặc biệt bắt buộc để tránh chặn mật khẩu mạnh hợp lệ, nhưng vẫn cho phép.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {}

    /** ≥8 ký tự, có ít nhất 1 chữ thường, 1 chữ hoa, 1 chữ số. */
    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,100}$";

    public static final String MESSAGE =
            "Mật khẩu phải từ 8 ký tự trở lên và gồm cả chữ hoa, chữ thường và chữ số";
}
