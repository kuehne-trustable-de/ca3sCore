package de.trustable.ca3s.core.service.dto.acme;

import java.io.IOException;
import java.util.Map;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class JWKJsonDeserializer extends StdDeserializer<JsonWebKey> {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -6791583612249649908L;

	public JWKJsonDeserializer() {
        super(JsonWebKey.class);
    }

    @Override
    public JsonWebKey deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final JsonLocation currentLocation = jsonParser.getCurrentLocation();
        try {
            return JsonWebKey.Factory.newJwk((Map<String, Object>) jsonParser.readValueAs(Map.class));
        } catch (JoseException e) {
            throw new JsonParseException("Unable to parse Json Web Key", currentLocation, e);
        }
    }
}