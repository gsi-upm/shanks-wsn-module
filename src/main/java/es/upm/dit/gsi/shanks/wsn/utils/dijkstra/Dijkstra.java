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
package es.upm.dit.gsi.shanks.wsn.utils.dijkstra;

/**
 * Project: shanks-wsn-module File:
 * es.upm.dit.gsi.shanks.wsn.utils.Djisktra.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 30/06/2014
 * @version 0.1
 * 
 */
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Dijkstra {
	public static void computePaths(Vertex startVertex) {
		startVertex.minDistance = 0.;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(startVertex);

		while (!vertexQueue.isEmpty()) {
			Vertex vertex1 = vertexQueue.poll();

			// Visit each edge exiting u
			for (Edge edge : vertex1.adjacencies) {
				Vertex vertex2 = edge.target;
				double weight = edge.weight;
				double distanceThroughU = vertex1.minDistance + weight;
				if (distanceThroughU < vertex2.minDistance) {
					vertexQueue.remove(vertex2);
					vertex2.minDistance = distanceThroughU;
					vertex2.previous = vertex1;
					vertexQueue.add(vertex2);
				}
			}
		}
	}

	public static List<Vertex> getShortestPathTo(Vertex target) {
		List<Vertex> path = new ArrayList<Vertex>();
		for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
			path.add(vertex);
		Collections.reverse(path);
		return path;
	}
}