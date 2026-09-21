package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import java.math.BigDecimal;

final class HangMucCalcToken {
    HangMucCalcTokenType type;
    BigDecimal number;
    char op;
    String ma;
    HangMucCalcField field;

    static HangMucCalcToken number(BigDecimal value) {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.NUMBER;
        t.number = value;
        return t;
    }

    static HangMucCalcToken op(char value) {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.OP;
        t.op = value;
        return t;
    }

    static HangMucCalcToken lparen() {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.LPAREN;
        return t;
    }

    static HangMucCalcToken rparen() {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.RPAREN;
        return t;
    }

    static HangMucCalcToken percent() {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.PERCENT;
        return t;
    }

    static HangMucCalcToken maRef(String ma, HangMucCalcField field) {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.MA_REF;
        t.ma = ma;
        t.field = field;
        return t;
    }

    static HangMucCalcToken selfRef(HangMucCalcField field) {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.SELF_REF;
        t.field = field;
        return t;
    }

    static HangMucCalcToken sumRef(HangMucCalcField field) {
        HangMucCalcToken t = new HangMucCalcToken();
        t.type = HangMucCalcTokenType.SUM_REF;
        t.field = field;
        return t;
    }
}
