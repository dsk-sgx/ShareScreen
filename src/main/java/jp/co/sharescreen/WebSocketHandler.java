package jp.co.sharescreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  private static final Map<Integer, List<WebSocketSession>> sessions = new ConcurrentHashMap<Integer, List<WebSocketSession>>();

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
    Integer level = getScreenLevel(session.getUri().getPath());
    List<WebSocketSession> list = sessions.get(level);
    if (list == null) {
      list = new ArrayList<>();
      sessions.put(level, list);
    }
    list.add(session);
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
    sessions.get(getScreenLevel(session.getUri().getPath())).removeIf(e -> e.equals(session));
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
    long start = System.currentTimeMillis();
    Set<Integer> levels = sessions.keySet();
    Integer maxLevel = levels.stream().max(Comparator.naturalOrder()).get();
    levels.forEach(level -> {
      new Thread(() -> {
        final BinaryMessage message = new BinaryMessage(imageService.screenShot(level, maxLevel));
        sessions.get(level).parallelStream().forEach((session) -> {
          try {
            session.sendMessage(message);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
        return;
      }).start();
    });
    System.out.println("END:" + (System.currentTimeMillis() - start));

  }

  /**
   * URLからスクリーンの階層を取得します。
   * 
   * @param path
   * @return
   */
  public Integer getScreenLevel(String path) {
    return Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
  }
}
