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

import java.util.HashMap;

import javax.swing.JFrame;

import sim.display.Console;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.ShanksSimulation2DGUI;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;

/**
 * Project: wsn File: es.upm.dit.gsi.shanks.wsn.WSNSimulation2DGUI.java
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
public class WSNSimulation2DGUI extends ShanksSimulation2DGUI {

	/**
	 * Constructor
	 * 
	 * @param sim
	 */
	public WSNSimulation2DGUI(ShanksSimulation sim) {
		super(sim);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.ShanksSimulation2DGUI#addDisplays(es.upm.dit.gsi
	 * .shanks.model.scenario.portrayal.Scenario2DPortrayal)
	 */
	@Override
	public void addDisplays(Scenario2DPortrayal scenarioPortrayal) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.ShanksSimulation2DGUI#addCharts(es.upm.dit.gsi.
	 * shanks.model.scenario.portrayal.Scenario2DPortrayal)
	 */
	@Override
	public void addCharts(Scenario2DPortrayal scenarioPortrayal) throws ShanksException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.ShanksSimulation2DGUI#locateFrames(es.upm.dit.gsi
	 * .shanks.model.scenario.portrayal.Scenario2DPortrayal)
	 */
	@Override
	public void locateFrames(Scenario2DPortrayal scenarioPortrayal) {
		Console console = (Console) this.controller;
		console.setLocation(100, 50);
		console.setSize(500, 400);
		HashMap<String, JFrame> frames = scenarioPortrayal.getFrameList();
		JFrame mainFrame = frames.get(Scenario2DPortrayal.MAIN_DISPLAY_ID);
		mainFrame.setLocation(600, 200);
		mainFrame.setSize(800, 800);
		mainFrame.setVisible(true);
	}

}
