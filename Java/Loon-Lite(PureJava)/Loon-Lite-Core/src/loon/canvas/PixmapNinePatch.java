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
package loon.canvas;

public class PixmapNinePatch extends NinePatchAbstract<Pixmap, Pixmap> {

	public PixmapNinePatch(Pixmap image) {
		super(image);
	}

	public PixmapNinePatch(Pixmap image, Repeat r) {
		super(image, r);
	}

	@Override
	public int[] getPixels(Pixmap img, int x, int y, int w, int h) {
		return img.getRGB(x, y, w, h);
	}

	@Override
	public int getImageWidth(Pixmap img) {
		return img.getWidth();
	}

	@Override
	public int getImageHeight(Pixmap img) {
		return img.getHeight();
	}

	@Override
	public void pos(Pixmap p, int x, int y) {
		p.translate(x, y);
	}

	@Override
	public void draw(Pixmap p, Pixmap img, int x, int y, int scaledWidth, int scaledHeight) {
		p.drawPixmap(img, x, y, scaledWidth, scaledHeight);
	}

	@Override
	public void draw(Pixmap p, Pixmap img, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
		p.drawPixmap(img, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh);
	}

}
