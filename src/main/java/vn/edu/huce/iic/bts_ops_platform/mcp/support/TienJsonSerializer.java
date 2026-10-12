package vn.edu.huce.iic.bts_ops_platform.mcp.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/** Ghi 1 số tiền VNĐ (mọi kiểu Number) thành chuỗi đọc được qua {@link McpFormat#tien(Number)}; dùng qua {@link Tien}. */
public class TienJsonSerializer extends StdSerializer<Number> {

    public TienJsonSerializer() {
        super(Number.class);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(McpFormat.tien(value));
    }
}
