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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import sim.util.Bag;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.movement.Location;
import es.upm.dit.gsi.shanks.agent.capability.perception.PercipientShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.perception.ShanksAgentPerceptionCapability;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.element.link.Link;
import es.upm.dit.gsi.shanks.wsn.model.element.device.Battery;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;

/**
 * Project: wsn File:
 * es.upm.dit.gsi.shanks.wsn.agent.ZigBeeSensorNodeSofware.java
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

	private double stepTime;

	private double consumedTimeInStep;

	private List<Message> msgsReadyToProcess;

	private double receptionConsumption = 19.7; // in mA
	private double txrxIdleConsumption = 0.020; // in mA
	private double sensorDetectingconsumption = 0.1; // in mA
	private double sensorNonDetectingconsumption = 0.006; // in mA
	private double cpuConsumption = 8; // in mA
	private double cpuIdleConsumption = 0.017; // in mA

	private double incrementTempActive = 2.0; // 2ºC in 1 second active
	private double decrementTempIdle = 0.5; // 0.5ºC in 1 second idle

	private double memoryMsgSizePct = 0.05; // 5% of memory

	private int sensitivity = -90; // -90 dBm

	private double maxNoise;
	private double minNoise;

	private MersenneTwisterFast random;

	private int msgCounter;

	private ShanksSimulation sim;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 * @param stepTime
	 * @param random
	 */
	public ZigBeeSensorNodeSofware(String id, Logger logger, ZigBeeSensorNode hardware, double perceptionRange,
			double stepTime, double maxNoise, double minNoise, MersenneTwisterFast random, ShanksSimulation sim) {
		super(id, logger);
		this.hardware = hardware;
		this.hardware.setSoftware(this);
		this.perceptionRange = perceptionRange;
		this.stepTime = stepTime;
		this.msgsReadyToProcess = new ArrayList<Message>();
		this.consumedTimeInStep = 0.0;
		this.maxNoise = maxNoise;
		this.minNoise = minNoise;
		this.random = random;
		this.msgCounter = 0;
		this.sim = sim;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	@Override
	public void checkMail() {
		this.restartTimer();
		List<Message> inbox = this.getInbox();

		List<Message> lostMsgs = new ArrayList<Message>();

		for (Message msg : inbox) {

			// Check lost messages with real emitted power
			String msgId = msg.getMsgId();
			String[] msgIdparts = msgId.split(":");
			int power = Integer.parseInt(msgIdparts[3]);
			double noise = this.getGaussianNoise();

			String sensorId = msgIdparts[0];
			ZigBeeSensorNode sensor = (ZigBeeSensorNode) this.sim.getScenario().getCurrentElements().get(sensorId);

			Double2D pos1 = this.getHardware().getPosition();
			Double2D pos2 = sensor.getPosition();
			double distance = pos1.distance(pos2);
			double distanceKm = distance / 1000;
			double loss = 100 + (20 * Math.log10(distanceKm)); // in dB for
																// 2,4GHz
			double snr = (power - loss) - noise;
			if (snr < this.sensitivity) {
				lostMsgs.add(msg);
			}
		}
		if (lostMsgs.size() > 0 && inbox.size() > 0) {
			this.getLogger().info(this.getID() + "-> Ratio of lost messages: " + lostMsgs.size() + "/" + inbox.size());
		}
		inbox.removeAll(lostMsgs);

		double freeMemoryPct = this.getHardware().getMemory().getFreeMemory();
		int msgsMemoryCapacity = (int) (freeMemoryPct / memoryMsgSizePct);
		if (inbox.size() > msgsMemoryCapacity) {
			int discardMsgs = inbox.size() - msgsMemoryCapacity;
			inbox.subList(msgsMemoryCapacity, inbox.size()).clear();
			this.getLogger().warning(
					this.getID() + "-> Memory full -> Discarding " + discardMsgs + " messages in the queue.");
		}
		double memoryUsed = inbox.size() * memoryMsgSizePct;
		this.getHardware().getMemory().setLoad(memoryUsed);

		Message msg;
		double freeCPUPct = this.getHardware().getCpu().getFreeCPU();
		double timeToReceive = ((double) this.stepTime) * 0.35 * freeCPUPct;
		// 35% of time for receiving, 35% of time sending (forwarding) and the
		// remaining 30% detecting
		while (this.consumedTimeInStep < timeToReceive && inbox.size() > 0) {
			msg = inbox.get(0);
			inbox.remove(msg);
			this.getLogger().finest("Inbox size of " + this.getID() + ": " + inbox.size() + ". Processing 1 message.");
			this.receivePackage(msg);
			this.msgsReadyToProcess.add(msg);
		}

		if (inbox.size() > 0) {
			this.getLogger().fine(
					"Pending messages of " + this.getID() + ": " + inbox.size()
							+ ". Too many messages to process in only one step.");
		}
	}

	/**
	 * @return
	 */
	private double getGaussianNoise() {
		double mean = (maxNoise + minNoise) / 2;
		double std = (maxNoise - minNoise) / 4;
		double gaussian = this.random.nextGaussian();
		double noise = (gaussian * std) + mean;
		return noise;
	}

	/**
	 * 
	 */
	private void restartTimer() {
		this.consumedTimeInStep = 0.0;
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
		long step = simulation.schedule.getSteps();
		this.getLogger().finest(
				"-> Reasoning cycle of " + this.getID() + " with perception range: " + this.getPerceptionRange()
						+ " -> Step: " + step);

		double externalDmgPctg = this.getHardware().getExternalDamagedPctg();
		if (this.random.nextDouble() < externalDmgPctg) {
			// If external damage, nothing to do in this cycle: no detection, no
			// forward msgs, etc.
			this.getLogger().warning(
					"External damage of " + this.getHardware().getID()
							+ " caused the lost of all pending messages in step " + this.sim.getSchedule().getSteps());
			int size = this.getInbox().size();
			if (size > 0) {
				this.getLogger().warning("Total messages lost: " + size);
			} else {
				this.getLogger().warning("No messages lost because inbox was empty in this step.");
			}
			this.getInbox().clear();
			this.getHardware().getMemory().setLoad(0.0);
			this.getHardware().getCpu().setIdleLoad();
		} else {

			// Process incoming messages
			if (this.msgsReadyToProcess.size() > 0) {
				this.getHardware().getCpu().incrementLoad(0.8);
			} else {
				this.getHardware().getCpu().setIdleLoad();
			}
			for (Message msg : this.msgsReadyToProcess) {
				Message newMsg = new Message();
				String content = (String) msg.getPropCont();
				content = content + "&" + this.buildMessageContent(false, simulation);
				newMsg.setPropCont(content);
				newMsg.setMsgId(this.getHardware().getID() + ":" + simulation.schedule.getSteps() + ":"
						+ this.msgCounter++ + ":" + this.getMessageEmittedPower());
				newMsg.setInReplyTo(msg.getInReplyTo() + "&" + msg.getMsgId());
				newMsg = this.setReceiverInPath(newMsg);
				this.sendPackage(newMsg);
				this.getLogger().finest(
						"Resent message from " + msg.getSender() + " to " + msg.getReceiver() + " towards "
								+ newMsg.getReceiver());
				this.getHardware().getMemory().decrementLoad(memoryMsgSizePct);
			}
			this.msgsReadyToProcess.clear();

			// Detect target agents
			TargetAgent target = this.perceive(simulation);
			double sensorDmgPct = this.getHardware().getSensorDamagedPctg();
			boolean failing = false;
			if (this.random.nextDouble() < sensorDmgPct) {
				failing = true;
			}
			if ((target != null && !failing) || (target == null && failing)) {
				// Send detection message if detection + not sensor failure (OK)
				// and if not detection + sensor failing (NOK-failure)
				if (failing) {
					this.getLogger().warning(
							"Sensor damage of " + this.getHardware().getID()
									+ " caused false positive detection in step " + this.sim.getSchedule().getSteps());
				}
				this.getHardware().getCpu().incrementLoad(0.2);
				this.getHardware().getMemory().incrementLoad(0.1);
				this.getHardware().setDetecting(true);
				this.getLogger().fine("Target detected by sensor " + this.getHardware().getID());
				this.sendDetectionMessage(simulation);

				// Adjust variables
				this.consumeReaminingTime(false);
				double cpudamaged = this.getHardware().getCpu().getDamagePercentage();
				double temp = this.incrementTempActive * this.stepTime * (1.0 + cpudamaged) / 1000;
				this.getHardware().increaseTemp(temp);
			} else {
				// Don't send detection message if not detection + not sensor
				// failure (OK)
				// and if detection + sensor failing (NOK-failure)
				if (failing) {
					this.getLogger().warning(
							"Sensor damage of " + this.getHardware().getID()
									+ " caused false negative detection in step " + this.sim.getSchedule().getSteps());
				}

				this.getHardware().setDetecting(false);

				// Adjust variables
				this.consumeReaminingTime(true);
				double cpudamaged = this.getHardware().getCpu().getDamagePercentage();
				double temp = this.incrementTempActive * this.consumedTimeInStep * (1.0 + cpudamaged) / 1000;
				this.getHardware().increaseTemp(temp);
				double idleTime = this.stepTime - this.consumedTimeInStep;
				temp = this.decrementTempIdle * idleTime / 1000;
				this.getHardware().decreaseTemp(temp);
			}

			// Finish step and free CPU load
			// but not memory because maybe there are pending messages.
			this.getHardware().getCpu().setIdleLoad();

		}
	}
	/**
	 * 
	 */
	private void consumeReaminingTime(boolean idle) {
		double remainingTime = this.stepTime - this.consumedTimeInStep;
		if (remainingTime < 0) {
			this.getLogger().fine(this.getID() + " consuming more time than stepTime! -> " + this.consumedTimeInStep);
			if (idle) {
				this.getHardware()
						.getBattery()
						.consume(cpuIdleConsumption + txrxIdleConsumption + sensorNonDetectingconsumption,
								remainingTime);
			} else {
				this.getHardware().getBattery()
						.consume(cpuConsumption + txrxIdleConsumption + sensorDetectingconsumption, remainingTime);
			}
		}
	}

	/**
	 * @param distance
	 * @param sim
	 */
	private void sendDetectionMessage(ShanksSimulation sim) {
		Message msg = new Message();
		String content = this.buildMessageContent(true, sim);
		msg.setMsgId(this.getHardware().getID() + ":" + sim.schedule.getSteps() + ":" + this.msgCounter++ + ":"
				+ this.getMessageEmittedPower());
		msg.setInReplyTo("TargetDetected");
		msg.setPropCont(content);
		msg = this.setReceiverInPath(msg);
		this.sendPackage(msg);
		this.getLogger().finest("Detection message sent from " + this.getID() + " to " + msg.getReceiver());
	}
	/**
	 * @param detecting
	 * @param sim
	 * @return
	 */
	private String buildMessageContent(boolean detecting, ShanksSimulation sim) {
		// Msg content ->
		// CPU:80.0/MEM:50.0/TMP:50.0/D:T/BAT:80.15
		ZigBeeSensorNode sensor = this.getHardware();
		double roundedTemp = Math.round(sensor.getTemp() * 100.0) / 100.0;
		double roundedcpu = Math.round(sensor.getCpu().getLoad() * 100.0) / 100.0;
		double roundedMemory = Math.round(sensor.getMemory().getLoad() * 100.0) / 100.0;
		String d = "";
		if (detecting) {
			d = "T";
		} else {
			d = "F";
		}
		String content = "CPU:" + roundedcpu + "/MEM:" + roundedMemory + "/TMP:" + roundedTemp + "/DET:" + d + "/BAT:"
				+ sensor.getBattery().getCurrentChargePercentage();
		return content;
	}

	/**
	 * @param msg
	 */
	private void sendPackage(Message msg) {
		Battery battery = this.hardware.getBattery();
		double time = this.getMessageProcessingTime(msg);
		double current = this.getMessageEmissionCurrentConsumption();
		battery.consume(current, time);
		this.sendMsg(msg);
		this.consumedTimeInStep += time;
	}

	/**
	 * @param msg
	 * @return
	 */
	private void receivePackage(Message msg) {
		Battery battery = this.hardware.getBattery();
		double time = this.getMessageProcessingTime(msg);
		double current = this.getMessageReceptionCurrentConsumption(msg);
		battery.consume(current, time);
		this.consumedTimeInStep += time;
	}

	/**
	 * @param msg
	 * @return
	 */
	private double getMessageReceptionCurrentConsumption(Message msg) {
		return receptionConsumption + cpuConsumption;
	}

	/**
	 * @return
	 */
	private double getMessageEmissionCurrentConsumption() {
		Link link = this.hardware.getPath2sink();
		List<Device> devices = link.getLinkedDevices();
		int t = 0;
		if (devices.get(0) == this.hardware) {
			t = 1;
		}
		ZigBeeSensorNode sensor = (ZigBeeSensorNode) devices.get(t);
		double emissionCurrent = this.hardware.getRequiredCurrentForEmissionToNode(sensor, this.maxNoise);
		double totalEmissionConsumption = emissionCurrent + cpuConsumption;
		return totalEmissionConsumption;
	}

	/**
	 * @return
	 */
	private int getMessageEmittedPower() {
		Link link = this.hardware.getPath2sink();
		List<Device> devices = link.getLinkedDevices();
		int t = 0;
		if (devices.get(0) == this.hardware) {
			t = 1;
		}
		ZigBeeSensorNode sensor = (ZigBeeSensorNode) devices.get(t);
		int emittedPower = this.hardware.getEmmitedPowerToNode(sensor, this.maxNoise);
		return emittedPower;
	}

	/**
	 * @param msg
	 * @return
	 */
	private double getMessageProcessingTime(Message msg) {
		Object content = msg.getPropCont();
		String s = content.toString() + msg.getInReplyTo();
		byte[] asciiBytes = new byte[0];
		try {
			asciiBytes = s.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			this.getLogger().warning("Impossible to get package size in bytes: " + e.getMessage());
		}
		int bytes = asciiBytes.length;
		double bits = bytes * 8;
		// 108kbps = 108bpms
		// time in ms
		double time = bits / 108;
		return time;
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
		ZigBeeSensorNode node = (ZigBeeSensorNode) d;
		msg.setReceiver(node.getSoftware().getID());
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
				double origdistance = ShanksAgentPerceptionCapability.getDistanceTo(simulation, this, ag);
				/**
				 * 1/20 is the error range of the sensor this is not read from
				 * sensor specs because it does not detect distance only detect
				 * movement or not (boolean detection)
				 */
				double noise = simulation.random.nextGaussian() * this.perceptionRange / 20;
				double distance = origdistance + noise;
				if (distance <= this.getPerceptionRange()) {
					this.getLogger().finest(
							"Distance: " + distance + " -> Noise: " + noise + " Real distance: " + origdistance);
					return ag;
				}
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
