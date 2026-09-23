package vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong;

/**
 * 1 dòng của câu gộp {@code progressBundle}: {@link #getKind()} = 'G' (nhóm đối tượng, dùng phần của {@link ProgressGroupProjection}),
 * 'T' (top đối tượng theo giá trị kỳ) hoặc 'C' (đối tượng chưa có sản lượng, dùng phần của {@link DoiTuongChuaCoProjection}).
 * Trường không thuộc loại dòng đó là null.
 */
public interface ProgressBundleRow extends ProgressGroupProjection, DoiTuongChuaCoProjection {
    String getKind();
}
