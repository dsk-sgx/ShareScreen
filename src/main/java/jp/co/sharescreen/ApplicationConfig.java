package jp.co.sharescreen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import jp.co.sharescreen.handler.WebSocketHandler;

@Component
public class ApplicationConfig implements WebSocketConfigurer {

  @Autowired
  WebSocketHandler webSocketHandler;

  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(webSocketHandler, "/screens");
  }
}
