/**
 * Copyright 2014 Álvaro Carrera Barroso
 * Grupo de Sistemas Inteligentes - Universidad Politécnica de Madrid
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.upm.dit.gsi.shanks.wsn.agent;

import jason.asSemantics.Message;

import java.util.List;
import java.util.logging.Logger;

import sim.util.Bag;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.movement.Location;
import es.upm.dit.gsi.shanks.agent.capability.perception.PercipientShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.perception.ShanksAgentPerceptionCapability;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.element.link.Link;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeCoordinatorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;

/**
 * Project: wsn File: es.upm.dit.gsi.shanks.wsn.agent.SensorNodeSofware.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 27/06/2014
 * @version 0.1
 * 
 */
public class ZigBeeSensorNodeSofware extends SimpleShanksAgent implements PercipientShanksAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4641219982632838849L;

	private ZigBeeSensorNode hardware;

	private double perceptionRange;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 */
	public ZigBeeSensorNodeSofware(String id, Logger logger, ZigBeeSensorNode hardware, double perceptionRange) {
		super(id, logger);
		this.hardware = hardware;
		this.hardware.setSoftware(this);
		this.perceptionRange = perceptionRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	@Override
	public void checkMail() {
		List<Message> inbox = this.getInbox();
		Message msg;
		if (inbox.size() > 0) {
			this.getLogger().finest("Inbox size of " + this.getID() + ": " + inbox.size() + ". Processing 1 message.");
			msg = inbox.get(0);
			inbox.remove(msg);
			Message newMsg = new Message();
			newMsg.setPropCont(msg);
			newMsg = this.setReceiverInPath(newMsg);
			int step = new Integer(msg.getMsgId().split(":")[1]);
			step++; // Check this msgId, no tiene porque ser simplemente +1, se
					// necesita comprobar el step actual
			// TODO meter en una lista de pendingToProcess y hacerlo durante el
			// reasoningCycle
			newMsg.setMsgId(this.getHardware().getID() + ":" + step);
			this.sendMsg(newMsg);
			this.getLogger().finest(
					"Resent message from " + msg.getSender() + " to " + msg.getReceiver() + " towards "
							+ newMsg.getReceiver());
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.agent.SimpleShanksAgent#executeReasoningCycle(es
	 * .upm.dit.gsi.shanks.ShanksSimulation)
	 */
	@Override
	public void executeReasoningCycle(ShanksSimulation simulation) {
		this.getLogger().finest(
				"-> Reasoning cycle of " + this.getID() + " with perception range: " + this.getPerceptionRange()
						+ " -> Step: " + simulation.schedule.getSteps());
		TargetAgent target = this.perceive(simulation);

		if (target != null) {
			this.getHardware().setDetecting(true);
			this.getLogger().info("Target detected by sensor " + this.getHardware().getID());

			double origdistance = ShanksAgentPerceptionCapability.getDistanceTo(simulation, this, target);
			double noise = simulation.random.nextGaussian() * this.perceptionRange / 20;
			double distance = origdistance + noise;
			this.getLogger().info("Distance: " + distance + " -> Noise: " + noise + " Real distance: " + origdistance);

			this.sendDetectionMessage(distance, simulation);
		} else {
			this.getHardware().setDetecting(false);
		}

	}

	/**
	 * @param distance
	 * @param sim
	 */
	private void sendDetectionMessage(double distance, ShanksSimulation sim) {
		Message msg = new Message();
		msg.setPropCont(distance);
		msg = this.setReceiverInPath(msg);
		msg.setMsgId(this.getHardware().getID() + ":" + sim.schedule.getSteps());
		this.sendMsg(msg);
		this.getLogger().finest("Message sent from " + this.getID() + " to " + msg.getReceiver());
	}

	/**
	 * @param msg
	 * @return
	 */
	private Message setReceiverInPath(Message msg) {
		Link link = this.hardware.getPath2sink();
		List<Device> devices = link.getLinkedDevices();
		int t = 0;
		if (devices.get(0) == this.hardware) {
			t = 1;
		}
		Device d = devices.get(t);
		if (d.getClass().equals(ZigBeeSensorNode.class)) {
			ZigBeeSensorNode node = (ZigBeeSensorNode) d;
			msg.setReceiver(node.getSoftware().getID());
		} else {
			ZigBeeCoordinatorNode bs = (ZigBeeCoordinatorNode) d;
			msg.setReceiver(bs.getSoftware().getID());
		}
		return msg;
	}

	/**
	 * @param simulation
	 * @return
	 * 
	 */
	private TargetAgent perceive(ShanksSimulation simulation) {
		Bag objects = ShanksAgentPerceptionCapability.getPercepts(simulation, this);
		for (int i = 0; i < objects.size(); i++) {
			Object obj = objects.get(i);
			if (obj.getClass().equals(TargetAgent.class)) {
				TargetAgent ag = (TargetAgent) obj;
				return ag;
			}
		}
		return null;
	}

	/**
	 * @return the hardware
	 */
	public ZigBeeSensorNode getHardware() {
		return hardware;
	}

	/**
	 * @param hardware
	 *            the hardware to set
	 */
	public void setHardware(ZigBeeSensorNode hardware) {
		this.hardware = hardware;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.agent.capability.perception.PercipientShanksAgent
	 * #getPerceptionRange()
	 */
	@Override
	public double getPerceptionRange() {
		return this.perceptionRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.agent.capability.perception.PercipientShanksAgent
	 * #getCurrentLocation()
	 */
	@Override
	public Location getCurrentLocation() {
		return new Location(this.getHardware().getPosition());
	}

}
