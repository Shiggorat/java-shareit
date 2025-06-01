package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EntityManager entityManager; // для сохранения тестовых данных

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        user1 = new User(null, "User1", "user1@example.com");
        user2 = new User(null, "User2", "user2@example.com");
        entityManager.persist(user1);
        entityManager.persist(user2);

        item1 = new Item(null, "Item1", "Description1", true, user2, null);
        item2 = new Item(null, "Item2", "Description2", true, user2, null);
        entityManager.persist(item1);
        entityManager.persist(item2);

        Booking booking1 = new Booking(null, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), item1, user1, Status.APPROVED);
        Booking booking2 = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3), item2, user1, Status.WAITING);
        Booking booking3 = new Booking(null, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(8), item2, user1, Status.REJECTED);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
    }

    @Test
    void findBookingsByBooker_Id_ShouldReturnBookings() {
        List<Booking> bookings = bookingRepository.findBookingsByBooker_Id(user1.getId(), Sort.by(Sort.Direction.DESC, "start"));
        assertThat(bookings).hasSize(3);
    }

    @Test
    void findBookingsByBooker_IdAndEndBefore_ShouldReturnPastBookings() {
        List<Booking> bookings = bookingRepository.findBookingsByBooker_IdAndEndBefore(
                user1.getId(), Sort.by(Sort.Direction.DESC, "end"), LocalDateTime.now());
        assertThat(bookings).hasSize(2); // booking1 и booking3
    }

    @Test
    void findBookingsByBooker_IdAndStartAfter_ShouldReturnFutureBookings() {
        List<Booking> bookings = bookingRepository.findBookingsByBooker_IdAndStartAfter(
                user1.getId(), Sort.by(Sort.Direction.ASC, "start"), LocalDateTime.now());
        assertThat(bookings).hasSize(1); // booking2
    }

    @Test
    void findBookingsByBooker_IdAndStatus_ShouldReturnStatusFiltered() {
        List<Booking> waitingBookings = bookingRepository.findBookingsByBooker_IdAndStatus(
                user1.getId(), Sort.by(Sort.Direction.DESC, "start"), Status.WAITING);
        assertThat(waitingBookings).hasSize(1);

        List<Booking> rejectedBookings = bookingRepository.findBookingsByBooker_IdAndStatus(
                user1.getId(), Sort.by(Sort.Direction.DESC, "start"), Status.REJECTED);
        assertThat(rejectedBookings).hasSize(1);

        List<Booking> approvedBookings = bookingRepository.findBookingsByBooker_IdAndStatus(
                user1.getId(), Sort.by(Sort.Direction.DESC, "start"), Status.APPROVED);
        assertThat(approvedBookings).hasSize(1);
    }

    @Test
    void findBookingsByItem_Owner_Id_ShouldReturnOwnerItems() {
        List<Booking> bookings = bookingRepository.findBookingsByItem_Owner_Id(user2.getId(), Sort.by(Sort.Direction.DESC, "start"));
        assertThat(bookings).hasSize(3);
    }
    @Test
    void findBookingsByItem_Owner_IdAndEndBefore_ShouldReturnPastOwnerItems() {
        List<Booking> bookings = bookingRepository.findBookingsByItem_Owner_IdAndEndBefore(
                user2.getId(), Sort.by(Sort.Direction.DESC, "end"), LocalDateTime.now());
        assertThat(bookings).hasSize(2); // бронирования с end в прошлом (booking1 и booking3)
    }

    @Test
    void findBookingsByItem_Owner_IdAndStartAfter_ShouldReturnUpcomingOwnerItems() {
        List<Booking> bookings = bookingRepository.findBookingsByItem_Owner_IdAndStartAfter(
                user2.getId(), Sort.by(Sort.Direction.ASC, "start"), LocalDateTime.now());
        assertThat(bookings).hasSize(1); // бронирование с start в будущем (booking2)
    }
}
