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

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import de.tschumacher.mqttservice.consumer.MQTTMessageConsumer;
import de.tschumacher.mqttservice.consumer.MQTTMessageHandler;
import de.tschumacher.mqttservice.exception.MQTTServiceException;
import de.tschumacher.mqttservice.message.MQTTMessage;
import de.tschumacher.mqttservice.message.coder.StringMQTTCoder;

public class MQTTServiceTest {
  private DefaultMQTTService<String> service = null;
  private MQTTMessageConsumer<String> mqttMessageConsumer = null;
  private MqttClient client;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    this.mqttMessageConsumer = Mockito.mock(MQTTMessageConsumer.class);
    this.client = Mockito.mock(MqttClient.class);
    this.service = new DefaultMQTTService<String>(mqttMessageConsumer, client);
  }

  @After
  public void afterTest() {
    Mockito.verifyNoMoreInteractions(this.mqttMessageConsumer);
    Mockito.verifyNoMoreInteractions(this.client);
  }

  @Test
  public void connectTest() throws MqttSecurityException, MqttException {
    Mockito.when(client.isConnected()).thenReturn(false);

    this.service.connect();

    Mockito.verify(this.client, Mockito.times(1)).connect(Matchers.any(MqttConnectOptions.class));
    Mockito.verify(this.client, Mockito.times(1)).setCallback(Matchers.any(MqttCallback.class));
    Mockito.verify(this.client, Mockito.times(1)).isConnected();
  }

  @Test(expected = MQTTServiceException.class)
  public void connectExceptionTest() throws MqttSecurityException, MqttException {

    Mockito.doThrow(new MqttException(0)).when(client)
        .connect(Matchers.any(MqttConnectOptions.class));
    Mockito.when(client.isConnected()).thenReturn(false);

    try {
      this.service.connect();
    } catch (Exception e) {
      throw e;
    } finally {

      Mockito.verify(this.client, Mockito.times(1)).isConnected();
      Mockito.verify(this.client, Mockito.times(1)).connect(Matchers.any(MqttConnectOptions.class));
      Mockito.verify(this.client, Mockito.times(1)).setCallback(Matchers.any(MqttCallback.class));

    }
  }

  @Test
  public void disconnectTest() throws MqttSecurityException, MqttException {

    Mockito.when(client.isConnected()).thenReturn(true);
    this.service.disconnect();;

    Mockito.verify(this.client, Mockito.times(1)).isConnected();
    Mockito.verify(this.client, Mockito.times(1)).disconnect();
  }

  @Test
  public void disconnectFailTest() throws MqttSecurityException, MqttException {

    Mockito.when(client.isConnected()).thenReturn(false);
    this.service.disconnect();

    Mockito.verify(this.client, Mockito.times(1)).isConnected();
  }

  @Test(expected = MQTTServiceException.class)
  public void disconnectExceptionTest() throws MqttSecurityException, MqttException {

    Mockito.doThrow(new MqttException(0)).when(client).disconnect();
    Mockito.when(client.isConnected()).thenReturn(true);

    try {
      this.service.disconnect();
    } catch (Exception e) {
      throw e;
    } finally {
      Mockito.verify(this.client, Mockito.times(1)).isConnected();
      Mockito.verify(this.client, Mockito.times(1)).disconnect();
    }
  }


  @Test
  @SuppressWarnings("unchecked")
  public void subscribeTest() throws MqttSecurityException, MqttException {


    String topic = "topic";
    MQTTMessageHandler<String> handler = Mockito.mock(MQTTMessageHandler.class);
    this.service.subscribe(topic, handler);

    Mockito.verify(this.mqttMessageConsumer, Mockito.times(1)).addHandler(topic, handler);
    Mockito.verify(this.client, Mockito.times(1)).subscribe(topic);
  }

  @Test(expected = MQTTServiceException.class)
  @SuppressWarnings("unchecked")
  public void subscribeExceptionTest() throws MqttSecurityException, MqttException {


    String topic = "topic";
    MQTTMessageHandler<String> handler = Mockito.mock(MQTTMessageHandler.class);
    Mockito.doThrow(new MqttException(0)).when(client).subscribe(topic);
    try {
      this.service.subscribe(topic, handler);
    } catch (Exception e) {
      throw e;
    } finally {
      Mockito.verify(this.mqttMessageConsumer, Mockito.times(1)).addHandler(topic, handler);
      Mockito.verify(this.client, Mockito.times(1)).subscribe(topic);
    }
  }

  @Test
  public void unsubscribeTest() throws MqttSecurityException, MqttException {
    String topic = "topic";

    this.service.unsubscribe(topic);

    Mockito.verify(this.mqttMessageConsumer, Mockito.times(1)).removeHandler(topic);
    Mockito.verify(this.client, Mockito.times(1)).unsubscribe(topic);
  }

  @Test(expected = MQTTServiceException.class)
  public void unsubscribeExceptionTest() throws MqttSecurityException, MqttException {
    String topic = "topic";
    Mockito.doThrow(new MqttException(0)).when(client).unsubscribe(topic);
    try {
      this.service.unsubscribe(topic);
    } catch (Exception e) {
      throw e;
    } finally {
      Mockito.verify(this.client, Mockito.times(1)).unsubscribe(topic);
    }
  }

  @Test
  public void publishTest() throws MqttSecurityException, MqttException {
    String topic = "topic";
    MQTTMessage<String> message = new MQTTMessage<String>(new StringMQTTCoder(), "message");

    this.service.publish(topic, message);

    Mockito.verify(this.client, Mockito.times(1)).publish(Matchers.anyString(),
        Matchers.any(MqttMessage.class));
  }

  @Test(expected = MQTTServiceException.class)
  public void publishExceptionTest() throws MqttSecurityException, MqttException {
    String topic = "topic";
    MQTTMessage<String> message = new MQTTMessage<String>(new StringMQTTCoder(), "message");
    Mockito.doThrow(new MqttException(0)).when(client)
        .publish(Matchers.anyString(), Matchers.any(MqttMessage.class));

    try {
      this.service.publish(topic, message);
    } catch (Exception e) {
      throw e;
    } finally {
      Mockito.verify(this.client, Mockito.times(1)).publish(Matchers.anyString(),
          Matchers.any(MqttMessage.class));
    }
  }
}
