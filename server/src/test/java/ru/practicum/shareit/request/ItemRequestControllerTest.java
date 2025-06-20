package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoRequests;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutput;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoIdAndName;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private final UserDto userOleg = new UserDto(1L, "Oleg", "oleg@yandex.ru");
    private final UserDtoIdAndName userOlegShort = new UserDtoIdAndName(userOleg.getId(), userOleg.getName());
    private final UserDto userIrina = new UserDto(2L, "Irina", "irina@yandex.ru");
    private final ItemRequestDto request = new ItemRequestDto(
            4L,
            "I want this item!",
            userOleg.getId(),
            LocalDateTime.of(2023, 1, 20, 12, 0, 0)
    );
    private final ItemDto dryer = new ItemDto(
            3L,
            "Dryer",
            "For curly hair",
            true,
            userIrina,
            request.getId()
    );
    private final ItemDtoRequests dryerForRequest = new ItemDtoRequests(
            dryer.getId(),
            dryer.getName(),
            dryer.getDescription(),
            dryer.getAvailable(),
            dryer.getRequestId()
    );
    private final ItemRequestOutput requestWithItems = new ItemRequestOutput(
            request.getId(),
            request.getDescription(),
            userOlegShort,
            request.getCreated(),
            List.of(dryerForRequest)
    );
    private final String created = "2023-01-20T12:00:00";
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    @Test
    void create_shouldSuccess() throws Exception {
        Mockito
                .when(itemRequestService.create(anyLong(), any()))
                .thenReturn(requestWithItems);

        mvc.perform(
                        post("/requests")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .content(mapper.writeValueAsString(request))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(request.getDescription())))
                .andExpect(jsonPath("$.requester.id", is(request.getRequesterId()), Long.class))
                .andExpect(jsonPath("$.created", is(created)));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .create(1L, request);
    }

    @Test
    void getById_shouldSuccess() throws Exception {
        Mockito
                .when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(requestWithItems);

        mvc.perform(
                        get("/requests/{requestId}", request.getId())
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestWithItems.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestWithItems.getDescription())))
                .andExpect(jsonPath("$.created", is(created)))
                .andExpect(jsonPath("$.items[0].id",
                        is(dryer.getId()), Long.class))
                .andExpect(
                        jsonPath("$.items[0].description",
                                is(dryer.getDescription()))
                );

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getById(1L, 4L);
    }

    @Test
    void getAll_shouldSuccess() throws Exception {
        Mockito
                .when(itemRequestService.getAll(anyLong()))
                .thenReturn(List.of(requestWithItems));

        mvc.perform(
                        get("/requests")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(requestWithItems.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(requestWithItems.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(created)))
                .andExpect(jsonPath("$.[0].items[0].id",
                        is(requestWithItems.getItems().getFirst().getId()), Long.class))
                .andExpect(jsonPath("$.[0].items[0].name",
                        is(requestWithItems.getItems().getFirst().getName())))
                .andExpect(
                        jsonPath("$.[0].items[0].description",
                                is(requestWithItems.getItems().getFirst().getDescription()))
                )
                .andExpect(
                        jsonPath("$.[0].items[0].available",
                                is(requestWithItems.getItems().getFirst().getAvailable()))
                )
                .andExpect(
                        jsonPath("$.[0].items[0].requestId",
                                is(requestWithItems.getItems().getFirst().getRequestId()), Long.class
                        )
                );

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getAll(1L);
    }

    @Test
    void getAllAnotherUsers_shouldSuccess() throws Exception {
        Mockito
                .when(itemRequestService.getAllAnotherUsers(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestWithItems));

        mvc.perform(
                        get("/requests/all")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .param("from", "0")
                                .param("size", "1")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(requestWithItems.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(requestWithItems.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(created)))
                .andExpect(jsonPath("$.[0].items[0].id",
                        is(requestWithItems.getItems().getFirst().getId()), Long.class))
                .andExpect(jsonPath("$.[0].items[0].name",
                        is(requestWithItems.getItems().getFirst().getName())))
                .andExpect(
                        jsonPath("$.[0].items[0].description",
                                is(requestWithItems.getItems().getFirst().getDescription()))
                )
                .andExpect(
                        jsonPath("$.[0].items[0].available",
                                is(requestWithItems.getItems().getFirst().getAvailable()))
                )
                .andExpect(
                        jsonPath(
                                "$.[0].items[0].requestId",
                                is(requestWithItems.getItems().getFirst().getRequestId()), Long.class
                        )
                );

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getAllAnotherUsers(1L, 0, 1);
    }
}
