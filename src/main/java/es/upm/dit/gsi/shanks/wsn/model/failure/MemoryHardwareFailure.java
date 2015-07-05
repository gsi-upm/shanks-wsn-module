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
package es.upm.dit.gsi.shanks.wsn.model.failure;

import java.util.List;
import java.util.logging.Logger;

import sim.engine.Steppable;
import es.upm.dit.gsi.shanks.model.element.NetworkElement;
import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementFieldException;
import es.upm.dit.gsi.shanks.model.event.failure.Failure;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;

/**
 * Project: shanks-wsn-module
 * File: es.upm.dit.gsi.shanks.wsn.model.failure.MemoryHardwareFailure.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingeniería de Sistemas Telemáticos
 * Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 02/09/2014
 * @version 0.1
 * 
 */
public class MemoryHardwareFailure extends Failure {


	private static double prob = 1/100000;
	
	/**
	 * Constructor
	 *
	 * @param generator
	 * @param logger
	 */
	public MemoryHardwareFailure(Steppable generator, Logger logger) {
		super("Memory_Damaged_Failure_"+System.currentTimeMillis(), generator, prob, logger);
	}

	/**
	 * Constructor
	 *
	 * @param id
	 * @param generator
	 * @param prob
	 * @param logger
	 */
	public MemoryHardwareFailure(String id, Steppable generator, double prob, Logger logger) {
		super(id, generator, prob, logger);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.shanks.model.event.failure.Failure#isResolved()
	 */
	@Override
	public boolean isResolved() {
		List<NetworkElement> affectedElements = this.getCurrentAffectedElements();
		boolean resolved = false;
		for (NetworkElement element : affectedElements) {
			if (element instanceof ZigBeeSensorNode) {
				// Checking status / properties of the network element
				if (!element.getStatus().get(ZigBeeSensorNode.MEMORY_DAMAGED_STATUS)) {
					resolved = true;
				} else {
					resolved = false;
					break;
				}
			}
		}
		return resolved;
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.shanks.model.event.networkelement.ProbabilisticNetworkElementEvent#changeOtherFields()
	 */
	@Override
	public void changeOtherFields() throws UnsupportedNetworkElementFieldException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.shanks.model.event.Event#addPossibleAffected()
	 */
	@Override
	public void addPossibleAffected() {
		this.addPossibleAffectedElementState(ZigBeeSensorNode.class, ZigBeeSensorNode.MEMORY_DAMAGED_STATUS, true);
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.shanks.model.event.Event#interactWithNE()
	 */
	@Override
	public void interactWithNE() {
		// TODO Auto-generated method stub

	}

}
