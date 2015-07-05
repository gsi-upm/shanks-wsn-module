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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.csvreader.CsvWriter;

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
	 * @throws IOException
	 */
	public static void main(String[] args) throws ShanksException, IOException {

//		long[] seeds = new long[]{8245, 40, 547, 115, 200, 35, 9087, 4568, 87, 6752};
//		int[] sensors = new int[]{25, 50, 75, 100};
//		int[] routers = new int[]{5, 10, 15, 20, 25};
//		int simid = 0;

		long[] seeds = new long[]{8245};
		int[] sensors = new int[]{100};
		int[] routers = new int[]{15};
		int simid = 0;
		


		String scenarioName = "WSNScenario-"+simid;
		String outputFolder = "output/"+scenarioName+"/";
		File dir = new File(outputFolder);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		CsvWriter writer = new CsvWriter(new FileWriter(outputFolder+"global-result-"+scenarioName+".csv"), ';');
		// feed in your array (or convert your data to an array)
		String[] headers = new String[]{"simulationID", "seed", "steps", "sensors", "routers", "leafRouters", "lostMsgs", "rcvdMsgs"};
		writer.writeRecord(headers);
		writer.flush();
		
		for (long seed : seeds) {
			for (int sensor : sensors) {
				for (int router : routers) {

					// *****************CONFIGURATION PARAMETERS*************
					// Properties to configure scenario
					Properties scenarioProperties = new Properties();
					
					scenarioProperties.put("SimID", Integer.toString(simid));
					scenarioProperties.put("OutputFolder", outputFolder);
					
//					scenarioProperties.put(Scenario.SIMULATION_GUI, Scenario.SIMULATION_2D);
					scenarioProperties.put(Scenario.SIMULATION_GUI, Scenario.NO_GUI);
					// scenarioProperties.put(WSNScenario.RANDOM_SEED, "40");
					scenarioProperties.put(WSNScenario.RANDOM_SEED, Long.toString(seed));
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
					// scenarioProperties.put(WSNScenario.SENSORS, "100");
					scenarioProperties.put(WSNScenario.SENSORS, Integer.toString(sensor));
					/**
					 * Number of sensors clusters in the simulation
					 */
					// scenarioProperties.put(WSNScenario.CLUSTERS, "20");
					scenarioProperties.put(WSNScenario.CLUSTERS, Integer.toString(router));
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
					 * -> 0.1388888 m/100ms = 1.38m/s = 5km/h (regular speed for
					 * a person walking)
					 * 
					 * -> 1.1 m/100ms = 11m/s = (aprox) 100m/9seg (almost
					 * olympic record)
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
					 * percentage of the maximum range to save energy an ensure
					 * the reception even with high noise
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

					// TODO borrar esto tras experiments
					//initial for fgct2015 submmited version -> 6000
					scenarioProperties.put("MaxSteps", "1000");

					// ***************** END OF CONFIGURATION
					// PARAMETERS*************
					Logger logger = Logger.getLogger(scenarioName);
					LogConfigurator.log2File(logger, "simulation", Level.OFF, Level.OFF);

					// String seedString =
					// scenarioProperties.getProperty(WSNScenario.RANDOM_SEED);
					// long seed = new Integer(seedString);

					WSNSimulation sim = new WSNSimulation(seed, WSNScenario.class, scenarioName,
							WSNScenarioStates.SUNNY.toString(), scenarioProperties);
					WSNSimulation2DGUI gui = new WSNSimulation2DGUI(sim);
					gui.start();
					sim.start();
					String maxSteps = scenarioProperties.getProperty("MaxSteps");
					long steps = new Integer(maxSteps) + 1;
					do
						if (!sim.schedule.step(sim))
							break;
					while (sim.schedule.getSteps() < steps);

					sim.finish();
					gui.finish();
					gui.quit();

					System.out.println("Step: " + maxSteps);
					System.out.println("Seed: " + seed);
					System.out.println("Sensors: " + sensor);
					System.out.println("Routers: " + router);
					System.out.println("LeafRouters: " + scenarioProperties.getProperty("leafRouters"));

					System.out.println("LostMsgs: " + scenarioProperties.getProperty("lostMsgs"));

					System.out.println("RcvdMsgs: " + scenarioProperties.getProperty("msgs"));
					System.out.println("------------FIN DE SIMULACION -> sim id: " + simid + "---------------");

					String[] row = new String[8];
					row[0] = Integer.toString(simid);
					row[1] = Long.toString(seed);
					row[2] = maxSteps;
					row[3] = Integer.toString(sensor);
					row[4] = Integer.toString(router);
					row[5] = scenarioProperties.getProperty("leafRouters");
					row[6] = scenarioProperties.getProperty("lostMsgs");
					row[7] = scenarioProperties.getProperty("msgs");
							
					writer.writeRecord(row);
					writer.flush();
					simid++;
				}
			}
		}

		writer.close();
		System.exit(0);
	}

}
