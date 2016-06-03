/*
 * Copyright 2016 Tobias Schumacher
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.tschumacher.mqttservice;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tschumacher.mqttservice.consumer.MQTTMessageConsumer;
import de.tschumacher.mqttservice.consumer.MQTTMessageHandler;
import de.tschumacher.mqttservice.exception.MQTTServiceException;
import de.tschumacher.mqttservice.message.MQTTMessage;
import de.tschumacher.mqttservice.message.MQTTMessageFactory;
import de.tschumacher.mqttservice.message.coder.StringMQTTCoder;


public class DefaultMQTTService<F> implements MQTTService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultMQTTService.class);
  private static final boolean CLEAN_SESSION = true;
  private final String broker;
  private final String clientId;
  private final MQTTMessageConsumer<F> mqttMessageConsumer;
  private MqttClient client;


  public DefaultMQTTService(String broker, String clientId) {
    this(broker, clientId, null);
  }

  public DefaultMQTTService(String broker, String clientId,
      MQTTMessageConsumer<F> mqttMessageConsumer) {
    this.broker = broker;
    this.clientId = clientId;
    this.mqttMessageConsumer = mqttMessageConsumer;
  }

  public void connect() {
    try {
      MqttClient mqttClient = getClient();
      mqttClient.setCallback(createCallback());
      mqttClient.connect(createOptions());
    } catch (MqttException e) {
      throw new MQTTServiceException(e);
    }
  }

  public void disconnect() {
    try {
      MqttClient mqttClient = getClient();
      if (mqttClient.isConnected()) {
        mqttClient.disconnect();
      }
    } catch (MqttException e) {
      throw new MQTTServiceException(e);
    }

  }

  public void subscribe(String topic, MQTTMessageHandler<F> handler) {
    try {
      MqttClient mqttClient = getClient();
      addHandler(topic, handler);
      mqttClient.subscribe(topic);
    } catch (MqttException e) {
      throw new MQTTServiceException(e);
    }
  }

  public void unsubscribe(String topic) {
    try {
      MqttClient mqttClient = getClient();
      mqttClient.unsubscribe(topic);
      removeHandler(topic);
    } catch (MqttException e) {
      throw new MQTTServiceException(e);
    }
  }



  public void publish(String topic, MQTTMessage<F> message) {
    try {
      MqttClient mqttClient = getClient();
      mqttClient.publish(topic, createMessage(message));
    } catch (MqttException e) {
      throw new MQTTServiceException(e);
    }
  }

  private MqttMessage createMessage(MQTTMessage<F> message) {
    return new MqttMessage(message.getByteContent());
  }

  private void addHandler(String topic, MQTTMessageHandler<F> handler) {
    if (mqttMessageConsumer != null)
      mqttMessageConsumer.addHandler(topic, handler);
  }

  private void removeHandler(String topic) {
    if (mqttMessageConsumer != null)
      mqttMessageConsumer.removeHandler(topic);

  }

  private MqttCallback createCallback() {
    return new MqttCallback() {

      @Override
      public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (mqttMessageConsumer != null)
          mqttMessageConsumer.receive(topic, message);
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {

      }

      @Override
      public void connectionLost(Throwable cause) {
        logger.error("mqtt connection lost", cause);
      }
    };
  }


  private MqttConnectOptions createOptions() {
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(CLEAN_SESSION);
    return connOpts;
  }


  private MqttClient getClient() throws MqttException {
    if (client == null) {
      client = new MqttClient(broker, clientId);
    }
    return client;
  }

  public static void main(String[] args) {
    MQTTMessageConsumer<String> mqttMessageConsumer = new MQTTMessageConsumer<String>();
    DefaultMQTTService<String> mqttService =
        new DefaultMQTTService<String>("tcp://server.closelink.net:1883", "client",
            mqttMessageConsumer);
    mqttService.connect();
    final MQTTMessageFactory<String> mqttMessageFactory =
        new MQTTMessageFactory<String>(new StringMQTTCoder());
    MQTTMessageHandler<String> handler = new MQTTMessageHandler<String>() {

      @Override
      public void messageArrived(MQTTMessage<String> message) {
        System.out.println(message.getContent());
      }

      @Override
      public MQTTMessageFactory<String> getCoder() {
        return mqttMessageFactory;
      }
    };
    String topic = "/top";
    mqttService.subscribe(topic, handler);
    mqttService.publish(topic, mqttMessageFactory.createMessage("mega test"));
    mqttService.disconnect();
  }
}
