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
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.HashMap;

import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.portrayal.SimpleInspector;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.device.Device;
import es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;

/**
 * @author a.carrera
 * 
 */
public class SensorNode2DPortrayal extends Device2DPortrayal implements Portrayal {

	private Image sensorImage = null;
	private Image routerImage = null;
	private Image baseImage = null;
	private ImageObserver io = null;
	private double localScale = 0.3;
	private double scale = 1.0;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public SensorNode2DPortrayal() throws IOException {
		if (sensorImage == null) {
			sensorImage = es.upm.dit.gsi.shanks.wsn.utils.ImageLoaderHelper
					.getImage("src/main/resources/icons/wi-fi-tag.png");
		}
		if (routerImage == null) {
			routerImage = es.upm.dit.gsi.shanks.wsn.utils.ImageLoaderHelper
					.getImage("src/main/resources/icons/wirelessrouter.png");
		}
		if (baseImage == null) {
			baseImage = es.upm.dit.gsi.shanks.wsn.utils.ImageLoaderHelper
					.getImage("src/main/resources/icons/wirelesstransport.png");
		}
		if (io == null) {
			io = es.upm.dit.gsi.shanks.wsn.utils.ImageLoaderHelper.getImageObserver();
		}
	}

	/**
     * 
     */
	private static final long serialVersionUID = 3180819560173840065L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal
	 * #draw(java.lang.Object, java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
	 */
	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

		ShanksSimulation sim = (ShanksSimulation) info.gui.state;
		try {
			Scenario2DPortrayal scenarioPortrayal = (Scenario2DPortrayal) sim.getScenarioPortrayal();
			Display2D mainDisplay = scenarioPortrayal.getDisplays().get(Scenario2DPortrayal.MAIN_DISPLAY_ID);
			scale = mainDisplay.getScale();
		} catch (ShanksException e) {
			sim.getLogger().warning(
					"Problems to get the scale in the main display window. Exception message: " + e.getMessage());
		}

		// Draw the devices
		ZigBeeSensorNode sensorNode = (ZigBeeSensorNode) object;
		String id = sensorNode.getID();

		int x;
		int y;
		int w;
		int h;

		if (id.startsWith("base")) {
			double width = ((double) baseImage.getWidth(io)) * scale * localScale;
			double height = ((double) baseImage.getHeight(io)) * scale * localScale;
			x = (int) (info.draw.x - width / 2.0);
			y = (int) (info.draw.y - height / 2.0);
			w = (int) (width);
			h = (int) (height);

			graphics.drawImage(baseImage, x, y, w, h, io);
			graphics.setColor(Color.black);
			graphics.drawString(sensorNode.getID(), x - 3, y);
		} else {
			id = id.split("-")[1];
			Color color = Color.white;
			double width;
			double height;
			Image image;

			if (sensorNode.isZigBeeRouter()) {
				image = routerImage;
				width = ((double) routerImage.getWidth(io)) * scale * localScale;
				height = ((double) routerImage.getHeight(io)) * scale * localScale;
			} else {
				image = sensorImage;
				width = ((double) sensorImage.getWidth(io)) * scale * localScale;
				height = ((double) sensorImage.getHeight(io)) * scale * localScale;
			}

			if (sensorNode.isDetecting()) {
				color = Color.blue;
			}

			x = (int) (info.draw.x - width / 2.0);
			y = (int) (info.draw.y - height / 2.0);
			w = (int) (width);
			h = (int) (height);
			graphics.drawImage(image, x, y, w, h, color, io);
		}

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
			graphics.fillRect(x + 5, y - 5, (int) 5, (int) 5);
		}
		if (status.get(ZigBeeSensorNode.MEMORY_DAMAGED_STATUS)) {
			graphics.setColor(Color.magenta);
			graphics.fillRect(x - 5, y, (int) 5, (int) 5);
		}
		if (status.get(ZigBeeSensorNode.SENSOR_DAMAGED_STATUS)) {
			graphics.setColor(Color.orange);
			graphics.fillRect(x, y + h, (int) 5, (int) 5);
		}
		if (status.get(ZigBeeSensorNode.EXTERNAL_DAMAGE_STATUS)) {
			graphics.setColor(Color.black);
			graphics.fillRect(x + w, y + h, (int) 5, (int) 5);
		}

		// // ****************************************
		// // *******CODE TO SHOW CIRCLES AS DEVICES**
		// // ****************************************
		//
		// ZigBeeSensorNode sensorNode = (ZigBeeSensorNode) object;
		// String id = sensorNode.getID();
		// if (id.startsWith("base")) {
		//
		// final double width = 10;
		// final double height = 10;
		// graphics.setColor(Color.black);
		//
		// // Draw the devices
		// final int x = (int) (info.draw.x - width / 2.0);
		// final int y = (int) (info.draw.y - height / 2.0);
		// final int w = (int) (width);
		// final int h = (int) (height);
		// graphics.fillRect(x, y, w, h);
		//
		// // Draw the devices ID ID
		// graphics.setColor(Color.black);
		// graphics.drawString(sensorNode.getID(), x - 3, y);
		// } else {
		// id = id.split("-")[1];
		// double width = 10;
		// double height = 10;
		//
		// if (sensorNode.isDetecting()) {
		// graphics.setColor(Color.blue);
		// } else if (sensorNode.isZigBeeRouter()) {
		// graphics.setColor(Color.gray);
		// } else {
		// graphics.setColor(Color.green);
		// }
		//
		// if (sensorNode.isZigBeeRouter()) {
		// width = 20;
		// height = 20;
		// }
		//
		// // Draw the devices
		// final int x = (int) (info.draw.x - width / 2.0);
		// final int y = (int) (info.draw.y - height / 2.0);
		// final int w = (int) (width);
		// final int h = (int) (height);
		// graphics.fillOval(x, y, w, h);
		//
		// // Draw the devices ID ID
		// graphics.setColor(Color.black);
		// if (sensorNode.isZigBeeRouter()) {
		// graphics.drawString("CH-" + id, x - 3, y);
		// } else {
		// graphics.drawString(id, x - 3, y);
		// }
		//
		// // Draw states
		// HashMap<String, Boolean> status = sensorNode.getStatus();
		// if (status.get(ZigBeeSensorNode.CPU_DAMAGED_STATUS)) {
		// graphics.setColor(Color.red);
		// graphics.fillRect(x - 5, y - 5, (int) 5, (int) 5);
		// }
		// if (status.get(ZigBeeSensorNode.BATTERY_DAMAGED_STATUS)) {
		// graphics.setColor(Color.yellow);
		// graphics.fillRect(x + 5, y - 5, (int) 5, (int) 5);
		// }
		// if (status.get(ZigBeeSensorNode.MEMORY_DAMAGED_STATUS)) {
		// graphics.setColor(Color.magenta);
		// graphics.fillRect(x - 5, y, (int) 5, (int) 5);
		// }
		// if (status.get(ZigBeeSensorNode.SENSOR_DAMAGED_STATUS)) {
		// graphics.setColor(Color.orange);
		// graphics.fillRect(x, y + h, (int) 5, (int) 5);
		// }
		// if (status.get(ZigBeeSensorNode.EXTERNAL_DAMAGE_STATUS)) {
		// graphics.setColor(Color.black);
		// graphics.fillRect(x + w, y + h, (int) 5, (int) 5);
		// }
		//
		// }

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
