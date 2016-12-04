package jp.co.sharescreen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.sharescreen.dto.ClientEventDto;
import jp.co.sharescreen.service.ImageService;
import jp.co.sharescreen.service.OperationService;

@Service
public class WebSocketHandler extends BinaryWebSocketHandler {

  private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<String, WebSocketSession>();

  @Autowired
  ImageService imageService;

  @Autowired
  OperationService operationService;

  /**
   * 接続が確立したセッションをプールします。
   * 
   * @param session セッション
   * @throws Exception 例外が発生した場合
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.put(session.getId(), session);
    sendImage();
  }

  /**
   * 切断された接続をプールから削除します。
   * 
   * @param session セッション
   * @param status ステータス
   * @throws Exception 例外が発生した場合
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session.getId());
  }

  /**
   * クライアントで実行した操作を、サーバで実行します。<br>
   */
  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    ClientEventDto event = new ObjectMapper().readValue((String)message.getPayload(), ClientEventDto.class);
    operationService.execute(event);
  }

  /**
   * 一定間隔でサーバのスクリーンショットをとり、クライアントへpushします。<br>
   * 
   * @throws Exception
   */
  @Scheduled(fixedDelayString = "${screenshot.interval}")
  public synchronized void sendImage() throws Exception {
    final BinaryMessage message = new BinaryMessage(imageService.screenShot());
    for (Map.Entry<String, WebSocketSession> e : sessions.entrySet()) {
      e.getValue().sendMessage(message);
    }
  }
}
