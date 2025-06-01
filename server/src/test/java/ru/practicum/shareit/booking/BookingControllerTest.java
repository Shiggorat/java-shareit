package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.exception.NotFoundCustomException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.ItemDtoIdAndName;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDtoIdAndName;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private final User userOleg = new User(1L, "Oleg", "oleg@yandex.ru");
    private final UserDtoIdAndName userOlegDto = new UserDtoIdAndName(userOleg.getId(), userOleg.getName());
    private final User userIrina = new User(2L, "Irina", "irina@yandex.ru");
    private final Item dryer = new Item(3L, "Dryer", "For curly hair", false,
            userIrina, null);
    private final ItemDtoIdAndName dryerDto = new ItemDtoIdAndName(dryer.getId(), dryer.getName());
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime futureStart = now.plusDays(1);
    private final LocalDateTime futureEnd = futureStart.plusDays(4);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
    private final String startDate = futureStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private final String endDate = futureEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private final BookingDtoOutput bookingDto = new BookingDtoOutput(
            4L,
            futureStart,
            futureEnd,
            dryerDto,
            userOlegDto,
            Status.WAITING);
    private final BookingDtoInput inputBookingDto = new BookingDtoInput(futureStart, futureEnd, userOleg.getId());
    private final BookingDtoOutput approved = new BookingDtoOutput(bookingDto.getId(), bookingDto.getStart(), bookingDto.getEnd(),
            bookingDto.getItem(), bookingDto.getBooker(), Status.APPROVED);

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;
    @MockBean private ItemRepository itemRepository;
    @Autowired
    private MockMvc mvc;

    @Test
    void shouldCreate() throws Exception {
        Mockito
                .when(bookingService.create(anyLong(), any()))
                .thenReturn(bookingDto);

        mvc.perform(
                        post("/bookings")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .content(mapper.writeValueAsString(inputBookingDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(startDate)))
                .andExpect(jsonPath("$.end", is(endDate)))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));

        Mockito
                .verify(bookingService, Mockito.times(1))
                .create(1L, inputBookingDto);
    }

    @Test
    void shouldReturnNotFoundIfUserWrong() throws Exception {
        Mockito
                .when(bookingService.create(anyLong(), any()))
                .thenThrow(new NotFoundException("User with this id not found"));

        mvc.perform(
                        post("/bookings")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .content(mapper.writeValueAsString(inputBookingDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestIfItemUnavailable() throws Exception {
        Mockito
                .when(bookingService.create(anyLong(), any()))
                .thenThrow(new ValidateException("Item is not available"));

        mvc.perform(
                        post("/bookings")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .content(mapper.writeValueAsString(inputBookingDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldForbiddenCreate() throws Exception {
        Mockito
                .when(bookingService.create(anyLong(), any()))
                .thenThrow(new NotFoundException("User with this id cannot add item with this id"));

        mvc.perform(
                        post("/bookings")
                                .header("X-Sharer-User-Id", userIrina.getId())
                                .content(mapper.writeValueAsString(inputBookingDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldUpdateStatus() throws Exception {
        Mockito
                .when(bookingService.updateStatusOfBooking(anyLong(), anyLong(), eq(true)))
                .thenReturn(approved);

        mvc.perform(
                        patch("/bookings/{bookingId}", bookingDto.getId())
                                .header("X-Sharer-User-Id", userIrina.getId())
                                .param("approved", "true")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(approved.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(startDate)))
                .andExpect(jsonPath("$.end", is(endDate)))
                .andExpect(jsonPath("$.item.id", is(approved.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(approved.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(approved.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(approved.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(approved.getStatus().name())));

        Mockito.verify(bookingService, Mockito.times(1))
                .updateStatusOfBooking(2L, 4L, true);
    }

    @Test
    void updateStatus_shouldThrowException() throws Exception {
        Mockito
                .when(bookingService.updateStatusOfBooking(anyLong(), anyLong(), eq(true)))
                .thenThrow(new NotFoundCustomException("You are not owner of this item"));

        mvc.perform(
                        patch("/bookings/{bookingId}", bookingDto.getId())
                                .header("X-Sharer-User-Id", userIrina.getId())
                                .param("approved", "true")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        Mockito.verify(bookingService, Mockito.times(1))
                .updateStatusOfBooking(2L, 4L, true);
    }

    @Test
    void shouldReturnById() throws Exception {
        Mockito
                .when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(
                        get("/bookings/{bookingId}", bookingDto.getId())
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(startDate)))
                .andExpect(jsonPath("$.end", is(endDate)))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getById(1L, 4L);
    }

    @Test
    void shouldReturnByUser() throws Exception {
        Mockito
                .when(bookingService.getAllByUser(anyLong(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(
                        get("/bookings")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .param("state", "ALL")
                                .param("from", "0")
                                .param("size", "1")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(startDate)))
                .andExpect(jsonPath("$.[0].end", is(endDate)))
                .andExpect(jsonPath("$.[0].item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().name())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getAllByUser(1L, State.ALL);
    }

    @Test
    void shouldReturnByOwner() throws Exception {
        Mockito
                .when(bookingService.getAllByOwner(anyLong(), any()))
                .thenReturn(List.of(bookingDto));

        mvc.perform(
                        get("/bookings/owner")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .param("state", "ALL")
                                .param("from", "0")
                                .param("size", "1")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(startDate)))
                .andExpect(jsonPath("$.[0].end", is(endDate)))
                .andExpect(jsonPath("$.[0].item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().name())));
    }
}