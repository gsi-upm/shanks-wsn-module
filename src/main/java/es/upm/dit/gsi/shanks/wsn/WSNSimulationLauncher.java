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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;
import es.upm.dit.gsi.shanks.wsn.model.scenario.WSNScenario;
import es.upm.dit.gsi.shanks.wsn.model.scenario.WSNScenarioStates;

/**
 * Project: wsn File: es.upm.dit.gsi.shanks.wsn.WSNSimulationLauncher.java
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
public class WSNSimulationLauncher {

	/**
	 * Constructor
	 * 
	 */
	public WSNSimulationLauncher() {
	}

	/**
	 * @param args
	 * @throws ShanksException
	 */
	public static void main(String[] args) throws ShanksException {

		// Properties to configure scenario
		Properties scenarioProperties = new Properties();
		scenarioProperties.put(Scenario.SIMULATION_GUI, Scenario.SIMULATION_2D);
		scenarioProperties.put(WSNScenario.SENSORS, "100");
		scenarioProperties.put(WSNScenario.FIELD_WIDTH, "100");
		scenarioProperties.put(WSNScenario.FIELD_HEIGHT, "100");
		scenarioProperties.put(WSNScenario.CLUSTERS, "10");
		scenarioProperties.put(WSNScenario.PERCEPTION_RANGE, "5");
		scenarioProperties.put(WSNScenario.TARGETS, "1");
		scenarioProperties.put(WSNScenario.TARGET_SPEED, "0.1");

		String scenarioName = "WSNScenario";

		Logger logger = Logger.getLogger(scenarioName);
		LogConfigurator.log2File(logger, "simulation", Level.OFF, Level.WARNING);

		WSNSimulation sim = new WSNSimulation(System.currentTimeMillis(), WSNScenario.class, scenarioName,
				WSNScenarioStates.SUNNY.toString(), scenarioProperties);
		WSNSimulation2DGUI gui = new WSNSimulation2DGUI(sim);
		gui.start();
	}

}
