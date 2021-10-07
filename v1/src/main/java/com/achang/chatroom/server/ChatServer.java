package com.achang.chatroom.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/******
 @author 阿昌
 @create 2021-09-13 20:23
 聊天室服务端
 *******
 */
public class ChatServer {
    //服务端启动的方法
    public void startServer() throws IOException {
        //1、创建Selector选择器
        Selector selector = Selector.open();

        //2、创建ServerSocketChannel通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3、为channel通道绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(9090));
        serverSocketChannel.configureBlocking(false);//设置非阻塞模式

        //4、把serverSocketChannel绑定到selector上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动.......");

        //5、循环监听是否有连接连入
        while (true) {
            int select = selector.select();

            //如果为0，则为没连接，没有获取到，就跳出循环
            if (select == 0) {
                continue;
            }

            //获取可用channel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            //遍历
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                //移除 set 集合当前 selectionKey
                iterator.remove();

                //6、根据就绪状态，调用对应方法实现具体业务操作
                if (selectionKey.isAcceptable()) {
                    //6.1 如果 accept 状态
                    acceptOperator(serverSocketChannel, selector);
                }
                if (selectionKey.isReadable()) {
                    //6.2 如果可读状态
                    readOperator(selector, selectionKey);
                }
            }
        }
    }

    //处理可读状态操作
    private void readOperator(Selector selector, SelectionKey selectionKey) throws IOException {
        //1 从 SelectionKey 获取到已经就绪的通道
        SocketChannel channel = (SocketChannel) selectionKey.channel();

        //2 创建 buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //3 循环读取客户端消息
        int readLength = channel.read(byteBuffer);
        String message = "";//用于接收解码后的信息

        //表示里面有数据
        if (readLength > 0) {
            //切换读模式
            byteBuffer.flip();
            //读取内容
            message += Charset.forName("UTF-8").decode(byteBuffer);
        }

        //4 将 channel 再次注册到选择器上，监听可读状态
        channel.register(selector, SelectionKey.OP_READ);

        //5 把客户端发送消息，广播到其他客户端
        if (message.length() > 0) {
            //广播给其他客户端
            System.out.println(message);
            castOtherClient(message, selector, channel);
        }
    }

    //广播到其他客户端
    private void castOtherClient(String message, Selector selector, SocketChannel channel) throws IOException {
        //1 获取所有已经接入 channel
        Set<SelectionKey> selectionKeySet = selector.keys();
        //2 循环想所有 channel 广播消息
        for (SelectionKey selectionKey : selectionKeySet) {
            //获取每个 channel
            Channel tarChannel = selectionKey.channel();
            //不需要给自己发送
            if (tarChannel instanceof SocketChannel && tarChannel != channel) {//不向自己广播
                ((SocketChannel) tarChannel).write(Charset.forName("UTF-8").encode(message));
            }
        }
    }


    //处理接入状态操作
    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        //1 接入状态，创建 socketChannel
        SocketChannel accept = serverSocketChannel.accept();
        //2 把 socketChannel 设置非阻塞模式
        accept.configureBlocking(false);
        //3 把 channel 注册到 selector 选择器上，监听可读状态
        accept.register(selector, SelectionKey.OP_READ);
        //4 客户端回复信息
        accept.write(Charset.forName("UTF-8").encode("欢迎进入聊天室，请注意隐私安全"));
    }

    //主函数启动
    public static void main(String[] args) throws IOException {
        new ChatServer().startServer();
    }
}
