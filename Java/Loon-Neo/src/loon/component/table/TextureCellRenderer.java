/**
 * Copyright 2014
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
 * @version 0.4.2
 */
package loon.component.table;

import loon.LTexture;
import loon.canvas.Canvas;
import loon.canvas.LColor;
import loon.geom.Dimension;
import loon.opengl.GLEx;

public class TextureCellRenderer implements ICellRenderer {

	private boolean scaleTexture = true;

	@Override
	public void paint(GLEx g, Object value, int x, int y, int width, int height) {
		if (!(value instanceof LTexture)) {
			return;
		}
		LTexture textire = (LTexture) value;
		if (scaleTexture) {
			g.draw(textire, x, y, width, height, LColor.white);
		} else {
			g.draw(textire, x, y, LColor.white);
		}
	}

	@Override
	public void paint(Canvas g, Object value, int x, int y, int width, int height) {
		if (!(value instanceof LTexture)) {
			return;
		}
		LTexture textire = (LTexture) value;
		int old = g.getFillColor();
		if (scaleTexture) {
			g.draw(textire.getImage(), x, y, width, height);
		} else {
			g.draw(textire.getImage(), x, y);
		}
		g.setFillColor(old);
	}

	public void setScaleTexture(boolean s) {
		this.scaleTexture = s;
	}

	@Override
	public Dimension getCellContentSize(Object value) {
		if (value == null) {
			return null;
		}
		if (!(value instanceof LTexture)) {
			return null;
		}
		LTexture texture = (LTexture) value;
		return new Dimension(texture.getWidth(), texture.getHeight());
	}
}
