package org.springframework.cloud.stream.binder.jms.utils;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Soby Chacko
 */
public class Destinations {

	private final Topic topic;
	private final Queue[] groups;

	public Destinations(Topic topic, Queue[] groups) {
		this.topic = topic;
		this.groups = groups;
	}

	public Topic getTopic() {
		return topic;
	}

	public Queue[] getGroups() {
		return groups;
	}

	public static class Factory {
		private Topic topic;
		private Queue[] groups;

		public Factory withTopic(Topic topic) {
			this.topic = topic;
			return this;
		}

		public Factory withGroups(Queue groups[]) {
			this.groups = groups;
			return this;
		}

		public Factory addGroup(Queue group) {
			this.groups = (Queue[]) ArrayUtils.add(groups, group);
			return this;
		}

		public Destinations build() {
			return new Destinations(this.topic, this.groups);
		}

	}
}
