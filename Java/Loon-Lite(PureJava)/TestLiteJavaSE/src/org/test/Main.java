/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package org.test;

import java.awt.GraphicsConfiguration;

import loon.se.JavaSEApplication;
import loon.se.JavaSESetting;
import loon.se.window.JavaSEAppCanvas;
import loon.se.window.JavaSEAppFrame;

public class Main {

	public static void main(String[] args) {
		JavaSESetting setting = new JavaSESetting();
		setting.width = 800;
		setting.height = 600;
		GraphicsConfiguration config = JavaSEApplication.getGraphicsConfiguration();
		JavaSEAppFrame frame = new JavaSEAppFrame(config, setting);
		JavaSEAppCanvas canvas = new JavaSEAppCanvas(config, setting);
		frame.playCanvas(canvas);
	}

}
