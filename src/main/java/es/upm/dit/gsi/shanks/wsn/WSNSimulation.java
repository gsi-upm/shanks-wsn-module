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

import java.util.Properties;

import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;

/**
 * Project: wsn File: es.upm.dit.gsi.shanks.wsn.WSNSimulation.java
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
public class WSNSimulation extends ShanksSimulation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227634202835889448L;

	/**
	 * Constructor
	 * 
	 * @param seed
	 * @param scenarioClass
	 * @param scenarioID
	 * @param initialState
	 * @param properties
	 * @throws ShanksException
	 */
	public WSNSimulation(long seed, Class<? extends Scenario> scenarioClass, String scenarioID, String initialState,
			Properties properties) throws ShanksException {
		super(seed, scenarioClass, scenarioID, initialState, properties);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.ShanksSimulation#addSteppables()
	 */
	@Override
	public void addSteppables() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.ShanksSimulation#registerShanksAgents()
	 */
	@Override
	public void registerShanksAgents() throws ShanksException {
		// TODO Auto-generated constructor stub
	}

}
