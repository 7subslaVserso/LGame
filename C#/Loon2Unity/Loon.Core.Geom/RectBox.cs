using Loon.Utils;
using System;
using Loon.Java;
using UnityEngine;

namespace Loon.Core.Geom {
	
	public class RectBox : Shape {
	
		public class Rect2i {
	
			public int left;
	
			public int top;
	
			public int right;
	
			public int bottom;

            public override int GetHashCode()
            {
                return JavaRuntime.IdentityHashCode(this);
            }

			public Rect2i() {
			}
	
			public Rect2i(int left_0, int top_1, int right_2, int bottom_3) {
				this.left = left_0;
				this.top = top_1;
				this.right = right_2;
				this.bottom = bottom_3;
			}
	
			public Rect2i(Rect2i r) {
				left = r.left;
				top = r.top;
				right = r.right;
				bottom = r.bottom;
			}
	
			public override bool Equals(object obj) {
				Rect2i r = (Rect2i) obj;
				if (r != null) {
					return left == r.left && top == r.top && right == r.right
							&& bottom == r.bottom;
				}
				return false;
			}
	
			public bool IsEmpty() {
				return left >= right || top >= bottom;
			}
	
			public int Width() {
				return right - left;
			}
	
			public int Height() {
				return bottom - top;
			}
	
			public int CenterX() {
				return (left + right) >> 1;
			}
	
			public int CenterY() {
				return (top + bottom) >> 1;
			}
	
			public float ExactCenterX() {
				return (left + right) * 0.5f;
			}
	
			public float ExactCenterY() {
				return (top + bottom) * 0.5f;
			}
	
			public void SetEmpty() {
				left = right = top = bottom = 0;
			}
	
			public void Set(int left_0, int top_1, int right_2, int bottom_3) {
				this.left = left_0;
				this.top = top_1;
				this.right = right_2;
				this.bottom = bottom_3;
			}
	
			public void Set(Rect2i src) {
				this.left = src.left;
				this.top = src.top;
				this.right = src.right;
				this.bottom = src.bottom;
			}
	
			public void Offset(int dx, int dy) {
				left += dx;
				top += dy;
				right += dx;
				bottom += dy;
			}
	
			public void OffsetTo(int newLeft, int newTop) {
				right += newLeft - left;
				bottom += newTop - top;
				left = newLeft;
				top = newTop;
			}
	
			public void Inset(int dx, int dy) {
				left += dx;
				top += dy;
				right -= dx;
				bottom -= dy;
			}
	
			public bool Contains(int x, int y) {
				return left < right && top < bottom && x >= left && x < right
						&& y >= top && y < bottom;
			}
	
			public bool Contains(int left_0, int top_1, int right_2, int bottom_3) {
				return this.left < this.right && this.top < this.bottom
						&& this.left <= left_0 && this.top <= top_1
						&& this.right >= right_2 && this.bottom >= bottom_3;
			}
	
			public bool Contains(Rect2i r) {
				return this.left < this.right && this.top < this.bottom
						&& left <= r.left && top <= r.top && right >= r.right
						&& bottom >= r.bottom;
			}
	
			public bool Intersect(int left_0, int top_1, int right_2, int bottom_3) {
				if (this.left < right_2 && left_0 < this.right && this.top < bottom_3
						&& top_1 < this.bottom) {
					if (this.left < left_0) {
						this.left = left_0;
					}
					if (this.top < top_1) {
						this.top = top_1;
					}
					if (this.right > right_2) {
						this.right = right_2;
					}
					if (this.bottom > bottom_3) {
						this.bottom = bottom_3;
					}
					return true;
				}
				return false;
			}
	
			public bool Intersect(Rect2i r) {
				return Intersect(r.left, r.top, r.right, r.bottom);
			}
	
			public bool SetIntersect(Rect2i a, Rect2i b) {
				if (a.left < b.right && b.left < a.right && a.top < b.bottom
						&& b.top < a.bottom) {
					left = Math.Max(a.left,b.left);
					top = Math.Max(a.top,b.top);
					right = Math.Min(a.right,b.right);
					bottom = Math.Min(a.bottom,b.bottom);
					return true;
				}
				return false;
			}
	
