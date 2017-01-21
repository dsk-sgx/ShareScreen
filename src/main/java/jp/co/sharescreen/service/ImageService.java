package jp.co.sharescreen.service;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

  /**
   * スクリーンショットを取得し、バイナリデータを返します。<br>
   * 
   * @param level スクリーンの階層（1以上の整数）
   * @return スクリーンショットのバイト配列
   */
  public byte[] screenShot(Integer level, Integer max) {
    try {
      Robot robot = new Robot();
      int height = SCREEN_SIZE.height / max;
      int start = height * (level - 1);

      BufferedImage image = robot.createScreenCapture(new Rectangle(0, start, SCREEN_SIZE.width, height));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(image, "png", out);
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
