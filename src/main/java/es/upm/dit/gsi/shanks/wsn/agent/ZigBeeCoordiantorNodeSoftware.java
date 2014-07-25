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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.NetworkElement;
import es.upm.dit.gsi.shanks.model.element.link.Link;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.link.RoutePathLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.SensorLink;

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
	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 * @throws ShanksException
	 */
	public ZigBeeCoordiantorNodeSoftware(String id, Logger logger, ZigBeeSensorNode hardware,
			ShanksSimulation simulation) throws ShanksException {
		super(id, logger);
		this.hardware = hardware;
		this.hardware.setSoftware(this);
		this.sim = simulation;

		// TODO implement this
		long startTime = System.currentTimeMillis();
		try {
			this.populateOntology(this.sim);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		this.getLogger().info("Time in milliseconds: " + (endTime - startTime));
	}

	/**
	 * @param sim
	 * @throws ShanksException
	 * @throws FileNotFoundException
	 */
	private void populateOntology(ShanksSimulation sim) throws ShanksException, FileNotFoundException {
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();

		// TODO add this file as parameter
		// Load main file
		String url = "/media/Datos/git/shanks-wsn-module/src/main/resources/wsn-ndl.owl";
		this.getLogger().info("Loading ontology from: " + url);
		Model baseModel = ModelFactory.createDefaultModel();
		// Model baseModel =
		// ModelFactory.createDefaultModel(ReificationStyle.Minimal);
		baseModel.read(url);

		// Initialize system functions and templates
		SPINModuleRegistry.get().init();

		// Create OntModel with imports
		model = JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);
		this.getLogger().info("Model loaded successfully...");

		// Create an individual for devices
		HashMap<String, NetworkElement> elements = sim.getScenario().getCurrentElements();
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
				sensor.addProperty(Vocabulary.hasComponent, memory);
				sensor.addProperty(Vocabulary.hasComponent, transceiver);
				sensor.addProperty(Vocabulary.hasComponent, motionSensor);

				// CPU properties
				cpu.addProperty(Vocabulary.cores, model.createTypedLiteral(1));
				cpu.addProperty(Vocabulary.cpuspeed, model.createTypedLiteral((float) 0.008));
				// Memory properties
				memory.addProperty(Vocabulary.size, model.createTypedLiteral((float) 0.00025));
				// Transceiver properties

				// Motion Sensor properties

			} else if (element.getClass().equals(ZigBeeSensorNode.class) && element.getID().startsWith("base")) {
				ZigBeeSensorNode device = (ZigBeeSensorNode) element;
				String elementid = device.getID();
				Individual baseStation = model
						.createIndividual(Vocabulary.wsnNdlNS + elementid, Vocabulary.BaseStation);
				baseStation.addProperty(Vocabulary.name, model.createTypedLiteral(elementid));
				// BaseStation location
				Individual location = model.createIndividual(Vocabulary.wsnNdlNS + "location-" + elementid,
						Vocabulary.Location);
				baseStation.addProperty(Vocabulary.locatedAt, location);
				Double2D position = device.getPosition();
				location.addProperty(Vocabulary.longitude, model.createTypedLiteral((float) position.x));
				location.addProperty(Vocabulary.latitude, model.createTypedLiteral((float) position.y));
			}

		}

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

		// Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		// reasoner = reasoner.bindSchema(model);
		// // Obtain standard OWL-DL spec and attach the reasoner
		// OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM;
		// ontModelSpec.setReasoner(reasoner);
		// // Create ontology model with reasoner support
		// model = ModelFactory.createOntologyModel(ontModelSpec, model);

		// model = JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM, model);

		// TODO create network object groups for paths, clusters and more with
		// rules

		this.getLogger().info("All individuals created successfully...");

		// SPIN RULES
		this.getLogger().info("Start applying rules...");
		String rulesFile = "src/main/resources/rules/topology.rls";
		this.parseAndLoadSPINRules(model, rulesFile);

		this.getLogger().info("Size before inference: " + model.size());
		SPINInferences.run(model, model, null, null, false, null);
		this.getLogger().info("Size after inference: " + model.size());

		// String query = "SELECT ?node WHERE { ?node a wsn-ndl:ZigBeeRouter }";
		// Query arqQuery = ARQFactory.get().createQuery(model, query);
		//
		// QueryExecution qExe = QueryExecutionFactory.create(arqQuery, model);
		// ResultSet results = qExe.execSelect();
		// ResultSetFormatter.out(System.out, results);

		try {
			model.write(new FileWriter("src/main/resources/result.rdf"));
			model.write(new FileWriter("/home/alvaro/TBCFreeWorkspace/wsn-ndl/result.rdf"));
		} catch (IOException e) {
			this.getLogger().severe("Problem saving model in OWL file.");
			e.printStackTrace();
		}
	}
	/**
	 * @param model
	 * @param rulesFile
	 * @throws FileNotFoundException
	 */
	private void parseAndLoadSPINRules(Model model, String rulesFile) throws FileNotFoundException {

		Scanner scanner = new Scanner(new File(rulesFile)).useDelimiter("\\n---\\n");

		while (scanner.hasNext()) {
			String uri = scanner.next();
			String rule = scanner.next();
			Query arqQuery = ARQFactory.get().createQuery(model, rule);
			ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
			org.topbraid.spin.model.Query spinQuery = arq2SPIN.createQuery(arqQuery, null);
			Resource resource = model.getResource(uri);
			resource.addProperty((Property) SPIN.rule, spinQuery);
		}

		SPINModuleRegistry.get().registerAll(model, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	@Override
	public void checkMail() {
		List<Message> inbox = this.getInbox();
		for (Message msg : inbox) {
			String content = (String) msg.getPropCont();
			this.getLogger().fine("Content: " + content);
			String id = msg.getMsgId();
			this.getLogger().fine("MsgID: " + id);
			String reply = msg.getInReplyTo();
			this.getLogger().fine("InReplyTo: " + reply);
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

		SPINInferences.run(model, model, null, null, false, null);
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

}
