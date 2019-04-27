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
package loon.utils.html.css;

import loon.LSystem;
import loon.LTexture;
import loon.canvas.LColor;
import loon.opengl.GLEx;
import loon.utils.html.css.CssDimensions.Rect;

public class CssColorCmd extends CssCmd {

	private LColor displayColor = LColor.white.cpy();
	public CssColor color;
	public Rect rect;
	private LTexture texture;

	public CssColorCmd(float w,float h) {
		super(w, h);
		texture = LSystem.base().graphics().finalColorTex();
	}

	@Override
	public void paint(GLEx g, float x, float y) {

		if (color != null) {
			displayColor.setColor(color.r, color.g, color.b, color.a);
		}

		if (rect != null) {
			g.draw(texture, rect.x, rect.y, rect.width, rect.height, displayColor);
		}

	}

}
