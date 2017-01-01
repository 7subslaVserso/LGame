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
package loon.opengl;

import loon.LRelease;
import loon.LSystem;
import loon.LTexture;
import loon.LTextureBatch;
import loon.LTextureBatch.Cache;
import loon.canvas.Canvas;
import loon.canvas.LColor;
import loon.event.Updateable;
import loon.font.LFont;
import loon.font.TextLayout;
import loon.geom.Affine2f;
import loon.utils.CharArray;
import loon.utils.GLUtils;
import loon.utils.IntMap;
import loon.utils.MathUtils;
import loon.utils.ObjectMap;
import loon.utils.StringUtils;

public class LSTRFont implements LRelease {

	private class UpdateStringFont implements Updateable {

		private LSTRFont strfont;

		public UpdateStringFont(LSTRFont strf) {
			this.strfont = strf;
		}

		@Override
		public void action(Object a) {
			strfont.fontSize = (int) strfont.font.getSize();
			strfont.ascent = strfont.font.getAscent();
			if (strfont.additionalChars != null
					&& strfont.additionalChars.length > strfont.totalCharSet) {
				strfont.textureWidth *= 2;
			}
			Canvas canvas = LSystem.base().graphics()
					.createCanvas(strfont.textureWidth, strfont.textureHeight);
			canvas.setColor(LColor.white);
			canvas.setFont(strfont.font);
			int rowHeight = 0;
			int positionX = 0;
			int positionY = 0;
			int customCharsLength = (strfont.additionalChars != null) ? strfont.additionalChars.length
					: 0;
			strfont.totalCharSet = customCharsLength == 0 ? strfont.totalCharSet
					: 0;
			StringBuilder sbr = new StringBuilder(strfont.totalCharSet);

			for (int i = 0, size = strfont.totalCharSet + customCharsLength; i < size; i++) {
				char ch = (i < strfont.totalCharSet) ? (char) i
						: strfont.additionalChars[i - strfont.totalCharSet];

				TextLayout layout = strfont.font.getLayoutText(String
						.valueOf(ch));

				int charwidth = layout.charWidth(ch);

				if (charwidth <= 0) {
					charwidth = 1;
				}

				int charheight = (int) layout.getHeight();
				if (charheight <= 0) {
					charheight = strfont.fontSize;
				} else if (StringUtils.isAlphabet(ch)) {
					if (ch == 'i' && charheight > 24) {
						charheight -= 4;
					}
				}

				IntObject newIntObject = new IntObject();

				newIntObject.width = charwidth;
				newIntObject.height = charheight;

				if (positionX + newIntObject.width >= strfont.textureWidth) {
					layout = strfont.font.getLayoutText(sbr.toString());
					canvas.fillText(layout, 0, positionY);
					sbr.delete(0, sbr.length());
					positionX = 0;
					positionY += rowHeight;
					rowHeight = 0;
				}

				newIntObject.storedX = positionX;
				newIntObject.storedY = positionY;

				if (newIntObject.height > strfont.fontHeight) {
					strfont.fontHeight = newIntObject.height;
				}

				if (newIntObject.height > rowHeight) {
					rowHeight = newIntObject.height;
				}

				sbr.append(ch);

				positionX += newIntObject.width;

				if (i < strfont.totalCharSet) {
					strfont.charArray[i] = newIntObject;
				} else {
					strfont.customChars.put(ch, newIntObject);
				}
			}
			if (sbr.length() > 0) {
				TextLayout layout = strfont.font.getLayoutText(sbr.toString());
				canvas.fillText(layout, 0, positionY);
				sbr = null;
			}
			LTextureBatch tmpbatch = strfont.fontBatch;
			strfont.fontBatch = new LTextureBatch(
					strfont.texture = canvas.toTexture());
			strfont.fontBatch.setBlendState(BlendState.AlphaBlend);
			if (tmpbatch != null) {
				tmpbatch.close();
			}
			strfont._initChars = true;
			strfont.isDrawing = false;
		}

	}

