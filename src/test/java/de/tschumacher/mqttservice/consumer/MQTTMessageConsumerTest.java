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
package de.tschumacher.mqttservice.consumer;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import de.tschumacher.mqttservice.message.MQTTMessage;
import de.tschumacher.mqttservice.message.MQTTMessageFactory;
import de.tschumacher.mqttservice.message.coder.StringMQTTCoder;

public class MQTTMessageConsumerTest {
  private MQTTMessageConsumer<String> service = null;


  @Before
  public void setUp() {
    this.service = new MQTTMessageConsumer<String>();
  }


  @SuppressWarnings("unchecked")
  @Test
  public void receiveTest() throws MqttSecurityException, MqttException {

    MQTTMessageHandler<String> handler = Mockito.mock(MQTTMessageHandler.class);
    Mockito.when(handler.getCoder()).thenReturn(
        new MQTTMessageFactory<String>(new StringMQTTCoder()));
    MqttMessage message = new MqttMessage();
    String topic = "topic";

    this.service.addHandler(topic, handler);
    this.service.receive(topic, message);
    this.service.removeHandler(topic);

    Mockito.verify(handler).messageArrived(Matchers.any(MQTTMessage.class));
    Mockito.verify(handler).getCoder();
    Mockito.verifyNoMoreInteractions(handler);

  }

  @Test
  public void receiveWithoutHandlerTest() throws MqttSecurityException, MqttException {

    MqttMessage message = new MqttMessage();
    String topic = "topic";

    this.service.receive(topic, message);

  }
}
