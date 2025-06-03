package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
    public BookingClient bookingClient(@Value("${shareit-server.url}") String serverUrl) {
        return new BookingClient(serverUrl, restTemplate());
    }

    @Bean
    public ItemClient itemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new ItemClient(serverUrl, builder);
    }

    @Bean
    public ItemRequestClient itemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new ItemRequestClient(serverUrl, builder);
    }

    @Bean
    public UserClient userClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        return new UserClient(serverUrl, builder);
    }
}
