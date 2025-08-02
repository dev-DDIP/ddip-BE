package com.knu.ddip.auth.domain;

import com.knu.ddip.auth.exception.OAuthBadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceTypeTest {

    @Test
    void fromString_WithWebString_ShouldReturnPhoneType() {
        // Given
        String value = "phone";

        // When
        DeviceType result = DeviceType.fromString(value);

        // Then
        assertThat(result).isEqualTo(DeviceType.PHONE);
    }

    @Test
    void fromString_WithAppString_ShouldReturnPhoneType() {
        // Given
        String value = "phone";

        // When
        DeviceType result = DeviceType.fromString(value);

        // Then
        assertThat(result).isEqualTo(DeviceType.PHONE);
    }

    @Test
    void fromString_WithMixedCaseString_ShouldBeCaseInsensitive() {
        // Given
        String value = "PhoNe";

        // When
        DeviceType result = DeviceType.fromString(value);

        // Then
        assertThat(result).isEqualTo(DeviceType.PHONE);
    }

    @Test
    void fromString_WithNullString_ShouldThrowException() {
        // When, Then
        assertThatThrownBy(() -> DeviceType.fromString(null))
                .isInstanceOf(OAuthBadRequestException.class)
                .hasMessage("state값이 없습니다.");
    }

    @Test
    void fromString_WithEmptyString_ShouldThrowException() {
        // Given
        String value = "";

        // When, Then
        assertThatThrownBy(() -> DeviceType.fromString(value))
                .isInstanceOf(OAuthBadRequestException.class)
                .hasMessage("state값이 없습니다.");
    }
}