			public bool Intersects(int left_0, int top_1, int right_2, int bottom_3) {
				return this.left < right_2 && left_0 < this.right && this.top < bottom_3
						&& top_1 < this.bottom;
			}
	
			public static bool Intersects(Rect2i a, Rect2i b) {
				return a.left < b.right && b.left < a.right && a.top < b.bottom
						&& b.top < a.bottom;
			}
	
			public void Union(int left_0, int top_1, int right_2, int bottom_3) {
				if ((left_0 < right_2) && (top_1 < bottom_3)) {
					if ((this.left < this.right) && (this.top < this.bottom)) {
						if (this.left > left_0)
							this.left = left_0;
						if (this.top > top_1)
							this.top = top_1;
						if (this.right < right_2)
							this.right = right_2;
						if (this.bottom < bottom_3)
							this.bottom = bottom_3;
					} else {
						this.left = left_0;
						this.top = top_1;
						this.right = right_2;
						this.bottom = bottom_3;
					}
				}
			}
	
			public void Union(Rect2i r) {
				Union(r.left, r.top, r.right, r.bottom);
			}
	
			public void Union(int x, int y) {
				if (x < left) {
					left = x;
				} else if (x > right) {
					right = x;
				}
				if (y < top) {
					top = y;
				} else if (y > bottom) {
					bottom = y;
				}
			}
	
			public void Sort() {
				if (left > right) {
					int temp = left;
					left = right;
					right = temp;
				}
				if (top > bottom) {
					int temp_0 = top;
					top = bottom;
					bottom = temp_0;
				}
			}
	
			public void Scale(float scale) {
				if (scale != 1.0f) {
					left = (int) (left * scale + 0.5f);
					top = (int) (top * scale + 0.5f);
					right = (int) (right * scale + 0.5f);
					bottom = (int) (bottom * scale + 0.5f);
				}
			}
	
		}

        public override int GetHashCode()
        {
            return JavaRuntime.IdentityHashCode(this);
        }

		public int width;
	
		public int height;
	
		public void Offset(Vector2f offset) {
			x += offset.x;
			y += offset.y;
		}
	
		public void Offset(int offsetX, int offsetY) {
			x += offsetX;
			y += offsetY;
		}
	
		public int Left() {
			return this.X();
		}
	
		public int Right() {
			return (int) (this.x + this.width);
		}
	
		public int Top() {
			return this.Y();
		}
	
		public int Bottom() {
			return (int) (this.y + this.height);
		}
	
		public RectBox() {
			SetBounds(0, 0, 0, 0);
		}
	
		public RectBox(int x, int y, int width_0, int height_1) {
			SetBounds(x, y, width_0, height_1);
		}
	
		public RectBox(float x, float y, float width_0, float height_1) {
			SetBounds(x, y, width_0, height_1);
		}
	
		public RectBox(double x, double y, double width_0, double height_1) {
			SetBounds(x, y, width_0, height_1);
		}
	
		public RectBox(RectBox rect) {
			SetBounds(rect.x, rect.y, rect.width, rect.height);
		}
	
		public void SetBounds(RectBox rect) {
			SetBounds(rect.x, rect.y, rect.width, rect.height);
		}
	
		public void SetBounds(double x, double y, double width_0, double height_1) {
			SetBounds((float) x, (float) y, (float) width_0, (float) height_1);
		}
	
		public void SetBounds(float x, float y, float width_0, float height_1) {
			this.type = ShapeType.BOX_SHAPE;
			this.x = x;
			this.y = y;
			this.width = (int) width_0;
			this.height = (int) height_1;
			this.minX = x;
			this.minY = y;
			this.maxX = x + width_0;
			this.maxY = y + height_1;
			this.pointsDirty = true;
			this.CheckPoints();
		}
	
		public void Inflate(int horizontalValue, int verticalValue) {
			this.x -= horizontalValue;
			this.y -= verticalValue;
			this.width += horizontalValue * 2;
			this.height += verticalValue * 2;
		}
	
		public void SetLocation(RectBox r) {
			this.x = r.x;
			this.y = r.y;
		}
	
		public void SetLocation(Point r) {
			this.x = r.x;
			this.y = r.y;
		}
	
		public void SetLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}
	
