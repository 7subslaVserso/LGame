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
package loon.action.sprite;

import java.util.Comparator;

import loon.Director.Origin;
import loon.LObject;
import loon.LSysException;
import loon.LSystem;
import loon.LTexture;
import loon.PlayerUtils;
import loon.Screen;
import loon.action.ActionBind;
import loon.action.ActionListener;
import loon.action.ActionTween;
import loon.action.PlaceActions;
import loon.action.collision.CollisionObject;
import loon.action.map.Field2D;
import loon.action.sprite.Sprites.Created;
import loon.canvas.LColor;
import loon.events.ResizeListener;
import loon.geom.Affine2f;
import loon.geom.BoxSize;
import loon.geom.Circle;
import loon.geom.Dimension;
import loon.geom.Ellipse;
import loon.geom.Line;
import loon.geom.RectBox;
import loon.geom.Triangle2f;
import loon.geom.Vector2f;
import loon.geom.XY;
import loon.opengl.GLEx;
import loon.utils.IArray;
import loon.utils.LayerSorter;
import loon.utils.MathUtils;
import loon.utils.StrBuilder;
import loon.utils.TArray;

/**
 * 一个精灵类的具体实现,可以用来充当ECS模式中的实体对象用类(当然,Loon中并不强制要求使用ECS模式进行开发)
 */
public class Entity extends LObject<IEntity> implements CollisionObject, IEntity, IArray, BoxSize {

	private static final int CHILDREN_CAPACITY_DEFAULT = 4;

	protected Origin _origin = Origin.CENTER;
	protected boolean _visible = true;
	protected boolean _deform = true;
	protected boolean _ignoreUpdate = false;
	protected boolean _childrenVisible = true;
	protected boolean _childrenIgnoreUpdate = false;
	protected boolean _childrenSortPending = false;
	protected boolean _debugDraw = false;

	protected boolean _followRotation = true;
	protected boolean _followScale = true;
	protected boolean _followColor = true;

	protected int _idxTag = Entity.TAG_INVALID;

	protected boolean _repaintDraw = false;

	// 是否传递本身偏移设定数据到自绘部分
	protected boolean _repaintAutoOffset = false;

	protected TArray<IEntity> _childrens;

	protected RectBox _shear;
	protected LColor _baseColor = new LColor(LColor.white);
	private LColor _debugDrawColor = LColor.red;

	protected Vector2f _offset = new Vector2f();
	private boolean _createShadow;
	private boolean _xySort;

	protected float _fixedWidthOffset = 0f;
	protected float _fixedHeightOffset = 0f;

	protected float _rotationCenterX = -1;
	protected float _rotationCenterY = -1;

	protected float _scaleX = 1f;
	protected float _scaleY = 1f;

	protected float _scaleCenterX = -1;
	protected float _scaleCenterY = -1;

	protected float _skewX = 0;
	protected float _skewY = 0;

	protected float _skewCenterX = -1;
	protected float _skewCenterY = -1;

	protected boolean _flipX = false, _flipY = false;

	private final static LayerSorter<IEntity> entitySorter = new LayerSorter<IEntity>(false);

	private ResizeListener<IEntity> _resizeListener;

	private boolean _stopUpdate = false;

	private Sprites _sprites = null;

	protected float _width, _height;

	protected LTexture _image;

	public Entity() {
		this((LTexture) null);
	}

	public Entity(final String path) {
		this(LSystem.loadTexture(path));
	}

	public Entity(final String path, final float x, final float y) {
		this(LSystem.loadTexture(path), x, y);
	}

	public Entity(final LTexture texture) {
		this(texture, 0, 0, texture == null ? 0 : texture.getWidth(), texture == null ? 0 : texture.getHeight());
	}

	public Entity(final LTexture texture, final float x, final float y) {
		this(texture, x, y, texture == null ? 0 : texture.getWidth(), texture == null ? 0 : texture.getHeight());
	}

	public Entity(final LTexture texture, final float x, final float y, final float w, final float h) {
		this.setLocation(x, y);
		this._width = w;
		this._height = h;
		this._image = texture;
	}

	public static Entity make(LTexture texture, final float x, final float y) {
		return new Entity(texture, x, y);
	}

	public static Entity make(String path, final float x, final float y) {
		return new Entity(LSystem.loadTexture(path), x, y);
	}

	public static Entity make(String path) {
		return new Entity(LSystem.loadTexture(path), 0, 0);
	}

	protected void onUpdateColor() {

	}

	public IEntity setTexture(String path) {
		return setTexture(LSystem.loadTexture(path));
	}

	public IEntity setTexture(LTexture tex) {
		this._image = tex;
		if (_width <= 0) {
			_width = _image.width();
		}
		if (_height <= 0) {
			_height = _image.height();
		}
		this._repaintDraw = (tex == null);
		return this;
	}

	@Override
	public boolean isVisible() {
		return this._visible;
	}

	@Override
	public void setVisible(final boolean v) {
		this._visible = v;
	}

	@Override
	public boolean isChildrenVisible() {
		return this._childrenVisible;
	}

	@Override
	public IEntity setChildrenVisible(final boolean v) {
		this._childrenVisible = v;
		return this;
	}

	@Override
	public boolean isIgnoreUpdate() {
		return this._ignoreUpdate;
	}

	@Override
	public void setIgnoreUpdate(final boolean u) {
		this._ignoreUpdate = u;
	}

	@Override
	public boolean isChildrenIgnoreUpdate() {
		return this._childrenIgnoreUpdate;
	}

	@Override
	public IEntity setChildrenIgnoreUpdate(final boolean c) {
		this._childrenIgnoreUpdate = c;
		return this;
	}

	@Override
	public int getIndexTag() {
		return this._idxTag;
	}

