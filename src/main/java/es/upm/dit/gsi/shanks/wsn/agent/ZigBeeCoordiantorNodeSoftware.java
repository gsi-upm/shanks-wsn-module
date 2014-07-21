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

import java.util.List;
import java.util.logging.Logger;

import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;

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

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 */
	public ZigBeeCoordiantorNodeSoftware(String id, Logger logger, ZigBeeSensorNode hardware,
			ShanksSimulation simulation) {
		super(id, logger);
		this.hardware = hardware;
		this.hardware.setSoftware(this);
		this.sim = simulation;
		// TODO implement this

//		// Initialize system functions and templates
//		SPINModuleRegistry.get().init();
//
//		// Load main file
//		String url = "/media/Datos/ontologies/Diagnosis Ontology SpecGen/Diagnosis.owl";
//		this.getLogger().info("Loading ontology from: " + url);
//		Model baseModel = ModelFactory.createDefaultModel();
//		// Model baseModel =
//		// ModelFactory.createDefaultModel(ReificationStyle.Minimal);
//		baseModel.read(url);
//
//		// Initialize system functions and templates
//		SPINModuleRegistry.get().init();
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
