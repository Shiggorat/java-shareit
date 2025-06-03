package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDtoOutput create(long userId, BookingDtoInput bookingDto) {
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ServerException("Item is not available in this time");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User with id = " + userId + " is not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() ->
                        new NotFoundException("Item with id = " + bookingDto.getItemId() + " is not found"));
        Booking booking = bookingMapper.fromInputDto(bookingDto, item, user);
        if (booking.getItem().getOwner().getId() == userId) {
            throw new NotFoundException("Owner can't booking item");
        }
        if (!item.getAvailable()) {
            throw new ServerException("Item is is not available");
        }
        booking.setStatus(Status.WAITING);

        Booking newBooking = bookingRepository.save(booking);
        return bookingMapper.toOutputDto(newBooking);
    }

    @Override
    @Transactional
    public BookingDtoOutput updateStatusOfBooking(long ownerId, long id, boolean approved) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Booking with this id is not found")
        );
        userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new AccessException("User with id = " + ownerId + " is not found"));
        log.info("User {} is attempting to update booking {}", ownerId, id);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.info("User {} is not owner of item {}", ownerId, booking.getItem().getId());
            throw new AccessException("Not owner of this item");
        }

        Status status;
        if (approved) {
            status = Status.APPROVED;
        } else {
            status = Status.REJECTED;
        }
        if (Objects.equals(booking.getStatus(), status)) {
            throw new EmailException("new status is equals old status");
        }
        booking.setStatus(status);

        bookingRepository.save(booking);
        return bookingMapper.toOutputDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoOutput getById(long userId, long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Booking with this id not found")
        );
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User with id = " + userId + " not found"));

        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return bookingMapper.toOutputDto(booking);
        } else {
            throw new NotFoundException("Not owner or booker of this item");
        }
    }

@Override
@Transactional(readOnly = true)
public List<BookingDtoOutput> getAllByOwner(long ownerId, State state, int from, int size) {
    if (!userRepository.existsById(ownerId)) {
        throw new NotFoundException("User with this id is not found");
    }
    return sortByState(state, ownerId, "owner", from, size).stream().map(bookingMapper::toOutputDto)
            .collect(Collectors.toList());
}

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOutput> getAllByUser(long userId, State state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with this id is not found");
        }
        return sortByState(state, userId, "user", from, size).stream().map(bookingMapper::toOutputDto)
                .collect(Collectors.toList());
    }

    private List<Booking> sortByState(State state, long id, String person, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        List<Booking> bookings;
        if (person.equals("owner")) {
            bookings = switch (state) {
                case ALL -> bookingRepository.findBookingsByItem_Owner_Id(id, pageable);
                case CURRENT -> bookingRepository.findBookingsByItem_Owner_IdAndStartBeforeAndEndAfter(
                        id,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        pageable);
                case PAST -> bookingRepository.findBookingsByItem_Owner_IdAndEndBefore(
                        id,
                        LocalDateTime.now(),
                        pageable);
                case FUTURE -> bookingRepository.findBookingsByItem_Owner_IdAndStartAfter(
                        id,
                        LocalDateTime.now(),
                        pageable);
                case WAITING -> bookingRepository.findBookingsByItem_Owner_IdAndStatus(id,
                        Status.WAITING,
                        pageable);
                case REJECTED -> bookingRepository.findBookingsByItem_Owner_IdAndStatus(id,
                        Status.REJECTED,
                        pageable);
                default -> throw new NotFoundException("Nonexistent state");
            };
        } else {
            bookings = switch (state) {
                case ALL -> bookingRepository.findBookingsByBooker_Id(id, pageable);
                case CURRENT -> bookingRepository.findBookingsByBooker_IdAndStartBeforeAndEndAfter(
                        id,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        pageable);
                case PAST -> bookingRepository.findBookingsByBooker_IdAndEndBefore(
                        id,
                        LocalDateTime.now(),
                        pageable);
                case FUTURE -> bookingRepository.findBookingsByBooker_IdAndStartAfter(
                        id,
                        LocalDateTime.now(),
                        pageable);
                case WAITING -> bookingRepository.findBookingsByBooker_IdAndStatus(id,
                        Status.WAITING,
                        pageable);
                case REJECTED -> bookingRepository.findBookingsByBooker_IdAndStatus(id,
                        Status.REJECTED,
                        pageable);
                default -> throw new NotFoundException("Nonexistent state");
            };
        }
        return bookings;
    }
}