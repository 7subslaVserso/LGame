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
package loon;

import loon.canvas.Canvas;
import loon.font.TextFormat;
import loon.font.TextLayout;
import loon.font.TextWrap;
import loon.geom.Affine2f;
import loon.geom.Dimension;
import loon.geom.FloatValue;
import loon.utils.GLUtils;
import loon.utils.Scale;
import loon.utils.reply.UnitPort;

public abstract class Graphics {

	protected Canvas defaultRenderTarget;

	protected final Counter amount;

	protected boolean flipScreen;

	protected final LGame game;

	protected final Dimension viewSizeM = new Dimension();

	protected final FloatValue viewDPISacle = new FloatValue(1f);

	protected Scale scale = null;

	protected int viewPixelWidth, viewPixelHeight;

	private Display display = null;

	// 创建一个半永久的纹理，用以批量进行颜色渲染
	private static LTexture colorTex;

	/**
	 * 返回一个缩放比例，用以让当前设备加载的资源按照此比例进行资源缩放
	 * 
	 * @return
	 */
	public Scale scale() {
		return scale;
	}

	public Affine2f getViewAffine() {
		display = game.display();
		return display.GL().tx();
	}

	public Graphics setDPIScale(float v) {
		viewDPISacle.set(v);
		return this;
	}

	public float onDPI(float v) {
		return viewDPISacle.scaled(v);
	}

	public int width() {
		return screenSize().getWidth();
	}

	public int height() {
		return screenSize().getHeight();
	}

	public void setFlip(boolean flip) {
		this.flipScreen = flip;
	}

	public boolean flip() {
		return this.flipScreen;
	}

	public abstract Dimension screenSize();

	public Canvas createCanvas(float width, float height) {
		return createCanvasImpl(scale, scale.scaledCeil(width), scale.scaledCeil(height));
	}

	public Canvas createCanvas(Dimension size) {
		return createCanvas(size.width, size.height);
	}

	public LTexture createTexture(float width, float height) {
		int texWidth = scale.scaledCeil(width);
		int texHeight = scale.scaledCeil(height);
		if (texWidth <= 0 || texHeight <= 0) {
			throw new LSysException("Invalid texture size: " + texWidth + "x" + texHeight);
		}
		int id = createTexture();
		return new LTexture(this, id, texWidth, texHeight, scale, width, height);
	}

	public LTexture createTexture(Dimension size) {
		return createTexture(size.width, size.height);
	}

	public abstract TextLayout layoutText(String text, TextFormat format);

	public abstract TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap);

	private static class DisposePort extends UnitPort {

		private final LRelease _release;

		DisposePort(LRelease r) {
			this._release = r;
		}

		@Override
		public void onEmit() {
			_release.close();
		}

	}

	public void queueForDispose(final LRelease resource) {
		game.frame.connect(new DisposePort(resource)).once();
	}

	public LTexture finalColorTex() {
		if (colorTex == null) {
			Canvas canvas = createCanvas(1, 1);
			canvas.setFillColor(0xFFFFFFFF).fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			colorTex = canvas.toTexture();
			colorTex.setDisabledTexture(true);
		}
		return colorTex;
	}

	protected Graphics(LGame game, Scale scale) {
		this.game = game;
		this.scale = scale;
		this.amount = new Counter();
	}

	protected abstract Canvas createCanvasImpl(Scale scale, int pixelWidth, int pixelHeight);

	protected void viewportChanged(Scale scale, int viewWidth, int viewHeight) {
		Display d = game.display();
		LSystem.viewSize.setSize((int) (viewWidth / LSystem.getScaleWidth()),
				(int) (viewHeight / LSystem.getScaleHeight()));
		this.scale = scale;
		this.viewPixelWidth = viewWidth;
		this.viewPixelHeight = viewHeight;
		this.viewSizeM.width = game.setting.scaling() ? LSystem.invXScaled(viewPixelWidth)
				: scale.invScaled(viewPixelWidth);
		this.viewSizeM.height = game.setting.scaling() ? LSystem.invXScaled(viewPixelHeight)
				: scale.invScaled(viewPixelHeight);
		if (d != null) {
			d.resize(LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
		}
	}

	protected boolean isAllowResize(int viewWidth, int viewHeight) {
		if (game.setting.isCheckResize) {
			Dimension size = this.screenSize();
			if (size == null || size.width <= 0 || size.height <= 0) {
				return true;
			}
			if (game.setting.landscape() && viewWidth > viewHeight) {
				return true;
			} else if (viewWidth < viewHeight) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	public int createTexture() {
		return createTexture(0);
	}

	public int createTexture(int count) {
		int id = amount.increment() + count;
		if (LSystem.containsTexture(id)) {
			return createTexture(1);
		}
		if (GLUtils.getCurrentHardwareTextureID() == id) {
			return createTexture(1);
		}
		GLUtils.bindTexture(id);
		return id;
	}

	public LGame game() {
		return game;
	}

	public LSetting setting() {
		return game.setting;
	}

	public abstract Canvas getCanvas();

}
