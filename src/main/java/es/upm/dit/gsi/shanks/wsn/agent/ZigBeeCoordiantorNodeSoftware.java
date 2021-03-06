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

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

import sim.util.Double2D;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;

import ec.util.MersenneTwisterFast;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.NetworkElement;
import es.upm.dit.gsi.shanks.model.element.link.Link;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.link.RoutePathLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.SensorLink;
import es.upm.dit.gsi.shanks.wsn.model.scenario.WSNScenario;

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

	private ZigBeeSensorNode hardware;

	private ShanksSimulation sim;

	private OntModel model;

	private Individual baseStationB2D2Agent;

	private double minNoise;

	private double maxNoise;

	private MersenneTwisterFast random;

	private int sensitivity = -90; // -90dBm

	private HashMap<String, Long> lastKnownMsgs;

	private List<Individual> incomingMsgs;

	private HashMap<Long, Area> targetZones;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 * @param maxNoise
	 * @param minNoise
	 * @param random
	 * @param hardware
	 * @param simulation
	 * @throws ShanksException
	 */
	public ZigBeeCoordiantorNodeSoftware(String id, Logger logger, double maxNoise, double minNoise,
			MersenneTwisterFast random, ZigBeeSensorNode hardware, ShanksSimulation simulation) throws ShanksException {
		super(id, logger);
		this.hardware = hardware;
		this.hardware.setSoftware(this);
		this.sim = simulation;
		this.maxNoise = maxNoise;
		this.minNoise = minNoise;
		this.random = random;
		this.lastKnownMsgs = new HashMap<String, Long>();
		this.targetZones = new HashMap<Long, Area>();
		try {
			// Initialize system functions and templates
			SPINModuleRegistry.get().init();

			HashMap<String, NetworkElement> elements = sim.getScenario().getCurrentElements();
			String ontologyURI = (String) this.sim.getScenario().getProperties().get(WSNScenario.ONTOLOGY_URI);
			// Create individuals
			this.getLogger().info("Loading ontology from: " + ontologyURI);
			model = this.loadModelWithImports(ontologyURI);

			this.populateOntology(model, elements);

			model.setNsPrefix("nml-base", Vocabulary.nmlBaseNS);
			model.setNsPrefix("wsn-ndl", Vocabulary.wsnNdlNS);
			model.setNsPrefix("b2d2-wsn", Vocabulary.diagnosisNS);
			model.removeNsPrefix("");

			// SPIN RULES
			String rulesFile = (String) this.sim.getScenario().getProperties().get(WSNScenario.TOPOLOGY_RULES_FILE);
			this.loadSPINRules(rulesFile, model);

			SPINModuleRegistry.get().registerAll(model, null);

			this.getLogger().info("Size before inference: " + model.size());
			SPINInferences.run(model, model, null, null, false, null);
			this.getLogger().info("Size after inference: " + model.size());

			this.writeModelInFiles(model, sim);

		} catch (FileNotFoundException e) {
			this.getLogger().severe("File not found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			this.getLogger().severe("Problem saving model in OWL file.");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param model
	 * @throws IOException
	 */
	private void writeModelInFiles(OntModel model, ShanksSimulation sim) throws IOException {
		String simId = (String) sim.getScenario().getProperties().get("SimID");
		String outputFolder = (String) sim.getScenario().getProperties().get("OutputFolder");
		FileWriter fw = new FileWriter(outputFolder+"ndl-result-simID-"+simId+".rdf");
		model.write(fw);
		fw.close();

		// fw = new
		// FileWriter("/home/alvaro/TBCFreeWorkspace/wsn-ndl/result.rdf");
		// model.write(fw);
		// fw.close();
	}

	/**
	 * @param model
	 */
	private void performQueries(OntModel model, ShanksSimulation simulation) {

		String query;
		Query arqQuery;
		QueryExecution qExe;
		ResultSet results;

		// query =
		// "SELECT (COUNT(?node) AS ?clusterCount) WHERE { ?node a wsn-ndl:Cluster . }";
		// arqQuery = ARQFactory.get().createQuery(model, query);
		// qExe = QueryExecutionFactory.create(arqQuery, model);
		// results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
		/**
		 *
		 */
		// query =
		// "SELECT (?node AS ?clusters) WHERE { ?node a wsn-ndl:Cluster . }";
		// arqQuery = ARQFactory.get().createQuery(model, query);
		// qExe = QueryExecutionFactory.create(arqQuery, model);
		// results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
		/**
		 *
		 */
		// query =
		// "SELECT (COUNT(?node) AS ?wsnTopologyCount) WHERE { ?node a wsn-ndl:WSNTopology . }";
		// arqQuery = ARQFactory.get().createQuery(model, query);
		// qExe = QueryExecutionFactory.create(arqQuery, model);
		// results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
		/**
		 *
		 */
		// query =
		// "SELECT (COUNT(?node) AS ?router) WHERE { ?node a wsn-ndl:ZigBeeRouter . }";
		// arqQuery = ARQFactory.get().createQuery(model, query);
		// qExe = QueryExecutionFactory.create(arqQuery, model);
		// results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
		/**
		 *
		 */
		query = "SELECT (COUNT(?node) AS ?leafRouters) WHERE { ?node a wsn-ndl:ZigBeeLeafRouter . }";
		arqQuery = ARQFactory.get().createQuery(model, query);
		qExe = QueryExecutionFactory.create(arqQuery, model);
		results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
//		ResultSetFormatter.outputAsCSV(System.out, results);
		
		QuerySolution result = results.next();
		Literal lit = result.getLiteral("leafRouters");
		String leafRouters = lit.getString();
		
		/**
		 *
		 */
		query = "SELECT (COUNT(?node) AS ?message) WHERE { ?node a b2d2-wsn:ZigBeeMessage . }";
		arqQuery = ARQFactory.get().createQuery(model, query);
		qExe = QueryExecutionFactory.create(arqQuery, model);
		results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
//		ResultSetFormatter.outputAsCSV(System.out, results);
		

		result = results.next();
		lit = result.getLiteral("message");
		String messages = lit.getString();
		/**
		 *
		 */
		// query =
		// "SELECT (COUNT(?node) AS ?motionSensor) WHERE { ?node a b2d2-wsn:MotionSensor . }";
		// arqQuery = ARQFactory.get().createQuery(model, query);
		// qExe = QueryExecutionFactory.create(arqQuery, model);
		// results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
		/**
		 *
		 */
		// query =
		// "SELECT (COUNT(?msg) AS ?receivedByB2D2Agent) WHERE { ?agent a wsn-ndl:ZigBeeBaseStation . ?msg wsn-ndl:isReceivedBy ?agent . }";
		// arqQuery = ARQFactory.get().createQuery(model, query);
		// qExe = QueryExecutionFactory.create(arqQuery, model);
		// results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
		/**
		 *
		 */
//		query = "SELECT (COUNT(?msg) AS ?B2D2AgentReceives) WHERE { ?agent a wsn-ndl:ZigBeeBaseStation . ?agent wsn-ndl:receives ?msg . }";
//		arqQuery = ARQFactory.get().createQuery(model, query);
//		qExe = QueryExecutionFactory.create(arqQuery, model);
//		results = qExe.execSelect();
//		// ResultSetFormatter.out(System.out, results);
//		ResultSetFormatter.outputAsCSV(System.out, results);

		/**
		 *
		 */
		query = "SELECT (COUNT(?lostmsg) AS ?LostMessages) WHERE { ?lostmsg a wsn-ndl:LostMessage . }";
		arqQuery = ARQFactory.get().createQuery(model, query);
		qExe = QueryExecutionFactory.create(arqQuery, model);
		results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);
//		ResultSetFormatter.outputAsCSV(System.out, results);
		

		result = results.next();
		lit = result.getLiteral("LostMessages");
		String lostmessages = lit.getString();
		
		Properties props = simulation.getScenario().getProperties();
		props.put("lostMsgs", lostmessages);
		props.put("msgs", messages);
		props.put("leafRouters", leafRouters);
	}
	/**
	 * 
	 * Check reasoning inference (no spin rules infereces, only OWL inferences
	 * and restrictions)
	 * 
	 * @param model
	 */
	public void checkOntologyModelInconsistencies(OntModel model) {
		Reasoner reasoner = model.getReasoner();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, model);

		ValidityReport report = infmodel.validate();
		if (!report.isValid()) {
			this.getLogger().severe("Incosistent ontology -> isValid: " + report.isValid());
		} else {
			this.getLogger().severe("No incosistent ontology -> isValid: " + report.isValid());
		}
	}

	/**
	 * @param model
	 * @param elements
	 * @throws ShanksException
	 */
	private void populateOntology(OntModel model, HashMap<String, NetworkElement> elements) throws ShanksException {

		// Create OntModel with imports
		this.getLogger().info("Populating model...");

		// Create an individual for devices
		for (Entry<String, NetworkElement> entry : elements.entrySet()) {
			NetworkElement element = entry.getValue();
			if (element.getClass().equals(ZigBeeSensorNode.class) && element.getID().startsWith("sensor-")) {
				ZigBeeSensorNode device = (ZigBeeSensorNode) element;
				String elementid = device.getID();
				// Create sensor in ontology and all its properties
				Individual sensor = model.createIndividual(Vocabulary.wsnNdlNS + elementid, Vocabulary.MicaZ);
				sensor.addProperty(Vocabulary.name, model.createTypedLiteral(elementid));
				if (device.isZigBeeRouter()) {
					sensor.addProperty(Vocabulary.isRouter, model.createTypedLiteral(true));
				} else {
					sensor.addProperty(Vocabulary.isRouter, model.createTypedLiteral(false));
				}
				// Sensor location
				Individual location = model.createIndividual(Vocabulary.wsnNdlNS + "location-" + elementid,
						Vocabulary.Location);
				sensor.addProperty(Vocabulary.locatedAt, location);
				Double2D position = device.getPosition();
				location.addProperty(Vocabulary.longitude, model.createTypedLiteral((float) position.x));
				location.addProperty(Vocabulary.latitude, model.createTypedLiteral((float) position.y));

				// Sensor cpu, memory and transceiver
				Individual cpu = model.createIndividual(Vocabulary.wsnNdlNS + "cpu-" + elementid, Vocabulary.Cpu);
				Individual memory = model.createIndividual(Vocabulary.wsnNdlNS + "memory-" + elementid,
						Vocabulary.Memory);
				Individual transceiver = model.createIndividual(Vocabulary.wsnNdlNS + "transceiver-" + elementid,
						Vocabulary.Transceiver);
				Individual motionSensor = model.createIndividual(Vocabulary.wsnNdlNS + "sensor-" + elementid,
						Vocabulary.Motionsensor);

				sensor.addProperty(Vocabulary.hasComponent, cpu);
				sensor.addProperty(Vocabulary.hasCPU, cpu);
				sensor.addProperty(Vocabulary.hasComponent, memory);
				sensor.addProperty(Vocabulary.hasMemory, memory);
				sensor.addProperty(Vocabulary.hasComponent, transceiver);
				sensor.addProperty(Vocabulary.hasTransceiver, transceiver);
				sensor.addProperty(Vocabulary.hasComponent, motionSensor);
				sensor.addProperty(Vocabulary.hasSensor, motionSensor);

				// CPU properties
				cpu.addProperty(Vocabulary.cores, model.createTypedLiteral(1));
				cpu.addProperty(Vocabulary.cpuspeed, model.createTypedLiteral((float) 0.008));
				// Memory properties
				memory.addProperty(Vocabulary.size, model.createTypedLiteral((float) 0.00025));
				// Transceiver properties

				// Motion Sensor properties

			} else if (element.getClass().equals(ZigBeeSensorNode.class) && element.getID().startsWith("base-station")) {
				ZigBeeSensorNode device = (ZigBeeSensorNode) element;
				String elementid = device.getID();
				Individual baseStation = model
						.createIndividual(Vocabulary.wsnNdlNS + elementid, Vocabulary.BaseStation);
				baseStation.addProperty(Vocabulary.name, model.createTypedLiteral(elementid));

				this.baseStationB2D2Agent = baseStation;

				// BaseStation location
				Individual location = model.createIndividual(Vocabulary.wsnNdlNS + "location-" + elementid,
						Vocabulary.Location);
				baseStation.addProperty(Vocabulary.locatedAt, location);
				Double2D position = device.getPosition();
				location.addProperty(Vocabulary.longitude, model.createTypedLiteral((float) position.x));
				location.addProperty(Vocabulary.latitude, model.createTypedLiteral((float) position.y));
			}

		}

		// Create individuals for links
		for (Entry<String, NetworkElement> entry : elements.entrySet()) {
			NetworkElement element = entry.getValue();
			if (element.getClass().equals(RoutePathLink.class)) {
				RoutePathLink link = (RoutePathLink) element;
				String elementid = link.getID();
				Individual routerPathLink = model.createIndividual(Vocabulary.wsnNdlNS + elementid,
						Vocabulary.RoutePathLink);
				routerPathLink.addProperty(Vocabulary.name, model.createTypedLiteral(elementid));
				ZigBeeSensorNode node0 = (ZigBeeSensorNode) link.getLinkedDevices().get(0);
				ZigBeeSensorNode node1 = (ZigBeeSensorNode) link.getLinkedDevices().get(1);
				Link pathToSink0 = node0.getPath2sink();
				Link pathToSink1 = node1.getPath2sink();
				Resource resourceNode0 = model.getResource(Vocabulary.wsnNdlNS + node0.getID());
				Resource resourceNode1 = model.getResource(Vocabulary.wsnNdlNS + node1.getID());
				if (pathToSink0 != null && pathToSink0.equals(link)) {
					resourceNode0.addProperty(Vocabulary.isSource, routerPathLink);
					resourceNode1.addProperty(Vocabulary.isSink, routerPathLink);
				} else if (pathToSink1 != null && pathToSink1.equals(link)) {
					resourceNode0.addProperty(Vocabulary.isSink, routerPathLink);
					resourceNode1.addProperty(Vocabulary.isSource, routerPathLink);
				} else {
					String message = "Incoherent information in path2sink for nodes: " + node0.getID() + " and "
							+ node1.getID();
					this.getLogger().severe(message);
					throw new ShanksException(message);
				}
			} else if (element.getClass().equals(SensorLink.class)) {
				SensorLink link = (SensorLink) element;
				String elementid = link.getID();
				Individual sensorLink = model.createIndividual(Vocabulary.wsnNdlNS + elementid, Vocabulary.SensorLink);
				sensorLink.addProperty(Vocabulary.name, model.createTypedLiteral(elementid));
				ZigBeeSensorNode node0 = (ZigBeeSensorNode) link.getLinkedDevices().get(0);
				ZigBeeSensorNode node1 = (ZigBeeSensorNode) link.getLinkedDevices().get(1);
				Resource resourceNode0 = model.getResource(Vocabulary.wsnNdlNS + node0.getID());
				Resource resourceNode1 = model.getResource(Vocabulary.wsnNdlNS + node1.getID());
				if (node0.isZigBeeRouter()) {
					resourceNode0.addProperty(Vocabulary.isSink, sensorLink);
					resourceNode1.addProperty(Vocabulary.isSource, sensorLink);
				} else if (node1.isZigBeeRouter()) {
					resourceNode0.addProperty(Vocabulary.isSource, sensorLink);
					resourceNode1.addProperty(Vocabulary.isSink, sensorLink);
				} else {
					String message = "Incoherent information in path2sink for nodes: " + node0.getID() + " and "
							+ node1.getID();
					this.getLogger().severe(message);
					throw new ShanksException(message);
				}
			}
		}

		this.getLogger().info("All individuals created successfully...");
	}

	/**
	 * @param rulesFile
	 * @param model
	 * @throws FileNotFoundException
	 */
	private void loadSPINRules(String rulesFile, OntModel model) throws FileNotFoundException {

		this.getLogger().fine("---> loadSPINRules(): Starting to load rules from " + rulesFile);
		Scanner scanner = new Scanner(new File(rulesFile)).useDelimiter("\\n---\\n");

		int i = 0;
		while (scanner.hasNext()) {
			i++;
			String uri = scanner.next();
			String rule = scanner.next();
			Query arqQuery = ARQFactory.get().createQuery(model, rule);
			ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
			org.topbraid.spin.model.Query spinQuery = arq2SPIN.createQuery(arqQuery, null);
			Resource resource = model.getResource(uri);
			resource.addProperty((Property) SPIN.rule, spinQuery);
		}
		this.getLogger().info("<--- loadSPINRules(): " + i + " rules loaded from file: " + rulesFile);
	}

	/**
	 * @return
	 */
	private double getGaussianNoise() {
		double mean = (maxNoise + minNoise) / 2;
		double std = (maxNoise - minNoise) / 4;
		double gaussian = this.random.nextGaussian();
		double noise = (gaussian * std) + mean;
		return noise;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	@Override
	public void checkMail() {
		List<Message> inbox = this.getInbox();
		List<Message> lostMsgs = new ArrayList<Message>();

		for (Message msg : inbox) {

			// Check lost messages with real emitted power
			String msgId = msg.getMsgId();
			String[] msgIdparts = msgId.split(":");
			int power = Integer.parseInt(msgIdparts[3]);
			double noise = this.getGaussianNoise();

			String sensorId = msgIdparts[0];
			ZigBeeSensorNode sensor = (ZigBeeSensorNode) this.sim.getScenario().getCurrentElements().get(sensorId);

			Double2D pos1 = this.getHardware().getPosition();
			Double2D pos2 = sensor.getPosition();
			double distance = pos1.distance(pos2);
			double distanceKm = distance / 1000;
			double loss = 100 + (20 * Math.log10(distanceKm)); // in dB for
																// 2,4GHz
			double snr = (power - loss) - noise;
			if (snr < this.sensitivity) {
				lostMsgs.add(msg);
			}
		}
		if (lostMsgs.size() > 0 && inbox.size() > 0) {
			this.getLogger().info(this.getID() + "-> Ratio of lost messages: " + lostMsgs.size() + "/" + inbox.size());
		}
		inbox.removeAll(lostMsgs);

		// Process the remaining messages
		this.incomingMsgs = new ArrayList<Individual>();

		for (Message msg : inbox) {
			String lastContent = (String) msg.getPropCont();
			this.getLogger().fine("Content: " + lastContent);
			String lastId = msg.getMsgId();
			this.getLogger().fine("MsgID: " + lastId);
			String reply = msg.getInReplyTo();
			this.getLogger().fine("InReplyTo: " + reply);

			String[] contents = lastContent.split("&");
			String[] ids = reply.split("&");
			for (int i = 0; i < ids.length - 1; i++) {
				ids[i] = ids[i + 1];
			}
			ids[ids.length - 1] = lastId;

			Individual[] allMsgs = new Individual[ids.length];

			for (int i = 0; i < contents.length; i++) {

				String id = ids[i];
				String content = contents[i];

				String[] msgIdparts = id.split(":");
				String sensorId = msgIdparts[0];
				long step = Long.parseLong(msgIdparts[1]);
				int msgCounter = Integer.parseInt(msgIdparts[2]);
				int power = Integer.parseInt(msgIdparts[3]);

				String[] contentData = content.split("/");
				double cpu = Double.parseDouble(contentData[0].split(":")[1]);
				double mem = Double.parseDouble(contentData[1].split(":")[1]);
				double tmp = Double.parseDouble(contentData[2].split(":")[1]);
				String aux = contentData[3].split(":")[1];
				boolean detecting = false;
				if (aux.equals("T")) {
					detecting = true;
				}
				double bat = Double.parseDouble(contentData[4].split(":")[1]);

				// Create individual
				Individual sensor = model.getIndividual(Vocabulary.wsnNdlNS + sensorId);
				Individual ontMsg = model.createIndividual(Vocabulary.diagnosisNS + sensorId + "_msgId-" + msgCounter,
						Vocabulary.ZigBeeMessage);
				ontMsg.addProperty(Vocabulary.msgContent, model.createTypedLiteral(content));
				ontMsg.addProperty(Vocabulary.msgCounter, model.createTypedLiteral(msgCounter));
				ontMsg.addProperty(Vocabulary.msgID, model.createTypedLiteral(id));
				ontMsg.addProperty(Vocabulary.inReplyTo, model.createTypedLiteral(reply));
				ontMsg.addProperty(Vocabulary.emittedPower, model.createTypedLiteral(power));
				ontMsg.addProperty(Vocabulary.timemstamp, model.createTypedLiteral(step));
				ontMsg.addProperty(Vocabulary.isSentBy, sensor);
				ontMsg.addProperty(Vocabulary.cpuLoad, model.createTypedLiteral(cpu));
				ontMsg.addProperty(Vocabulary.memoryLoad, model.createTypedLiteral(mem));
				ontMsg.addProperty(Vocabulary.temp, model.createTypedLiteral(tmp));
				ontMsg.addProperty(Vocabulary.isDetecting, model.createTypedLiteral(detecting));
				ontMsg.addProperty(Vocabulary.batteryLevel, model.createTypedLiteral(bat));

				for (int j = 0; j < i; j++) {
					ontMsg.addProperty(Vocabulary.contains, allMsgs[j]);
					sensor.addProperty(Vocabulary.receives, allMsgs[j]);
					allMsgs[j].addProperty(Vocabulary.isReceivedBy, sensor);
				}

				this.baseStationB2D2Agent.addProperty(Vocabulary.receives, ontMsg);
				ontMsg.addProperty(Vocabulary.isReceivedBy, this.baseStationB2D2Agent);
				allMsgs[i] = ontMsg;

				this.incomingMsgs.add(ontMsg);
			}

		}

		inbox.clear();
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

		SPINInferences.run(model, model, null, null, false, null);

		// Monitoring actions

		List<Individual> lostMessagesDiscovered = this.discoverLostMessages(incomingMsgs);

		this.analyseLostMessages(lostMessagesDiscovered);

		// TODO estimateTargetZones se ha comentado porque daba un error
		this.estimateTargetZones(incomingMsgs, simulation);

		// TODO generate nodos sospechosos:
		// para false positives and false negatives
		// &
		// culpable de mensajes perdidos

		this.incomingMsgs.clear();

		// Diagnosis actions

		// TODO analizar historicos de cpu, temp y memoria

		// TODO crear o no un diagnóstico para razonar con la BN

		// This code snippet is for debugging with TBC
		// Perform some queries and get simple numeric results.

		String maxSteps = simulation.getScenario().getProperties().getProperty("MaxSteps");
		int steps = Integer.parseInt(maxSteps);

		if (simulation.getSchedule().getSteps() % steps == 0 && simulation.getSchedule().getSteps() != 0) {
			// this.performQueries(model);
			this.getLogger().info("Size before inference: " + model.size());
			SPINInferences.run(model, model, null, null, false, null);
			this.getLogger().info("Size after inference: " + model.size());
			try {
				this.writeModelInFiles(model,sim);
				this.performQueries(model, simulation);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (simulation.getSchedule().getSteps() % 500 == 0) {
			System.out.println("Step: " + simulation.getSchedule().getSteps());
		}
	}
	/**
	 * @param lostMessagesDiscovered
	 * 
	 */
	private void analyseLostMessages(List<Individual> lostMessagesDiscovered) {

		HashMap<String, Integer> lostMsgsBySender = new HashMap<String, Integer>();

		for (Individual lostMsg : lostMessagesDiscovered) {
			String sender = lostMsg.getProperty(Vocabulary.isSentBy).getProperty(Vocabulary.name).getString();
			if (!lostMsgsBySender.containsKey(sender)) {
				lostMsgsBySender.put(sender, 1);
			} else {
				int counter = lostMsgsBySender.get(sender);
				lostMsgsBySender.put(sender, counter + 1);
			}
		}

		// Map with sender - routers in path
		HashMap<String, List<Resource>> paths = new HashMap<String, List<Resource>>();

		for (String sender : lostMsgsBySender.keySet()) {
			// Get router by node
			String query = "SELECT (?rs as ?routers) " + "WHERE {" + " ?s a b2d2-wsn:MicaZ ." + " ?s nml-base:name "
					+ "\"" + sender + "\"^^xsd:string ." + " ?c a b2d2-wsn:Cluster ." + " ?c nml-base:hasNode ?s . "
					+ " ?r a b2d2-wsn:ZigBeeRouter ." + " ?c nml-base:hasNode ?r ."
					+ " ?p a b2d2-wsn:PathToBaseStation ." + " ?p nml-base:hasTopology ?c ."
					+ " ?p nml-base:hasNode ?rs ." + " ?rs a b2d2-wsn:ZigBeeRouter ." + " }";
			Query arqQuery = ARQFactory.get().createQuery(model, query);
			QueryExecution qExe = QueryExecutionFactory.create(arqQuery, model);
			ResultSet results = qExe.execSelect();

			this.getLogger().fine("Sender " + sender + " lostMsgs-> " + lostMsgsBySender.get(sender));

			List<Resource> routersInPath = new ArrayList<Resource>();
			while (results.hasNext()) {
				QuerySolution result_router = results.next();
				Resource path_router_resource = result_router.getResource("routers");
				if (!routersInPath.contains(path_router_resource)) {
					routersInPath.add(path_router_resource);
				}
			}
			// Add the path to explored paths list
			paths.put(sender, routersInPath);

		}

		// TODO analizar si tienen path común
		// 3: ver si desde ese router hay un path común hasta el base
		// station
		for (Entry<String, List<Resource>> entry : paths.entrySet()) {
			this.getLogger().warning(entry.toString());
		}

	}

	/**
	 * @param incomingMessages
	 * @param simulation
	 */
	private void estimateTargetZones(List<Individual> incomingMessages, ShanksSimulation simulation) {
		// Explore detecting sensors to know where is the target
		for (Individual msg : this.incomingMsgs) {
			// If any sensor is detecting, the target should be in that region.
			boolean detecting = msg.getProperty(Vocabulary.isDetecting).getBoolean();
			if (detecting) {
				Resource sensor = msg.getProperty(Vocabulary.isSentBy).getResource();
				String name = sensor.getProperty(Vocabulary.name).getString();
				Resource location = sensor.getProperty(Vocabulary.locatedAt).getResource();
				double x = location.getProperty(Vocabulary.longitude).getFloat();
				double y = location.getProperty(Vocabulary.latitude).getFloat();
				Resource motionSensor = sensor.getProperty(Vocabulary.hasSensor).getResource();
				double range = motionSensor.getProperty(Vocabulary.hasPerceptionRange).getDouble();

				/**
				 * The size of this rectangle is to ensure there is no false
				 * negative while intersection calculation. It should be: (x -
				 * (range), y - (range), range*2,range*2) But, the randomiser
				 * used in the sensor detecting code, it more or less sensitive.
				 * 
				 * This size is enough to ensure empty target zones are really
				 * empties.
				 */
				Rectangle2D rect = new Rectangle2D.Double(x - (range), y - (range), range * 2, range * 2);
				Area candidateZone = new Area(rect);

				long step = msg.getProperty(Vocabulary.timemstamp).getLong();
				this.getLogger().fine(
						"Calculating target zone with detecting message from " + name + " generated in step: " + step);
				if (this.targetZones.get(step) == null) {
					/**
					 * Is it required here to measure the speed and the possible
					 * next region? If that is done, we can detect false
					 * positive while no other sensor are detecting But it is
					 * really complex and it would be useful in a few cases...
					 */
					this.targetZones.put(step, candidateZone);
				} else {
					Area copy = (Area) this.targetZones.get(step).clone();
					copy.intersect(candidateZone);
					if (copy.isEmpty()) {
						/**
						 * This snippet works only if there is a unique target
						 * agent (1 targetAgent)
						 * 
						 * If there are more agents, it should be more
						 * targetZones hashmaps (one per agent)
						 */
						this.getLogger().warning(
								"Empty target zone! Warning of false positive -> hardware sensor possible!");
						/**
						 * This snippet looks for the damaged sensor
						 * 
						 * It explores messages to know if other close sensors
						 * detects the target in the same, the previous or the
						 * following steps and count the most common opinion.
						 */
						// 1st - Detect possible regions
						HashMap<Resource, Double2D> detectingSensors = new HashMap<Resource, Double2D>();
						String query = "SELECT (?x as ?msg) WHERE { ?x a b2d2-wsn:ZigBeeMessage . ?x b2d2-wsn:isDetecting true . ?x b2d2-wsn:timestamp "
								+ step + " . }";
						Query arqQuery = ARQFactory.get().createQuery(model, query);
						QueryExecution qExe = QueryExecutionFactory.create(arqQuery, model);
						ResultSet results = qExe.execSelect();
						while (results.hasNext()) {
							QuerySolution result = results.next();
							Resource detectingMessage = result.getResource("msg");
							Resource detectingSensor = detectingMessage.getProperty(Vocabulary.isSentBy).getResource();
							Resource detectingLocation = detectingSensor.getProperty(Vocabulary.locatedAt)
									.getResource();
							long detectingX = detectingLocation.getProperty(Vocabulary.longitude).getLong();
							long detectingY = detectingLocation.getProperty(Vocabulary.latitude).getLong();
							Double2D detectingPosition = new Double2D(detectingX, detectingY);
							detectingSensors.put(detectingSensor, detectingPosition);
						}

						// 2nd - Vote for a region
						HashMap<Resource, Double> distances = new HashMap<Resource, Double>();
						/**
						 * The window should have width enough to ensure other
						 * sensors detect something. But not too much to avoid
						 * conflicts... 10<=windowLength<=100
						 */
						int windowLength = 50;
						Resource farDistanceSensor = null;
						while (farDistanceSensor == null) {
							this.countVotesForRegions(windowLength, step, detectingSensors, distances);

							// 3rd - Pick the less popular choice avoiding ties
							double maxTotalDistance = Collections.max(distances.values());
							Resource provisionalFarDistanceSensor = null;
							boolean tieDetected = false;
							for (Entry<Resource, Double> entry : distances.entrySet()) {
								if (entry.getValue() == maxTotalDistance) {
									if (provisionalFarDistanceSensor == null) {
										provisionalFarDistanceSensor = entry.getKey();
									} else {
										tieDetected = true;
										break;
									}
								}
							}
							if (!tieDetected) {
								farDistanceSensor = provisionalFarDistanceSensor;
							} else {
								this.getLogger().warning("Tie detected! Increasing window length!");
								distances.clear();
								windowLength = windowLength + 5;
								if (windowLength > simulation.getSchedule().getSteps()) {
									// Impossible to do any metric...
									// Possible infinite loop here, mandatory ->
									// break the tie in anyway
									break;
								}
							}
						}
						// 4th - Generate a possible hardware sensor fault
						try {
							this.getLogger().info(
									"Possible hardware sensor problem for sensor: "
											+ farDistanceSensor.getProperty(Vocabulary.name).getString());
							// TODO create observation
							this.getLogger().warning("Ahora tienes que crear la observación");
						} catch (NullPointerException e) {
							this.getLogger().warning("Impossible to break the tie for a possible failure node.");
						}
					} else {
						this.targetZones.put(step, copy);
					}

				}

			}
		}
	}
	/**
	 * @param windowLength
	 * @param step
	 * @param detectingSensors
	 * @param distances
	 */
	private void countVotesForRegions(int windowLength, long step, HashMap<Resource, Double2D> detectingSensors,
			HashMap<Resource, Double> distances) {
		long initialTimestamp = step - windowLength / 2;
		long finalTimestamp = step + windowLength / 2;
		String query = "SELECT (?x as ?msg) WHERE { ?x a b2d2-wsn:ZigBeeMessage . ?x b2d2-wsn:isDetecting true . ?x b2d2-wsn:timestamp ?tmp FILTER (?tmp >= "
				+ initialTimestamp + " && ?tmp <= " + finalTimestamp + ") }";
		Query arqQuery = ARQFactory.get().createQuery(model, query);
		QueryExecution qExe = QueryExecutionFactory.create(arqQuery, model);
		ResultSet results = qExe.execSelect();
		List<String> alreadyVoted = new ArrayList<String>();
		while (results.hasNext()) {
			QuerySolution result = results.next();
			Resource resource = result.getResource("msg");
			String sensorName = resource.getProperty(Vocabulary.isSentBy).getProperty(Vocabulary.name).getString();
			if (!alreadyVoted.contains(sensorName)) {
				alreadyVoted.add(sensorName);
				Resource auxlocation = resource.getProperty(Vocabulary.isSentBy).getProperty(Vocabulary.locatedAt)
						.getResource();
				long auxX = auxlocation.getProperty(Vocabulary.longitude).getLong();
				long auxY = auxlocation.getProperty(Vocabulary.latitude).getLong();
				Double2D auxPosition = new Double2D(auxX, auxY);
				for (Entry<Resource, Double2D> entry : detectingSensors.entrySet()) {
					double distance = auxPosition.distance(entry.getValue());
					if (distances.containsKey(entry.getKey())) {
						double previous = distances.get(entry.getKey());
						distances.put(entry.getKey(), previous + distance);
					} else {
						distances.put(entry.getKey(), distance);
					}
				}
			}
		}
	}

	/**
	 * @param incomingMessages
	 * @return
	 */
	private List<Individual> discoverLostMessages(List<Individual> incomingMessages) {

		List<Individual> lostMessagesDiscovered = new ArrayList<Individual>();

		for (Individual msg : this.incomingMsgs) {
			long msgID = msg.getProperty(Vocabulary.msgCounter).getLong();
			String sender = msg.getProperty(Vocabulary.isSentBy).getProperty(Vocabulary.name).getString();
			this.getLogger().finer("MSG RECEIVED -> msg id: " + msgID + " for sender: " + sender);

			// Check last message received -> Discover lost messages
			if (!this.lastKnownMsgs.containsKey(sender)) {
				if (msgID != 0) {
					this.getLogger().warning(
							"Lost message found. First message received from " + sender + " with id: " + msgID);
					for (int i = 0; i < msgID; i++) {
						// Create lost message individuals
						Individual sensor = model.getIndividual(Vocabulary.wsnNdlNS + sender);
						Individual ontMsg = model.createIndividual(Vocabulary.diagnosisNS + sender + "_lostMsgId-" + i,
								Vocabulary.LostMessage);
						ontMsg.addProperty(Vocabulary.msgCounter, model.createTypedLiteral(i));
						// ontMsg.addProperty(Vocabulary.timemstamp,
						// model.createTypedLiteral(step));
						ontMsg.addProperty(Vocabulary.isSentBy, sensor);
						lostMessagesDiscovered.add(ontMsg);
					}
				}
			} else {
				long lastId = this.lastKnownMsgs.get(sender);
				if (msgID > lastId + 1) {
					this.getLogger().warning(
							"Lost message found. Last message received from " + sender + " with id: " + msgID);
					for (long i = (lastId + 1); i < msgID; i++) {
						// Create lost message individuals
						Individual sensor = model.getIndividual(Vocabulary.wsnNdlNS + sender);
						Individual ontMsg = model.createIndividual(Vocabulary.diagnosisNS + sender + "_lostMsgId-" + i,
								Vocabulary.LostMessage);
						ontMsg.addProperty(Vocabulary.msgCounter, model.createTypedLiteral(i));
						// ontMsg.addProperty(Vocabulary.timemstamp,
						// model.createTypedLiteral(step));
						ontMsg.addProperty(Vocabulary.isSentBy, sensor);
						lostMessagesDiscovered.add(ontMsg);
					}
				}
			}
			// Set last known msgs in any case
			this.lastKnownMsgs.put(sender, msgID);
		}

		return lostMessagesDiscovered;
	}

	/**
	 * @return the hardware
	 */
	public ZigBeeSensorNode getHardware() {
		return hardware;
	}

	/**
	 * @param hardware
	 *            the hardware to set
	 */
	public void setHardware(ZigBeeSensorNode hardware) {
		this.hardware = hardware;
	}

	/**
	 * @param url
	 * @return
	 */
	private OntModel loadModelWithImports(String url) {
		Model baseModel = ModelFactory.createDefaultModel();
		baseModel.read(url);
		return JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);

	}

}
