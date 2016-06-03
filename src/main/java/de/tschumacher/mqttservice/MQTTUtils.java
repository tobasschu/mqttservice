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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;

public class MQTTUtils {
  public static String getQueueUrl(final AmazonSQS sqs, final String queueName) {

    final GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(queueName);
    return sqs.getQueueUrl(getQueueUrlRequest).getQueueUrl();
  }

  public static String create(final AmazonSQS sqs, final String queueName) {
    final CreateQueueRequest createQueueRequest = new CreateQueueRequest().withQueueName(queueName);
    final String queueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
    return queueUrl;
  }

  public static String createIfNotExists(final AmazonSQS sqs, final String queueName) {
    String queueUrl;
    try {
      queueUrl = getQueueUrl(sqs, queueName);
    } catch (final QueueDoesNotExistException e) {
      queueUrl = create(sqs, queueName);
    }

    return queueUrl;
  }
}