	private boolean _isClose = false;

	private int _initDraw = -1;

	private float updateX = 0, updateY = 0;

	private char newLineFlag = '\n';

	private LTexture texture;

	private boolean useCache, isDrawing, isasyn;

	private float offsetX = 1, offsetY = 1;

	private ObjectMap<String, Cache> displays;

	private int totalCharSet = 256;

	private IntMap<IntObject> customChars = new IntMap<IntObject>();

	private IntObject[] charArray = new IntObject[totalCharSet];

	private LColor[] colors = null;

	private LFont font;

	private IntObject intObject;

	private Cache display;

	private float ascent;

	private int charCurrent;

	private int totalWidth = 0, totalHeight = 0;

	private int textureWidth = 512;

	private int textureHeight = 512;

	private int fontSize = 0;

	private int fontHeight = 0;

	private LTextureBatch fontBatch;

	private class IntObject {

		public int width;

		public int height;

		public int storedX;

		public int storedY;

	}

	private boolean _initChars = false;

	private char[] additionalChars = null;

	public LSTRFont(LFont font) {
		this(font, (char[]) null, true);
	}

	public LSTRFont(LFont font, String strings) {
		this(font, strings.toCharArray(), true);
	}

	public LSTRFont(LFont font, String[] strings) {
		this(font, StringUtils.unificationStrings(strings).toCharArray(), true);
	}

	public LSTRFont(LFont font, boolean asyn) {
		this(font, (char[]) null, asyn);
	}

	public LSTRFont(LFont font, String strings, boolean asyn) {
		this(font, strings.toCharArray(), asyn);
	}

	public LSTRFont(LFont font, String[] strings, boolean asyn) {
		this(font, StringUtils.unificationStrings(strings).toCharArray(), asyn);
	}

	private String text;

	public String getText() {
		return text;
	}

	public LSTRFont(LFont font, char[] chs, boolean asyn) {
		this.displays = new ObjectMap<String, Cache>(totalCharSet);
		this.useCache = true;
		this.font = font;
		this.isasyn = asyn;
		this.fontSize = font.getSize();
		this.fontHeight = font.getHeight();
		this.ascent = font.getAscent();
		int customCharsLength = (additionalChars != null) ? additionalChars.length
				: 0;
		this.totalCharSet = customCharsLength == 0 ? totalCharSet : 0;
		if (chs != null && chs.length > 0) {
			int size = chs.length;
			CharArray chars = new CharArray();
			for (int i = 0; i < size; i++) {
				char ch = chs[i];
				if (!chars.contains(ch))
					chars.add(ch);
			}
			if (chs.length == chars.length) {
				this.additionalChars = chs;
			} else {
				this.additionalChars = chars.items;
			}
			this.text = new String(additionalChars);
			if (additionalChars != null
					&& additionalChars.length > totalCharSet) {
				textureWidth *= 2;
			}
			this.make(asyn);
			chars = null;
		}
		if (StringUtils.isEmpty(text)) {
			_isClose = true;
			return;
		}
	}

	private void make() {
		make(isasyn);
	}

	private synchronized void make(boolean asyn) {
		if (_isClose) {
			return;
		}
		if (_initChars) {
			return;
		}
		if (isDrawing) {
			return;
		}
		isDrawing = true;
		updateX = 0;
		updateY = LSystem.isHTML5() ? 1f : 0;
		Updateable update = new UpdateStringFont(this);
		if (asyn) {
			LSystem.load(update);
		} else {
			update.action(null);
		}
	}

	public LTexture getTexture() {
		return texture;
	}

	public void drawString(String chars, float x, float y) {
		drawString(x, y, 1f, 1f, 0, 0, 0, chars, LColor.white, 0,
				chars.length() - 1);
	}

	public void drawString(String chars, float x, float y, LColor color) {
		drawString(x, y, 1f, 1f, 0, 0, 0, chars, color, 0, chars.length() - 1);
	}

