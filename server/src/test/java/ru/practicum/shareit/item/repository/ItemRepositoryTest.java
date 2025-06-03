package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void getByText_shouldSuccess() {
        User userOleg = new User(null, "nam", "man@yandex.ru");
        userRepository.save(userOleg);
        Item item = itemRepository.save(new Item(null, "playstation", "For real man",
                true, userOleg, null));

        List<Item> result = itemRepository.findByText("ays", Pageable.unpaged());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.getFirst().getId());
        assertEquals(item.getName(), result.getFirst().getName());
        assertEquals(item.getAvailable(), result.getFirst().getAvailable());
    }
}
