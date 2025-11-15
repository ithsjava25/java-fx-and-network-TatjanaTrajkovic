package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    public boolean sendCalled = false;
    public String sentUser;
    public String sentMessage;

    public boolean receiveCalled = false;
    public Consumer<NtfyMessageDto> listener;

    @Override
    public void sendMessage(String user, String message) {
        this.sendCalled = true;
        this.sentUser = user;
        this.sentMessage = message;
    }

    @Override
    public void receiveMessage(Consumer<NtfyMessageDto> listener) {
        this.receiveCalled = true;
        this.listener = listener;
    }
}
