package com.achang.chatroom.clinet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/******
 @author 阿昌
 @create 2021-09-13 20:45
 聊天室客户端
 *******
 */
public class ChatClient {
    //启动方法
    public void startClient(String name) throws IOException {
        //连接服务端
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9090));
        //接收服务端响应数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);//设置非阻塞连接
        socketChannel.register(selector, SelectionKey.OP_READ);//将通道注册到selector上
        //创建线程，来接收服务端的响应信息
        new Thread(new ClientThread(selector)).start();

        //向服务端发送信息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String msg = scanner.nextLine();
            if (msg.length()>0){
                //写入通道消息，让他发送给服务端
                socketChannel.write(Charset.forName("UTF-8").encode(name+": "+msg));
            }
        }
    }
}
