package de.tschumacher.mqttservice.exception;


public class MQTTServiceException extends RuntimeException {

  public MQTTServiceException(Exception e) {
    super(e);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
