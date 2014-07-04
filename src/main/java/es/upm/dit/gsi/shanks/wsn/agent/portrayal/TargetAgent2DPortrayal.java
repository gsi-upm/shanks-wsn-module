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
package es.upm.dit.gsi.shanks.wsn.agent.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal;

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.agent.portrayal.TargetAgent.java
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
public class TargetAgent2DPortrayal extends Device2DPortrayal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4840622097839730565L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.portrayal.SimplePortrayal2D#draw(java.lang.Object,
	 * java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
	 */
	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

		graphics.setColor(Color.red);

		final int w = (int) 20;
		final int h = (int) 20;
		final int x = (int) (info.draw.x - w / 2.0);
		final int y = (int) (info.draw.y - h / 2.0);
		graphics.fillOval(x, y, w, h);

	}

}
