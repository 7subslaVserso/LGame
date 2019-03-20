/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
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
package loon.utils.res;

import loon.geom.XY;

public class TextureData implements XY {

	protected int sourceW = 0;

	protected int sourceH = 0;

	protected int x = 0;

	protected int y = 0;

	protected int offX = 0;

	protected int offY = 0;

	protected String name = "unkown";

	protected TextureData() {

	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	protected int w = 0;

	public int w() {
		return w;
	}

	protected int h = 0;

	public int h() {
		return h;
	}

	public int offX() {
		return offX;
	}

	public int offY() {
		return offY;
	}

	public int sourceW() {
		return sourceW;
	}

	public int sourceH() {
		return sourceH;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	public String name() {
		return name;
	}

}
