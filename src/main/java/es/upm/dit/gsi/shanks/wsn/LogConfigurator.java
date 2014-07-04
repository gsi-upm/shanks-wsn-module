/*******************************************************************************
 * Copyright (c) 2013 alvarocarrera Grupo de Sistemas Inteligentes - Universidad Politécnica de Madrid. (GSI-UPM)
 * http://www.gsi.dit.upm.es/
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * 
 * Contributors:
 *     alvarocarrera - initial API and implementation
 ******************************************************************************/
package es.upm.dit.gsi.shanks.wsn;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.launcher.logging.LogConfigurator.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 31/10/2013
 * @version 0.1
 * 
 */
public class LogConfigurator {

	/**
	 * Constructor
	 * 
	 */
	public static void log2File(Logger logger, String name, Level fileHandlerLevel, Level consoleHandlerLevel,
			String dir) {
		try {
			File aux = new File(".");
			String parent = aux.getCanonicalPath();
			File f;
			if (dir == null || dir.equals("") || dir.equals(".")) {
				f = new File(parent + File.separator + "logs");
			} else {
				f = new File(parent + File.separator + dir + File.separator + "logs");
			}
			if (!f.isDirectory()) {
				boolean made = f.mkdirs();
				if (!made) {
					logger.warning("Impossible to create log directory");
					System.exit(1);
				}
			}
			logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);
			String path;
			if (dir == null || dir.equals("") || dir.equals(".")) {
				path = parent + File.separator + "logs" + File.separator + name + "%u.log";
			} else {
				path = parent + File.separator + dir + File.separator + "logs" + File.separator + name + "%u.log";
			}
			int maxsizeMB = 20;
			int maxfiles = 20;
			FileHandler fh = new FileHandler(path, maxsizeMB * 1024 * 1000, maxfiles);
			fh.setFormatter(new SimpleFormatter());
			fh.setEncoding("UTF-8");
			fh.setLevel(fileHandlerLevel);
			logger.addHandler(fh);
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(new SimpleFormatter());
			ch.setEncoding("UTF-8");
			ch.setLevel(consoleHandlerLevel);
			logger.addHandler(ch);
		} catch (IOException e) {
			logger.warning("Error configuring the log file.");
			logger.warning(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * @param logger
	 * @param name
	 * @param fileLevel
	 * @param consoleLevel
	 */
	public static void log2File(Logger logger, String name, Level fileLevel, Level consoleLevel) {
		LogConfigurator.log2File(logger, name, fileLevel, consoleLevel, null);
	}

}