	public void drawString(String chars, float x, float y, float rotation,
			LColor color) {
		drawString(x, y, 1f, 1f, 0, 0, rotation, chars, color, 0,
				chars.length() - 1);
	}

	public void drawString(String chars, float x, float y, float rotation) {
		drawString(x, y, 1f, 1f, 0, 0, rotation, chars, LColor.white, 0,
				chars.length() - 1);
	}

	public void drawString(String chars, float x, float y, float sx, float sy,
			float ax, float ay, float rotation, LColor c) {
		drawString(x, y, sx, sy, ax, ay, rotation, chars, c, 0,
				chars.length() - 1);
	}

	private void drawString(float x, float y, float sx, float sy, float ax,
			float ay, float rotation, String chars, LColor c, int startIndex,
			int endIndex) {
		if (_isClose) {
			return;
		}
		if (StringUtils.isEmpty(chars)) {
			return;
		}
		make();
		if (processing()) {
			return;
		}
		if (StringUtils.isEmpty(chars)) {
			return;
		}
		if (_initDraw < 1) {
			_initDraw++;
			return;
		}
		if (texture.isClose()) {
			return;
		}
		if (displays.size > LSystem.DEFAULT_MAX_CACHE_SIZE) {
			synchronized (displays) {
				for (Cache cache : displays.values()) {
					if (cache != null) {
						cache.close();
						cache = null;
					}
				}
			}
			displays.clear();
		}

		this.intObject = null;
		this.charCurrent = 0;
		this.totalWidth = 0;
		this.totalHeight = 0;
		if (rotation != 0 && (ax == 0 && ay == 0)) {
			TextLayout layout = font.getLayoutText(chars);
			ax = layout.bounds.width / 2;
			ay = layout.bounds.height;
		}
		if (useCache) {
			display = displays.get(chars);
			if (display == null) {
				fontBatch.begin();
				float old = fontBatch.getFloatColor();
				fontBatch.setColor(c);
				char[] charList = chars.toCharArray();
				for (int i = 0; i < charList.length; i++) {
					charCurrent = charList[i];
					if (charCurrent < totalCharSet) {
						intObject = charArray[charCurrent];
					} else {
						intObject = customChars.get(charCurrent);
					}
					if (charCurrent == newLineFlag) {
						totalHeight += fontSize;
						totalWidth = 0;
					}
					if (intObject != null) {
						if ((i >= startIndex) || (i <= endIndex)) {
							fontBatch.drawQuad(totalWidth, totalHeight,
									(totalWidth + intObject.width) - offsetX,
									(totalHeight + intObject.height) - offsetY,
									intObject.storedX, intObject.storedY,
									intObject.storedX + intObject.width
											- offsetX, intObject.storedY
											+ intObject.height - offsetY);
						}
						totalWidth += intObject.width;
					}
				}
				fontBatch.commit(x, y, sx, sy, ax, ay, rotation);
				fontBatch.setColor(old);
				displays.put(chars, display = fontBatch.newCache());
			} else if (display != null && fontBatch != null
					&& fontBatch.toTexture() != null) {
				fontBatch.postCache(display, c, x, y, sx, sy, ax, ay, rotation);
			}
		} else {
			fontBatch.begin();
			float old = fontBatch.getFloatColor();
			fontBatch.setColor(c);
			char[] charList = chars.toCharArray();
			for (int i = 0; i < charList.length; i++) {
				charCurrent = charList[i];
				if (charCurrent < totalCharSet) {
					intObject = charArray[charCurrent];
				} else {
					intObject = customChars.get(charCurrent);
				}
				if (charCurrent == newLineFlag) {
					totalHeight += fontSize;
					totalWidth = 0;
				}
				if (intObject != null) {
					if ((i >= startIndex) || (i <= endIndex)) {
						fontBatch.drawQuad(totalWidth, totalHeight,
								(totalWidth + intObject.width) - offsetX,
								(totalHeight + intObject.height) - offsetY,
								intObject.storedX, intObject.storedY,
								intObject.storedX + intObject.width - offsetX,
								intObject.storedY + intObject.height - offsetY);
					}
					totalWidth += intObject.width;
				}
			}
			fontBatch.setColor(old);
			fontBatch.commit(x, y, sx, sy, ax, ay, rotation);
		}
	}

