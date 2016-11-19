package jp.co.sharescreen.handler;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Service
public class WebSocketHandler extends BinaryWebSocketHandler {

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    session.sendMessage(new TextMessage("hello".getBytes()));
  }
}
