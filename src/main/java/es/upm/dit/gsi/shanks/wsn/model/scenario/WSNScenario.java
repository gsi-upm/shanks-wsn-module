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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.NetworkElement;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.event.failure.Failure;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario3DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.Battery;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.link.RoutePathLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.SensorLink;
import es.upm.dit.gsi.shanks.wsn.model.failure.BatteryHardwareFailure;
import es.upm.dit.gsi.shanks.wsn.model.failure.CpuHardwareFailure;
import es.upm.dit.gsi.shanks.wsn.model.failure.ExternalDamageFailure;
import es.upm.dit.gsi.shanks.wsn.model.failure.MemoryHardwareFailure;
import es.upm.dit.gsi.shanks.wsn.model.failure.SensorHardwareFailure;
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
	public static final String STEP_TIME = "StepTime";
	public static final String TARGETS = "Targets";
	public static final String RANDOM_SEED = "Seed";
	public static final String SENSOR_WIFI_RANGE_PERCENTAGE = "SensorRangePctg";
	public static final String MAX_NOISE_IN_DB = "MaxNoiseInDB";
	public static final String MIN_NOISE_IN_DB = "MinNoiseInDB";
	public static final String TOPOLOGY_RULES_FILE = "TopologyRulesFile";
	public static final String ONTOLOGY_URI = "OntologyURI";
	// TODO add parameters for consumption

	private int sensorsNum;
	private int height;
	private int width;
	private List<ZigBeeSensorNode> clusterheads;
	private int maxNoiseInDB;

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#addNetworkElements()
	 */
	@Override
	public void addNetworkElements() throws ShanksException {
		Logger logger = this.getLogger();

		// Read properties
		Properties properties = this.getProperties();
		String seedString = properties.getProperty(WSNScenario.RANDOM_SEED);
		long seed = new Integer(seedString);
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);
		String sensorsString = properties.getProperty(SENSORS);
		this.sensorsNum = new Integer(sensorsString);
		String fieldWidth = properties.getProperty(FIELD_WIDTH);
		this.width = new Integer(fieldWidth);
		String fieldHeight = properties.getProperty(FIELD_HEIGHT);
		this.height = new Integer(fieldHeight);
		String clustersString = properties.getProperty(CLUSTERS);
		int clusters = new Integer(clustersString);
		String sensorRangeString = properties.getProperty(SENSOR_WIFI_RANGE_PERCENTAGE);
		double sensorRange = new Double(sensorRangeString);
		String noiseString = properties.getProperty(MAX_NOISE_IN_DB);
		this.maxNoiseInDB = new Integer(noiseString);

		// Base Station on the top
		// Double2D pos = new Double2D(width / 2, 0);

		// Base Station in the middle
		Double2D pos = new Double2D(width / 2, height / 2);

		Battery battery = Battery.getInfiniteBattery();
		ZigBeeSensorNode base = new ZigBeeSensorNode("base-station", ZigBeeSensorNode.OK_READY, true, logger, pos,
				battery, rnd);
		this.addNetworkElement(base);

		List<ZigBeeSensorNode> heads = new ArrayList<ZigBeeSensorNode>();
		List<ZigBeeSensorNode> sensors = new ArrayList<ZigBeeSensorNode>();

		// Look for the radio range with the given noise level
		int rangeRadioDistance = 0;
		Double2D orig = new Double2D(0, 0);
		ZigBeeSensorNode auxNode = new ZigBeeSensorNode("aux", ZigBeeSensorNode.OK_READY, false, logger, orig, battery,
				rnd);
		boolean inRange = true;
		while (inRange == true) {
			Double2D p = new Double2D(0, ++rangeRadioDistance);
			ZigBeeSensorNode auxNode2 = new ZigBeeSensorNode("aux2", ZigBeeSensorNode.OK_READY, false, logger, p,
					battery, rnd);
			double d = this.getPathCost(auxNode, auxNode2);
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
			battery = Battery.get2AABatteryAlkaline();
			ZigBeeSensorNode node = new ZigBeeSensorNode("sensor-" + i, ZigBeeSensorNode.OK_READY, false, logger, pos,
					battery, rnd);
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
					ZigBeeSensorNode node = (ZigBeeSensorNode) this.getNetworkElement(vertex.toString());
					ZigBeeSensorNode node2 = (ZigBeeSensorNode) this.getNetworkElement(vertex2.toString());
					double dist = this.getPathCost(node, node2);
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
			}

			this.getLogger().finer("Path: " + path);
		}
		this.getLogger().finer("Routing Links created. Starting creation of wifi links...");

		// Add wifi links
		int clusterSize = (int) (sensorsNum - clusters) / clusters;
		int remaining = (int) (sensorsNum - clusters) % clusters;

		List<ZigBeeSensorNode> pendingSensors = new ArrayList<ZigBeeSensorNode>();
		pendingSensors.addAll(sensors);
		pendingSensors.removeAll(heads);
		int sensorCounter = 0;
		int effectiveRangeDistance = (int) (rangeRadioDistance * sensorRange);
		for (int i = 0; i < clusterSize; i++) {
			for (int j = 0; j < clusters; j++) {
				ZigBeeSensorNode head = heads.get(j);
				ZigBeeSensorNode sensor = this.getCloserSensor(head, pendingSensors);
				List<ZigBeeSensorNode> hlist = new ArrayList<ZigBeeSensorNode>();
				hlist.add(head);
				this.moveInRange(sensor, hlist, null, effectiveRangeDistance);
				head = this.getClusterHead(sensor, heads);
				SensorLink wifiLink = new SensorLink("wifi-" + sensor.getID(), SensorLink.OK_READY, 2, this.getLogger());
				wifiLink.connectDevices(sensor, head);
				sensor.setPath2Sink(wifiLink);
				this.addNetworkElement(wifiLink);
			}
		}

		if (sensorCounter < sensorsNum) {
			for (int i = 0; i < remaining; i++) {
				ZigBeeSensorNode head = heads.get(rnd.nextInt(clusters));
				ZigBeeSensorNode sensor = this.getCloserSensor(head, pendingSensors);
				List<ZigBeeSensorNode> hlist = new ArrayList<ZigBeeSensorNode>();
				hlist.add(head);
				this.moveInRange(sensor, hlist, null, effectiveRangeDistance);
				head = this.getClusterHead(sensor, heads);
				SensorLink wifiLink = new SensorLink("wifi-" + sensor.getID(), SensorLink.OK_READY, 2, this.getLogger());
				wifiLink.connectDevices(sensor, head);
				sensor.setPath2Sink(wifiLink);
				this.addNetworkElement(wifiLink);
			}
		}

		this.getLogger().finer("All Wifi links created for all clusters.");

	}
	/**
	 * @param head
	 * @param pendingSensors
	 * @return
	 */
	private ZigBeeSensorNode getCloserSensor(ZigBeeSensorNode head, List<ZigBeeSensorNode> pendingSensors) {
		ZigBeeSensorNode candidate = null;
		double minDistance = Double.MAX_VALUE;
		for (ZigBeeSensorNode node : pendingSensors) {
			double d = node.getPosition().distance(head.getPosition());
			if (d < minDistance) {
				minDistance = d;
				candidate = node;
			}
		}
		pendingSensors.remove(candidate);
		return candidate;
	}

	/**
	 * @param node
	 * @param heads
	 * @param base
	 * @param rangeRadioDistance
	 * @throws ShanksException
	 */
	private void moveInRange(ZigBeeSensorNode node, List<ZigBeeSensorNode> heads, ZigBeeSensorNode base,
			int rangeRadioDistance) throws ShanksException {
		Double2D closestPos = this.getClosestNodePosition(node, heads, base);
		Double2D originalPos = node.getPosition();
		double speed = ((double) rangeRadioDistance) / 5;
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
	private Double2D getClosestNodePosition(ZigBeeSensorNode node, List<ZigBeeSensorNode> heads, ZigBeeSensorNode base)
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
		double[] consumptions = new double[heads.size()];
		for (int i = 0; i < heads.size(); i++) {
			consumptions[i] = this.getPathCost(sensor, heads.get(i));
		}
		int minPos = Integer.MAX_VALUE;
		double minConsumption = Double.MAX_VALUE;
		for (int i = 0; i < heads.size(); i++) {
			if (minConsumption > consumptions[i]) {
				minConsumption = consumptions[i];
				minPos = i;
			}
		}

		List<Integer> eq = new ArrayList<Integer>();
		for (int i = 0; i < consumptions.length; i++) {
			if (minConsumption == consumptions[i]) {
				eq.add(i);
			}
		}
		if (eq.size() > 1) {
			double minDistance = Double.MAX_VALUE;
			for (Integer e : eq) {
				double dist = sensor.getPosition().distance(heads.get(e).getPosition());
				if (minDistance > dist) {
					minDistance = dist;
					minPos = e;
				}
			}
			return heads.get(minPos);
		} else if (minPos == Integer.MAX_VALUE && minConsumption == Double.MAX_VALUE) {
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
			RoutePathLink link = new RoutePathLink(linkName, RoutePathLink.OK_READY, 2, this.getLogger());
			Device device1 = (Device) this.getNetworkElement(v1.toString());
			Device device2 = (Device) this.getNetworkElement(v2.toString());
			link.connectDevices(device1, device2);
			ZigBeeSensorNode node = (ZigBeeSensorNode) device2;
			node.setPath2Sink(link);
			this.addNetworkElement(link);
		}
	}

	/**
	 * @param source
	 * @param destination
	 * @return
	 * @throws ShanksException
	 */
	private double getPathCost(ZigBeeSensorNode source, ZigBeeSensorNode destination) throws ShanksException {

		double emissionConsumption = source.getRequiredCurrentForEmissionToNode(destination, this.maxNoiseInDB);

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
		/**
		 * Measure the time of processing and resend packages to make this
		 * calculus more real Efficient data throughput: 108kbps = 13.5KB/s Avg
		 * 40Bytes/package = 320bits/package t = 320 / 108000 = 2.96ms (aprox.
		 * 3ms)
		 */
		double t = 3.0 / 1000.0;
		if (emissionConsumption < Double.MAX_VALUE) {
			double receptionConsumption = 19.7; // in mA
			double cpuConsumption = 8; // in mA
			double totalConsumption = emissionConsumption + receptionConsumption + cpuConsumption; // mA/s
			double totalConsumptionPerPackage = totalConsumption * t;
			return totalConsumptionPerPackage;
		} else {
			return Double.MAX_VALUE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#addPossibleFailures()
	 */
	@Override
	public void addPossibleFailures() {
		HashMap<String, NetworkElement> elements = this.getCurrentElements();
		List<Set<NetworkElement>> nodeSetsList = new ArrayList<Set<NetworkElement>>();
		for (Entry<String, NetworkElement> entry : elements.entrySet()) {
			NetworkElement element = entry.getValue();
			if (element.getClass().equals(ZigBeeSensorNode.class) && !element.getID().startsWith("base-station")) {
				Set<NetworkElement> nodeSet = new HashSet<NetworkElement>();
				nodeSet.add(element);
				nodeSetsList.add(nodeSet);
			}
		}

		this.addPossibleFailure(CpuHardwareFailure.class, nodeSetsList);
		this.addPossibleFailure(BatteryHardwareFailure.class, nodeSetsList);
		this.addPossibleFailure(MemoryHardwareFailure.class, nodeSetsList);
		this.addPossibleFailure(SensorHardwareFailure.class, nodeSetsList);
		this.addPossibleFailure(ExternalDamageFailure.class, nodeSetsList);
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
