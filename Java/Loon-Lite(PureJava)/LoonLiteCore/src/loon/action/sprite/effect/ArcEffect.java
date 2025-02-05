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
package loon.action.sprite.effect;

import loon.LSystem;
import loon.action.sprite.Entity;
import loon.canvas.LColor;
import loon.opengl.GLEx;
import loon.utils.MathUtils;
import loon.utils.timer.LTimer;

/**
 * 0.3.2版新增类，单一色彩的圆弧渐变特效
 */
public class ArcEffect extends Entity implements BaseEffect {

	private final int arcDiv;

	private int step;

	private int curTurn = 1;

	private int tmpColor;

	private int[] sign = { 1, -1 };

	private boolean autoRemoved;

	private boolean completed;

	private LTimer timer;

	public ArcEffect(LColor c) {
		this(c, 0, 0, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public ArcEffect(LColor c, int x, int y, int width, int height) {
		this(c, x, y, width, height, 10);
	}

	public ArcEffect(LColor c, int x, int y, int width, int height, int div) {
		this.setLocation(x, y);
		this.setSize(width, height);
		this.timer = new LTimer(200);
		this.setColor(c == null ? LColor.black : c);
		this.setRepaint(true);
		this.setTurn(1);
		arcDiv = div;
	}

	public void setDelay(long delay) {
		timer.setDelay(delay);
	}

	public long getDelay() {
		return timer.getDelay();
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public void onUpdate(long elapsedTime) {
		if (completed) {
			return;
		}

		if (this.step >= this.arcDiv) {
			this.completed = true;
		}

		if (timer.action(elapsedTime)) {
			step++;
		}
		if (this.completed) {
			if (autoRemoved && getSprites() != null) {
				getSprites().remove(this);
			}
		}
	}

	@Override
	public void repaint(GLEx g, float offsetX, float offsetY) {
		if (completed) {
			return;
		}
		tmpColor = g.color();
		g.setColor(_baseColor);
		if (step <= 1) {
			g.fillRect(drawX(offsetX), drawY(offsetY), _width, _height);
		} else {
			float deg = 360f / this.arcDiv * this.step;
			if (deg < 360) {
				float length = MathUtils.sqrt(MathUtils.pow(_width / 2, 2.0f) + MathUtils.pow(_height / 2, 2.0f));
				float x = drawX(_width / 2 - length + offsetX);
				float y = drawY(_height / 2 - length + offsetY);
				float w = _width / 2 + length - x;
				float h = _height / 2 + length - y;
				g.fillArc(x, y, w, h, 0, this.sign[this.curTurn] * deg);
			}
		}
		g.setColor(tmpColor);
	}

	@Override
	public ArcEffect reset() {
		super.reset();
		this.completed = false;
		this.step = 0;
		this.curTurn = 1;
		return this;
	}

	public int getTurn() {
		return curTurn;
	}

	public void setTurn(int turn) {
		this.curTurn = turn;
	}

	public boolean isAutoRemoved() {
		return autoRemoved;
	}

	public ArcEffect setAutoRemoved(boolean autoRemoved) {
		this.autoRemoved = autoRemoved;
		return this;
	}

	@Override
	public void close() {
		super.close();
		completed = true;
	}

}
