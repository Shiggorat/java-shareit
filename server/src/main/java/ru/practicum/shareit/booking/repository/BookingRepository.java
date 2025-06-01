package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingsByBooker_Id(long bookerId, Sort sort);

    List<Booking> findBookingsByBooker_IdAndStartBeforeAndEndAfter(long bookerId,
                                                                   LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findBookingsByBooker_IdAndEndBefore(long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findBookingsByBooker_IdAndStartAfter(long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findBookingsByBooker_IdAndStatus(long bookerId, Status status, Sort sort);

    List<Booking> findBookingsByItem_Owner_Id(long ownerId, Sort sort);

    List<Booking> findBookingsByItem_Owner_IdAndStartBeforeAndEndAfter(long ownerId, LocalDateTime start,
                                                                       LocalDateTime end, Sort sort);

    List<Booking> findBookingsByItem_Owner_IdAndEndBefore(long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findBookingsByItem_Owner_IdAndStartAfter(long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findBookingsByItem_Owner_IdAndStatus(long ownerId, Status status, Sort sort);

    List<Booking> findBookingsByBooker_IdAndItem_IdAndEndIsBefore(long bookerId, long itemId,
                                                                  LocalDateTime localDateTime);

    Optional<Booking> findFirstByItem_IdAndStartBeforeOrderByEndDesc(long id, LocalDateTime localDateTime);

    Optional<Booking> findFirstByItem_IdAndStartAfterOrderByEndDesc(long id, LocalDateTime localDateTime);

    @Query(value = "select book from Booking as book where book.item.id in ?1 and book.status = 'APPROVED'" +
            " and book.start <= current_timestamp order by book.end desc")
    List<Booking> findLastBookings(Set<Long> itemsId);

    @Query(value = "select book from Booking as book where book.item.id in ?1 and book.status = 'APPROVED'" +
            " and book.start > current_timestamp order by book.end asc")
    List<Booking> findNextBookings(Set<Long> itemsId);
}
