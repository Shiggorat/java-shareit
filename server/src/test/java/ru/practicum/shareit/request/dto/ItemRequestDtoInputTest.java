package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoInputTest {

    @Autowired
    private JacksonTester<ItemRequestDto> jacksonTester;

    @Test
    void serialization() throws IOException {
        UserDto user = new UserDto(1L, "nam", "man@yandex.ru");
        ItemRequestDto request = new ItemRequestDto(
                1L,
                "I want it",
                user.getId(),
                LocalDateTime.now().plusHours(1).withNano(0));
        JsonContent<ItemRequestDto> result = jacksonTester.write(request);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.requesterId");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(request.getId().intValue());
        assertThat(result).extractingJsonPathValue("$.description").isEqualTo(request.getDescription());
        assertThat(result).extractingJsonPathNumberValue("$.requesterId")
                .isEqualTo(request.getRequesterId().intValue());
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo(request.getCreated().toString());
    }
}
