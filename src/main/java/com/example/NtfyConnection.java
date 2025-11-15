package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {
    void sendMessage(String user, String message);
    void receiveMessage(Consumer<NtfyMessageDto> listener);

}
