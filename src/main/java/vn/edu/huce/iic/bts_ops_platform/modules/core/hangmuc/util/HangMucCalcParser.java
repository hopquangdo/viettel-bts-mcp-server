package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import java.math.BigDecimal;
import java.util.List;

final class HangMucCalcParser {
    private final List<HangMucCalcToken> tokens;
    private final HangMucCalcEvalContext ctx;
    private final int currentRow;
    private final HangMucCalcField defaultField;
    int index;

    HangMucCalcParser(
            List<HangMucCalcToken> tokens,
            HangMucCalcEvalContext ctx,
            int currentRow,
            HangMucCalcField defaultField) {
        this.tokens = tokens;
        this.ctx = ctx;
        this.currentRow = currentRow;
        this.defaultField = defaultField;
    }

    private BigDecimal applyPercent(BigDecimal value) {
        if (peek() != null && peek().type == HangMucCalcTokenType.PERCENT) {
            index += 1;
            return value.divide(HangMucThanhTienCalculator.HUNDRED, HangMucThanhTienCalculator.MC);
        }
        return value;
    }

    private HangMucCalcToken peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    private BigDecimal parsePrimary() {
        HangMucCalcToken token = peek();
        if (token == null) throw new IllegalStateException("Biểu thức chưa hoàn chỉnh");
        if (token.type == HangMucCalcTokenType.NUMBER) {
            index += 1;
            return applyPercent(token.number);
        }
        if (token.type == HangMucCalcTokenType.SUM_REF) {
            index += 1;
            HangMucCalcGridRow row = ctx.grid.byRowNumber.get(currentRow);
            if (row == null) throw new IllegalStateException("@SUM cần dòng hiện tại");
            return applyPercent(HangMucThanhTienCalculator.sumChildren(row, token.field, ctx));
        }
        if (token.type == HangMucCalcTokenType.MA_REF) {
            index += 1;
            HangMucCalcGridRow row = ctx.grid.byMa.get(HangMucThanhTienCalculator.normalizeMa(token.ma));
            if (row == null) throw new IllegalStateException("Không tìm thấy mã: " + token.ma);
            HangMucCalcField field = token.field != null ? token.field : defaultField;
            return applyPercent(HangMucThanhTienCalculator.getFieldValue(row.rowNumber, field, ctx));
        }
        if (token.type == HangMucCalcTokenType.SELF_REF) {
            index += 1;
            return applyPercent(HangMucThanhTienCalculator.getFieldValue(currentRow, token.field, ctx));
        }
        if (token.type == HangMucCalcTokenType.LPAREN) {
            index += 1;
            BigDecimal value = parseAddSub();
            if (peek() == null || peek().type != HangMucCalcTokenType.RPAREN) {
                throw new IllegalStateException("Thiếu dấu )");
            }
            index += 1;
            return applyPercent(value);
        }
        if (token.type == HangMucCalcTokenType.OP && token.op == '-') {
            index += 1;
            return parsePrimary().negate();
        }
        if (token.type == HangMucCalcTokenType.OP && token.op == '+') {
            index += 1;
            return parsePrimary();
        }
        throw new IllegalStateException("Biểu thức không hợp lệ");
    }

    private BigDecimal parseMulDiv() {
        BigDecimal value = parsePrimary();
        while (true) {
            HangMucCalcToken token = peek();
            if (token == null || token.type != HangMucCalcTokenType.OP
                    || (token.op != '*' && token.op != '/')) {
                break;
            }
            char op = token.op;
            index += 1;
            BigDecimal right = parsePrimary();
            if (op == '*') {
                value = value.multiply(right, HangMucThanhTienCalculator.MC);
            } else {
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalStateException("Không thể chia cho 0");
                }
                value = value.divide(right, HangMucThanhTienCalculator.MC);
            }
        }
        return value;
    }

    BigDecimal parseAddSub() {
        BigDecimal value = parseMulDiv();
        while (true) {
            HangMucCalcToken token = peek();
            if (token == null || token.type != HangMucCalcTokenType.OP
                    || (token.op != '+' && token.op != '-')) {
                break;
            }
            char op = token.op;
            index += 1;
            BigDecimal right = parseMulDiv();
            value = op == '+' ? value.add(right) : value.subtract(right);
        }
        return value;
    }
}
