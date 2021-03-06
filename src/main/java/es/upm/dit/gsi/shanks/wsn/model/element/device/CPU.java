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

import es.upm.dit.gsi.shanks.ShanksSimulation;

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.model.element.device.CPU.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 02/09/2014
 * @version 0.2
 * 
 */
public class CPU {

	private double load;
	private double damagePercentage;
	private boolean damaged;
	/**
	 * Constructor
	 * 
	 */
	public CPU() {
		this.load = 0.0;
		this.damagePercentage = 0.0;
		this.damaged = false;
	}

	/**
	 * @return the load
	 */
	public double getLoad() {
		double aux = load + damagePercentage;
		if (aux > 1.0) {
			return 1.0;
		} else {
			return aux;
		}
	}
	/**
	 * @param load
	 *            the load to set
	 */
	public void setLoad(double load) {
		this.load = load;
	}
	/**
	 * @return the damagePercentage
	 */
	public double getDamagePercentage() {
		return damagePercentage;
	}
	/**
	 * @param damagePercentage
	 *            the damagePercentage to set
	 */
	public void setDamagePercentage(double damagePercentage) {
		this.damagePercentage = damagePercentage;
	}

	/**
	 * @return
	 */
	public double getRandomDamage(ShanksSimulation sim) {
		if (damaged) {
			return sim.random.nextDouble();
		} else {
			return 0.0;
		}
	}

	/**
	 * @return the damaged
	 */
	public boolean isDamaged() {
		return damaged;
	}

	/**
	 * @param damaged
	 *            the damaged to set
	 */
	public void setDamaged(boolean damaged) {
		if (!this.damaged) {
			this.damagePercentage = 0.0;
		}
		this.damaged = damaged;
	}

	public void incrementLoad(double load) {
		this.load += load;
		if (this.load > 1.0) {
			this.load = 1.0;
		}
	}

	/**
	 * @return
	 */
	public double getNonDamagedCPU() {
		return (1.0 - this.damagePercentage);
	}
	
	public void setIdleLoad() {
		this.load = 0.0; 
	}
	
	public double getFreeCPU() {
		return 1 - this.getLoad();
	}

}
