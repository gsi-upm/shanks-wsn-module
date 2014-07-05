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
import es.upm.dit.gsi.shanks.agent.capability.movement.ShanksAgentMovementCapability;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.event.failiure.Failure;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario3DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeCoordinatorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.link.RoutePathLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.WifiLink;
import es.upm.dit.gsi.shanks.wsn.model.scenario.portrayal.WSNScenario2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.utils.dijkstra.Dijkstra;
import es.upm.dit.gsi.shanks.wsn.utils.dijkstra.Edge;
import es.upm.dit.gsi.shanks.wsn.utils.dijkstra.Vertex;

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
	public static final String PERCEPTION_RANGE = "PerceptionRange";
	public static final String TARGET_SPEED = "Speed";
	public static final String TARGETS = "Targets";

	private int sensorsNum;
	private int height;
	private int width;
	private List<ZigBeeSensorNode> clusterheads;

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

		// Base Station on the top
		// Double2D pos = new Double2D(width / 2, 0);

		// Base Station in the middle
		Double2D pos = new Double2D(width / 2, height / 2);

		ZigBeeCoordinatorNode base = new ZigBeeCoordinatorNode("base-station", "OK", false, logger, pos);
		this.addNetworkElement(base);

		List<ZigBeeSensorNode> heads = new ArrayList<ZigBeeSensorNode>();
		List<ZigBeeSensorNode> sensors = new ArrayList<ZigBeeSensorNode>();

		int rangeRadioDistance = 0;
		Double2D orig = new Double2D(0, 0);
		boolean inRange = true;
		while (inRange == true) {
			Double2D p = new Double2D(0, ++rangeRadioDistance);
			double d = this.getPathCost(orig, p);
			if (d == Double.MAX_VALUE) {
				rangeRadioDistance = rangeRadioDistance - 5;
				inRange = false;
			}
		}

		// Create sensors nodes
		boolean[][] map = new boolean[width][height];
		for (int i = 0; i < sensorsNum; i++) {
			int w;
			int h;
			do {
				w = rnd.nextInt(width);
				h = rnd.nextInt(height);
				if (map[w][h] == false) {
					pos = new Double2D(w, h);
					map[w][h] = true;
				}
			} while (map[h][w] == true);

			ZigBeeSensorNode node = new ZigBeeSensorNode("sensor-" + i, "OK", false, logger, pos);
			sensors.add(node);
			this.addNetworkElement(node);
		}

		// Choose cluster heads/ zigbee routers
		while (heads.size() < clusters) {
			int aux = rnd.nextInt(this.sensorsNum);
			ZigBeeSensorNode node = (ZigBeeSensorNode) this.getNetworkElement("sensor-" + aux);
			while (heads.contains(node)) {
				aux = rnd.nextInt(this.sensorsNum);
				node = (ZigBeeSensorNode) this.getNetworkElement("sensor-" + aux);
			}
			node.setZigBeeRouter(true);
			this.moveInRange(node, heads, base, rangeRadioDistance);
			heads.add(node);
		}

		this.setClusterheads(heads);

		List<Vertex> vertexs = new ArrayList<Vertex>();
		Vertex baseVertex = new Vertex("base-station");
		vertexs.add(baseVertex);
		for (ZigBeeSensorNode head : heads) {
			vertexs.add(new Vertex(head.getID()));
		}
		for (Vertex vertex : vertexs) {
			List<Edge> list = new ArrayList<Edge>();
			for (Vertex vertex2 : vertexs) {
				if (vertex != vertex2) {
					Double2D pos1 = this.getLocation(vertex.toString());
					Double2D pos2 = this.getLocation(vertex2.toString());
					double dist = this.getPathCost(pos1, pos2);
					if (dist < Double.MAX_VALUE) {
						Edge edge = new Edge(vertex2, dist);
						list.add(edge);
					}
				}
			}

			Edge[] edges = new Edge[list.size()];
			for (int i = 0; i < list.size(); i++) {
				edges[i] = list.get(i);
			}
			vertex.adjacencies = edges;
		}
		Dijkstra.computePaths(baseVertex);

		for (Vertex v : vertexs) {
			List<Vertex> path = Dijkstra.getShortestPathTo(v);

			int pathSize = path.size();
			for (int i = 0; i < pathSize - 1; i++) {
				Vertex v1 = path.get(i);
				Vertex v2 = path.get(i + 1);
				this.createRoutePathLink(v1, v2);
				// TODO review this - no se si el path esta en el sentido
				// correcto
			}

			this.getLogger().finer("Path: " + path);
		}
		this.getLogger().finer("Routing Links created. Starting creation of wifi links...");

		// Add wifi links

		// TODO balancear clusters para que no sean tan diferentes
		
		for (ZigBeeSensorNode sensor : sensors) {
			if (!heads.contains(sensor)) {
				ZigBeeSensorNode head = this.getClusterHead(sensor, heads);
				if (head == null) {
					this.moveInRange(sensor, heads, null, rangeRadioDistance);
					head = this.getClusterHead(sensor, heads);
				}
				WifiLink wifiLink = new WifiLink("wifi-" + sensor.getID(), "OK", 2, this.getLogger());
				wifiLink.connectDevices(sensor, head);
				sensor.setPath2Sink(wifiLink);
				this.addNetworkElement(wifiLink);
			}
		}
		this.getLogger().finer("All Wifi links created for all clusters.");

	}
	/**
	 * @param node
	 * @param heads
	 * @param base
	 * @param rangeRadioDistance
	 * @throws ShanksException
	 */
	private void moveInRange(ZigBeeSensorNode node, List<ZigBeeSensorNode> heads, ZigBeeCoordinatorNode base,
			int rangeRadioDistance) throws ShanksException {
		Double2D closestPos = this.getClosestNode(node, heads, base);
		Double2D originalPos = node.getPosition();
		double speed = rangeRadioDistance / 5;
		Double2D currentPos = node.getPosition();
		double distance = currentPos.distance(closestPos);

		while (distance > rangeRadioDistance) {
			Double2D direction = closestPos.subtract(currentPos);
			direction = direction.normalize();
			Double2D movement = direction.multiply(speed);
			Double2D newPos = currentPos.add(movement);
			currentPos = newPos;
			distance = currentPos.distance(closestPos);
		}
		this.getLogger().finest(
				"Sensor moved: " + node.getID() + " -> Orignal Pos: " + originalPos.toString() + " / Final Pos: "
						+ currentPos.toString() + " / Target Pos: " + closestPos.toString());
		node.setPosition(currentPos);

	}

	/**
	 * @param node
	 * @param heads
	 * @param base
	 * @return
	 * @throws ShanksException
	 */
	private Double2D getClosestNode(ZigBeeSensorNode node, List<ZigBeeSensorNode> heads, ZigBeeCoordinatorNode base)
			throws ShanksException {
		Double2D closestPos = null;
		double minDistance = Double.MAX_VALUE;
		if (base != null) {
			double distance = node.getPosition().distance(base.getPosition());
			if (minDistance > distance) {
				minDistance = distance;
				closestPos = base.getPosition();
			}
		}

		for (ZigBeeSensorNode head : heads) {
			double distance = node.getPosition().distance(head.getPosition());
			if (minDistance > distance) {
				minDistance = distance;
				closestPos = head.getPosition();
			}
		}
		return closestPos;
	}

	/**
	 * @param sensor
	 * @param heads
	 * @return
	 * @throws ShanksException
	 */
	private ZigBeeSensorNode getClusterHead(ZigBeeSensorNode sensor, List<ZigBeeSensorNode> heads)
			throws ShanksException {
		Double2D sensorPos = sensor.getPosition();
		double[] consumptions = new double[heads.size()];
		for (int i = 0; i < heads.size(); i++) {
			consumptions[i] = this.getPathCost(sensorPos, heads.get(i).getPosition());
		}
		int minPos = Integer.MAX_VALUE;
		double minConsumption = Double.MAX_VALUE;
		for (int i = 0; i < heads.size(); i++) {
			if (minConsumption > consumptions[i]) {
				minConsumption = consumptions[i];
				minPos = i;
			}
		}

		if (minPos == Integer.MAX_VALUE && minConsumption == Double.MAX_VALUE) {
			return null;
		} else {
			ZigBeeSensorNode head = heads.get(minPos);
			return head;
		}
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
			ZigBeeSensorNode node = (ZigBeeSensorNode) device2;
			node.setPath2Sink(link);
			this.addNetworkElement(link);
		}
	}

	/**
	 * @param pos1
	 * @param pos2
	 * @return
	 * @throws ShanksException
	 */
	private double getPathCost(Double2D pos1, Double2D pos2) throws ShanksException {

		double distance = pos1.distance(pos2);
		double distanceKm = distance / 1000;
		double loss = 100 + (20 * Math.log10(distanceKm)); // in dB
		// TODO add these parameters configurable with properties
		// TODO noise, sensitivity and cpu
		double noiseIndoor = 26; // Aprox. value. in dB value indoor
		// double noiseIndoor = 10; // Aprox. value. in dB value outdoor
		double sensitivy = -90; // Sensitivity in dBm
		// Emission power required to ensure the reception (in dBm)
		double emisionPower = sensitivy + loss + noiseIndoor;
		double emissionConsumption = Double.MAX_VALUE;

		// String packet = "CPU:80/MEM:50/TMP:50/D:Y/S100:S101:S102:S103:S104";
		// String packet = "CPU:80/MEM:50/TMP:50/D:Y/S100:S101:S102";
		// byte[] asciiBytes;
		// try {
		// asciiBytes = packet.getBytes("ASCII");
		// this.getLogger().severe("Bytes: " + asciiBytes.length); // prints
		// "12"
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		// Measure the time of processing and resend packages to make this
		// calculus more real
		// Efficient data throughput: 108kbps = 13.5KB/s
		// Avg 40Bytes/package = 320bits/package
		// t = 320 / 108000 = 2.96ms (aprox. 3ms)
		double t = 3.0 / 1000.0;

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

		if (emissionConsumption < Double.MAX_VALUE) {
			double receptionConsumption = 19.7; // in mA
			double cpuConsumption = 8; // in mA
			double totalConsumption = emissionConsumption + receptionConsumption + cpuConsumption; // mA/s
			double totalConsumptionPerPackage = totalConsumption * t;
			// double totalConsumptionPerPackage = totalConsumption -
			// cpuConsumption;
			return totalConsumptionPerPackage;
		} else {
			return Double.MAX_VALUE;
		}
	}

	/**
	 * @param string
	 * @return
	 * @throws ShanksException
	 */
	private Double2D getLocation(String id) throws ShanksException {
		try {
			ZigBeeSensorNode node = (ZigBeeSensorNode) this.getNetworkElement(id);
			return node.getPosition();
		} catch (Exception e) {
			try {
				ZigBeeCoordinatorNode node = (ZigBeeCoordinatorNode) this.getNetworkElement(id);
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
	public List<ZigBeeSensorNode> getClusterheads() {
		return clusterheads;
	}

	/**
	 * @param clusterheads
	 *            the clusterheads to set
	 */
	public void setClusterheads(List<ZigBeeSensorNode> clusterheads) {
		this.clusterheads = clusterheads;
	}

}
