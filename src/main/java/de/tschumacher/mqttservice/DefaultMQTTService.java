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

import de.tschumacher.mqttservice.consumer.MQTTMessageConsumer;
import de.tschumacher.mqttservice.consumer.MQTTMessageHandler;
import de.tschumacher.mqttservice.exception.MQTTServiceException;
import de.tschumacher.mqttservice.handler.MQTTServiceConnectionHandler;
import de.tschumacher.mqttservice.message.MQTTMessage;


public class DefaultMQTTService<F> implements MQTTService<F> {
  private static final boolean CLEAN_SESSION = true;
  private final String broker;
  private final String clientId;
  private final MQTTMessageConsumer<F> mqttMessageConsumer;
  private MqttClient client;
  private MQTTServiceConnectionHandler connectionHandler;


  // FOR TESTING
  public DefaultMQTTService(MQTTMessageConsumer<F> mqttMessageConsumer, MqttClient client) {
    super();
    this.broker = null;
    this.clientId = null;
    this.mqttMessageConsumer = mqttMessageConsumer;
    this.client = client;
  }

  public DefaultMQTTService(String broker, String clientId) {
    this(broker, clientId, null, null);
  }

  public DefaultMQTTService(String broker, String clientId,
      MQTTMessageConsumer<F> mqttMessageConsumer) {
    this(broker, clientId, mqttMessageConsumer, null);
  }

  public DefaultMQTTService(String broker, String clientId,
      MQTTMessageConsumer<F> mqttMessageConsumer, MQTTServiceConnectionHandler connectionHandler) {
    this.broker = broker;
    this.clientId = clientId;
    this.mqttMessageConsumer = mqttMessageConsumer;
    this.setConnectionHandler(connectionHandler);
  }

  @Override
  public void connect() {
    try {
      final MqttClient mqttClient = getClient();
      if (!mqttClient.isConnected()) {
        mqttClient.setCallback(createCallback());
        mqttClient.connect(createOptions());
      }
    } catch (final MqttException e) {
      throw new MQTTServiceException(e);
    }
  }

  @Override
  public void disconnect() {
    try {
      final MqttClient mqttClient = getClient();
      if (mqttClient.isConnected()) {
        mqttClient.disconnect();
      }
    } catch (final MqttException e) {
      throw new MQTTServiceException(e);
    }

  }

  @Override
  public void subscribe(String topic, MQTTMessageHandler<F> handler) {
    try {
      final MqttClient mqttClient = getClient();
      addHandler(topic, handler);
      mqttClient.subscribe(topic);
    } catch (final MqttException e) {
      throw new MQTTServiceException(e);
    }
  }

  @Override
  public void unsubscribe(String topic) {
    try {
      final MqttClient mqttClient = getClient();
      mqttClient.unsubscribe(topic);
      removeHandler(topic);
    } catch (final MqttException e) {
      throw new MQTTServiceException(e);
    }
  }


  @Override
  public void publish(String topic, MQTTMessage<F> message) {
    try {
      final MqttClient mqttClient = getClient();
      mqttClient.publish(topic, createMessage(message));
    } catch (final MqttException e) {
      throw new MQTTServiceException(e);
    }
  }

  private MqttMessage createMessage(MQTTMessage<F> message) {
    return new MqttMessage(message.getByteContent());
  }

  private void addHandler(String topic, MQTTMessageHandler<F> handler) {
    if (this.mqttMessageConsumer != null) {
      this.mqttMessageConsumer.addHandler(topic, handler);
    }
  }

  private void removeHandler(String topic) {
    if (this.mqttMessageConsumer != null) {
      this.mqttMessageConsumer.removeHandler(topic);
    }

  }

  private MqttCallback createCallback() {
    return new MqttCallback() {

      @Override
      public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (DefaultMQTTService.this.mqttMessageConsumer != null) {
          DefaultMQTTService.this.mqttMessageConsumer.receive(topic, message);
        }
      }

      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {

      }

      @Override
      public void connectionLost(Throwable cause) {
        if (DefaultMQTTService.this.connectionHandler != null) {
          DefaultMQTTService.this.connectionHandler.connectionLost(cause);
        }
      }
    };
  }


  private MqttConnectOptions createOptions() {
    final MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(CLEAN_SESSION);
    return connOpts;
  }


  private MqttClient getClient() throws MqttException {
    if (this.client == null) {
      this.client = new MqttClient(this.broker, this.clientId);
    }
    return this.client;
  }



  public void setConnectionHandler(MQTTServiceConnectionHandler connectionHandler) {
    this.connectionHandler = connectionHandler;
  }

}
