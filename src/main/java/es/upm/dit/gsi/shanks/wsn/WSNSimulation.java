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
package es.upm.dit.gsi.shanks.wsn;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.NetworkElement;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;
import es.upm.dit.gsi.shanks.wsn.agent.TargetAgent;
import es.upm.dit.gsi.shanks.wsn.agent.ZigBeeCoordiantorNodeSoftware;
import es.upm.dit.gsi.shanks.wsn.agent.ZigBeeSensorNodeSofware;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;
import es.upm.dit.gsi.shanks.wsn.model.scenario.WSNScenario;

/**
 * Project: wsn File: es.upm.dit.gsi.shanks.wsn.WSNSimulation.java
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
public class WSNSimulation extends ShanksSimulation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227634202835889448L;

	/**
	 * Constructor
	 * 
	 * @param seed
	 * @param scenarioClass
	 * @param scenarioID
	 * @param initialState
	 * @param properties
	 * @throws ShanksException
	 */
	public WSNSimulation(long seed, Class<? extends Scenario> scenarioClass, String scenarioID, String initialState,
			Properties properties) throws ShanksException {
		super(seed, scenarioClass, scenarioID, initialState, properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.ShanksSimulation#addSteppables()
	 */
	@Override
	public void addSteppables() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.ShanksSimulation#registerShanksAgents()
	 */
	@Override
	public void registerShanksAgents() throws ShanksException {
		HashMap<String, NetworkElement> elementsMap = this.getScenario().getCurrentElements();
		String percp = this.getScenario().getProperties().getProperty(WSNScenario.PERCEPTION_RANGE);
		double perceptionRange = new Double(percp);
		String stepTimeString = this.getScenario().getProperties().getProperty(WSNScenario.STEP_TIME);
		double stepTime = new Double(stepTimeString);

		String maxNoiseString = this.getScenario().getProperties().getProperty(WSNScenario.MAX_NOISE_IN_DB);
		double maxNoise = new Double(maxNoiseString);
		String minNoiseString = this.getScenario().getProperties().getProperty(WSNScenario.MIN_NOISE_IN_DB);
		double minNoise = new Double(minNoiseString);
		// Install software in nodes
		for (Entry<String, NetworkElement> entry : elementsMap.entrySet()) {
			if (entry.getValue().getClass().equals(ZigBeeSensorNode.class)) {
				ZigBeeSensorNode node = (ZigBeeSensorNode) entry.getValue();
				if (node.getID().startsWith("base")) {
					ZigBeeCoordiantorNodeSoftware bsoftware = new ZigBeeCoordiantorNodeSoftware("software-"
							+ node.getID(), logger, node, this);
					this.registerShanksAgent(bsoftware);
				} else {
					ZigBeeSensorNodeSofware software = new ZigBeeSensorNodeSofware("software-" + node.getID(), logger,
							node, perceptionRange, stepTime, maxNoise, minNoise, this.random);
					this.registerShanksAgent(software);
				}
			}
		}

		// Create target agents
		String targetsString = this.getScenario().getProperties().getProperty(WSNScenario.TARGETS);
		int targets = new Integer(targetsString);
		for (int i = 0; i < targets; i++) {
			TargetAgent targetAgent = new TargetAgent("target-" + i, this);
			this.registerShanksAgent(targetAgent);
		}
	}
}
