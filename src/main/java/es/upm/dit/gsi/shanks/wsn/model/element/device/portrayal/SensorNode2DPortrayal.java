/*******************************************************************************
 * Copyright  (C) 2014 Álvaro Carrera Barroso
 * Grupo de Sistemas Inteligentes - Universidad Politecnica de Madrid
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
 *******************************************************************************/
package es.upm.dit.gsi.shanks.wsn.model.element.device.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.portrayal.SimpleInspector;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;

/**
 * @author a.carrera
 * 
 */
public class SensorNode2DPortrayal extends Device2DPortrayal implements Portrayal {

	/**
     * 
     */
	private static final long serialVersionUID = 3180819560173840065L;
	// private BufferedImage bi = null;
	// private ImageObserver io = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal
	 * #draw(java.lang.Object, java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
	 */
	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

		// ****************************************
		// *******CODE TO SHOW CIRCLES AS DEVICES**
		// ****************************************

		ZigBeeSensorNode sensorNode = (ZigBeeSensorNode) object;
		String id = sensorNode.getID();
		if (id.startsWith("base")) {

			final double width = 10;
			final double height = 10;
			graphics.setColor(Color.black);

			// Draw the devices
			final int x = (int) (info.draw.x - width / 2.0);
			final int y = (int) (info.draw.y - height / 2.0);
			final int w = (int) (width);
			final int h = (int) (height);
			graphics.fillRect(x, y, w, h);

			// Draw the devices ID ID
			graphics.setColor(Color.black);
			graphics.drawString(sensorNode.getID(), x - 3, y);
		} else {
			id = id.split("-")[1];
			double width = 10;
			double height = 10;

			if (sensorNode.isDetecting()) {
				graphics.setColor(Color.blue);
			} else if (sensorNode.isZigBeeRouter()) {
				graphics.setColor(Color.gray);
			} else {
				graphics.setColor(Color.green);
			}

			if (sensorNode.isZigBeeRouter()) {
				width = 20;
				height = 20;
			}

			// Draw the devices
			final int x = (int) (info.draw.x - width / 2.0);
			final int y = (int) (info.draw.y - height / 2.0);
			final int w = (int) (width);
			final int h = (int) (height);
			graphics.fillOval(x, y, w, h);

			// Draw the devices ID ID
			graphics.setColor(Color.black);
			if (sensorNode.isZigBeeRouter()) {
				graphics.drawString("CH-" + id, x - 3, y);
			} else {
				graphics.drawString(id, x - 3, y);
			}

			// Draw states
			HashMap<String, Boolean> status = sensorNode.getStatus();
			if (status.get(ZigBeeSensorNode.CPU_DAMAGED_STATUS)) {
				graphics.setColor(Color.red);
				graphics.fillRect(x - 5, y - 5, (int) 5, (int) 5);
			}
			if (status.get(ZigBeeSensorNode.BATTERY_DAMAGED_STATUS)) {
				graphics.setColor(Color.yellow);
				graphics.fillRect(x - 5, y - 5, (int) 5, (int) 5);
			}
			if (status.get(ZigBeeSensorNode.MEMORY_DAMAGED_STATUS)) {
				graphics.setColor(Color.magenta);
				graphics.fillRect(x - 5, y - 5, (int) 5, (int) 5);
			}
			if (status.get(ZigBeeSensorNode.SENSOR_DAMAGED_STATUS)) {
				graphics.setColor(Color.orange);
				graphics.fillRect(x - 5, y - 5, (int) 5, (int) 5);
			}
			if (status.get(ZigBeeSensorNode.EXTERNAL_DAMAGE_STATUS)) {
				graphics.setColor(Color.black);
				graphics.fillRect(x - 5, y - 5, (int) 5, (int) 5);
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.portrayal.SimplePortrayal2D#hitObject(java.lang.Object,
	 * sim.portrayal.DrawInfo2D)
	 */
	public boolean hitObject(Object object, DrawInfo2D info) {
		double w = info.draw.getWidth();
		double h = info.draw.getHeight();
		// double w = info.draw.width;
		// double h = info.draw.height;
		double x = (info.draw.x - w / 2.0);
		double y = (info.draw.y - h / 2.0);
		Rectangle2D target = info.clip;
		// target.setRect(info.clip.x, info.clip.y, info.clip.width * 500.0,
		// info.clip.height * 500.0);
		boolean value = target.intersects(x, y, w, h);
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * 
	 * sim.portrayal.SimplePortrayal2D#getInspector(sim.portrayal.LocationWrapper
	 * , sim.display.GUIState)
	 */
	public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
		Device obj = (Device) wrapper.getObject();
		Inspector inspector = new SimpleInspector(obj, state, obj.getID());
		return inspector;

	}
}
