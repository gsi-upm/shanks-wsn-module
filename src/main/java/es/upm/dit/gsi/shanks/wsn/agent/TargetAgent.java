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

import java.util.Properties;

import sim.util.Double2D;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.movement.Location;
import es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.movement.ShanksAgentMovementCapability;
import es.upm.dit.gsi.shanks.wsn.model.scenario.WSNScenario;

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.agent.TargetAgent.java
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
public class TargetAgent extends SimpleShanksAgent implements MobileShanksAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3753592226299553383L;

	private Location currentLocation;
	private Location targetLocation;
	private boolean allowToMove;
	private double speed;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param logger
	 */
	/**
	 * Constructor
	 * 
	 * @param id
	 * @param simulation
	 */
	public TargetAgent(String id, ShanksSimulation simulation) {
		super(id, simulation.getLogger());
		this.stopMovement();
		this.currentLocation = new Location();
		this.targetLocation = new Location();
		Properties props = simulation.getScenario().getProperties();
		String speedString = simulation.getScenario().getProperties().getProperty(WSNScenario.TARGET_SPEED);
		double speed = new Double(speedString);
		this.setSpeed(speed);
		String ws = props.getProperty(WSNScenario.FIELD_WIDTH);
		String hs = props.getProperty(WSNScenario.FIELD_HEIGHT);
		int w = new Integer(ws);
		int h = new Integer(hs);
		Double2D agentPos = new Double2D(simulation.random.nextInt(w), simulation.random.nextInt(h));
		this.currentLocation.setLocation2D(agentPos);
		this.targetLocation.setLocation2D(agentPos);
		ShanksAgentMovementCapability.updateLocation(simulation, this, currentLocation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	@Override
	public void checkMail() {
		// Nothing to do
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

		double distance2TargetLocation = this.getCurrentLocation().getLocation2D().distance(this.getTargetLocation().getLocation2D());
		if (distance2TargetLocation > this.speed) {
			// Go to target location
			this.startMovement();
			ShanksAgentMovementCapability.goTo(simulation, this, currentLocation, targetLocation, speed);
		} else {
			// Search for new target location
			this.stopMovement();			
			Properties props = simulation.getScenario().getProperties();
			String ws = props.getProperty(WSNScenario.FIELD_WIDTH);
			String hs = props.getProperty(WSNScenario.FIELD_HEIGHT);
			int w = new Integer(ws);
			int h = new Integer(hs);
			Double2D targetPos = new Double2D(simulation.random.nextInt(w), simulation.random.nextInt(h));
			this.setTargetLocation(new Location(targetPos));
			this.getLogger().finer("New target location: " + targetLocation.getLocation2D());
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#setSpeed
	 * (java.lang.Double)
	 */
	@Override
	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#getSpeed
	 * ()
	 */
	@Override
	public double getSpeed() {
		return this.speed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * stopMovement()
	 */
	@Override
	public void stopMovement() {
		this.allowToMove = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * startMovement()
	 */
	@Override
	public void startMovement() {
		this.allowToMove = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * isAllowedToMove()
	 */
	@Override
	public boolean isAllowedToMove() {
		return this.allowToMove;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * getCurrentLocation()
	 */
	@Override
	public Location getCurrentLocation() {
		return this.currentLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * getTargetLocation()
	 */
	@Override
	public Location getTargetLocation() {
		return this.targetLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * setCurrentLocation
	 * (es.upm.dit.gsi.shanks.agent.capability.movement.Location)
	 */
	@Override
	public void setCurrentLocation(Location location) {
		this.currentLocation = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.capability.movement.MobileShanksAgent#
	 * setTargetLocation
	 * (es.upm.dit.gsi.shanks.agent.capability.movement.Location)
	 */
	@Override
	public void setTargetLocation(Location location) {
		this.targetLocation = location;
	}

}