	@Override
	public IEntity setIndexTag(final int idx) {
		this._idxTag = idx;
		return this;
	}

	@Override
	public boolean isRotated() {
		return this._objectRotation != 0f;
	}

	@Override
	public float getRotationCenterX() {
		return this._rotationCenterX;
	}

	@Override
	public float getRotationCenterY() {
		return this._rotationCenterY;
	}

	@Override
	public void setRotationCenterX(final float sx) {
		this._rotationCenterX = sx;
	}

	@Override
	public void setRotationCenterY(final float sy) {
		this._rotationCenterY = sy;
	}

	@Override
	public void setRotationCenter(final float sx, final float sy) {
		this._rotationCenterX = sx;
		this._rotationCenterY = sy;
	}

	@Override
	public float getPivotX() {
		return this._rotationCenterX;
	}

	@Override
	public float getPivotY() {
		return this._rotationCenterY;
	}

	@Override
	public void setPivotX(final float rx) {
		this._rotationCenterX = rx;
		this._scaleCenterX = rx;
	}

	@Override
	public void setPivotY(final float ry) {
		this._rotationCenterY = ry;
		this._scaleCenterY = ry;
	}

	@Override
	public void setPivot(final float rx, final float ry) {
		setPivotX(rx);
		setPivotY(ry);
	}

	public IEntity setAnchor(final float scale) {
		return setAnchor(scale, scale);
	}

	public IEntity setAnchor(final float sx, final float sy) {
		setPivot(_width * sx, _height * sy);
		return this;
	}

	public IEntity coord(float x, float y) {
		setLocation(x, y);
		return this;
	}

	@Override
	public boolean isScaled() {
		return (this._scaleX != 1) || (this._scaleY != 1);
	}

	@Override
	public float getScaleX() {
		return this._scaleX;
	}

	@Override
	public float getScaleY() {
		return this._scaleY;
	}

	@Override
	public void setScaleX(final float pScaleX) {
		this._scaleX = pScaleX;
	}

	@Override
	public void setScaleY(final float pScaleY) {
		this._scaleY = pScaleY;
	}

	protected void onScale() {
	}

	@Override
	public void setScale(final float pScale) {
		this.setScale(pScale, pScale);
	}

	@Override
	public void setScale(final float pScaleX, final float pScaleY) {
		this._scaleX = pScaleX;
		this._scaleY = pScaleY;
		if (_childrens != null) {
			for (int i = this._childrens.size - 1; i >= 0; i--) {
				final IEntity child = this._childrens.get(i);
				if (child != null && child.isFollowScale() && child != this) {
					child.setScale(pScaleX, pScaleY);
				}
			}
		}
		this.onScale();
	}

	@Override
	public float getScaleCenterX() {
		return this._scaleCenterX;
	}

	@Override
	public float getScaleCenterY() {
		return this._scaleCenterY;
	}

	@Override
	public void setScaleCenterX(final float sx) {
		this._scaleCenterX = sx;
	}

	@Override
	public void setScaleCenterY(final float sy) {
		this._scaleCenterY = sy;
	}

	@Override
	public void setScaleCenter(final float sx, final float sy) {
		this._scaleCenterX = sx;
		this._scaleCenterY = sy;
	}

	@Override
	public boolean isSkewed() {
		return (this._skewX != 0) || (this._skewY != 0);
	}

	@Override
	public float getSkewX() {
		return this._skewX;
	}

	@Override
	public float getSkewY() {
		return this._skewY;
	}

	@Override
	public void setSkewX(final float sx) {
		this._skewX = sx;
	}

	@Override
	public void setSkewY(final float sy) {
		this._skewY = sy;
	}

	@Override
	public void setSkew(final float pSkew) {
		this._skewX = pSkew;
		this._skewY = pSkew;
	}

	@Override
	public void setSkew(final float sx, final float sy) {
		this._skewX = sx;
		this._skewY = sy;
	}

	@Override
	public float getSkewCenterX() {
		return this._skewCenterX;
	}

	@Override
	public float getSkewCenterY() {
		return this._skewCenterY;
	}

	@Override
	public void setSkewCenterX(final float sx) {
		this._skewCenterX = sx;
	}

	@Override
	public void setSkewCenterY(final float sy) {
		this._skewCenterY = sy;
	}

	@Override
	public void setSkewCenter(final float sx, final float sy) {
		this._skewCenterX = sx;
		this._skewCenterY = sy;
	}

	@Override
	public boolean isRotatedOrScaledOrSkewed() {
		return (this._objectRotation != 0) || (this._scaleX != 1) || (this._scaleY != 1) || (this._skewX != 0)
				|| (this._skewY != 0);
	}

	@Override
	public float getRed() {
		return this._baseColor.r;
	}

	@Override
	public float getGreen() {
		return this._baseColor.g;
	}

	@Override
	public float getBlue() {
		return this._baseColor.b;
	}

	@Override
	public float getAlpha() {
		return super.getAlpha();
	}

	@Override
	public LColor getColor() {
		return this._baseColor.cpy();
	}

	private void updateAlpha(final float alpha) {
		if (_childrens != null) {
			for (int i = _childrens.size - 1; i > -1; i--) {
				IEntity entity = _childrens.get(i);
				if (entity != null && entity.isFollowColor() && entity != this) {
					entity.setAlpha(alpha);
				}
			}
		}
	}

	private void updateColor(final LColor color) {
		if (_childrens != null) {
			for (int i = _childrens.size - 1; i > -1; i--) {
				IEntity entity = _childrens.get(i);
				if (entity != null && entity.isFollowColor() && entity != this) {
					entity.setColor(color);
				}
			}
		}
	}

