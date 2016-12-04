package jp.co.sharescreen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * クライアントで発生したイベントを保持するクラスです。<br>
 * 
 * @author daisuke
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientEventDto {

  private String type;

  private Integer clientX;

  private Integer clientY;

}
