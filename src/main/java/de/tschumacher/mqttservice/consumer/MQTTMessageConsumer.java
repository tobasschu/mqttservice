package de.tschumacher.mqttservice.consumer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTMessageConsumer<F> {
  private Map<String, MQTTMessageHandler<F>> handlerMap;


  public MQTTMessageConsumer() {
    super();
    this.handlerMap = new HashMap<String, MQTTMessageHandler<F>>();
  }

  public void addHandler(String topic, MQTTMessageHandler<F> handler) {
    handlerMap.put(topic, handler);
  }

  public void removeHandler(String topic) {
    handlerMap.remove(topic);
  }

  public void receive(String topic, MqttMessage message) {
    MQTTMessageHandler<F> handler = handlerMap.get(topic);
    if (handler != null) {
      handler.messageArrived(handler.getCoder().createMessage(message.getPayload()));
    }
  }


}
