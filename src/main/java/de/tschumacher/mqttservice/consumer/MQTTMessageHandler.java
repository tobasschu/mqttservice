package de.tschumacher.mqttservice.consumer;

import de.tschumacher.mqttservice.message.MQTTMessage;
import de.tschumacher.mqttservice.message.MQTTMessageFactory;


public interface MQTTMessageHandler<F> {

  void messageArrived(MQTTMessage<F> message);

  MQTTMessageFactory<F> getCoder();

}
