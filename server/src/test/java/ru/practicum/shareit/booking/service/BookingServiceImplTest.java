package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.ItemDtoIdAndName;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDtoIdAndName;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDtoInput inputDto;
    private BookingDtoOutput outputDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        owner = new User();
        owner.setId(2L);

        item = new Item();
        item.setId(10L);
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(100L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        inputDto = createSampleBookingDtoInput();
        inputDto.setItemId(item.getId());
        inputDto.setStart(LocalDateTime.now().plusDays(1));
        inputDto.setEnd(LocalDateTime.now().plusDays(2));

        outputDto = createSampleBookingDtoOutput(123L);
    }

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
    void create_ShouldCreateBooking_WhenDataIsValid() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingMapper.fromInputDto(any(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toOutputDto(any())).thenReturn(outputDto);

        BookingDtoOutput result = bookingService.create(user.getId(), inputDto);

        assertNotNull(result);
        verify(userRepository).findById(user.getId());
        verify(itemRepository).findById(inputDto.getItemId());
        verify(bookingMapper).fromInputDto(any(), any(), any());
        verify(bookingRepository).save(any());
        verify(bookingMapper).toOutputDto(any());
    }

    @Test
    void create_ShouldThrowValidateException_WhenStartAfterEnd() {
        inputDto.setStart(LocalDateTime.now().plusDays(3));
        inputDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidateException.class, () ->
                bookingService.create(user.getId(), inputDto)
        );
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.create(user.getId(), inputDto)
        );
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenItemNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                bookingService.create(user.getId(), inputDto)
        );
    }

    @Test
    void getByID_ShouldReturnBooking_WhenUserIsBookerOrOwner() {
        long userID = user.getId();

        Booking existingBooking = new Booking();
        existingBooking.setId(200L);
        existingBooking.setItem(item);
        existingBooking.setBooker(user);

        when(bookingRepository.findById(existingBooking.getId()))
                .thenReturn(Optional.of(existingBooking));
        when(userRepository.findById(userID))
                .thenReturn(Optional.of(user));
        when(bookingMapper.toOutputDto(existingBooking))
                .thenReturn(outputDto);

        var result = bookingService.getById(userID, existingBooking.getId());

        assertNotNull(result);
        verify(bookingMapper).toOutputDto(existingBooking);
    }
}
