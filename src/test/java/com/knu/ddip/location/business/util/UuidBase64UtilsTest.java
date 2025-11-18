package com.knu.ddip.location.application.util;

import com.knu.ddip.location.application.util.UuidBase64Utils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidBase64UtilsTest {

    @Test
    void uuidEncodeAndDecodeTest() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        String encodedUuid = UuidBase64Utils.uuidToBase64String(uuid);

        UUID decodedUuid = UuidBase64Utils.base64StringToUuid(encodedUuid);

        // then
        assertThat(decodedUuid).isEqualTo(uuid);
    }

}