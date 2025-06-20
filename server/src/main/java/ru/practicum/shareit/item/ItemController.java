package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBookingAndComments;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDtoBookingAndComments> getAllBy(@RequestHeader("X-Sharer-User-Id") long sharerId,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "100") int size) {
        return itemService.getAll(sharerId, from, size);
    }

    @GetMapping("/{itemId}")
    public ItemDtoBookingAndComments getById(@RequestHeader("X-Sharer-User-Id") long sharerId,
                                             @PathVariable long itemId) {
        return itemService.getById(sharerId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> getByText(@RequestParam String text,
                                   @RequestParam(defaultValue = "0") int from,
                                   @RequestParam(defaultValue = "100") int size) {
        if (text.isBlank()) {
            return List.of();
        } else {
            return itemService.getByText(text, from, size);
        }
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long sharerId,
                          @RequestBody ItemDtoInput itemDto) {
        return itemService.create(sharerId, itemDto);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long sharerId,
                          @PathVariable long id, @RequestBody ItemDto itemDto) {
        return itemService.update(sharerId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@RequestHeader("X-Sharer-User-Id") long sharerId,
                           @PathVariable long id) {
        itemService.deleteById(sharerId, id);
    }

    @DeleteMapping
    public void deleteAll() {
        itemService.deleteAll();
    }
}
