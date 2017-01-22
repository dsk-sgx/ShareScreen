package jp.co.sharescreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  private static final Map<String, List<WebSocketSession>> sessions = new ConcurrentHashMap<String, List<WebSocketSession>>();

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
    String host = session.getRemoteAddress().getHostName();
    List<WebSocketSession> list = sessions.get(host);
    if (list == null) {
      list = new ArrayList<>();
      sessions.put(host, list);
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
    if (sessions.isEmpty()) {
      return;
    }
    final Integer maxLevel = 4;// TODO getMaxScreenLevel(sessions.values().get(0).getUri().getPath());
    Map<Integer, BinaryMessage> captures = new HashMap<>();
    for (int i = 1; i <= maxLevel; i++) {
      BinaryMessage message = new BinaryMessage(imageService.screenShot(i, maxLevel));
      captures.put(i, message);
    }
    sessions.forEach((k, v) -> {
      new Thread(() -> {
        v.parallelStream().forEach(session -> {
          BinaryMessage message = captures.get(getScreenLevel(session.getUri().getPath()));
          try {
            session.sendMessage(message);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
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
    String[] paths = path.split("/");
    return Integer.parseInt(paths[paths.length - 1]);
  }

  /**
   * URLからスクリーンの分割数を取得します。<br>
   * {@code path}が{@code screens/5/1}の場合、「5」を返します。
   * 
   * @param path
   * @return 分割数
   */
  public Integer getMaxScreenLevel(String path) {
    String[] paths = path.split("/");
    return Integer.parseInt(paths[paths.length - 2]);
  }
}
