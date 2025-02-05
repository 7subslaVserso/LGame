/**
 * Copyright 2008 - 2011
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
 * @version 0.1
 */
package loon.geom;

import loon.LObject;
import loon.LSystem;
import loon.action.ActionBind;
import loon.utils.MathUtils;
import loon.utils.NumberUtils;
import loon.utils.StringKeyValue;
import loon.utils.StringUtils;
import loon.utils.TArray;

public class RectBox extends Shape implements BoxSize, XYZW {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static RectBox toPixels(final RectBox rect, final XY point) {
		return new RectBox(rect.getX() * point.getX(), rect.getY() * point.getY(), rect.getWidth() * point.getX(),
				rect.getHeight() * point.getY());
	}

	public static RectBox toPixels(final RectBox rect, int tileWidth, int tileHeight) {
		return new RectBox(rect.x * tileWidth, rect.y * tileHeight, rect.width * tileWidth, rect.height * tileHeight);
	}

	public static RectBox toTitle(final RectBox rect, final XY point) {
		return new RectBox(rect.getX() / point.getX(), rect.getY() / point.getY(), rect.getWidth() / point.getX(),
				rect.getHeight() / point.getY());
	}

	public static RectBox toTitle(final RectBox rect, int tileWidth, int tileHeight) {
		return new RectBox(rect.x / tileWidth, rect.y / tileHeight, rect.width / tileWidth, rect.height / tileHeight);
	}

	public final static SetXY getRandom(RectBox rect, SetXY out) {
		if (out == null) {
			out = new PointF();
		}
		out.setX(rect.x + (MathUtils.random() * rect.width));
		out.setY(rect.y + (MathUtils.random() * rect.height));
		return out;
	}

	public final static RectBox at(String v) {
		if (StringUtils.isEmpty(v)) {
			return new RectBox();
		}
		String[] result = StringUtils.split(v, ',');
		int len = result.length;
		if (len > 3) {
			try {
				float x = Float.parseFloat(result[0].trim());
				float y = Float.parseFloat(result[1].trim());
				float width = Float.parseFloat(result[2].trim());
				float height = Float.parseFloat(result[3].trim());
				return new RectBox(x, y, width, height);
			} catch (Exception ex) {
			}
		}
		return new RectBox();
	}

	public final static RectBox at(int x, int y, int w, int h) {
		return new RectBox(x, y, w, h);
	}

	public final static RectBox at(float x, float y, float w, float h) {
		return new RectBox(x, y, w, h);
	}

	public final static RectBox fromActor(ActionBind bind) {
		return new RectBox(bind.getX(), bind.getY(), bind.getWidth(), bind.getHeight());
	}

	public final static RectBox inflate(RectBox src, int xScale, int yScale) {
		float destWidth = src.width + xScale;
		float destHeight = src.height + yScale;
		float destX = src.x - xScale / 2;
		float destY = src.y - yScale / 2;
		return new RectBox(destX, destY, destWidth, destHeight);
	}

	public final static RectBox intersect(RectBox src1, RectBox src2, RectBox dest) {
		if (dest == null) {
			dest = new RectBox();
		}
		float x1 = MathUtils.max(src1.getMinX(), src2.getMinX());
		float y1 = MathUtils.max(src1.getMinY(), src2.getMinY());
		float x2 = MathUtils.min(src1.getMaxX(), src2.getMaxX());
		float y2 = MathUtils.min(src1.getMaxY(), src2.getMaxY());
		dest.setBounds(x1, y1, x2 - x1, y2 - y1);
		return dest;
	}

	public final static RectBox getIntersection(RectBox a, RectBox b) {
		float a_x = a.getX();
		float a_r = a.getRight();
		float a_y = a.getY();
		float a_t = a.getBottom();
		float b_x = b.getX();
		float b_r = b.getRight();
		float b_y = b.getY();
		float b_t = b.getBottom();
		float i_x = MathUtils.max(a_x, b_x);
		float i_r = MathUtils.min(a_r, b_r);
		float i_y = MathUtils.max(a_y, b_y);
		float i_t = MathUtils.min(a_t, b_t);
		return i_x < i_r && i_y < i_t ? new RectBox(i_x, i_y, i_r - i_x, i_t - i_y) : null;
	}

