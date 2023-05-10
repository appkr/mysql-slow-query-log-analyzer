package dev.appkr.tool.domain.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.appkr.tool.domain.Carbon;
import java.io.IOException;

public class CarbonJsonSerializer extends JsonSerializer<Carbon> {

  @Override
  public void serialize(Carbon value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      return;
    }
    gen.writeString(value.toString());
  }
}
