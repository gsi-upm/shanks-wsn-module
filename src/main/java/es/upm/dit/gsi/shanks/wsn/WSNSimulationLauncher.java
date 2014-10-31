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
import es.upm.dit.gsi.shanks.wsn.utils.LogConfigurator;

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

		// *****************CONFIGURATION PARAMETERS*************
		// Properties to configure scenario
		Properties scenarioProperties = new Properties();
		scenarioProperties.put(Scenario.SIMULATION_GUI, Scenario.SIMULATION_2D);
		scenarioProperties.put(WSNScenario.RANDOM_SEED, "40");
		/**
		 * File that contains rules in .rls format
		 */
		scenarioProperties.put(WSNScenario.TOPOLOGY_RULES_FILE, "src/main/resources/rules/topology.rls");
		/**
		 * Ontology URI
		 */
		scenarioProperties.put(WSNScenario.ONTOLOGY_URI, "src/main/resources/b2d2-wsn.owl");
		/**
		 * Number of sensors in the simulation
		 */
		scenarioProperties.put(WSNScenario.SENSORS, "100");
		/**
		 * Number of sensors clusters in the simulation
		 */
		scenarioProperties.put(WSNScenario.CLUSTERS, "20");
		/**
		 * STEP_TIME in ms
		 */
		scenarioProperties.put(WSNScenario.STEP_TIME, "100");
		/**
		 * Number of targets to detect their movements
		 */
		scenarioProperties.put(WSNScenario.TARGETS, "1");
		/**
		 * TARGET_SPEED in m/step
		 * 
		 * -> 0.1388888 m/100ms = 1.38m/s = 5km/h (regular speed for a person
		 * walking)
		 * 
		 * -> 1.1 m/100ms = 11m/s = (aprox) 100m/9seg (almost olympic record)
		 * 
		 */
		scenarioProperties.put(WSNScenario.TARGET_SPEED, "0.138");
		/**
		 * FIELD_WIDTH in meters
		 */
		scenarioProperties.put(WSNScenario.FIELD_WIDTH, "100");
		/**
		 * FIELD_HEIGHT in meters
		 */
		scenarioProperties.put(WSNScenario.FIELD_HEIGHT, "100");
		/**
		 * PERCEPTION_RANGE of sensors in meters
		 */
		scenarioProperties.put(WSNScenario.PERCEPTION_RANGE, "5");
		/**
		 * SENSOR_WIFI_RANGE_PERCENTAGE = [0,1]
		 * 
		 * percentage of the maximum range to save energy an ensure the
		 * reception even with high noise
		 * 
		 */
		scenarioProperties.put(WSNScenario.SENSOR_WIFI_RANGE_PERCENTAGE, "1.0");
		/**
		 * MAX_NOISE_IN_DB
		 * 
		 * Aprox. Max. value. in dB value outdoor = 10dB
		 * 
		 * Aprox. Max. value. in dB value indoor = 26dB
		 * 
		 */
		scenarioProperties.put(WSNScenario.MAX_NOISE_IN_DB, "26");
		/**
		 * MIN_NOISE_IN_DB
		 * 
		 * Aprox. Min. value. in dB value outdoor = 5dB
		 * 
		 * Aprox. Min. value. in dB value indoor = 20dB
		 * 
		 */
		scenarioProperties.put(WSNScenario.MIN_NOISE_IN_DB, "20");

		String scenarioName = "WSNScenario";

		// ***************** END OF CONFIGURATION PARAMETERS*************
		Logger logger = Logger.getLogger(scenarioName);
		LogConfigurator.log2File(logger, "simulation", Level.OFF, Level.INFO);

		String seedString = scenarioProperties.getProperty(WSNScenario.RANDOM_SEED);
		long seed = new Integer(seedString);

		WSNSimulation sim = new WSNSimulation(seed, WSNScenario.class, scenarioName,
				WSNScenarioStates.SUNNY.toString(), scenarioProperties);
		WSNSimulation2DGUI gui = new WSNSimulation2DGUI(sim);
		gui.start();
	}

}
