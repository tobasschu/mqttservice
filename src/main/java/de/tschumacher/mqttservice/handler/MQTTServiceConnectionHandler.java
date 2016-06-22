package de.tschumacher.mqttservice.handler;

public interface MQTTServiceConnectionHandler {

  void connectionLost(Throwable cause);
}
