package jp.co.sharescreen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Service
public class WebSocketHandler extends BinaryWebSocketHandler {

  private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<String, WebSocketSession>();

  @Autowired
  ImageService imageService;

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
   * 一定間隔でサーバのスクリーンショットをとり、クライアントへpushします。<br>
   * 
   * @throws Exception
   */
  @Scheduled(fixedDelayString = "${screenshot.interval}")
  public void sendImage() throws Exception {
    BinaryMessage message = new BinaryMessage(imageService.screenShot());
    for (Map.Entry<String, WebSocketSession> e : sessions.entrySet()) {
      e.getValue().sendMessage(message);
    }
  }
}