		public void Grow(float h, float v) {
			SetX(GetX() - h);
			SetY(GetY() - v);
			SetWidth(GetWidth() + (h * 2));
			SetHeight(GetHeight() + (v * 2));
		}
	
		public void ScaleGrow(float h, float v) {
			Grow(GetWidth() * (h - 1), GetHeight() * (v - 1));
		}
	
		public override void SetScale(float sx, float sy) {
			if (scaleX != sx || scaleY != sy) {
				SetSize(width * (scaleX = sx), height * (scaleY * sy));
			}
		}
	
		public void SetSize(float width_0, float height_1) {
			SetWidth(width_0);
			SetHeight(height_1);
		}
	
		public bool Overlaps(RectBox rectangle) {
			return !(x > rectangle.x + rectangle.width || x + width < rectangle.x
					|| y > rectangle.y + rectangle.height || y + height < rectangle.y);
		}
	
		public int X() {
			return (int) x;
		}
	
		public int Y() {
			return (int) y;
		}
	
		public override float GetX() {
			return x;
		}
	
		public override void SetX(float x) {
			this.x = x;
		}
	
		public override float GetY() {
			return y;
		}
	
		public override void SetY(float y) {
			this.y = y;
		}
	
		public void Copy(RectBox other) {
			this.x = other.x;
			this.y = other.y;
			this.width = other.width;
			this.height = other.height;
		}
	
		public override float GetMinX() {
			return GetX();
		}
	
		public override float GetMinY() {
			return GetY();
		}
	
		public override float GetMaxX() {
			return this.x + this.width;
		}
	
		public override float GetMaxY() {
			return this.y + this.height;
		}
	
		public float GetRight() {
			return GetMaxX();
		}
	
		public float GetBottom() {
			return GetMaxY();
		}
	
		public float GetMiddleX() {
			return this.x + this.width / 2;
		}
	
		public float GetMiddleY() {
			return this.y + this.height / 2;
		}
	
		public override float GetCenterX() {
			return x + width / 2f;
		}
	
		public override float GetCenterY() {
			return y + height / 2f;
		}
	
		public static RectBox GetIntersection(RectBox a, RectBox b) {
			float a_x = a.GetX();
			float a_r = a.GetRight();
			float a_y = a.GetY();
			float a_t = a.GetBottom();
			float b_x = b.GetX();
			float b_r = b.GetRight();
			float b_y = b.GetY();
			float b_t = b.GetBottom();
			float i_x = MathUtils.Max(a_x, b_x);
			float i_r = MathUtils.Min(a_r, b_r);
			float i_y = MathUtils.Max(a_y, b_y);
			float i_t = MathUtils.Min(a_t, b_t);
			return (i_x < i_r && i_y < i_t) ? new RectBox(i_x, i_y, i_r - i_x, i_t
					- i_y) : null;
		}
	
		public static RectBox GetIntersection(RectBox a, RectBox b, RectBox result) {
			float a_x = a.GetX();
			float a_r = a.GetRight();
			float a_y = a.GetY();
			float a_t = a.GetBottom();
			float b_x = b.GetX();
			float b_r = b.GetRight();
			float b_y = b.GetY();
			float b_t = b.GetBottom();
			float i_x = MathUtils.Max(a_x, b_x);
			float i_r = MathUtils.Min(a_r, b_r);
			float i_y = MathUtils.Max(a_y, b_y);
			float i_t = MathUtils.Min(a_t, b_t);
			if (i_x < i_r && i_y < i_t) {
				result.SetBounds(i_x, i_y, i_r - i_x, i_t - i_y);
				return result;
			}
			return null;
		}
	
		public float[] ToFloat() {
			return new float[] { x, y, width, height };
		}
	
		public override RectBox GetRect() {
			return this;
		}

        private Rect rectangle = new Rect();

		public Rect GetRectangle2D()
        {
            rectangle.x = x;
            rectangle.y = y;
            rectangle.width = width;
            rectangle.height = height;
            return rectangle;
        }
	
		public override float GetHeight() {
			return height;
		}
	
		public void SetHeight(float height_0) {
			this.height = (int) height_0;
		}
	
		public override float GetWidth() {
			return width;
		}
	
		public void SetWidth(float width_0) {
			this.width = (int) width_0;
		}
	
