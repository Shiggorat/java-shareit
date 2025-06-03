package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") @Positive long requesterId) {
        return itemRequestClient.getItemRequests(requesterId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                          @PathVariable @Positive long requestId) {
        return itemRequestClient.getItemRequest(userId, requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllAnotherUsers(@RequestHeader("X-Sharer-User-Id") @Positive long requesterId,
                                                     @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                                     @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        return itemRequestClient.getAllAnotherUsers(requesterId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @Positive long requesterId,
                                         @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestClient.create(requesterId, itemRequestDto);
    }
}