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

import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeCoordinatorNode;

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.agent.BaseStationSoftware.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 01/07/2014
 * @version 0.1
 * 
 */
public class ZigBeeCoordiantorNodeSoftware extends SimpleShanksAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5967376293400865123L;

	private ZigBeeCoordinatorNode hardware;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 */
	public ZigBeeCoordiantorNodeSoftware(String id, Logger logger, ZigBeeCoordinatorNode hardware) {
		super(id, logger);
		this.hardware = hardware;
		this.hardware.setSoftware(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	@Override
	public void checkMail() {
		List<Message> inbox = this.getInbox();
		if (inbox.size() > 0) {
			this.getLogger().fine("BaseStation has received " + inbox.size() + " messages in the last step.");
			inbox.clear();
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
		this.getLogger()
				.finest("-> Reasoning cycle of " + this.getID() + " -> Step: " + simulation.schedule.getSteps());
	}

	/**
	 * @return the hardware
	 */
	public ZigBeeCoordinatorNode getHardware() {
		return hardware;
	}

	/**
	 * @param hardware
	 *            the hardware to set
	 */
	public void setHardware(ZigBeeCoordinatorNode hardware) {
		this.hardware = hardware;
	}

}
