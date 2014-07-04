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
package es.upm.dit.gsi.shanks.wsn.model.element.device;

import java.util.logging.Logger;

import sim.util.Double2D;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.element.link.Link;
import es.upm.dit.gsi.shanks.wsn.agent.ZigBeeSensorNodeSofware;

/**
 * Project: wsn File:
 * es.upm.dit.gsi.shanks.wsn.model.element.device.SensorNode.java
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
public class ZigBeeSensorNode extends Device {

	private Double2D position;

	private boolean isZigBeeRouter;

	private Link path2sink;

	private ZigBeeSensorNodeSofware software;

	private boolean detecting;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param initialState
	 * @param isGateway
	 * @param logger
	 * @param position
	 */
	public ZigBeeSensorNode(String id, String initialState, boolean isGateway, Logger logger, Double2D position) {
		super(id, initialState, isGateway, logger);
		this.setPosition(position);
		this.setZigBeeRouter(false);
		this.setDetecting(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.element.NetworkElement#fillIntialProperties()
	 */
	@Override
	public void fillIntialProperties() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.element.NetworkElement#checkProperties()
	 */
	@Override
	public void checkProperties() throws ShanksException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.element.NetworkElement#checkStatus()
	 */
	@Override
	public void checkStatus() throws ShanksException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.element.NetworkElement#setPossibleStates()
	 */
	@Override
	public void setPossibleStates() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the position
	 */
	public Double2D getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Double2D position) {
		this.position = position;
	}

	/**
	 * @return the isClusterHead
	 */
	public boolean isZigBeeRouter() {
		return isZigBeeRouter;
	}

	/**
	 * @param isZigBeeRouter
	 *            the isClusterHead to set
	 */
	public void setZigBeeRouter(boolean isZigBeeRouter) {
		this.isZigBeeRouter = isZigBeeRouter;
	}

	/**
	 * @return the path2sink
	 */
	public Link getPath2sink() {
		return path2sink;
	}

	/**
	 * @param path2sink
	 *            the path2sink to set
	 */
	public void setPath2Sink(Link path2sink) {
		this.path2sink = path2sink;
	}

	/**
	 * @return the software
	 */
	public ZigBeeSensorNodeSofware getSoftware() {
		return software;
	}

	/**
	 * @param software
	 *            the software to set
	 */
	public void setSoftware(ZigBeeSensorNodeSofware software) {
		this.software = software;
	}

	/**
	 * @return the detecting
	 */
	public boolean isDetecting() {
		return detecting;
	}

	/**
	 * @param detecting
	 *            the detecting to set
	 */
	public void setDetecting(boolean detecting) {
		this.detecting = detecting;
	}

}
