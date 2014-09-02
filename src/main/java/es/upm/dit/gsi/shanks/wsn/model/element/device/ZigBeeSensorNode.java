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
package es.upm.dit.gsi.shanks.wsn.model.element.device;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import sim.util.Double2D;
import es.upm.dit.gsi.shanks.agent.ShanksAgent;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.element.link.Link;

/**
 * Project: wsn File:
 * es.upm.dit.gsi.shanks.wsn.model.element.device.SensorNode.java
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
public class ZigBeeSensorNode extends Device {

	// Possible states
	public static final String CPU_DAMAGED_STATUS = "CpuDamaged";
	public static final String BATTERY_DAMAGED_STATUS = "BatteryDamaged";
	public static final String SENSOR_DAMAGED_STATUS = "SensorDamaged";
	public static final String MEMORY_DAMAGED_STATUS = "MemoryDamaged";
	public static final String EXTERNAL_DAMAGE_STATUS = "ExternalDamage";


	private Double2D position;

	private boolean isZigBeeRouter;

	private Link path2sink;

	private ShanksAgent software;

	private boolean detecting;

	private Battery battery;

	private CPU cpu;

	private Memory memory;

	private double temp;

	private Random rnd;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param initialState
	 * @param isGateway
	 * @param logger
	 * @param position
	 * @param battery
	 * @param rnd
	 */
	public ZigBeeSensorNode(String id, String initialState, boolean isGateway, Logger logger, Double2D position,
			Battery battery, Random rnd) {
		super(id, initialState, isGateway, logger);
		this.rnd = rnd;
		this.setPosition(position);
		this.setZigBeeRouter(false);
		this.setDetecting(false);
		this.setBattery(battery);
		this.setCpu(new CPU());
		this.setMemory(new Memory());
		this.setTemp(35.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.element.NetworkElement#fillIntialProperties()
	 */
	@Override
	public void fillIntialProperties() {
		// Nothing to do here, because properties are stored as attributes.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.element.NetworkElement#checkProperties()
	 */
	@Override
	public void checkProperties() throws ShanksException {
		HashMap<String, Boolean> status = this.getStatus();
		if (status.get(ZigBeeSensorNode.CPU_DAMAGED_STATUS)) {
			double damage = this.rnd.nextDouble();
			int intDamage = (int) (damage * 100);
			damage = ((double) intDamage) / 100;
			this.cpu.setDamagePercentage(damage);
			this.getSoftware().getLogger()
					.warning("Node: " + this.getID() + " -> CPU DAMAGED PERCENTAGE: " + this.cpu.getDamagePercentage());
			this.cpu.setDamaged(true);
		}
		if (status.get(ZigBeeSensorNode.BATTERY_DAMAGED_STATUS)) {
			double damage = this.rnd.nextDouble();
			int intDamage = (int) (damage * 100);
			damage = ((double) intDamage) / 100;
			// TODO
		}
		if (status.get(ZigBeeSensorNode.MEMORY_DAMAGED_STATUS)) {
			double damage = this.rnd.nextDouble();
			int intDamage = (int) (damage * 100);
			damage = ((double) intDamage) / 100;
			// TODO
		}
		if (status.get(ZigBeeSensorNode.SENSOR_DAMAGED_STATUS)) {
			double damage = this.rnd.nextDouble();
			int intDamage = (int) (damage * 100);
			damage = ((double) intDamage) / 100;
			// TODO
		}
		if (status.get(ZigBeeSensorNode.EXTERNAL_DAMAGE_STATUS)) {
			double damage = this.rnd.nextDouble();
			int intDamage = (int) (damage * 100);
			damage = ((double) intDamage) / 100;
			// TODO
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.element.NetworkElement#checkStatus()
	 */
	@Override
	public void checkStatus() throws ShanksException {
		if (!this.cpu.isDamaged()) {
			this.updateStatusTo(ZigBeeSensorNode.CPU_DAMAGED_STATUS, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.element.NetworkElement#setPossibleStates()
	 */
	@Override
	public void setPossibleStates() {
		this.addPossibleStatus(ZigBeeSensorNode.CPU_DAMAGED_STATUS);
		this.addPossibleStatus(ZigBeeSensorNode.BATTERY_DAMAGED_STATUS);
		this.addPossibleStatus(ZigBeeSensorNode.MEMORY_DAMAGED_STATUS);
		this.addPossibleStatus(ZigBeeSensorNode.SENSOR_DAMAGED_STATUS);
		this.addPossibleStatus(ZigBeeSensorNode.EXTERNAL_DAMAGE_STATUS);
		this.addPossibleStatus("OK");

	}

	/**
	 * @return the position
	 */
	public Double2D getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Double2D position) {
		this.position = position;
	}

	/**
	 * @return the isClusterHead
	 */
	public boolean isZigBeeRouter() {
		return isZigBeeRouter;
	}

	/**
	 * @param isZigBeeRouter
	 *            the isClusterHead to set
	 */
	public void setZigBeeRouter(boolean isZigBeeRouter) {
		this.isZigBeeRouter = isZigBeeRouter;
	}

	/**
	 * @return the path2sink
	 */
	public Link getPath2sink() {
		return path2sink;
	}

	/**
	 * @param path2sink
	 *            the path2sink to set
	 */
	public void setPath2Sink(Link path2sink) {
		this.path2sink = path2sink;
	}

	/**
	 * @return the software
	 */
	public ShanksAgent getSoftware() {
		return software;
	}

	/**
	 * @param software
	 *            the software to set
	 */
	public void setSoftware(ShanksAgent software) {
		this.software = software;
	}

	/**
	 * @return the detecting
	 */
	public boolean isDetecting() {
		return detecting;
	}

	/**
	 * @param detecting
	 *            the detecting to set
	 */
	public void setDetecting(boolean detecting) {
		this.detecting = detecting;
	}

	/**
	 * @return the battery
	 */
	public Battery getBattery() {
		return battery;
	}

	/**
	 * @param battery
	 *            the battery to set
	 */
	public void setBattery(Battery battery) {
		this.battery = battery;
	}

	/**
	 * @param sensor
	 * @param noise
	 *            in dB
	 * @return in mA
	 */
	public double getRequiredCurrentForEmissionToNode(ZigBeeSensorNode sensor, double noise) {
		Double2D pos1 = this.getPosition();
		Double2D pos2 = sensor.getPosition();
		double distance = pos1.distance(pos2);
		double distanceKm = distance / 1000;
		double loss = 100 + (20 * Math.log10(distanceKm)); // in dB for 2,4GHz
		double sensitivy = -90; // Sensitivity in dBm
		// Emission power required to ensure the reception (in dBm)
		double emisionPower = sensitivy + loss + noise;
		double emissionConsumption = Double.MAX_VALUE;

		// Consumption criteria (in mA) - (emissionPower in dBm)
		if (emisionPower < -24) {
			emissionConsumption = 7.3;
		} else if (emisionPower < -20) {
			emissionConsumption = 8.3;
		} else if (emisionPower < -18) {
			emissionConsumption = 8.8;
		} else if (emisionPower < -13) {
			emissionConsumption = 9.8;
		} else if (emisionPower < -10) {
			emissionConsumption = 10.4;
		} else if (emisionPower < -6) {
			emissionConsumption = 11.3;
		} else if (emisionPower < -2) {
			emissionConsumption = 15.6;
		} else if (emisionPower < 0) {
			emissionConsumption = 17.0;
		} else if (emisionPower < 3) {
			emissionConsumption = 20.2;
		} else if (emisionPower < 4) {
			emissionConsumption = 22.5;
		} else if (emisionPower < 5) {
			emissionConsumption = 26.9;
		}

		return emissionConsumption;
	}

	/**
	 * @param sensor
	 * @param noise
	 *            in dB
	 * @return in mA
	 */
	public int getEmmitedPowerToNode(ZigBeeSensorNode sensor, double noise) {
		Double2D pos1 = this.getPosition();
		Double2D pos2 = sensor.getPosition();
		double distance = pos1.distance(pos2);
		double distanceKm = distance / 1000;
		double loss = 100 + (20 * Math.log10(distanceKm)); // in dB for 2,4GHz
		double sensitivy = -90; // Sensitivity in dBm
		// Emission power required to ensure the reception (in dBm)
		double emisionPower = sensitivy + loss + noise;
		int emittedPower = Integer.MAX_VALUE;

		// Consumption criteria (in mA) - (emissionPower in dBm)
		if (emisionPower < -24) {
			emittedPower = -24;
		} else if (emisionPower < -20) {
			emittedPower = -20;
		} else if (emisionPower < -18) {
			emittedPower = -18;
		} else if (emisionPower < -13) {
			emittedPower = -13;
		} else if (emisionPower < -10) {
			emittedPower = -10;
		} else if (emisionPower < -6) {
			emittedPower = -6;
		} else if (emisionPower < -2) {
			emittedPower = -2;
		} else if (emisionPower < 0) {
			emittedPower = 0;
		} else if (emisionPower < 3) {
			emittedPower = 3;
		} else if (emisionPower < 4) {
			emittedPower = 4;
		} else if (emisionPower < 5) {
			emittedPower = 5;
		}

		return emittedPower;
	}

	/**
	 * @return the cpu
	 */
	public CPU getCpu() {
		return cpu;
	}

	/**
	 * @param cpu
	 *            the cpu to set
	 */
	public void setCpu(CPU cpu) {
		this.cpu = cpu;
	}

	/**
	 * @return the memory
	 */
	public Memory getMemory() {
		return memory;
	}

	/**
	 * @param memory
	 *            the memory to set
	 */
	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	/**
	 * @return the temp
	 */
	public double getTemp() {
		return temp;
	}

	/**
	 * @param temp
	 *            the temp to set
	 */
	public void setTemp(double temp) {
		this.temp = temp;
	}

	/**
	 * @param celsius
	 */
	public void increaseTemp(double celsius) {
		if (this.temp <= 100.0) {
			this.temp += celsius;
		}
	}

	/**
	 * @param celsius
	 */
	public void decreaseTemp(double celsius) {
		if (this.temp >= 25.0) {
			this.temp -= celsius;
		}
	}

}
