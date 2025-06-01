package ru.practicum.shareit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;

@Configuration
public class WebClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BookingClient bookingClient(RestTemplate restTemplate) {
        return new BookingClient(restTemplate);
    }

    @Bean
    public ItemClient itemClient(RestTemplate restTemplate) {
        return new ItemClient(restTemplate);
    }

    @Bean
    public ItemRequestClient itemRequestClient(RestTemplate restTemplate) {
        return new ItemRequestClient(restTemplate);
    }

    @Bean
    public UserClient userClient(RestTemplate restTemplate) {
        return new UserClient(restTemplate);
    }
}
