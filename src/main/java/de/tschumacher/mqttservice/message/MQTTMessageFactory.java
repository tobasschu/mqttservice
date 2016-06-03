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
package de.tschumacher.mqttservice.message;


public class MQTTMessageFactory<F> {

  private final MQTTMessageCoder<F> coder;

  public MQTTMessageFactory(final MQTTMessageCoder<F> coder) {
    super();
    this.coder = coder;
  }

  public MQTTMessage<F> createMessage(final byte[] body) {
    return new MQTTMessage<F>(this.coder, body);
  }

  public MQTTMessage<F> createMessage(final F body) {
    return new MQTTMessage<F>(this.coder, body);
  }

}
