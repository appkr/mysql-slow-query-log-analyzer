package dev.appkr.tools.domain.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.appkr.tools.domain.Carbon;
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
