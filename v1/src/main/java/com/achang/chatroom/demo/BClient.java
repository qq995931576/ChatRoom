package com.achang.chatroom.demo;

import com.achang.chatroom.clinet.ChatClient;

import java.io.IOException;

public class BClient {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("阿昌二号");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
