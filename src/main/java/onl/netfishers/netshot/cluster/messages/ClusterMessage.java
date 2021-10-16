/**
 * Copyright 2013-2021 Sylvain Cadilhac (NetFishers)
 * 
 * This file is part of Netshot.
 * 
 * Netshot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Netshot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Netshot.  If not, see <http://www.gnu.org/licenses/>.
 */
package onl.netfishers.netshot.cluster.messages;

import java.lang.management.ManagementFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import onl.netfishers.netshot.rest.RestViews.ClusteringView;

/**
 * HA-related message to be be exchanged with other Netshot instances
 * in a serialized form.
 */
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement()
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@Type(value = HelloClusterMessage.class, name = "Hello"),
	@Type(value = ReloadDriversMessage.class, name = "ReloadDrivers"),
	@Type(value = LoadTasksMessage.class, name = "LoadTasks"),
	@Type(value = AssignTasksMessage.class, name = "AssignTasks"),
})
public abstract class ClusterMessage {

	/** Message ID counter */
	static protected Long lastLocalMessageId = 0L;

	private String instanceId;

	/** Current system time */
	private long currentTime;

	/** Current Netshot uptime */
	private long upTime;

	/** Message ID */
	private long messageId;

	/**
	 * Default constructor
	 */
	public ClusterMessage(String instanceId) {
		this.instanceId = instanceId;
		this.currentTime = System.currentTimeMillis();
		this.upTime = ManagementFactory.getRuntimeMXBean().getUptime();
		synchronized (ClusterMessage.lastLocalMessageId) {
			ClusterMessage.lastLocalMessageId += 1;
			this.messageId = ClusterMessage.lastLocalMessageId;
		}
	}

	/**
	 * Hidden constructor
	 */
	protected ClusterMessage() {
	}

	@XmlElement
	@JsonView(ClusteringView.class)
	public String getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@XmlElement
	@JsonView(ClusteringView.class)
	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}

	@XmlElement
	@JsonView(ClusteringView.class)
	public long getUpTime() {
		return upTime;
	}

	public void setUpTime(long upTime) {
		this.upTime = upTime;
	}

	@XmlElement
	@JsonView(ClusteringView.class)
	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	@Override
	public String toString() {
		return "ClusterMessage [currentTime=" + currentTime + ", instanceId=" + instanceId + ", messageId=" + messageId
				+ ", upTime=" + upTime + "]";
	}
}