	public void drawString(GLEx gl, String chars, float x, float y) {
		drawString(gl, x, y, 1f, 1f, 0, chars, LColor.white);
	}

	public void drawString(GLEx gl, String chars, float x, float y, LColor color) {
		drawString(gl, x, y, 1f, 1f, 0, chars, color);
	}

	public void drawString(GLEx gl, String chars, float x, float y,
			float rotation, LColor color) {
		drawString(gl, x, y, 1f, 1f, rotation, chars, color);
	}

	public void drawString(GLEx gl, String chars, float x, float y,
			float rotation) {
		drawString(gl, x, y, 1f, 1f, rotation, chars, LColor.white);
	}

	public void drawString(GLEx gl, String chars, float x, float y, float sx,
			float sy, float rotation, LColor c) {
		drawString(gl, x, y, sx, sy, rotation, chars, c);
	}

	public void drawString(GLEx gl, float x, float y, float sx, float sy,
			float rotation, String chars, LColor c) {
		drawString(gl, x, y, sx, sy, 0, 0, rotation, chars, c, 0,
				chars.length() - 1);
	}

	public void drawString(GLEx gl, float x, float y, float sx, float sy,
			float ax, float ay, float rotation, String chars, LColor c) {
		drawString(gl, x, y, sx, sy, ax, ay, rotation, chars, c, 0,
				chars.length() - 1);
	}

	private void drawString(GLEx gl, float x, float y, float sx, float sy,
			float ax, float ay, float rotation, String chars, LColor c,
			int startIndex, int endIndex) {
		if (_isClose) {
			return;
		}
		if (StringUtils.isEmpty(chars)) {
			return;
		}
		make();
		if (processing()) {
			return;
		}
		if (StringUtils.isEmpty(chars)) {
			return;
		}
		if (_initDraw < 1) {
			_initDraw++;
			return;
		}
		if (texture.isClose()) {
			return;
		}
		this.intObject = null;
		this.charCurrent = 0;
		this.totalWidth = 0;
		this.totalHeight = 0;
		final LTexture texture = fontBatch.toTexture();
		int old = gl.color();
		char[] charList = chars.toCharArray();
		final boolean anchor = ax != 0 || ay != 0;
		final boolean scale = sx != 1f || sy != 1f;
		final boolean angle = rotation != 0;
		final boolean update = scale || angle || anchor;
		try {
			gl.setTint(c);
			if (update) {
				gl.saveTx();
				Affine2f xf = gl.tx();
				if (angle) {
					float centerX = x + this.getWidth(chars) / 2;
					float centerY = y + this.getHeight(chars) / 2;
					xf.translate(centerX, centerY);
					xf.preRotate(rotation);
					xf.translate(-centerX, -centerY);
				}
				if (scale) {
					float centerX = x + this.getWidth(chars) / 2;
					float centerY = y + this.getHeight(chars) / 2;
					xf.translate(centerX, centerY);
					xf.preScale(sx, sy);
					xf.translate(-centerX, -centerY);
				}
				if (anchor) {
					xf.translate(ax, ay);
				}
			}
			for (int i = 0; i < charList.length; i++) {
				charCurrent = charList[i];
				if (charCurrent < totalCharSet) {
					intObject = charArray[charCurrent];
				} else {
					intObject = customChars.get(charCurrent);
				}
				if (charCurrent == newLineFlag) {
					totalHeight += fontSize;
					totalWidth = 0;
				}

				if (intObject != null) {
					if ((i >= startIndex) || (i <= endIndex)) {
						gl.draw(texture,
								x + totalWidth,
								y + totalHeight,
								intObject.width * sx,
								intObject.height * sy,
								StringUtils.isChinese((char) charCurrent) ? intObject.storedX
										- updateX
										: intObject.storedX, intObject.storedY,
								intObject.width, intObject.height - updateY, c);
					}
					totalWidth += intObject.width;
				}
			}
		} finally {
			gl.setTint(old);
			if (update) {
				gl.restoreTx();
			}
		}
	}

