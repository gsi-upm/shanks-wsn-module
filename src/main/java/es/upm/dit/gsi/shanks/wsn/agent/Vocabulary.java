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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.agent.Vocabulary.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 21/07/2014
 * @version 0.1
 * 
 */
public class Vocabulary {

	/**
	 * <p>
	 * The RDF model that holds the vocabulary terms
	 * </p>
	 */
	private static Model m_model = ModelFactory.createDefaultModel();

	/**
	 * The namespaces of the vocabulary as a string
	 */
	public static final String diagnosisNS = "http://www.gsi.dit.upm.es/ontologies/b2d2/wsn#";
	public static final String wsnNdlNS = "http://www.gsi.dit.upm.es/ontologies/b2d2/wsn#";
	public static final String nmlBaseNS = "http://schemas.ogf.org/nml/2013/05/base#";
	public static final String indlNS = "http://www.science.uva.nl/research/sne/indl#";

	/**
	 * Resource for Ontology Classes
	 */
	// Resources for hardware description
	public static final Resource ZigBeeMessage = m_model.createResource(diagnosisNS + "ZigBeeMessage");
	public static final Resource LostMessage = m_model.createResource(diagnosisNS + "LostMessage");
	public static final Resource BaseStation = m_model.createResource(wsnNdlNS + "ZigBeeBaseStation");
	public static final Resource MicaZ = m_model.createResource(wsnNdlNS + "MicaZ");
	public static final Resource Location = m_model.createResource(nmlBaseNS + "Location");
	public static final Resource Cpu = m_model.createResource(wsnNdlNS + "ATmega128L");
	public static final Resource Memory = m_model.createResource(wsnNdlNS + "MicaZMemory");
	public static final Resource Transceiver = m_model.createResource(wsnNdlNS + "MPR2400");
	public static final Resource Motionsensor = m_model.createResource(wsnNdlNS + "Panasonic_PIR_WL_Series");
	public static final Resource RoutePathLink = m_model.createResource(wsnNdlNS + "RoutePathLink");
	public static final Resource SensorLink = m_model.createResource(wsnNdlNS + "SensorLink");
	// Resources for diagnosis description
	public static final Resource Hypothesis = m_model.createResource(wsnNdlNS + "Hypothesis");
	public static final Resource Observation = m_model.createResource(wsnNdlNS + "Observation");
	public static final Resource Diagnosis = m_model.createResource(wsnNdlNS + "Diagnosis");
	// Resources for faults taxonomy usage
	public static final Resource SensorFailure = m_model.createResource(wsnNdlNS + "SensorFailure");

	/**
	 * Object Properties 
	 */
	// Properties for hardware language
	public static final Property locatedAt = m_model.createProperty(nmlBaseNS + "locatedAt");
	public static final Property isSource = m_model.createProperty(nmlBaseNS + "isSource");
	public static final Property isSink = m_model.createProperty(nmlBaseNS + "isSink");
	public static final Property hasComponent = m_model.createProperty(indlNS + "hasComponent");
	public static final Property hasSensor = m_model.createProperty(wsnNdlNS + "hasSensor");
	public static final Property hasTransceiver = m_model.createProperty(wsnNdlNS + "hasTransceiver");
	public static final Property hasMemory = m_model.createProperty(wsnNdlNS + "hasMemory");
	public static final Property hasCPU = m_model.createProperty(wsnNdlNS + "hasCPU");
	public static final Property contains = m_model.createProperty(wsnNdlNS + "contains");
	public static final Property executesIn = m_model.createProperty(wsnNdlNS + "executesIn");
	public static final Property hosts = m_model.createProperty(wsnNdlNS + "hosts");
	public static final Property isReceivedBy = m_model.createProperty(wsnNdlNS + "isReceivedBy");
	public static final Property isSentBy = m_model.createProperty(wsnNdlNS + "isSentBy");
	public static final Property receives = m_model.createProperty(wsnNdlNS + "receives");
	public static final Property resendMessage = m_model.createProperty(wsnNdlNS + "resendMessage");
	public static final Property sends = m_model.createProperty(wsnNdlNS + "sends");
	// Properties for faults
	public static final Property possibleFault = m_model.createProperty(wsnNdlNS + "possibleFault");
	public static final Property hasFault = m_model.createProperty(wsnNdlNS + "hasFault");
	public static final Property affects = m_model.createProperty(wsnNdlNS + "affects");
	public static final Property isAffectedBy = m_model.createProperty(wsnNdlNS + "isAffectedBy");
	// Properties for diagnosis
	public static final Property hasHypothesis = m_model.createProperty(wsnNdlNS + "hasHypothesis");
	public static final Property hasObservation = m_model.createProperty(wsnNdlNS + "hasObservation");

	/**
	 * Datatype properties
	 */
	// Properties for hardware
	public static final Property latitude = m_model.createProperty(nmlBaseNS + "lat");
	public static final Property longitude = m_model.createProperty(nmlBaseNS + "long");
	public static final Property cores = m_model.createProperty(indlNS + "cores");
	public static final Property cpuspeed = m_model.createProperty(indlNS + "cpuspeed");
	public static final Property size = m_model.createProperty(indlNS + "size");
	public static final Property name = m_model.createProperty(nmlBaseNS + "name");
	public static final Property isRouter = m_model.createProperty(wsnNdlNS + "isRouter");
	public static final Property hasPerceptionRange = m_model.createProperty(wsnNdlNS + "hasPerceptionRange");
	public static final Property inReplyTo = m_model.createProperty(wsnNdlNS + "inReplyTo");
	public static final Property msgContent = m_model.createProperty(wsnNdlNS + "msgContent");
	public static final Property msgCounter = m_model.createProperty(wsnNdlNS + "msgCounter");
	public static final Property msgID = m_model.createProperty(wsnNdlNS + "msgID");
	public static final Property batteryLevel = m_model.createProperty(wsnNdlNS + "batteryLevel");
	public static final Property cpuLoad = m_model.createProperty(wsnNdlNS + "cpuLoad");
	public static final Property memoryLoad = m_model.createProperty(wsnNdlNS + "memoryLoad");
	public static final Property temp = m_model.createProperty(wsnNdlNS + "temp");
	public static final Property timemstamp = m_model.createProperty(wsnNdlNS + "timestamp");
	public static final Property emittedPower = m_model.createProperty(wsnNdlNS + "emittedPower");
	public static final Property isDetecting = m_model.createProperty(wsnNdlNS + "isDetecting");

}
