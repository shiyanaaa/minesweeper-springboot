package com.minesweeper.Controller;


import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.server.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

// 使用 @ServerEndpoint 注解表示此类是一个 WebSocket 端点
// 通过 value 注解，指定 websocket 的路径
@Component
@ServerEndpoint(value = "/channel/{userId}")
public class EchoChannel {
    private static int onlineCount = 0;
    private static ConcurrentHashMap<String, EchoChannel> webSocketSet = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoChannel.class);
    private String userId = "";
    private Session session;

    // 收到消息
    @OnMessage
    public void onMessage(String message) throws IOException{

        LOGGER.info("[websocket] 收到消息：id={}，message={}", this.session.getId(), message);

        if (message.equalsIgnoreCase("bye")) {
            // 由服务器主动关闭连接。状态码为 NORMAL_CLOSURE（正常关闭）。
            this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Bye"));;
            return;
        }


        this.session.getAsyncRemote().sendText("["+ Instant.now().toEpochMilli() +"] Hello " + message);
    }

    // 连接打开
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId, EndpointConfig endpointConfig){
        // 保存 session 到对象
        this.session = session;
        this.userId=userId;
        addOnlineCount();
        webSocketSet.put(userId, this);
        LOGGER.info("用户"+userId+"加入！当前在线人数为" + getOnlineCount());
    }

    // 连接关闭
    @OnClose
    public void onClose(CloseReason closeReason){
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        LOGGER.info("[websocket] 连接断开：id={}，reason={}", this.session.getId(),closeReason);
    }

    // 连接异常
    @OnError
    public void onError(Throwable throwable) throws IOException {

        LOGGER.info("[websocket] 连接异常：id={}，throwable={}", this.session.getId(), throwable.getMessage());

        // 关闭连接。状态码为 UNEXPECTED_CONDITION（意料之外的异常）
        this.session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
    }
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    public void sendtoAll(String message)  {
        for (String key : webSocketSet.keySet()) {
            if(webSocketSet.get(key).session.isOpen()){
                try {
                    webSocketSet.get(key).sendMessage(message);
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                }
            }else{
                webSocketSet.remove(key);
                LOGGER.info("[websocket] 用户"+key+"已断开连接");
            }

        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        EchoChannel.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        EchoChannel.onlineCount--;
    }

}
