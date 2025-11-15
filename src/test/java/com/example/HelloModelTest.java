package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
public class HelloModelTest {

    @Test
    void constructorShouldRegisterReceiveHandler(){
        NtfyConnectionSpy spy = new NtfyConnectionSpy();

        HelloModel model = new HelloModel(spy);

        assertTrue(spy.receiveCalled,
                "Model must call connection.receiveMessage() inside constructor");

        assertNotNull(spy.listener,
                "Model must pass a listener to receiveMessage()");
    }

    @Test
    void sendMessageShouldCallConnectionSendMessage(){
        NtfyConnectionSpy spy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(spy);

        model.sendMessage("Joe", "Hello world");

        assertTrue(spy.sendCalled, "Model must call connection.sendMessage()");
        assertEquals("Joe", spy.sentUser, "Model must pass the correct username");
        assertEquals("Hello world", spy.sentMessage, "Model must pass the correct message text");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wm){
        var topic = "testTopic";
        var url = "http://localhost:" + wm.getHttpPort();
        var con = new NtfyHttpConnection(url, topic);

        stubFor(post("/" + topic).willReturn(ok()));

        con.sendMessage("Jane", "Good Bye");

        verify(postRequestedFor(urlEqualTo("/" + topic))
                .withRequestBody(matchingJsonPath("$.user", equalTo("Jane")))
                .withRequestBody(matchingJsonPath("$.message", equalTo("Good Bye")))
        );
    }
}
