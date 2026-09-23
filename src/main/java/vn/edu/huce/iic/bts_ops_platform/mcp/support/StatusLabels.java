package vn.edu.huce.iic.bts_ops_platform.mcp.support;

/**
 * Đổi các mã trạng thái nội bộ (DB) sang text tiếng Việt cho response của AI tool, để LLM đọc/map
 * trực tiếp thay vì phải tự suy đoán ý nghĩa mã (vd "cho_duyet" -> "Chờ duyệt"). Không dùng cho
 * input filter — filter vẫn nhận mã gốc như cũ, các hàm ở đây chỉ áp cho output.
 */
public final class StatusLabels {

    private StatusLabels() {
    }

    /** Trạng thái duyệt biên bản (bảng bien_ban). */
    public static String bienBan(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "cho_duyet" -> "Chờ duyệt";
            case "da_duyet" -> "Đã duyệt";
            case "tu_choi" -> "Từ chối";
            default -> raw;
        };
    }

    /** Trạng thái xử lý vướng mắc (bảng vuong_mac). */
    public static String vuongMac(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "pending" -> "Chờ xử lý";
            case "in_progress" -> "Đang xử lý";
            case "resolved" -> "Đã xử lý";
            case "rejected" -> "Từ chối";
            default -> raw;
        };
    }

    /** Kiểu vướng mắc (bảng vuong_mac.kieu_vuong_mac). */
    public static String kieuVuongMac(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "giai_phong_mat_bang" -> "Giải phóng mặt bằng";
            case "thiet_ke" -> "Thiết kế";
            case "thu_tuc_phap_ly" -> "Thủ tục pháp lý";
            case "nguon_vat_lieu" -> "Nguồn vật liệu";
            case "thi_cong" -> "Thi công";
            default -> raw;
        };
    }

    /** Trạng thái thi công hợp đồng (hop_dong.trang_thai_thi_cong). */
    public static String trangThaiThiCong(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "CHUA_TC" -> "Chưa thi công";
            case "DANG_TC" -> "Đang thi công";
            case "HOAN_THANH" -> "Hoàn thành";
            case "HUY" -> "Đã hủy";
            default -> raw;
        };
    }

    /** Trạng thái đảm bảo vật tư A (hop_dong_doi_tuong.trang_thai_vat_tu_a). */
    public static String vatTuA(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "CHUA_DAM_BAO" -> "Chưa đảm bảo";
            case "DA_DAM_BAO" -> "Đã đảm bảo";
            default -> raw;
        };
    }

    /** Trạng thái yêu cầu/nhận vật tư B (hop_dong_doi_tuong.trang_thai_vat_tu_b). */
    public static String vatTuB(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "CHUA_YEU_CAU" -> "Chưa yêu cầu";
            case "DA_YEU_CAU" -> "Đã yêu cầu";
            case "DA_NHAN" -> "Đã nhận";
            default -> raw;
        };
    }

    public static String tramTonTrangThai(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "cho_quyet_toan" -> "Chờ quyết toán";
            default -> raw;
        };
    }

    public static String nguonViecStatus(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "dang_trien_khai" -> "Đang triển khai";
            case "vuong_phap_ly" -> "Vướng pháp lý";
            case "da_hoan_thanh" -> "Đã hoàn thành";
            case "gan_hoan_thanh" -> "Gần hoàn thành";
            default -> raw;
        };
    }

    public static String nguonViecPhapLyStage(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "nhap" -> "Nháp";
            case "chua_co" -> "Chưa có pháp lý";
            case "da_co" -> "Đã có pháp lý";
            default -> raw;
        };
    }

    public static String nguonViecLoaiCv(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "TK" -> "Tư vấn";
            case "KSTK" -> "Khảo sát thiết kế";
            case "TC" -> "Thi công";
            default -> raw;
        };
    }

    public static String nguonViecLinhVuc(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "tu-van" -> "Tư vấn";
            case "kiem-dinh" -> "Kiểm định";
            case "gphtvt" -> "Giải pháp HTVT";
            case "do-luong" -> "Đo lường";
            case "ict" -> "ICT";
            case "dan-dung" -> "Dân dụng";
            case "tong" -> "Tổng hợp";
            case "" -> "Chưa xác định";
            default -> raw;
        };
    }

    public static String volumeTrangThai(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "binh_thuong" -> "Bình thường";
            case "canh_bao" -> "Cảnh báo";
            case "vuot_nguong" -> "Vượt ngưỡng";
            default -> raw;
        };
    }

    public static String volumeAlertType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "shortage" -> "Thiếu định mức";
            case "surplus" -> "Vượt định mức";
            case "warning" -> "Cảnh báo";
            case "normal" -> "Bình thường";
            case "abnormal" -> "Bất thường";
            default -> raw;
        };
    }

    public static String loaiBienBan(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw) {
            case "BIEN_BAN_SO_1" -> "Biên bản nghiệm thu khối lượng";
            case "NHAT_KY_THI_CONG" -> "Nhật ký thi công";
            case "BIEN_BAN_SO_2" -> "Biên bản nghiệm thu tổng hợp";
            case "BAO_CAO_KHAO_SAT" -> "Báo cáo khảo sát (Bìa + Thuyết minh)";
            case "BAN_GIAO_MAT_BANG" -> "Biên bản bàn giao mặt bằng";
            case "YEU_CAU_VAT_TU" -> "Biên bản yêu cầu cung cấp vật tư";
            case "NHAN_VAT_TU" -> "Biên bản nhận vật tư";
            default -> raw;
        };
    }
}
