package jp.co.sharescreen.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Service
public class WebSocketHandler extends BinaryWebSocketHandler {

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
    session.sendMessage(new BinaryMessage("hello".getBytes()));
  }
}