	@Override
	public void setColor(final LColor pColor) {
		this._baseColor.setColor(pColor);
		this.updateColor(_baseColor);
		this.onUpdateColor();
	}

	@Override
	public void setColor(final int pColor) {
		this._baseColor.setColor(pColor);
		this.updateColor(_baseColor);
		this.onUpdateColor();
	}

	@Override
	public void setColor(final float r, final float g, final float b) {
		this._baseColor.setColor(r, g, b);
		this.updateColor(_baseColor);
		this.onUpdateColor();

	}

	@Override
	public void setColor(final float r, final float g, final float b, final float a) {
		this._baseColor.setColor(r, g, b, a);
		this.updateColor(_baseColor);
		this.onUpdateColor();
	}

	@Override
	public void setAlpha(final float a) {
		super.setAlpha(a);
		this._baseColor.a = a;
		this.updateAlpha(a);
		this.onUpdateColor();
	}

	@Override
	public int getChildCount() {
		if (this._childrens == null) {
			return 0;
		}
		return this._childrens.size;
	}

	@Override
	public IEntity getChildByTag(final int idx) {
		if (this._childrens == null) {
			return null;
		}
		for (int i = this._childrens.size - 1; i >= 0; i--) {
			final IEntity child = this._childrens.get(i);
			if (child.getIndexTag() == idx) {
				return child;
			}
		}
		return null;
	}

	@Override
	public IEntity getChildByIndex(final int pIndex) {
		if (this._childrens == null) {
			return null;
		}
		return this._childrens.get(pIndex);
	}

	@Override
	public IEntity getFirstChild() {
		if (this._childrens == null) {
			return null;
		}
		return this._childrens.get(0);
	}

	@Override
	public IEntity getLastChild() {
		if (this._childrens == null) {
			return null;
		}
		return this._childrens.get(this._childrens.size - 1);
	}

	@Override
	public IEntity addChild(final IEntity e) {
		if (e == null) {
			return this;
		}
		if (e == this) {
			return this;
		}
		if (this._childrens == null) {
			this.allocateChildren();
		}
		this._childrens.add(e);
		e.setParent(this);
		e.setSprites(this._sprites);
		e.setState(State.ADDED);
		e.onAttached();
		return this;
	}

	@Override
	public IEntity addChildAt(final IEntity e, float x, float y) {
		if (e != null) {
			e.setLocation(x, y);
			addChild(e);
		}
		return this;
	}

	@Override
	public IEntity sortChildren() {
		return this.sortChildren(true);
	}

	@Override
	public IEntity sortChildren(final boolean i) {
		if (this._childrens == null) {
			return this;
		}
		if (i) {
			entitySorter.sort(this._childrens);
		} else {
			this._childrenSortPending = true;
		}
		return this;
	}

	@Override
	public IEntity sortChildren(final Comparator<IEntity> e) {
		if (this._childrens == null) {
			return this;
		}
		entitySorter.sort(this._childrens, e);
		return this;
	}

	@Override
	public boolean removeSelf() {
		final IEntity parent = this._objectSuper;
		if (parent != null) {
			return parent.removeChild(this);
		} else {
			return false;
		}
	}

	@Override
	public IEntity removeChildren() {
		if (this._childrens == null) {
			return this;
		}
		for (int i = this._childrens.size - 1; i >= 0; i--) {
			final IEntity removed = this._childrens.get(i);
			if (removed != null) {
				removed.setState(State.REMOVED);
				removed.onDetached();
			}
			// 删除精灵同时，删除缓动动画
			if (removed != null && removed instanceof ActionBind) {
				removeActionEvents((ActionBind) removed);
			}
		}
		this._childrens.clear();
		return this;
	}

	@Override
	public boolean removeChild(final IEntity e) {
		if (e == null) {
			return true;
		}
		if (this._childrens == null) {
			return false;
		}
		boolean removed = this._childrens.remove(e);
		if (removed) {
			e.setState(State.REMOVED);
			e.onDetached();
		}
		// 删除精灵同时，删除缓动动画
		if (removed && e instanceof ActionBind) {
			removeActionEvents((ActionBind) e);
		}
		return removed;
	}

	@Override
	public IEntity removeChild(final int idx) {
		if (this._childrens == null) {
			return null;
		}
		for (int i = this._childrens.size - 1; i >= 0; i--) {
			if (this._childrens.get(i).getIndexTag() == idx) {
				final IEntity removed = this._childrens.removeIndex(i);
				if (removed != null) {
					removed.setState(State.REMOVED);
					removed.onDetached();
				}
				// 删除精灵同时，删除缓动动画
				if (removed != null && (removed instanceof ActionBind)) {
					removeActionEvents((ActionBind) removed);
				}
				return removed;
			}
		}
		return null;
	}

	@Override
	public void onAttached() {

	}

	@Override
	public void onDetached() {

	}

	@Override
	public Object getUserData() {
		return this.Tag;
	}

	@Override
	public IEntity setUserData(final Object u) {
		this.Tag = u;
		return this;
	}

	@Override
	public final void createUI(final GLEx g) {
		this.createUI(g, 0f, 0f);
	}

	@Override
	public final void createUI(final GLEx g, final float offsetX, final float offsetY) {
		if (this._visible) {
			this.onManagedPaint(g, offsetX, offsetY);
		}
	}

	@Override
	public IEntity reset() {
		this._visible = true;
		this._debugDraw = false;
		this._ignoreUpdate = false;
		this._childrenVisible = true;
		this._childrenIgnoreUpdate = false;

		this._objectRotation = 0f;
		this._previousRotation = 0f;
		this._scaleX = 1f;
		this._scaleY = 1f;
		this._skewX = 0f;
		this._skewY = 0f;
		this._flipX = false;
		this._flipY = false;

		this._baseColor.reset();

		if (this._childrens != null) {
			final TArray<IEntity> entities = this._childrens;
			for (int i = entities.size - 1; i >= 0; i--) {
				IEntity e = entities.get(i);
				if (e != null) {
					e.reset();
				}
			}
		}
		return this;
	}

