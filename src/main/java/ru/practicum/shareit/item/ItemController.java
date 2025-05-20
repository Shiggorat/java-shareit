package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingAndComments;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.service.ItemService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemDtoBookingAndComments> getAll(@RequestHeader("X-Sharer-User-Id") long ownerId) {
        return itemService.getAll(ownerId);
    }

    @GetMapping("/{id}")
    public ItemDtoBookingAndComments  findById(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                               @PathVariable long id) {
        return itemService.getById(ownerId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> findByText(@RequestParam String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        } else {
            return itemService.getByText(text);
        }
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long ownerId,
                          @Valid @RequestBody ItemDtoInput itemDto) {
        return itemService.create(ownerId, itemDto);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{id}")
    public ItemDto patch(@RequestHeader("X-Sharer-User-Id") long ownerId,
                         @PathVariable long id, @RequestBody ItemDto itemDto) {
        return itemService.update(ownerId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") long ownerId,
                           @PathVariable long id) {
        itemService.deleteById(ownerId, id);
    }

    @DeleteMapping
    public void deleteAll() {
        itemService.deleteAll();
    }
}