	public void setUpdateX(float x) {
		this.updateX = x;
	}

	public void setUpdateY(float y) {
		this.updateY = y;
	}

	public void addChar(char c, float x, float y, LColor color) {
		if (_isClose) {
			return;
		}
		make();
		if (processing()) {
			return;
		}
		if (_initDraw < 1) {
			_initDraw++;
			return;
		}
		if (texture.isClose()) {
			return;
		}
		this.charCurrent = c;
		if (charCurrent < totalCharSet) {
			intObject = charArray[charCurrent];
		} else {
			intObject = customChars.get(charCurrent);
		}
		if (intObject != null) {
			if (color != null) {
				setImageColor(color);
			}
			if (c == newLineFlag) {
				fontBatch.draw(colors, x, y + fontSize, intObject.width
						- offsetX, intObject.height - offsetY,
						intObject.storedX, intObject.storedY, intObject.storedX
								+ intObject.width - offsetX, intObject.storedY
								+ intObject.height - offsetY);
			} else {
				fontBatch.draw(colors, x, y, intObject.width - offsetX,
						intObject.height - offsetY, intObject.storedX,
						intObject.storedY, intObject.storedX + intObject.width
								- offsetX, intObject.storedY + intObject.height
								- offsetY);
			}
			if (colors != null) {
				colors = null;
			}
		}
	}
	
	public void startChar() {
		if (_isClose) {
			return;
		}
		make();
		if (processing()) {
			return;
		}
		if (_initDraw < 1) {
			_initDraw++;
			return;
		}
		if (texture.isClose()) {
			return;
		}
		fontBatch.begin();
	}

	public void stopChar() {
		if (_isClose) {
			return;
		}
		make();
		if (_initDraw < 1) {
			_initDraw++;
			return;
		}
		if (processing()) {
			return;
		}
		if (texture.isClose()) {
			return;
		}
		GL20 g = LSystem.base().graphics().gl;
		if (g != null) {
			int old = GLUtils.getBlendMode();
			GLUtils.setBlendMode(g, LSystem.MODE_NORMAL);
			fontBatch.end();
			GLUtils.setBlendMode(g, old);
		}
	}

	private boolean processing() {
		return fontBatch == null || isDrawing;
	}

	public void postCharCache() {
		if (_isClose) {
			return;
		}
		make();
		if (processing()) {
			return;
		}
		if (texture.isClose()) {
			return;
		}
		GL20 g = LSystem.base().graphics().gl;
		if (g != null) {
			int old = GLUtils.getBlendMode();
			GLUtils.setBlendMode(g, LSystem.MODE_NORMAL);
			fontBatch.postLastCache();
			GLUtils.setBlendMode(g, old);
		}
	}

	public Cache saveCharCache() {
		if (_isClose) {
			return null;
		}
		make();
		if (processing()) {
			return null;
		}
		if (texture.isClose()) {
			return null;
		}
		fontBatch.disposeLastCache();
		return fontBatch.newCache();
	}

	public LTextureBatch getFontBatch() {
		return fontBatch;
	}

	private void setImageColor(float r, float g, float b) {
		setColor(Painter.TOP_LEFT, r, g, b);
		setColor(Painter.TOP_RIGHT, r, g, b);
		setColor(Painter.BOTTOM_LEFT, r, g, b);
		setColor(Painter.BOTTOM_RIGHT, r, g, b);
	}

	private void setImageColor(LColor c) {
		if (c == null) {
			return;
		}
		setImageColor(c.r, c.g, c.b);
	}