	public float getScreenX() {
		float x = 0;
		ISprite parent = _objectSuper;
		if (parent != null) {
			x += parent.getX();
			for (; (parent = parent.getParent()) != null;) {
				x += parent.getX();
			}
		}
		return x + getX();
	}

	public float getScreenY() {
		float y = 0;
		ISprite parent = _objectSuper;
		if (parent != null) {
			y += parent.getY();
			for (; (parent = parent.getParent()) != null;) {
				y += parent.getY();
			}
		}
		return y + getY();
	}

	public float getCurrentX() {
		return this.getX() + _offset.x;
	}

	public float getCurrentY() {
		return this.getY() + _offset.y;
	}

	public float centerX() {
		return getX() + (getWidth() / 2f);
	}

	public float centerY() {
		return getY() + (getHeight() / 2f);
	}

	protected void prePaint(final GLEx g) {

	}

	public void paint(final GLEx g) {
		paint(g, 0, 0);
	}

	public void paint(final GLEx g, float offsetX, float offsetY) {
		if (_objectAlpha < 0.01f) {
			return;
		}
		final boolean exist = _image != null || (_width > 0 && _height > 0) || _repaintDraw;
		if (exist) {
			final boolean update = ((_objectRotation != 0 || !(_scaleX == 1f && _scaleY == 1f)
					|| !(_skewX == 0 && _skewY == 0)) || _flipX || _flipY) && _deform;
			final float nx = drawX(offsetX);
			final float ny = drawY(offsetY);
			if (update) {
				g.saveTx();
				g.saveBrush();
				Affine2f tx = g.tx();
				final float rotation = this._objectRotation;
				final float scaleX = this._scaleX;
				final float scaleY = this._scaleY;
				if (rotation != 0) {
					final float rotationCenterX = this._rotationCenterX == -1 ? (nx + _origin.ox(this._width))
							: nx + this._rotationCenterX;
					final float rotationCenterY = this._rotationCenterY == -1 ? (ny + _origin.oy(this._height))
							: ny + this._rotationCenterY;
					tx.translate(rotationCenterX, rotationCenterY);
					tx.preRotate(rotation);
					tx.translate(-rotationCenterX, -rotationCenterY);
				}
				final boolean flipX = this._flipX;
				final boolean flipY = this._flipY;
				if (flipX || flipY) {
					final float rotationCenterX = this._rotationCenterX == -1 ? (nx + _origin.ox(this._width))
							: nx + this._rotationCenterX;
					final float rotationCenterY = this._rotationCenterY == -1 ? (ny + _origin.oy(this._height))
							: ny + this._rotationCenterY;
					if (flipX && flipY) {
						Affine2f.transform(tx, rotationCenterX, rotationCenterY, Affine2f.TRANS_ROT180);
					} else if (flipX) {
						Affine2f.transform(tx, rotationCenterX, rotationCenterY, Affine2f.TRANS_MIRROR);
					} else if (flipY) {
						Affine2f.transform(tx, rotationCenterX, rotationCenterY, Affine2f.TRANS_MIRROR_ROT180);
					}
				}
				if ((scaleX != 1) || (scaleY != 1)) {
					final float scaleCenterX = this._scaleCenterX == -1 ? (nx + _origin.ox(this._width))
							: nx + this._scaleCenterX;
					final float scaleCenterY = this._scaleCenterY == -1 ? (ny + _origin.oy(this._height))
							: ny + this._scaleCenterY;
					tx.translate(scaleCenterX, scaleCenterY);
					tx.preScale(scaleX, scaleY);
					tx.translate(-scaleCenterX, -scaleCenterY);
				}
				final float skewX = this._skewX;
				final float skewY = this._skewY;
				if ((skewX != 0) || (skewY != 0)) {
					final float skewCenterX = this._skewCenterX == -1 ? (nx + _origin.ox(this._width))
							: nx + this._skewCenterX;
					final float skewCenterY = this._skewCenterY == -1 ? (ny + _origin.oy(this._height))
							: ny + this._skewCenterY;
					tx.translate(skewCenterX, skewCenterY);
					tx.preShear(skewX, skewY);
					tx.translate(-skewCenterX, -skewCenterY);
				}
			}
			if (_repaintDraw) {
				final boolean elastic = (_shear != null);
				if (elastic) {
					g.setClip(drawX(offsetX + _shear.x), drawY(offsetY + _shear.y), _shear.width, _shear.height);
				}
				final float tmp = g.alpha();
				g.setAlpha(_objectAlpha);
				if (_repaintAutoOffset) {
					repaint(g, nx, ny);
				} else {
					repaint(g, offsetX, offsetY);
				}
				g.setAlpha(tmp);
				if (elastic) {
					g.clearClip();
				}
			} else {
				if (_image != null) {
					if (_shear == null) {
						g.draw(_image, nx, ny, _width, _height, _baseColor);
					} else {
						g.draw(_image, nx, ny, _width, _height, _shear.x, _shear.y, _shear.width, _shear.height,
								_baseColor);
					}
				} else {
					g.fillRect(nx, ny, _width, _height, _baseColor);
				}
			}
			if (_debugDraw) {
				g.drawRect(nx, ny, _width, _height, _debugDrawColor);
			}
			if (update) {
				g.restoreBrush();
				g.restoreTx();
			}
		}
	}

	public IEntity setRepaint(boolean r) {
		this._repaintDraw = r;
		return this;
	}

