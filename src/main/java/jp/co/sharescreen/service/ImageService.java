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

  /**
   * スクリーンショットを取得し、バイナリデータを返します。<br>
   * 
   * @return スクリーンショットのバイト配列
   */
  public byte[] screenShot() throws Exception {
    Robot robot = new Robot();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    BufferedImage image = robot.createScreenCapture(new Rectangle(0, 0, screenSize.width, screenSize.height));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "png", out);
    return out.toByteArray();
  }
}
