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

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.model.element.device.Battery.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 30/06/2014
 * @version 0.1
 * 
 */
public class Battery {

	/**
	 * Maximum capacity of the battery in mAh
	 */
	private double capacity;
	/**
	 * Current charge of the battery in mAh
	 */
	private double currentCharge;
	/**
	 * Voltage of the battery
	 */
	private double voltage;

	/**
	 * Constructor
	 * 
	 * @param capacity
	 *            in mAh
	 * @param voltage
	 *            in V
	 */
	public Battery(double capacity, double voltage) {
		this.capacity = capacity;
		this.voltage = voltage;
		this.currentCharge = capacity;
	}

	/**
	 * Constructor
	 * 
	 * @param capacity
	 * @param currentCharge
	 * @param voltage
	 */
	public Battery(double capacity, double currentCharge, double voltage) {
		this.capacity = capacity;
		this.voltage = voltage;
		this.currentCharge = currentCharge;
	}

	/**
	 * @return the capacity
	 */
	public double getCapacity() {
		return capacity;
	}

	/**
	 * @return the currentCharge
	 */
	public double getCurrentChargeInmAh() {
		return currentCharge;
	}

	/**
	 * @return
	 */
	public double getCurrentChargeInmAs() {
		return currentCharge * 3600;
	}

	/**
	 * @return percentage in [0,1] range
	 */
	public double getCurrentChargePercentage() {
		double value = currentCharge / capacity;
		double rounded = Math.round(value * 100.0) / 100.0;
		return rounded;
	}

	/**
	 * @return the voltage
	 */
	public double getVoltage() {
		return voltage;
	}

	/**
	 * @param current
	 *            in mA
	 * @param time
	 *            in ms
	 */
	public void consume(double current, double time) {
		if (time > 0) {
			// Time in hours
			double ts = time / 3600000;
			this.currentCharge = this.currentCharge - (current * ts);
		}
	}

	/**
	 * Change the current charge to max capacity.
	 */
	public void recharge() {
		this.currentCharge = capacity;
	}
	
	/**
	 * @param capacity
	 */
	public void discharge(double capacity) {
		this.currentCharge = this.currentCharge - capacity;
	}

	/**
	 * @return
	 */
	public static Battery get2AABatteryAlkaline() {
		return new Battery(2600 * 2, 1.5 * 2);
	}

	/**
	 * @return
	 */
	public static Battery getInfiniteBattery() {
		return new Battery(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

}
