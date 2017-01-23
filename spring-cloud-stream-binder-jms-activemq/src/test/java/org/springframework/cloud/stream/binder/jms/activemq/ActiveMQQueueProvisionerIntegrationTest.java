package org.springframework.cloud.stream.binder.jms.activemq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.StreamMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.jms.test.ActiveMQTestUtils;
import org.springframework.cloud.stream.binder.jms.utils.Base64UrlNamingStrategy;
import org.springframework.cloud.stream.binder.jms.utils.DestinationNameResolver;
import org.springframework.cloud.stream.binder.jms.utils.TopicPartitionRegistrar;
import org.springframework.jms.core.JmsTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author José Carlos Valero
 * @since 1.1
 */
public class ActiveMQQueueProvisionerIntegrationTest {

	private static JmsTemplate jmsTemplate;
	private static ActiveMQQueueProvisioner target;
	private static ActiveMQConnectionFactory activeMQConnectionFactory;

	@BeforeClass
	public static void initTests() throws Exception {
		activeMQConnectionFactory = ActiveMQTestUtils.startEmbeddedActiveMQServer();
		jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
	}

	@Before
	public void setUp() throws Exception {
		target = new ActiveMQQueueProvisioner(activeMQConnectionFactory,
				new DestinationNameResolver(new Base64UrlNamingStrategy("anonymous.")));
	}

	@Test
	public void provisionTopicAndConsumerGroup_whenSingleGroup_createsInfrastructure() throws Exception {
		TopicPartitionRegistrar topicPartitionRegistrar = target.provisionProducerDestination("topic", new ProducerProperties());
		Queue queue = target.provisionConsumerDestination("topic", "group1", new ConsumerProperties());


//		QueueProvisioner.Destinations destinations = target.provisionConsumerDestination("topic", "group1");
//		Destination group = destinations.getGroups()[0];
//		Destination topic = destinations.getTopic();

		jmsTemplate.convertAndSend(topicPartitionRegistrar.getDestination(-1), "hi jms scs");
		Object payloadGroup1 = jmsTemplate.receiveAndConvert(queue);

		assertThat(payloadGroup1).isEqualTo("hi jms scs");
	}

	@Test
	public void provisionTopicAndConsumerGroup_whenMultipleGroups_createsInfrastructure() throws Exception {

		TopicPartitionRegistrar topicPartitionRegistrar = target.provisionProducerDestination("topic", new ProducerProperties());
		Queue queue1 = target.provisionConsumerDestination("topic", "group1", new ConsumerProperties());
		Queue queue2 = target.provisionConsumerDestination("topic", "group2", new ConsumerProperties());


//		QueueProvisioner.Destinations destinations = target.provisionTopicAndConsumerGroup("topic", "group1", "group2");
//		Destination group1 = destinations.getGroups()[0];
//		Destination group2 = destinations.getGroups()[1];
//		Destination topic = destinations.getTopic();

		jmsTemplate.convertAndSend(topicPartitionRegistrar.getDestination(-1), "hi groups");
		Object payloadGroup1 = jmsTemplate.receiveAndConvert(queue1);
		Object payloadGroup2 = jmsTemplate.receiveAndConvert(queue2);

		assertThat(payloadGroup1).isEqualTo("hi groups");
		assertThat(payloadGroup2).isEqualTo("hi groups");
	}



	private class CountingListener implements MessageListener {
		private final CountDownLatch latch;

		private final List<Exception> errors = new ArrayList<>();

		private final List<String> payloads = new ArrayList<>();
		private final List<Message> messages = new ArrayList<>();

		public CountingListener(CountDownLatch latch) {
			this.latch = latch;
		}

		public CountingListener(int expectedMessages) {
			this.latch = new CountDownLatch(expectedMessages);
		}

		@Override
		public void onMessage(Message message) {
			if (message instanceof StreamMessage) {
				try {
					payloads.add(((StreamMessage)message).readString());
				} catch (JMSException e) {
					errors.add(e);
				}
			}
			else {
				payloads.add(message.toString());
			}

			messages.add(message);
			latch.countDown();
		}

		boolean awaitExpectedMessages() throws InterruptedException {
			return awaitExpectedMessages(2000);
		}

		boolean awaitExpectedMessages(int timeout) throws InterruptedException {
			return latch.await(timeout, TimeUnit.MILLISECONDS);
		}
		public List<Exception> getErrors() {
			return errors;
		}

		public List<String> getPayloads() {
			return payloads;
		}

		public List<Message> getMessages() {
			return messages;
		}


	}
}