	private void setColor(int corner, float r, float g, float b) {
		if (colors == null) {
			colors = new LColor[] { new LColor(1, 1, 1, 1f),
					new LColor(1, 1, 1, 1f), new LColor(1, 1, 1, 1f),
					new LColor(1, 1, 1, 1f) };
		}
		colors[corner].r = r;
		colors[corner].g = g;
		colors[corner].b = b;
	}

	public int charWidth(char c) {
		if (_isClose) {
			return 0;
		}
		make();
		if (c == '\n') {
			return 0;
		}
		if (processing()) {
			return font.charWidth(c);
		}
		if (texture.isClose()) {
			return 0;
		}
		if (c < totalCharSet) {
			intObject = charArray[c];
		} else {
			intObject = customChars.get((int) c);
		}
		if (intObject != null) {
			return intObject.width;
		}
		return font.charWidth(c);
	}

	public int getWidth(String s) {
		if (_isClose) {
			return 0;
		}
		make();
		if (processing()) {
			return font.stringWidth(s);
		}
		if (texture.isClose()) {
			return 0;
		}
		int totalWidth = 0;
		IntObject intObject = null;
		int currentChar = 0;
		char[] charList = s.toCharArray();
		int maxWidth = 0;
		for (int i = 0; i < charList.length; i++) {
			currentChar = charList[i];
			if (currentChar < totalCharSet) {
				intObject = charArray[currentChar];
			} else {
				intObject = customChars.get(currentChar);
			}
			if (intObject != null) {
				if (currentChar == newLineFlag) {
					maxWidth = MathUtils.max(maxWidth, totalWidth);
					totalWidth = 0;
				}
				totalWidth += intObject.width;
			}
		}
		return MathUtils.max(maxWidth, totalWidth);
	}

	public int getHeight(String s) {
		if (_isClose) {
			return 0;
		}
		make();
		if (processing()) {
			return font.stringHeight(s);
		}
		if (texture.isClose()) {
			return 0;
		}
		int currentChar = 0;
		char[] charList = s.toCharArray();
		int lines = 0;
		int height = 0;
		int maxHeight = 0;
		for (int i = 0; i < charList.length; i++) {
			currentChar = charList[i];
			if (currentChar < totalCharSet) {
				intObject = charArray[currentChar];
			} else {
				intObject = customChars.get(currentChar);
			}
			if (intObject != null) {
				maxHeight = MathUtils.max(maxHeight, intObject.height);
				height = maxHeight;
			}
			if (currentChar == newLineFlag) {
				lines++;
				height = 0;
			}
		}
		return lines * getLineHeight() + height;
	}

	public int getHeight() {
		return fontHeight;
	}

	public int getSize() {
		return fontSize;
	}

	public int getLineHeight() {
		return fontHeight;
	}

	public float getAscent() {
		return ascent;
	}

	public LFont getFont() {
		return font;
	}

	public int getTotalCharSet() {
		return totalCharSet;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public char getNewLineFlag() {
		return newLineFlag;
	}

	public void setNewLineFlag(char newLineFlag) {
		this.newLineFlag = newLineFlag;
	}

	public float getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	public boolean isAsyn() {
		return isasyn;
	}

	public void setAsyn(boolean a) {
		this.isasyn = a;
	}

	public boolean isClose() {
		return _isClose;
	}

	@Override
	public synchronized void close() {
		if (_isClose) {
			return;
		}
		for (Cache c : displays.values()) {
			if (c != null) {
				c.close();
			}
		}
		displays.clear();
		if (fontBatch != null) {
			fontBatch.close();
			LTextureBatch.isBatchCacheDitry = true;
			LTextureBatch.clearBatchCaches();
		}
		if (texture != null) {
			texture.close(true);
		}
		fontBatch = null;
		isDrawing = false;
		_initChars = false;
		_initDraw = -1;
		_isClose = true;
	}

}