	public boolean isRepaint() {
		return this._repaintDraw;
	}

	protected void repaint(GLEx g, float offsetX, float offsetY) {

	}

	protected void postPaint(final GLEx g) {

	}

	private void allocateChildren() {
		this._childrens = new TArray<IEntity>(Entity.CHILDREN_CAPACITY_DEFAULT);
	}

	protected void onManagedPaint(final GLEx g, float offsetX, float offsetY) {
		final TArray<IEntity> children = this._childrens;
		if (!this._childrenVisible || (children == null)) {
			this.prePaint(g);
			this.paint(g, offsetX, offsetY);
			this.postPaint(g);
		} else {
			if (this._childrenSortPending) {
				entitySorter.sort(this._childrens);
				this._childrenSortPending = false;
			}
			final int childCount = children.size;
			this.prePaint(g);
			this.paint(g, offsetX, offsetY);
			for (int i = 0; i < childCount; i++) {
				final IEntity child = children.get(i);
				if (child != null) {
					float px = 0, py = 0;
					ISprite parent = child.getParent();
					if (parent == null) {
						parent = this;
					}
					px += parent.getX() + parent.getOffsetX();
					py += parent.getY() + parent.getOffsetY();
					for (; (parent = parent.getParent()) != null;) {
						px += parent.getX() + parent.getOffsetX();
						py += parent.getY() + parent.getOffsetY();
					}
					child.createUI(g, px + offsetX + child.getOffsetX(), py + offsetY + child.getOffsetY());
				}
			}
			this.postPaint(g);
		}
	}

	protected void onManagedUpdate(final long elapsedTime) {
		if (_stopUpdate) {
			return;
		}
		if ((this._childrens != null) && !this._childrenIgnoreUpdate) {
			final TArray<IEntity> entities = this._childrens;
			final int entityCount = entities.size;
			for (int i = 0; i < entityCount; i++) {
				entities.get(i).update(elapsedTime);
			}
		}
		onUpdate(elapsedTime);
	}

	protected void onUpdate(final long elapsedTime) {

	}

	@Override
	public void update(long elapsedTime) {
		if (!this._ignoreUpdate) {
			this.onManagedUpdate(elapsedTime);
		}
	}

	public boolean isCollision(Entity o) {
		RectBox src = getCollisionArea();
		RectBox dst = o.getCollisionArea();
		if (src.intersects(dst) || src.contains(dst)) {
			return true;
		}
		return false;
	}

	public int width() {
		return (int) getWidth();
	}

	public int height() {
		return (int) getHeight();
	}

	@Override
	public float getWidth() {
		return _width > 1 ? (_width * this._scaleX) - _fixedWidthOffset
				: _image == null ? 0 : (_image.getWidth() * this._scaleX) - _fixedWidthOffset;
	}

	@Override
	public float getHeight() {
		return _height > 1 ? (_height * this._scaleY) - _fixedHeightOffset
				: _image == null ? 0 : (_image.getHeight() * this._scaleY) - _fixedHeightOffset;
	}

	@Override
	public Field2D getField2D() {
		return null;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return _childrens != null && _childrens.size > 0;
	}

	@Override
	public boolean inContains(float x, float y, float w, float h) {
		return getCollisionBox().contains(x, y, w, h);
	}

	@Override
	public RectBox getRectBox() {
		return getCollisionBox();
	}

	@Override
	public RectBox getCollisionBox() {
		return getRect(x(), y(), getWidth(), getHeight());
	}

	@Override
	public LTexture getBitmap() {
		return _image;
	}

	public void clearImage() {
		if (_image != null) {
			_image.close();
			_image = null;
		}
	}

	@Override
	public void setWidth(float w) {
		if (w != this._width) {
			this.onResize();
		}
		this._width = MathUtils.max(1f, w);
	}

	@Override
	public void setHeight(float h) {
		if (h != this._height) {
			this.onResize();
		}
		this._height = MathUtils.max(1f, h);
	}

	public IEntity setSize(float w, float h) {
		if (this._width != w || this._height != h) {
			this._width = MathUtils.max(1f, w);
			this._height = MathUtils.max(1f, h);
			this.onResize();
		}
		return this;
	}