	public final static RectBox getIntersection(RectBox a, RectBox b, RectBox result) {
		float a_x = a.getX();
		float a_r = a.getRight();
		float a_y = a.getY();
		float a_t = a.getBottom();
		float b_x = b.getX();
		float b_r = b.getRight();
		float b_y = b.getY();
		float b_t = b.getBottom();
		float i_x = MathUtils.max(a_x, b_x);
		float i_r = MathUtils.min(a_r, b_r);
		float i_y = MathUtils.max(a_y, b_y);
		float i_t = MathUtils.min(a_t, b_t);
		if (i_x < i_r && i_y < i_t) {
			result.setBounds(i_x, i_y, i_r - i_x, i_t - i_y);
			return result;
		}
		return null;
	}

	public int width;

	public int height;

	private Matrix4 _rectMatrix;

	public RectBox() {
		setBounds(0, 0, 0, 0);
	}

	public RectBox(int width, int height) {
		setBounds(0, 0, width, height);
	}

	public RectBox(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
	}

	public RectBox(float x, float y, float width, float height) {
		setBounds(x, y, width, height);
	}

	public RectBox(double x, double y, double width, double height) {
		setBounds(x, y, width, height);
	}

	public RectBox(RectBox rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	public RectBox offset(Vector2f offset) {
		this.x += offset.x;
		this.y += offset.y;
		this.pointsDirty = true;
		checkPoints();
		return this;
	}

	public RectBox offset(int offsetX, int offsetY) {
		this.x += offsetX;
		this.y += offsetY;
		this.pointsDirty = true;
		checkPoints();
		return this;
	}

	public RectBox setBoundsFromCenter(float centerX, float centerY, float cornerX, float cornerY) {
		float halfW = MathUtils.abs(cornerX - centerX);
		float halfH = MathUtils.abs(cornerY - centerY);
		setBounds(centerX - halfW, centerY - halfH, halfW * 2.0, halfH * 2.0);
		return this;
	}

	public RectBox setBounds(RectBox rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
		return this;
	}

	public RectBox setBounds(double x, double y, double width, double height) {
		setBounds((float) x, (float) y, (float) width, (float) height);
		return this;
	}

	public RectBox setBounds(float x, float y, float width, float height) {
		if (this.x == x && this.y == y && this.width == width && this.height == height) {
			return this;
		}
		this.x = x;
		this.y = y;
		this.width = (int) width;
		this.height = (int) height;
		this.minX = x;
		this.minY = y;
		this.maxX = x + width;
		this.maxY = y + height;
		this.pointsDirty = true;
		this.checkPoints();
		return this;
	}

	public RectBox set(BoxSize size) {
		if (size == null) {
			return this;
		}
		return setBounds(size.getX(), size.getY(), size.getWidth(), size.getHeight());
	}

	public RectBox set(float x, float y, float width, float height) {
		return setBounds(x, y, width, height);
	}

	public Polygon getPolygon() {
		this.checkPoints();
		Polygon poly = new Polygon(this.points);
		return poly;
	}

	public RectBox inflate(int horizontalValue, int verticalValue) {
		this.x -= horizontalValue;
		this.y -= verticalValue;
		this.width += horizontalValue * 2;
		this.height += verticalValue * 2;
		this.minX = x;
		this.minY = y;
		this.maxX = x + width;
		this.maxY = y + height;
		this.pointsDirty = true;
		this.checkPoints();
		return this;
	}

	public RectBox setLocation(BoxSize r) {
		if (r == null) {
			return this;
		}
		return setLocation(r.getX(), r.getY());
	}

	public RectBox setLocation(XY r) {
		if (r == null) {
			return this;
		}
		return setLocation(r.getX(), r.getY());
	}

	public RectBox setLocation(Point r) {
		if (r == null) {
			return this;
		}
		return setLocation(r.x, r.y);
	}

	@Override
	public RectBox setLocation(float x, float y) {
		if (this.x == x && this.y == y) {
			return this;
		}
		super.setLocation(x, y);
		return this;
	}

	public RectBox setLocation(int x, int y) {
		if (this.x == x && this.y == y) {
			return this;
		}
		super.setLocation(x, y);
		return this;
	}

	public RectBox grow(float h, float v) {
		return setBounds(getX() - h, getY() - v, getWidth() + (h * 2), getHeight() + (v * 2));
	}

	public RectBox scaleGrow(float h, float v) {
		grow(getWidth() * (h - 1), getHeight() * (v - 1));
		return this;
	}

	@Override
	public void setScale(float sx, float sy) {
		if (scaleX != sx || scaleY != sy) {
			setSize(width * (scaleX = sx), height * (scaleY * sy));
		}
	}

	public RectBox setRotate(float r) {
		if (r != this.rotation) {
			this.rotation = r;
			int[] rect = MathUtils.getLimit(x, y, width, height, rotation);
			return setBounds(rect[0], rect[1], rect[2], rect[3]);
		}
		return this;
	}

	@Override
	public Shape setRotation(float r, float x, float y) {
		if (r != this.rotation) {
			super.setRotation(r, x, y);
			setBounds(minX, minY, (maxX - minX), (maxY - minY));
		}
		return this;
	}

	public RectBox setSize(float width, float height) {
		if (this.width == width && this.height == height) {
			return this;
		}
		return setBounds(this.x, this.y, width, height);
	}

	public boolean overlaps(RectBox rectangle) {
		return !(x > rectangle.x + rectangle.width || x + width < rectangle.x || y > rectangle.y + rectangle.height
				|| y + height < rectangle.y);
	}

	public Matrix4 getMatrix() {
		if (_rectMatrix == null) {
			_rectMatrix = new Matrix4();
		}
		return _rectMatrix.setToOrtho2D(this.x, this.y, this.width, this.height);
	}

	public int x() {
		return (int) x;
	}

	public int y() {
		return (int) y;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public float getZ() {
		return getWidth();
	}

	@Override
	public float getW() {
		return getHeight();
	}

	public RectBox copy(RectBox other) {
		this.x = other.x;
		this.y = other.y;
		this.width = other.width;
		this.height = other.height;
		return this;
	}

	@Override
	public float getMinX() {
		return getX();
	}

	@Override
	public float getMinY() {
		return getY();
	}

	@Override
	public float getMaxX() {
		return this.x + this.width;
	}

	@Override
	public float getMaxY() {
		return this.y + this.height;
	}

	public float getMiddleX() {
		return getCenterX();
	}

	public float getMiddleY() {
		return getCenterY();
	}

	@Override
	public float getCenterX() {
		return x + width / 2f;
	}

	@Override
	public float getCenterY() {
		return y + height / 2f;
	}

	public float getLeft() {
		return this.getMinX();
	}

	public RectBox setLeft(float value) {
		this.width += this.x - value;
		this.x = value;
		return this;
	}

	public float getRight() {
		return getMaxX();
	}

	public RectBox setRight(float v) {
		this.width = (int) (v - this.x);
		return this;
	}

	public float getTop() {
		return getMinY();
	}

	public RectBox setTop(float value) {
		this.height += this.y - value;
		this.y = value;
		return this;
	}

	public float getBottom() {
		return getMaxY();
	}

	public RectBox setBottom(float v) {
		this.height = (int) (v - this.y);
		return this;
	}

	public int Left() {
		return this.x();
	}

	public int Right() {
		return (int) getMaxX();
	}

	public int Top() {
		return this.y();
	}

	public int Bottom() {
		return (int) getMaxY();
	}

	public Vector2f topLeft() {
		return new Vector2f(this.getLeft(), this.getTop());
	}

	public Vector2f bottomRight() {
		return new Vector2f(this.getRight(), this.getBottom());
	}

	public RectBox normalize() {
		return normalize(this);
	}

	public RectBox normalize(RectBox r) {
		if (r.width < 0) {
			r.width = MathUtils.abs(r.width);
			r.x -= r.width;
		}
		if (r.height < 0) {
			r.height = MathUtils.abs(r.height);
			r.y -= r.height;
		}
		return this;
	}

	public float[] toFloat() {
		return new float[] { x, y, width, height };
	}

	@Override
	public RectBox getRect() {
		return this;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public void setHeight(float height) {
		this.height = (int) height;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public void setWidth(float width) {
		this.width = (int) width;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RectBox) {
			RectBox rect = (RectBox) obj;
			return equals(rect.x, rect.y, rect.width, rect.height);
		} else {
			return false;
		}
	}

	public boolean equals(float x, float y, float width, float height) {
		return (this.x == x && this.y == y && this.width == width && this.height == height);
	}

	public int getArea() {
		return width * height;
	}

	public Line[] getLines() {
		final Line[] lines = new Line[4];
		lines[0] = new Line(getMinX(), getMinY(), getMinX(), getMaxY());
		lines[1] = new Line(getMinX(), getMaxY(), getMaxX(), getMaxY());
		lines[2] = new Line(getMaxX(), getMaxY(), getMaxX(), getMinY());
		lines[3] = new Line(getMaxX(), getMinY(), getMinX(), getMinY());
		return lines;
	}

	/**
	 * 检查是否包含指定坐标
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	@Override
	public boolean contains(float x, float y) {
		return contains(x, y, 1f, 1f);
	}

	/**
	 * 检查是否包含指定坐标
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean contains(float x, float y, float width, float height) {
		return (x >= this.x && y >= this.y && ((x + width) <= (this.x + this.width))
				&& ((y + height) <= (this.y + this.height)));
	}

	/**
	 * 检查是否包含指定坐标
	 * 
	 * @param rect
	 * @return
	 */
	public boolean contains(RectBox rect) {
		return contains(rect.x, rect.y, rect.width, rect.height);
	}

	public boolean contains(LObject<?> rect) {
		return contains(rect.getCollisionArea());
	}

	public boolean contains(Circle circle) {
		float xmin = circle.x - circle.boundingCircleRadius;
		float xmax = xmin + 2f * circle.boundingCircleRadius;
		float ymin = circle.y - circle.boundingCircleRadius;
		float ymax = ymin + 2f * circle.boundingCircleRadius;
		return ((xmin > x && xmin < x + width) && (xmax > x && xmax < x + width))
				&& ((ymin > y && ymin < y + height) && (ymax > y && ymax < y + height));
	}

	public boolean contains(Vector2f v) {
		return contains(v.x, v.y);
	}

	public boolean contains(Vector3f v) {
		return contains(v.x, v.y);
	}

	public boolean contains(Vector4f v) {
		return contains(v.x, v.y);
	}

	public boolean contains(Point point) {
		if (this.x < point.x && this.x + this.width > point.x && this.y < point.y && this.y + this.height > point.y) {
			return true;
		}
		return false;
	}

	public boolean contains(PointF point) {
		if (this.x < point.x && this.x + this.width > point.x && this.y < point.y && this.y + this.height > point.y) {
			return true;
		}
		return false;
	}

	public boolean contains(PointI point) {
		if (this.x < point.x && this.x + this.width > point.x && this.y < point.y && this.y + this.height > point.y) {
			return true;
		}
		return false;
	}

	/**
	 * 判定矩形选框交集
	 * 
	 * @param rect
	 * @return
	 */
	public boolean intersects(RectBox rect) {
		return intersects(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * 判定矩形选框交集
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean intersects(float x, float y) {
		return intersects(x, y, 1f, 1f);
	}

	/**
	 * 判定矩形选框交集
	 * 
	 * @param xy
	 * @return
	 */
	public boolean intersects(XY xy) {
		if (xy == null) {
			return false;
		}
		return intersects(xy.getX(), xy.getY());
	}

	/**
	 * 判定矩形选框交集
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public boolean intersects(float x, float y, float width, float height) {
		return x + width > this.x && x < this.x + this.width && y + height > this.y && y < this.y + this.height;
	}

	/**
	 * 设定矩形选框交集
	 * 
	 * @param rect
	 */
	public RectBox intersection(RectBox rect) {
		return intersection(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * 设定矩形选框交集
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public RectBox intersection(float x, float y, float width, float height) {
		int x1 = (int) MathUtils.max(this.x, x);
		int y1 = (int) MathUtils.max(this.y, y);
		int x2 = (int) MathUtils.min(this.x + this.width - 1, x + width - 1);
		int y2 = (int) MathUtils.min(this.y + this.height - 1, y + height - 1);
		return setBounds(x1, y1, MathUtils.max(0, x2 - x1 + 1), MathUtils.max(0, y2 - y1 + 1));
	}

	/**
	 * 判定指定坐标是否位于当前RectBox内部
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean inside(int x, int y) {
		return (x >= this.x) && ((x - this.x) < this.width) && (y >= this.y) && ((y - this.y) < this.height);
	}

	/**
	 * 返回当前的矩形选框交集
	 * 
	 * @param rect
	 * @return
	 */
	public RectBox getIntersection(RectBox rect) {
		int x1 = (int) MathUtils.max(x, rect.x);
		int x2 = (int) MathUtils.min(x + width, rect.x + rect.width);
		int y1 = (int) MathUtils.max(y, rect.y);
		int y2 = (int) MathUtils.min(y + height, rect.y + rect.height);
		return new RectBox(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * 合并矩形选框
	 * 
	 * @param rect
	 */
	public RectBox union(RectBox rect) {
		return union(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * 合并矩形选框
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public RectBox union(float x, float y, float width, float height) {
		int x1 = (int) MathUtils.min(this.x, x);
		int y1 = (int) MathUtils.min(this.y, y);
		int x2 = (int) MathUtils.max(this.x + this.width - 1, x + width - 1);
		int y2 = (int) MathUtils.max(this.y + this.height - 1, y + height - 1);
		setBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
		return this;
	}

	@Override
	protected void createPoints() {

		float useWidth = width;
		float useHeight = height;
		points = new float[8];

		points[0] = x;
		points[1] = y;

		points[2] = x + useWidth;
		points[3] = y;

		points[4] = x + useWidth;
		points[5] = y + useHeight;

		points[6] = x;
		points[7] = y + useHeight;

		maxX = points[2];
		maxY = points[5];
		minX = points[0];
		minY = points[1];
		findCenter();
		calculateRadius();
	}

	@Override
	public Shape transform(Matrix3 transform) {
		checkPoints();
		Polygon resultPolygon = new Polygon();
		float result[] = new float[points.length];
		transform.transform(points, 0, result, 0, points.length / 2);
		resultPolygon.points = result;
		resultPolygon.findCenter();
		resultPolygon.checkPoints();
		return resultPolygon;
	}

	/**
	 * 水平移动X坐标执行长度
	 * 
	 * @param xMod
	 */
	public final RectBox modX(float xMod) {
		x += xMod;
		this.pointsDirty = true;
		this.checkPoints();
		return this;
	}

	/**
	 * 水平移动Y坐标指定长度
	 * 
	 * @param yMod
	 */
	public final RectBox modY(float yMod) {
		y += yMod;
		this.pointsDirty = true;
		this.checkPoints();
		return this;
	}

	/**
	 * 水平移动Width指定长度
	 * 
	 * @param w
	 */
	public RectBox modWidth(float w) {
		this.width += w;
		this.pointsDirty = true;
		this.checkPoints();
		return this;
	}

	/**
	 * 水平移动Height指定长度
	 * 
	 * @param h
	 */
	public RectBox modHeight(float h) {
		this.height += h;
		this.pointsDirty = true;
		this.checkPoints();
		return this;
	}

	/**
	 * 判断指定坐标是否在一条直线上
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public final boolean intersectsLine(final float x1, final float y1, final float x2, final float y2) {
		return contains(x1, y1) || contains(x2, y2);
	}

	/**
	 * 判定指定坐标是否位于当前RectBox内部
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean inside(float x, float y) {
		return (x >= this.x) && ((x - this.x) < this.width) && (y >= this.y) && ((y - this.y) < this.height);
	}

	public RectBox cpy() {
		return new RectBox(this.x, this.y, this.width, this.height);
	}

	public RectBox createIntersection(RectBox rectBox) {
		RectBox dest = new RectBox();
		dest.intersection(rectBox);
		intersect(this, rectBox, dest);
		return dest;
	}

	public float maxX() {
		return x() + width();
	}

	public float maxY() {
		return y() + height();
	}

	public RectBox mergeXY(RectBox rect, XY pos) {

		float minX = MathUtils.min(rect.x, x);
		float maxX = MathUtils.max(rect.getRight(), x);

		rect.x = minX;
		rect.width = MathUtils.floor(maxX - minX);

		float minY = MathUtils.min(rect.y, y);
		float maxY = MathUtils.max(rect.getBottom(), y);

		rect.y = minY;
		rect.height = MathUtils.floor(maxY - minY);

		return rect;
	}

	@Override
	public boolean isEmpty() {
		return getWidth() <= 0 || height() <= 0;
	}

	public RectBox setEmpty() {
		return this.setBounds(0f, 0f, 0f, 0f);
	}

	public RectBox offset(Point point) {
		x += point.x;
		y += point.y;
		this.pointsDirty = true;
		checkPoints();
		return this;
	}

	public RectBox offset(PointF point) {
		x += point.x;
		y += point.y;
		this.pointsDirty = true;
		checkPoints();
		return this;
	}

	public RectBox offset(PointI point) {
		x += point.x;
		y += point.y;
		this.pointsDirty = true;
		checkPoints();
		return this;
	}

	public RectBox inc(RectBox view) {
		if (view == null) {
			return cpy();
		}
		return new RectBox(x + view.x, y + view.y, width + view.width, height + view.height);
	}

	public RectBox sub(RectBox view) {
		if (view == null) {
			return cpy();
		}
		return new RectBox(x - view.x, y - view.y, width - view.width, height - view.height);
	}

	public RectBox mul(RectBox view) {
		if (view == null) {
			return cpy();
		}
		return new RectBox(x * view.x, y * view.y, width * view.width, height * view.height);
	}

	public RectBox div(RectBox view) {
		if (view == null) {
			return cpy();
		}
		return new RectBox(x / view.x, y / view.y, width / view.width, height / view.height);
	}

	public RectBox inc(float v) {
		return new RectBox(x + v, y + v, width + v, height + v);
	}

	public RectBox sub(float v) {
		return new RectBox(x - v, y - v, width - v, height - v);
	}

	public RectBox mul(float v) {
		return new RectBox(x * v, y * v, width * v, height * v);
	}

	public RectBox div(float v) {
		return new RectBox(x / v, y / v, width / v, height / v);
	}

	public RectBox add(float px, float py) {
		float x1 = MathUtils.min(x, px);
		float x2 = MathUtils.max(x + width, px);
		float y1 = MathUtils.min(y, py);
		float y2 = MathUtils.max(y + height, py);
		setBounds(x1, y1, x2 - x1, y2 - y1);
		return this;
	}

	public RectBox add(Vector2f v) {
		return add(v.x, v.y);
	}

	public RectBox add(RectBox r) {
		int tx2 = this.width;
		int ty2 = this.height;
		if ((tx2 | ty2) < 0) {
			setBounds(r.x, r.y, r.width, r.height);
		}
		int rx2 = r.width;
		int ry2 = r.height;
		if ((rx2 | ry2) < 0) {
			return this;
		}
		float tx1 = this.x;
		float ty1 = this.y;
		tx2 += tx1;
		ty2 += ty1;
		float rx1 = r.x;
		float ry1 = r.y;
		rx2 += rx1;
		ry2 += ry1;
		if (tx1 > rx1) {
			tx1 = rx1;
		}
		if (ty1 > ry1) {
			ty1 = ry1;
		}
		if (tx2 < rx2) {
			tx2 = rx2;
		}
		if (ty2 < ry2) {
			ty2 = ry2;
		}
		tx2 -= tx1;
		ty2 -= ty1;
		if (tx2 > Integer.MAX_VALUE) {
			tx2 = Integer.MAX_VALUE;
		}
		if (ty2 > Integer.MAX_VALUE) {
			ty2 = Integer.MAX_VALUE;
		}
		setBounds(tx1, ty1, tx2, ty2);
		return this;
	}

	public DirtyRect getDirtyRect() {
		return new DirtyRect(this);
	}

	public float getAspectRatio() {
		return (height == 0) ? MathUtils.NaN : (float) width / (float) height;
	}

	public float area() {
		return this.width * this.height;
	}

	@Override
	public float perimeter() {
		return 2f * (this.width + this.height);
	}

	public TArray<PointF> getMarchingAnts(float step, float quantity) {
		if (step == -1f) {
			step = perimeter() / quantity;
		} else {
			quantity = MathUtils.round(perimeter() / step);
		}
		final TArray<PointF> result = new TArray<PointF>();
		float x = getX();
		float y = getY();
		int face = 0;
		for (int i = 0; i < quantity; i++) {
			result.add(new PointF(x, y));
			switch (face) {
			case 0:
				x += step;
				if (x >= getRight()) {
					face = 1;
					y += (x - getRight());
					x = getRight();
				}
				break;
			case 1:
				y += step;
				if (y >= getBottom()) {
					face = 2;
					x -= (y - getBottom());
					y = getBottom();
				}
				break;
			case 2:
				x -= step;
				if (x <= getLeft()) {
					face = 3;
					y -= (getLeft() - x);
					x = getLeft();
				}
				break;

			case 3:
				y -= step;
				if (y <= getTop()) {
					face = 0;
					y = getTop();
				}
				break;
			}
		}
		return result;
	}

	public RectBox random() {
		final int w = LSystem.viewSize.getWidth();
		final int h = LSystem.viewSize.getHeight();
		this.x = MathUtils.random(0f, w);
		this.y = MathUtils.random(0f, h);
		this.width = MathUtils.random(0, w);
		this.height = MathUtils.random(0, h);
		return this;
	}

	public RectBox toPixels(XY point) {
		return toPixels(this, point);
	}

	public RectBox toPixels(int tileWidth, int tileHeight) {
		return toPixels(this, tileWidth, tileHeight);
	}

	public RectBox toTitle(final XY point) {
		return toTitle(this, point);
	}

	public RectBox toTitle(int tileWidth, int tileHeight) {
		return toTitle(this, tileWidth, tileHeight);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + NumberUtils.floatToIntBits(x);
		result = prime * result + NumberUtils.floatToIntBits(y);
		result = prime * result + NumberUtils.floatToIntBits(width);
		result = prime * result + NumberUtils.floatToIntBits(height);
		return result;
	}

	@Override
	public String toString() {
		StringKeyValue builder = new StringKeyValue("RectBox");
		builder.kv("x", x).comma().kv("y", y).comma().kv("width", width).comma().kv("height", height).comma()
				.kv("left", Left()).comma().kv("right", Right()).comma().kv("top", Top()).comma()
				.kv("bottom", Bottom());
		return builder.toString();
	}

}
