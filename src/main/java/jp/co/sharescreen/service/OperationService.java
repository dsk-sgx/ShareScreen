package jp.co.sharescreen.service;

import java.awt.Robot;
import java.awt.event.InputEvent;

import org.springframework.stereotype.Service;

import jp.co.sharescreen.dto.ClientEventDto;

@Service
public class OperationService {

  /**
   * クライアントの操作をサーバで実行します。<br>
   * 
   * @param event
   * @throws Exception
   */
  public void execute(ClientEventDto event) throws Exception {
    Robot robot = new Robot();
    robot.mouseMove(event.getClientX(), event.getClientY());
    if ("click".equals(event.getType())) {
      robot.mousePress(InputEvent.BUTTON1_MASK);
      robot.mouseRelease(InputEvent.BUTTON1_MASK);
    } else {
      robot.mousePress(InputEvent.BUTTON3_MASK);
      robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }
  }
}
