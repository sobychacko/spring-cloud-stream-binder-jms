/*
 *  Copyright 2002-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.stream.binder.jms;

import javax.jms.Queue;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.jms.utils.JmsMessageDrivenChannelAdapterFactory;
import org.springframework.cloud.stream.binder.jms.utils.JmsSendingMessageHandlerFactory;
import org.springframework.cloud.stream.binder.jms.utils.TopicPartitionRegistrar;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.messaging.MessageHandler;

/**
 * Binder definition for JMS.
 *
 * @author Jack Galilee
 * @author Jonathan Sharpe
 * @author Joseph Taylor
 * @author José Carlos Valero
 * @author Gary Russell
 * @since 1.1
 */
public class JMSMessageChannelBinder
		extends AbstractMessageChannelBinder<ConsumerProperties, ProducerProperties, Queue, TopicPartitionRegistrar, String> {

	private final JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory;
	private final JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory;

	public JMSMessageChannelBinder(ProvisioningProvider<ConsumerProperties, ProducerProperties, Queue, TopicPartitionRegistrar, String> provisioningProvider,
								   JmsSendingMessageHandlerFactory jmsSendingMessageHandlerFactory,
			JmsMessageDrivenChannelAdapterFactory jmsMessageDrivenChannelAdapterFactory) {
		super(true, null, provisioningProvider);
		this.jmsSendingMessageHandlerFactory = jmsSendingMessageHandlerFactory;
		this.jmsMessageDrivenChannelAdapterFactory = jmsMessageDrivenChannelAdapterFactory;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(TopicPartitionRegistrar destination,
														  ProducerProperties producerProperties) throws Exception {
		return this.jmsSendingMessageHandlerFactory.build(destination);
	}

	@Override
	protected org.springframework.integration.core.MessageProducer createConsumerEndpoint(
			String name, String group, Queue destination, ConsumerProperties properties) {
		return jmsMessageDrivenChannelAdapterFactory.build(destination, properties);
	}

}
