package com.minesweeper.Config;

import com.minesweeper.Controller.EchoChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@Configuration
public class WebSocketConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpointExporter (){

        ServerEndpointExporter exporter = new ServerEndpointExporter();

        // 手动注册 WebSocket 端点
//        exporter.setAnnotatedEndpointClasses(EchoChannel.class);
        System.out.println("注册");
        return exporter;
    }
}