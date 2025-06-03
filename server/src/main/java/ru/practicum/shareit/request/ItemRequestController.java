package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutput;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @GetMapping
    public List<ItemRequestOutput> getAll(@RequestHeader("X-Sharer-User-Id") long requesterId) {
        log.debug("Request GET to /requests");
        return itemRequestService.getAll(requesterId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestOutput getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long requestId) {
        log.debug("Request GET to /requests/{}", requestId);
        return itemRequestService.getById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestOutput> getAllAnotherUsers(@RequestHeader("X-Sharer-User-Id") long requesterId,
                                                      @RequestParam(name = "from", defaultValue = "0") int from,
                                                      @RequestParam(name = "size", defaultValue = "10") int size) {
        log.debug("Request GET to /requests/all");
        return itemRequestService.getAllAnotherUsers(requesterId, from, size);
    }

    @PostMapping
    public ItemRequestOutput create(@RequestHeader("X-Sharer-User-Id") long requesterId,
                                    @RequestBody ItemRequestDto itemRequestDto) {
        log.debug("Request POST to /requests");
        return itemRequestService.create(requesterId, itemRequestDto);
    }
}
