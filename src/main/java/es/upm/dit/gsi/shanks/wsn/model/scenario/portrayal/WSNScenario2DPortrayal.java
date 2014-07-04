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
package es.upm.dit.gsi.shanks.wsn.model.scenario.portrayal;

import java.util.HashMap;
import java.util.Map.Entry;

import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import es.upm.dit.gsi.shanks.exception.ShanksException;
import es.upm.dit.gsi.shanks.model.element.NetworkElement;
import es.upm.dit.gsi.shanks.model.element.link.Link;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
import es.upm.dit.gsi.shanks.model.scenario.portrayal.ScenarioPortrayal;
import es.upm.dit.gsi.shanks.wsn.agent.TargetAgent;
import es.upm.dit.gsi.shanks.wsn.agent.portrayal.TargetAgent2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeCoordinatorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.device.ZigBeeSensorNode;
import es.upm.dit.gsi.shanks.wsn.model.element.device.portrayal.BaseStation2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.device.portrayal.SensorNode2DPortrayal;
import es.upm.dit.gsi.shanks.wsn.model.element.link.RoutePathLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.WifiLink;
import es.upm.dit.gsi.shanks.wsn.model.element.link.portrayal.WirelessLink2DPortrayal;

/**
 * Project: wsn File:
 * es.upm.dit.gsi.shanks.wsn.model.scenario.portrayal.WSNScenario2DPortrayal
 * .java
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
public class WSNScenario2DPortrayal extends Scenario2DPortrayal {

	/**
	 * Constructor
	 * 
	 * @param scenario
	 * @param width
	 * @param height
	 * @throws ShanksException
	 */
	public WSNScenario2DPortrayal(Scenario scenario, int width, int height) throws ShanksException {
		super(scenario, width, height);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal#
	 * addPortrayals()
	 */
	@Override
	public void addPortrayals() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal#
	 * placeElements()
	 */
	@Override
	public void placeElements() {
		HashMap<String, NetworkElement> elements = this.getScenario().getCurrentElements();
		for (Entry<String, NetworkElement> entry : elements.entrySet()) {
			NetworkElement element = entry.getValue();
			if (element.getClass().equals(ZigBeeSensorNode.class)) {
				ZigBeeSensorNode node = (ZigBeeSensorNode) element;
				this.situateDevice(node, node.getPosition().x, node.getPosition().y);
			}
		}

		ZigBeeCoordinatorNode base = (ZigBeeCoordinatorNode) this.getScenario().getNetworkElement("base-station");
		this.situateDevice(base, base.getPosition().x, base.getPosition().y);

		for (Entry<String, NetworkElement> entry : elements.entrySet()) {
			NetworkElement element = entry.getValue();
			Class<? extends NetworkElement> cl = element.getClass();
			if (cl.equals(RoutePathLink.class) || cl.equals(WifiLink.class)) {
				Link link = (Link) element;
				this.drawLink(link);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.model.scenario.portrayal.ScenarioPortrayal#
	 * setupPortrayals()
	 */
	@Override
	public void setupPortrayals() {
		try {
			ContinuousPortrayal2D devicePortrayal = (ContinuousPortrayal2D) this.getPortrayals()
					.get(Scenario2DPortrayal.MAIN_DISPLAY_ID).get(ScenarioPortrayal.DEVICES_PORTRAYAL);
			NetworkPortrayal2D networkPortrayal = (NetworkPortrayal2D) this.getPortrayals()
					.get(Scenario2DPortrayal.MAIN_DISPLAY_ID).get(ScenarioPortrayal.LINKS_PORTRAYAL);

			// Portrayals for devices
			devicePortrayal.setPortrayalForClass(ZigBeeSensorNode.class, new SensorNode2DPortrayal());
			devicePortrayal.setPortrayalForClass(ZigBeeCoordinatorNode.class, new BaseStation2DPortrayal());
			devicePortrayal.setPortrayalForClass(TargetAgent.class, new TargetAgent2DPortrayal());

			// Portrayals for links
			networkPortrayal.setPortrayalForAll(new WirelessLink2DPortrayal());
			// NOTE: All objects in the network portrayal are Edge. Thus, we
			// have to specify only one portrayal and paint different links in
			// the code of that portrayal.

		} catch (Exception e) {
			this.getScenario().getLogger()
					.warning("Error configuring portrayals for the simulation: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