	public IEntity setBounds(RectBox rect) {
		return this.setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	public IEntity setBounds(float x, float y, float width, float height) {
		this.setLocation(x, y);
		this.setSize(width, height);
		return this;
	}

	public Dimension getDimension() {
		return new Dimension(this._width * this._scaleX, this._height * this._scaleY);
	}

	private void allocateShear() {
		if (_shear == null) {
			_shear = new RectBox(0, 0, this._width, this._height);
		}
	}

	public RectBox getClip() {
		return getShear();
	}

	public RectBox getShear() {
		allocateShear();
		return _shear;
	}

	public IEntity setShear(RectBox s) {
		allocateShear();
		this._shear.setBounds(s);
		return this;
	}

	public IEntity setShear(float x, float y, float w, float h) {
		allocateShear();
		this._shear.setBounds(x, y, w, h);
		return this;
	}

	public IEntity setClip(float x, float y, float w, float h) {
		setShear(x, y, w, h);
		return this;
	}

	public boolean isDeform() {
		return _deform;
	}

	public IEntity setDeform(boolean d) {
		this._deform = d;
		return this;
	}

	@Override
	public void setParent(ISprite s) {
		if (s instanceof IEntity) {
			setParent((IEntity) s);
		} else if (s instanceof ISprite) {
			setParent(new SpriteToEntity(s));
		}
	}

	public Origin getOrigin() {
		return _origin;
	}

	public IEntity setOrigin(Origin o) {
		this._origin = o;
		return this;
	}

	public boolean isStopUpdate() {
		return _stopUpdate;
	}

	public IEntity setStopUpdate(boolean s) {
		this._stopUpdate = s;
		return this;
	}

	@Override
	public IEntity setFlipX(boolean x) {
		this._flipX = x;
		return this;
	}

	@Override
	public IEntity setFlipY(boolean y) {
		this._flipY = y;
		return this;
	}

	@Override
	public IEntity setFlipXY(boolean x, boolean y) {
		setFlipX(x);
		setFlipY(y);
		return this;
	}

	@Override
	public boolean isFlipX() {
		return _flipX;
	}

	@Override
	public boolean isFlipY() {
		return _flipY;
	}

	@Override
	public boolean collides(ISprite e) {
		if (e == null || !e.isVisible()) {
			return false;
		}
		return intersects(e.getCollisionBox());
	}

	@Override
	public boolean collidesX(ISprite other) {
		if (other == null || !other.isVisible()) {
			return false;
		}
		RectBox rectSelf = getRectBox();
		RectBox a = new RectBox(rectSelf.getX(), 0, rectSelf.getWidth(), rectSelf.getHeight());
		RectBox rectDst = getRectBox();
		RectBox b = new RectBox(rectDst.getX(), 0, rectDst.getWidth(), rectDst.getHeight());
		return a.intersects(b);
	}

	@Override
	public boolean collidesY(ISprite other) {
		if (other == null || !other.isVisible()) {
			return false;
		}
		RectBox rectSelf = getRectBox();
		RectBox a = new RectBox(0, rectSelf.getY(), rectSelf.getWidth(), rectSelf.getHeight());
		RectBox rectDst = getRectBox();
		RectBox b = new RectBox(0, rectDst.getY(), rectDst.getWidth(), rectDst.getHeight());
		return a.intersects(b);
	}

	@Override
	public void toString(final StrBuilder s) {
		s.append(super.toString());
		if ((this._childrens != null) && (this._childrens.size > 0)) {
			s.append(LSystem.LS);
			s.append(" [");
			final TArray<IEntity> entities = this._childrens;
			for (int i = 0; i < entities.size; i++) {
				entities.get(i).toString(s);
				if (i < (entities.size - 1)) {
					s.append(", ");
				}
			}
			s.append("]");
		}
	}

	@Override
	public String toString() {
		final StrBuilder sbr = new StrBuilder();
		this.toString(sbr);
		return sbr.toString();
	}

	@Override
	public ActionTween selfAction() {
		return PlayerUtils.set(this);
	}

	@Override
	public boolean isActionCompleted() {
		return PlayerUtils.isActionCompleted(this);
	}

	@Override
	public int size() {
		return _childrens == null ? 0 : _childrens.size;
	}

	@Override
	public void clear() {
		if (_childrens != null) {
			removeChildren();
		}
	}

	@Override
	public boolean isEmpty() {
		return (_childrens == null ? true : _childrens.size == 0);
	}

	public Vector2f getOffset() {
		return _offset;
	}

	public IEntity setOffset(float x, float y) {
		this._offset.set(x, y);
		return this;
	}

	@Override
	public IEntity setOffset(Vector2f offset) {
		this._offset = offset;
		return this;
	}

	@Override
	public float getOffsetX() {
		return _offset.x;
	}

	public IEntity setOffsetX(float offsetX) {
		this._offset.setX(offsetX);
		return this;
	}

	@Override
	public float getOffsetY() {
		return _offset.y;
	}

	public IEntity setOffsetY(float offsetY) {
		this._offset.setY(offsetY);
		return this;
	}

	protected float drawX(float offsetX) {
		return offsetX + this._objectLocation.x + _offset.x;
	}

	protected float drawY(float offsetY) {
		return offsetY + this._objectLocation.y + _offset.y;
	}

	@Override
	public float getCenterX() {
		return getX() + getWidth() / 2f;
	}

	@Override
	public float getCenterY() {
		return getY() + getHeight() / 2f;
	}

	public IEntity in() {
		return in(30);
	}

	public IEntity in(float speed) {
		this.setAlpha(0f);
		this.selfAction().fadeIn(speed).start();
		return this;
	}

	public IEntity out() {
		return out(30);
	}

	public IEntity out(float speed) {
		this.selfAction().fadeOut(speed).start().setActionListener(new ActionListener() {

			@Override
			public void stop(ActionBind o) {
				if (getParent() != null) {
					getParent().removeChild((IEntity) o);
				}
				if (getScreen() != null) {
					getScreen().remove((ISprite) o);
				}
				if (_sprites != null) {
					_sprites.remove((ISprite) o);
				}
				close();
			}

			@Override
			public void start(ActionBind o) {

			}

			@Override
			public void process(ActionBind o) {

			}
		});
		return this;
	}

	@Override
	public ISprite setSprites(Sprites ss) {
		if (this._sprites == ss) {
			return this;
		}
		this._sprites = ss;
		return this;
	}

	@Override
	public Sprites getSprites() {
		return this._sprites;
	}

	private float toPixelScaleX(float x) {
		return MathUtils.iceil(x / _scaleX);
	}

	private float toPixelScaleY(float y) {
		return MathUtils.iceil(y / _scaleY);
	}

	public Vector2f getUITouch(float x, float y) {
		return getUITouch(x, y, null);
	}

	public Vector2f getUITouch(float x, float y, Vector2f pointResult) {
		if (!(x == -1 && y == -1 && pointResult != null)) {
			if (pointResult == null) {
				pointResult = new Vector2f(x, y);
			} else {
				pointResult.set(x, y);
			}
		}
		float newX = 0f;
		float newY = 0f;
		IEntity parent = getParent();
		if (parent != null) {
			newX = pointResult.x - parent.getX() - getX();
			newY = pointResult.y - parent.getX() - getY();
		} else {
			newX = pointResult.x - getX();
			newY = pointResult.y - getY();
		}
		final float angle = getRotation();
		if (angle == 0 || angle == 360) {
			pointResult.x = toPixelScaleX(newX);
			pointResult.y = toPixelScaleY(newY);
			return pointResult;
		}
		float oldWidth = _width;
		float oldHeight = _height;
		float newWidth = getWidth();
		float newHeight = getHeight();
		float offX = oldWidth / 2f - newWidth / 2f;
		float offY = oldHeight / 2f - newHeight / 2f;
		float posX = (newX - offX);
		float posY = (newY - offY);
		if (angle == 90) {
			offX = oldHeight / 2f - newWidth / 2f;
			offY = oldWidth / 2f - newHeight / 2f;
			posX = (newX - offY);
			posY = (newY - offX);
			pointResult.set(posX / getScaleX(), posY / getScaleY()).rotateSelf(90);
			pointResult.set(-pointResult.x, MathUtils.abs(pointResult.y - this._height));
		} else if (angle == -90) {
			offX = oldHeight / 2f - newWidth / 2f;
			offY = oldWidth / 2f - newHeight / 2f;
			posX = (newX - offY);
			posY = (newY - offX);
			pointResult.set(posX / getScaleX(), posY / getScaleY()).rotateSelf(-90);
			pointResult.set(-(pointResult.x - this._width), MathUtils.abs(pointResult.y));
		} else if (angle == -180 || angle == 180) {
			pointResult.set(posX / getScaleX(), posY / getScaleY()).rotateSelf(getRotation()).addSelf(_width, _height);
		} else {
			float rad = MathUtils.toRadians(angle);
			float sin = MathUtils.sin(rad);
			float cos = MathUtils.cos(rad);
			float dx = offX / getScaleX();
			float dy = offY / getScaleY();
			float dx2 = cos * dx - sin * dy;
			float dy2 = sin * dx + cos * dy;
			pointResult.x = _width - (newX - dx2);
			pointResult.y = _height - (newY - dy2);
		}
		return pointResult;
	}

	protected void onRotation() {
	}

	public IEntity addGroup(Created<? extends IEntity> s, int count) {
		if (s == null) {
			return this;
		}
		for (int i = 0; i < count; i++) {
			addChild(s.make());
		}
		return this;
	}

	public IEntity addGroup(LTexture tex, int count) {
		for (int i = 0; i < count; i++) {
			addChild(new Entity(tex));
		}
		return this;
	}

	public IEntity addGroup(String path, int count) {
		for (int i = 0; i < count; i++) {
			addChild(new Entity(path));
		}
		return this;
	}

	public IEntity rect(RectBox rect) {
		return rect(rect, 0);
	}

	public IEntity rect(RectBox rect, int shift) {
		PlaceActions.rect(this, rect, shift);
		return this;
	}

	public IEntity triangle(Triangle2f t) {
		return triangle(t, 1);
	}

	public IEntity triangle(Triangle2f t, int stepRate) {
		PlaceActions.triangle(this, t, stepRate);
		return this;
	}

	public IEntity circle(Circle circle) {
		return circle(circle, -1f, -1f);
	}

	public IEntity circle(Circle circle, float startAngle, float endAngle) {
		PlaceActions.circle(this, circle, startAngle, endAngle);
		return this;
	}

	public IEntity ellipse(Ellipse e) {
		return ellipse(e, -1f, -1f);
	}

	public Entity ellipse(Ellipse e, float startAngle, float endAngle) {
		PlaceActions.ellipse(this, e, startAngle, endAngle);
		return this;
	}

	public IEntity line(Line e) {
		PlaceActions.line(this, e);
		return this;
	}

	public IEntity rotateAround(XY point, float angle) {
		PlaceActions.rotateAround(this, point, angle);
		return this;
	}

	public IEntity rotateAroundDistance(XY point, float angle, float distance) {
		PlaceActions.rotateAroundDistance(this, point, angle, distance);
		return this;
	}

	@Override
	public void setRotation(float rotate) {
		super.setRotation(rotate);
		if (_childrens != null) {
			for (int i = _childrens.size - 1; i > -1; i--) {
				IEntity entity = _childrens.get(i);
				if (entity != null && entity.isFollowRotation() && entity != this) {
					entity.setRotation(rotate);
				}
			}
		}
		onRotation();
	}

	@Override
	public Screen getScreen() {
		if (this._sprites == null) {
			return LSystem.getProcess().getScreen();
		}
		return this._sprites.getScreen() == null ? LSystem.getProcess().getScreen() : this._sprites.getScreen();
	}

	@Override
	public float getContainerX() {
		if (_objectSuper != null) {
			return getScreenX() - getX();
		}
		return this._sprites == null ? super.getContainerX() : this._sprites.getX();
	}

	@Override
	public float getContainerY() {
		if (_objectSuper != null) {
			return getScreenX() - getY();
		}
		return this._sprites == null ? super.getContainerY() : this._sprites.getY();
	}

	@Override
	public float getContainerWidth() {
		return this._sprites == null ? super.getContainerWidth() : this._sprites.getWidth();
	}

	@Override
	public float getContainerHeight() {
		return this._sprites == null ? super.getContainerHeight() : this._sprites.getHeight();
	}

	@Override
	public RectBox getBoundingRect() {
		return getCollisionBox();
	}

	@Override
	public boolean containsPoint(float x, float y) {
		return inContains(x, y, 1, 1);
	}

	@Override
	public boolean intersects(CollisionObject o) {
		return getCollisionBox().intersects(o.getRectBox());
	}

	@Override
	public boolean intersects(RectBox rect) {
		return getCollisionBox().intersects(rect);
	}

	public IEntity softCenterOn(float x, float y) {
		final RectBox rect = getSprites() == null ? LSystem.viewSize.getRect() : getSprites().getBoundingBox();
		final IEntity sprite = this.getSuper();
		if (x != 0) {
			float dx = (x - rect.getWidth() / 2 / this.getScaleX() - this.getX()) / 3;
			if (sprite != null) {
				RectBox boundingBox = sprite.getRectBox();
				if (this.getX() + dx < boundingBox.getMinX()) {
					setX(boundingBox.getMinX() / this.getScaleX());
				} else if (this.getX() + dx > (boundingBox.getMaxX() - rect.getWidth()) / this.getScaleX()) {
					setX(MathUtils.max(boundingBox.getMaxX() - rect.getWidth(), boundingBox.getMinX())
							/ this.getScaleX());
				} else {
					this.setX(this.getX() + dx);
				}
			} else {
				this.setX(this.getX() + dx);
			}
		}
		if (y != 0) {
			float dy = (y - rect.getHeight() / 2 / this.getScaleY() - this.getY()) / 3;
			if (sprite != null) {
				RectBox boundingBox = sprite.getRectBox();
				if (this.getY() + dy < boundingBox.getMinY()) {
					this.setY(boundingBox.getMinY() / this.getScaleY());
				} else if (this.getY() + dy > (boundingBox.getMaxY() - rect.getHeight()) / this.getScaleY()) {
					this.setY(MathUtils.max(boundingBox.getMaxY() - rect.getHeight(), boundingBox.getMinY())
							/ this.getScaleY());
				} else {
					this.setY(this.getY() + dy);
				}
			} else {
				this.setY(this.getY() + dy);
			}
		}
		return this;
	}

	@Override
	public boolean showShadow() {
		return _createShadow;
	}

	public Entity createShadow(boolean s) {
		this._createShadow = s;
		return this;
	}

	public boolean isDebugDraw() {
		return _debugDraw;
	}

	public ISprite setDebugDraw(boolean debug) {
		this._debugDraw = debug;
		return this;
	}

	public ISprite debug() {
		return setDebugDraw(true);
	}

	public LColor getDebugDrawColor() {
		return _debugDrawColor.cpy();
	}

	public ISprite setDebugDrawColor(LColor debugColor) {
		if (debugColor == null) {
			return this;
		}
		this._debugDrawColor = debugColor;
		return this;
	}

	public ResizeListener<IEntity> getResizeListener() {
		return _resizeListener;
	}

	public IEntity setResizeListener(ResizeListener<IEntity> listener) {
		this._resizeListener = listener;
		return this;
	}

	@Override
	public void onResize() {
		if (_resizeListener != null) {
			_resizeListener.onResize(this);
		}
		if (_childrens != null) {
			for (int i = this._childrens.size - 1; i >= 0; i--) {
				final IEntity child = this._childrens.get(i);
				if (child != null && child != this) {
					child.onResize();
				}
			}
		}
	}

	@Override
	public IEntity setFollowRotation(boolean r) {
		this._followRotation = r;
		return this;
	}

	@Override
	public IEntity setFollowScale(boolean s) {
		this._followScale = s;
		return this;
	}

	@Override
	public boolean isFollowRotation() {
		return _followRotation;
	}

	@Override
	public boolean isFollowScale() {
		return _followScale;
	}

	@Override
	public boolean isFollowColor() {
		return _followColor;
	}

	@Override
	public float getFixedWidthOffset() {
		return _fixedWidthOffset;
	}

	@Override
	public ISprite setFixedWidthOffset(float fixedWidthOffset) {
		this._fixedWidthOffset = fixedWidthOffset;
		return this;
	}

	@Override
	public float getFixedHeightOffset() {
		return _fixedHeightOffset;
	}

	@Override
	public ISprite setFixedHeightOffset(float fixedHeightOffset) {
		this._fixedHeightOffset = fixedHeightOffset;
		return this;
	}

	public boolean isRepaintAutoOffset() {
		return _repaintAutoOffset;
	}

	public IEntity setRepaintAutoOffset(boolean autoOffset) {
		this._repaintAutoOffset = autoOffset;
		return this;
	}

	public boolean isMirror() {
		return this._flipX;
	}

	public IEntity setMirror(boolean mirror) {
		return setFlipX(mirror);
	}

	public boolean isDescendantOf(ISprite actor) {
		if (actor == null) {
			throw new LSysException("Actor cannot be null");
		}
		ISprite parent = this;
		for (;;) {
			if (parent == null) {
				return false;
			}
			if (parent == actor) {
				return true;
			}
			parent = parent.getParent();
		}
	}

	public boolean isAscendantOf(ISprite actor) {
		if (actor == null) {
			throw new LSysException("Actor cannot be null");
		}
		for (;;) {
			if (actor == null) {
				return false;
			}
			if (actor == this) {
				return true;
			}
			actor = actor.getParent();
		}
	}

	@Override
	public TArray<IEntity> getChildren() {
		return _childrens;
	}

	public boolean isClosed() {
		return isDisposed();
	}

	@Override
	public IEntity setFollowColor(boolean c) {
		this._followColor = c;
		return this;
	}

	@Override
	public boolean autoXYSort() {
		return _xySort;
	}

	public Entity setAutoXYSort(boolean a) {
		this._xySort = a;
		return this;
	}

	@Override
	public void close() {
		if (!isDisposed()) {
			if (_image != null) {
				_image.close();
				_image = null;
			}
		}
		_stopUpdate = false;
		_resizeListener = null;
		setState(State.DISPOSED);
		removeChildren();
		removeActionEvents(this);
	}

}