		public override bool Equals(object obj) {
			if (obj  is  RectBox) {
				RectBox rect = (RectBox) obj;
				return Equals(rect.x, rect.y, rect.width, rect.height);
			} else {
				return false;
			}
		}
	
		public bool Equals(float x, float y, float width_0, float height_1) {
			return (this.x == x && this.y == y && this.width == width_0 && this.height == height_1);
		}
	
		public int GetArea() {
			return width * height;
		}
	
		public override bool Contains(float x, float y) {
			return Contains(x, y, 0, 0);
		}
	
		public bool Contains(float x, float y, float width_0, float height_1) {
			return (x >= this.x && y >= this.y
					&& ((x + width_0) <= (this.x + this.width)) && ((y + height_1) <= (this.y + this.height)));
		}
	
		public bool Contains(RectBox rect) {
			return Contains(rect.x, rect.y, rect.width, rect.height);
		}
	
		public bool Intersects(RectBox rect) {
			return Intersects(rect.x, rect.y, rect.width, rect.height);
		}
	
		public bool Intersects(int x, int y) {
			return Intersects(0, 0, width, height);
		}
	
		public bool Intersects(float x, float y, float width_0, float height_1) {
			return x + width_0 > this.x && x < this.x + this.width
					&& y + height_1 > this.y && y < this.y + this.height;
		}
	
		public void Intersection(RectBox rect) {
			Intersection(rect.x, rect.y, rect.width, rect.height);
		}
	
		public void Intersection(float x, float y, float width_0, float height_1) {
			int x1 = (int) MathUtils.Max(this.x, x);
			int y1 = (int) MathUtils.Max(this.y, y);
			int x2 = (int) MathUtils.Min(this.x + this.width - 1, x + width_0 - 1);
			int y2 = (int) MathUtils.Min(this.y + this.height - 1, y + height_1 - 1);
			SetBounds(x1, y1, Math.Max(0,x2 - x1 + 1), Math.Max(0,y2 - y1 + 1));
		}
	
		public bool Inside(int x, int y) {
			return (x >= this.x) && ((x - this.x) < this.width) && (y >= this.y)
					&& ((y - this.y) < this.height);
		}
	
		public RectBox GetIntersection(RectBox rect) {
			int x1 = (int) MathUtils.Max(x, rect.x);
			int x2 = (int) MathUtils.Min(x + width, rect.x + rect.width);
			int y1 = (int) MathUtils.Max(y, rect.y);
			int y2 = (int) MathUtils.Min(y + height, rect.y + rect.height);
			return new RectBox(x1, y1, x2 - x1, y2 - y1);
		}
	
		public void Union(RectBox rect) {
			Union(rect.x, rect.y, rect.width, rect.height);
		}
	
		public void Union(float x, float y, float width_0, float height_1) {
			int x1 = (int) MathUtils.Min(this.x, x);
			int y1 = (int) MathUtils.Min(this.y, y);
			int x2 = (int) MathUtils.Max(this.x + this.width - 1, x + width_0 - 1);
			int y2 = (int) MathUtils.Max(this.y + this.height - 1, y + height_1 - 1);
			SetBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
		}
	
		protected internal override void CreatePoints() {
	
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
			FindCenter();
			CalculateRadius();
	
		}
	
		public override Shape Transform(Matrix transform) {
			CheckPoints();
			Polygon resultPolygon = new Polygon();
			float[] result = new float[points.Length];
			transform.Transform(points, 0, result, 0, points.Length / 2);
			resultPolygon.points = result;
			resultPolygon.FindCenter();
			resultPolygon.CheckPoints();
			return resultPolygon;
		}
	
		public void ModX(float xMod) {
			x += xMod;
		}
	
		public void ModY(float yMod) {
			y += yMod;
		}
	
		public void ModWidth(float w) {
			this.width += (int)w;
		}
	
		public void ModHeight(float h) {
            this.height += (int)h;
		}
	
		public bool IntersectsLine(float x1, float y1,
				float x2, float y2) {
			return Contains(x1, y1) || Contains(x2, y2);
		}
	
		public bool Inside(float x, float y) {
			return (x >= this.x) && ((x - this.x) < this.width) && (y >= this.y)
					&& ((y - this.y) < this.height);
		}
	
	}
}
