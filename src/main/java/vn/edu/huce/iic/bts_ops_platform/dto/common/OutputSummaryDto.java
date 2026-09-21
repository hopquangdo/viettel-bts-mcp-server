package vn.edu.huce.iic.bts_ops_platform.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Tổng hợp số lượng/giá trị trong 1 kỳ — dùng chung cho mọi tool OVERVIEW có khái niệm "đối tượng phát sinh giá trị". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutputSummaryDto {
    private int totalObjects;
    private int objectsWithOutput;
    private int objectsWithoutOutput;
    private BigDecimal periodValue;
    private BigDecimal todayValue;
    private BigDecimal averageValuePerObject;
    private String unit;
    private int openIssueCount;
}
