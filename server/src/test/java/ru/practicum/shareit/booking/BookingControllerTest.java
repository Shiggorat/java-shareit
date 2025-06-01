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
import java.util.Optional;

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
    private final String startDate = futureStart.format(formatter);
    private final String endDate = futureEnd.format(formatter);
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
        // Создаем мок Item с available = false
        Item unavailableDryer = new Item(dryer.getId(), dryer.getName(), dryer.getDescription(), false,
                userIrina, null);

        // Мокаем поведение сервиса или репозитория, который ищет Item по ID
        Mockito.when(itemRepository.findById(dryer.getId()))
                .thenReturn(Optional.of(unavailableDryer));

        // Выполняем POST-запрос на создание бронирования
        mvc.perform(
                        post("/bookings")
                                .header("X-Sharer-User-Id", userOleg.getId())
                                .content(mapper.writeValueAsString(inputBookingDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest()); // ожидаем 400 Bad Request
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





//package ru.practicum.shareit.booking;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import ru.practicum.shareit.booking.dto.BookingDtoInput;
//import ru.practicum.shareit.booking.dto.BookingDtoOutput;
//import ru.practicum.shareit.booking.service.BookingService;
//import ru.practicum.shareit.item.dto.ItemDtoIdAndName;
//import ru.practicum.shareit.user.dto.UserDtoIdAndName;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(BookingController.class)
//public class BookingControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private BookingService bookingService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private BookingDtoInput createSampleBookingDtoInput() {
//        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
//        return new BookingDtoInput(
//                now.plusDays(1),
//                now.plusDays(2),
//                123L // itemId
//        );
//    }
//
//    private BookingDtoOutput createSampleBookingDtoOutput(Long id) {
//        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
//        ItemDtoIdAndName item = new ItemDtoIdAndName(123L, "Item Name");
//        UserDtoIdAndName booker = new UserDtoIdAndName(1L, "User Name");
//        return new BookingDtoOutput(
//                id,
//                now.plusDays(1),
//                now.plusDays(2),
//                item,
//                booker,
//                Status.WAITING
//        );
//    }
//
//    @Test
//    void create_ShouldReturnCreatedBooking() throws Exception {
//        Long userId = 1L;
//        BookingDtoInput inputDto = createSampleBookingDtoInput();
//        BookingDtoOutput outputDto = createSampleBookingDtoOutput(1L);
//
//        Mockito.when(bookingService.create(eq(userId), any(BookingDtoInput.class)))
//                .thenReturn(outputDto);
//
//        mockMvc.perform(post("/bookings")
//                        .header("X-Sharer-User-Id", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(outputDto.getId().intValue())))
//                .andExpect(jsonPath("$.start", is(outputDto.getStart().toString())))
//                .andExpect(jsonPath("$.end", is(outputDto.getEnd().toString())))
//                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId().intValue())))
//                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())))
//                .andExpect(jsonPath("$.booker.id", is(outputDto.getBooker().getId().intValue())))
//                .andExpect(jsonPath("$.booker.name", is(outputDto.getBooker().getName())))
//                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())));
//    }
//
//    @Test
//    void updateStatus_ShouldReturnUpdatedBooking() throws Exception {
//        Long ownerId = 2L;
//        Long bookingId = 10L;
//        boolean approved = true;
//
//        BookingDtoOutput outputDto = createSampleBookingDtoOutput(bookingId);
//
//        Mockito.when(bookingService.updateStatusOfBooking(eq(ownerId), eq(bookingId), eq(approved)))
//                .thenReturn(outputDto);
//
//        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
//                        .header("X-Sharer-User-Id", ownerId)
//                        .param("approved", String.valueOf(approved)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(outputDto.getId().intValue())));
//    }
//
//    @Test
//    void getById_ShouldReturnBooking() throws Exception {
//        Long userId = 1L;
//        Long bookingId = 5L;
//
//        BookingDtoOutput outputDto = createSampleBookingDtoOutput(bookingId);
//
//        Mockito.when(bookingService.getById(userId, bookingId))
//                .thenReturn(outputDto);
//
//        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
//                        .header("X-Sharer-User-Id", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(outputDto.getId().intValue())));
//    }
//
//    @Test
//    void getAllByUser_ShouldReturnList() throws Exception {
//        Long userId = 1L;
//
//        BookingDtoOutput output1 = createSampleBookingDtoOutput(1L);
//        List<BookingDtoOutput> list = Collections.singletonList(output1);
//
//        Mockito.when(bookingService.getAllByUser(eq(userId), any()))
//                .thenReturn(list);
//
//        mockMvc.perform(get("/bookings")
//                        .header("X-Sharer-User-Id", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].id", is(output1.getId().intValue())));
//    }
//
//    @Test
//    void getAllByOwner_ShouldReturnList() throws Exception {
//        Long ownerId = 2L;
//
//        BookingDtoOutput output1 = createSampleBookingDtoOutput(1L);
//        List<BookingDtoOutput> list = Collections.singletonList(output1);
//
//        Mockito.when(bookingService.getAllByOwner(eq(ownerId), any()))
//                .thenReturn(list);
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header("X-Sharer-User-Id", ownerId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].id", is(output1.getId().intValue())));
//    }
//}