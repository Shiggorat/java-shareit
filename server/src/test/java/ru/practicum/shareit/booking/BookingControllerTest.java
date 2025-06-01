package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDtoIdAndName;
import ru.practicum.shareit.user.dto.UserDtoIdAndName;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingDtoInput createSampleBookingDtoInput() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return new BookingDtoInput(
                now.plusDays(1),
                now.plusDays(2),
                123L // itemId
        );
    }

    private BookingDtoOutput createSampleBookingDtoOutput(Long id) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemDtoIdAndName item = new ItemDtoIdAndName(123L, "Item Name");
        UserDtoIdAndName booker = new UserDtoIdAndName(1L, "User Name");
        return new BookingDtoOutput(
                id,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                Status.WAITING
        );
    }

    @Test
    void create_ShouldReturnCreatedBooking() throws Exception {
        Long userId = 1L;
        BookingDtoInput inputDto = createSampleBookingDtoInput();
        BookingDtoOutput outputDto = createSampleBookingDtoOutput(1L);

        Mockito.when(bookingService.create(eq(userId), any(BookingDtoInput.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId().intValue())))
                .andExpect(jsonPath("$.start", is(outputDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(outputDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(outputDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(outputDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(outputDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(outputDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(outputDto.getStatus().toString())));
    }

    @Test
    void updateStatus_ShouldReturnUpdatedBooking() throws Exception {
        Long ownerId = 2L;
        Long bookingId = 10L;
        boolean approved = true;

        BookingDtoOutput outputDto = createSampleBookingDtoOutput(bookingId);

        Mockito.when(bookingService.updateStatusOfBooking(eq(ownerId), eq(bookingId), eq(approved)))
                .thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId().intValue())));
    }

    @Test
    void getById_ShouldReturnBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 5L;

        BookingDtoOutput outputDto = createSampleBookingDtoOutput(bookingId);

        Mockito.when(bookingService.getById(userId, bookingId))
                .thenReturn(outputDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputDto.getId().intValue())));
    }

    @Test
    void getAllByUser_ShouldReturnList() throws Exception {
        Long userId = 1L;

        BookingDtoOutput output1 = createSampleBookingDtoOutput(1L);
        List<BookingDtoOutput> list = Collections.singletonList(output1);

        Mockito.when(bookingService.getAllByUser(eq(userId), any()))
                .thenReturn(list);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(output1.getId().intValue())));
    }

    @Test
    void getAllByOwner_ShouldReturnList() throws Exception {
        Long ownerId = 2L;

        BookingDtoOutput output1 = createSampleBookingDtoOutput(1L);
        List<BookingDtoOutput> list = Collections.singletonList(output1);

        Mockito.when(bookingService.getAllByOwner(eq(ownerId), any()))
                .thenReturn(list);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(output1.getId().intValue())));
    }
}