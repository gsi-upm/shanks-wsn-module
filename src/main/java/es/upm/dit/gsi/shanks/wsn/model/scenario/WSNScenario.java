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
package es.upm.dit.gsi.shanks.wsn.model.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import sim.util.Double2D;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.event.failiure.Failure;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario3DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.BaseStation;
import es.upm.dit.gsi.shanks.wsn.model.element.device.SensorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.link.RoutePathLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.WifiLink;
import es.upm.dit.gsi.shanks.wsn.model.scenario.portrayal.WSNScenario2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.utils.Dijkstra;
import es.upm.dit.gsi.shanks.wsn.utils.Edge;
import es.upm.dit.gsi.shanks.wsn.utils.Vertex;

/**
 * Project: wsn File: es.upm.dit.gsi.shanks.wsn.model.scenario.WSNScenario.java
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
public class WSNScenario extends Scenario {

	public static final String SENSORS = "Sensors";
	public static final String FIELD_WIDTH = "FieldWidth";
	public static final String FIELD_HEIGHT = "FieldHeight";
	public static final String CLUSTERS = "Clusters";

	private int sensorsNum;
	private int height;
	private int width;
	private List<SensorNode> clusterheads;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param initialState
	 * @param properties
	 * @param logger
	 * @throws ShanksException
	 */
	public WSNScenario(String id, String initialState, Properties properties, Logger logger) throws ShanksException {
		super(id, initialState, properties, logger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.scenario.Scenario#createScenario2DPortrayal()
	 */
	@Override
	public Scenario2DPortrayal createScenario2DPortrayal() throws ShanksException {
		return new WSNScenario2DPortrayal(this, this.width, this.height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.scenario.Scenario#createScenario3DPortrayal()
	 */
	@Override
	public Scenario3DPortrayal createScenario3DPortrayal() throws ShanksException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#setPossibleStates()
	 */
	@Override
	public void setPossibleStates() {
		for (WSNScenarioStates state : WSNScenarioStates.values()) {
			this.addPossibleStatus(state.toString());
		}
		// this.addPossibleStatus("SUNNY");
		// this.addPossibleStatus("CLOUDY");
		// this.addPossibleStatus("RAINY");
		// this.addPossibleStatus("STORM");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#addNetworkElements()
	 */
	@Override
	public void addNetworkElements() throws ShanksException {
		Logger logger = this.getLogger();
		Random rnd = new Random();

		// Read properties
		Properties properties = this.getProperties();
		String sensorsString = properties.getProperty(SENSORS);
		this.sensorsNum = new Integer(sensorsString);
		String fieldWidth = properties.getProperty(FIELD_WIDTH);
		this.width = new Integer(fieldWidth);
		String fieldHeight = properties.getProperty(FIELD_HEIGHT);
		this.height = new Integer(fieldHeight);
		String clustersString = properties.getProperty(CLUSTERS);
		int clusters = new Integer(clustersString);

		Double2D pos = new Double2D(width / 2, -50);
		BaseStation base = new BaseStation("base-station", "OK", false, logger, pos);
		this.addNetworkElement(base);

		List<SensorNode> sensors = new ArrayList<SensorNode>();

		// Create sensors nodes
		for (int i = 0; i < sensorsNum; i++) {
			pos = new Double2D(rnd.nextInt(width), rnd.nextInt(height));
			SensorNode node = new SensorNode("sensor-" + i, "OK", false, logger, pos);
			sensors.add(node);
			this.addNetworkElement(node);
		}

		// Choose cluster heads
		List<SensorNode> heads = new ArrayList<SensorNode>();
		while (heads.size() < clusters) {
			int aux = rnd.nextInt(this.sensorsNum);
			SensorNode node = (SensorNode) this.getNetworkElement("sensor-" + aux);
			node.setClusterHead(true);
			heads.add(node);
		}

		this.setClusterheads(heads);

		List<Vertex> vertexs = new ArrayList<Vertex>();
		Vertex baseVertex = new Vertex("base-station");
		vertexs.add(baseVertex);
		for (SensorNode head : heads) {
			vertexs.add(new Vertex(head.getID()));
		}
		for (Vertex vertex : vertexs) {
			Edge[] edges = new Edge[vertexs.size() - 1];
			int i = 0;
			for (Vertex vertex2 : vertexs) {
				if (vertex != vertex2) {
					double dist = this.getEffectiveDistance(vertex, vertex2);
					edges[i++] = new Edge(vertex2, dist);
				}
			}
			vertex.adjacencies = edges;
		}
		Dijkstra.computePaths(baseVertex);

		for (Vertex v : vertexs) {
			this.getLogger().finer("Distance to " + v + ": " + v.minDistance);
			List<Vertex> path = Dijkstra.getShortestPathTo(v);

			int pathSize = path.size();
			for (int i = 0; i < pathSize - 1; i++) {
				Vertex v1 = path.get(i);
				Vertex v2 = path.get(i + 1);
				this.createRoutePathLink(v1, v2);
			}

			this.getLogger().finer("Path: " + path);
		}

		// Add wifi links
		for (SensorNode sensor : sensors) {
			if (!heads.contains(sensor)) {
				WifiLink wifiLink = new WifiLink("wifi-" + sensor.getID(), "OK", 2, this.getLogger());
				SensorNode head = this.getClusterHead(sensor, heads);
				wifiLink.connectDevices(sensor, head);
				this.addNetworkElement(wifiLink);
			}
		}

	}

	/**
	 * @param sensor
	 * @param heads
	 * @return
	 */
	private SensorNode getClusterHead(SensorNode sensor, List<SensorNode> heads) {
		Double2D sensorPos = sensor.getPosition();
		double[] poss = new double[heads.size()];
		for (int i = 0; i < heads.size(); i++) {
			poss[i] = sensorPos.distance(heads.get(i).getPosition());
		}
		int minPos = Integer.MAX_VALUE;
		// int minPos2 = Integer.MAX_VALUE;
		double minValue = Double.MAX_VALUE;
		for (int i = 0; i < heads.size(); i++) {
			if (minValue > poss[i]) {
				minValue = poss[i];
				// minPos2 = minPos;
				minPos = i;
			}
		}
		// if (minPos == 0) {
		// minValue = Double.MAX_VALUE;
		// for (int i = 1; i < heads.size(); i++) {
		// if (minValue > poss[i]) {
		// minValue = poss[i];
		// minPos2 = i;
		// }
		// }
		// }

		SensorNode candidate1 = heads.get(minPos);
		// SensorNode candidate2 = heads.get(minPos2);
		// int cluster1size = candidate1.getLinks().size();
		// int cluster2size = candidate2.getLinks().size();
		// if (cluster2size != 0 && cluster1size / cluster2size >= 2) {
		// return candidate2;
		// } else {
		return candidate1;
		// }
	}

	/**
	 * @param v2
	 * @param v1
	 * @throws ShanksException
	 * 
	 */
	private void createRoutePathLink(Vertex v1, Vertex v2) throws ShanksException {
		// Draw links
		String linkName = "path-" + v1.toString() + "-" + v2.toString();
		if (this.getNetworkElement(linkName) == null) {
			RoutePathLink link = new RoutePathLink(linkName, "OK", 2, this.getLogger());
			Device device1 = (Device) this.getNetworkElement(v1.toString());
			Device device2 = (Device) this.getNetworkElement(v2.toString());
			link.connectDevices(device1, device2);
			this.addNetworkElement(link);
		}
	}

	/**
	 * @param vertex
	 * @param vertex2
	 * @return
	 * @throws ShanksException
	 */
	private double getEffectiveDistance(Vertex vertex, Vertex vertex2) throws ShanksException {
		Double2D pos1 = this.getLocation(vertex.toString());
		Double2D pos2 = this.getLocation(vertex2.toString());
		double distance = pos1.distance(pos2);
		return Math.pow(distance, 2);
		// return distance;
	}

	/**
	 * @param string
	 * @return
	 * @throws ShanksException
	 */
	private Double2D getLocation(String id) throws ShanksException {
		try {
			SensorNode node = (SensorNode) this.getNetworkElement(id);
			return node.getPosition();
		} catch (Exception e) {
			try {
				BaseStation node = (BaseStation) this.getNetworkElement(id);
				return node.getPosition();
			} catch (Exception e2) {
				this.getLogger().warning("Impossible to get location of device: " + id + " -> " + e2.getMessage());
				throw new ShanksException(e.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#addPossibleFailures()
	 */
	@Override
	public void addPossibleFailures() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#addPossibleEvents()
	 */
	@Override
	public void addPossibleEvents() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.scenario.Scenario#getPenaltiesInStatus(java
	 * .lang.String)
	 */
	@Override
	public HashMap<Class<? extends Failure>, Double> getPenaltiesInStatus(String status) throws ShanksException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the clusterheads
	 */
	public List<SensorNode> getClusterheads() {
		return clusterheads;
	}

	/**
	 * @param clusterheads
	 *            the clusterheads to set
	 */
	public void setClusterheads(List<SensorNode> clusterheads) {
		this.clusterheads = clusterheads;
	}

}
