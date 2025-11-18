package com.knu.ddip.location.application.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import static java.util.Base64.getUrlDecoder;
import static java.util.Base64.getUrlEncoder;

public abstract class UuidBase64Utils {

    private static final Base64.Encoder B64_URL = getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DEC = getUrlDecoder();

    public static String uuidToBase64String(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return B64_URL.encodeToString(bb.array());
    }

    public static UUID base64StringToUuid(String string) {
        byte[] bytes = B64_DEC.decode(string);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }
}
