/**
 * Copyright 2008 - 2010
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

import loon.LObject.State;

import loon.LRelease;
import loon.LSystem;
import loon.LTexture;
import loon.Screen;
import loon.Visible;
import loon.action.ActionBind;
import loon.action.ActionControl;
import loon.action.PlaceActions;
import loon.component.layout.Margin;
import loon.events.QueryEvent;
import loon.events.ResizeListener;
import loon.geom.Circle;
import loon.geom.DirtyRectList;
import loon.geom.Ellipse;
import loon.geom.Line;
import loon.geom.PointI;
import loon.geom.RectBox;
import loon.geom.Triangle2f;
import loon.geom.XY;
import loon.opengl.GLEx;
import loon.utils.CollectionUtils;
import loon.utils.IArray;
import loon.utils.LayerSorter;
import loon.utils.MathUtils;
import loon.utils.StringUtils;
import loon.utils.TArray;
import loon.utils.reply.Callback;

/**
 * 精灵精灵总父类，用来注册，控制，以及渲染所有精灵精灵（所有默认【不支持】触屏的精灵，被置于此。不过，
 * 当LNode系列精灵和SpriteBatchScreen合用时，也支持触屏.）
 * 
 */
public class Sprites extends PlaceActions implements IArray, Visible, LRelease {

	public static interface Created<T> {
		T make();
	}

	public static interface SpriteListener {

		public Sprites update(ISprite spr);

	}

	private final DirtyRectList _dirtyList = new DirtyRectList();

	protected ISprite[] _sprites;

	private boolean _createShadow;

	private ISpritesShadow _spriteShadow;

	private Margin _margin;

	private float _scrollX;

	private float _scrollY;

	private boolean _sortableChildren;

	private ResizeListener<Sprites> _resizeListener;

	private int _currentPoshash = 1;

	private int _lastPosHash = 1;

	private int viewX;

	private int viewY;

	private int viewWidth;

	private int viewHeight;

	private boolean _isViewWindowSet = false, _limitViewWindows = false, _visible = true, _closed = false;

	private SpriteListener sprListerner;

	private final static LayerSorter<ISprite> spriteLayerSorter = new LayerSorter<ISprite>();

	private final static SpriteSorter<ISprite> spriteXYSorter = new SpriteSorter<ISprite>();

	private int _size;

	private int _width, _height;

	private float _newLineHeight = -1f;

	private Screen _screen;

	private final String _sprites_name;

	private boolean _autoSortLayer;

	public Sprites(Screen screen, int w, int h) {
		this(null, screen, w, h);
	}

	public Sprites(Screen screen, float width, float height) {
		this(null, screen, (int) width, (int) height);
	}

	public Sprites(Screen screen) {
		this(null, screen, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public Sprites(String name, Screen screen, float w, float h) {
		this(name, screen, (int) w, (int) h);
	}

	public Sprites(String name, Screen screen, int w, int h) {
		this._screen = screen;
		this._sortableChildren = this._visible = true;
		this._sprites = new ISprite[CollectionUtils.INITIAL_CAPACITY];
		this._sprites_name = StringUtils.isEmpty(name) ? "Sprites" + LSystem.getSpritesSize() : name;
		this.setSize(w, h);
		LSystem.pushSpritesPool(this);
	}

	/**
	 * 设定Sprites大小
	 * 
	 * @param w
	 * @param h
	 * @return
	 */
	public Sprites setSize(int w, int h) {
		if (this._width != w || this._height != h) {
			this._width = w;
			this._height = h;
			if (this._width == 0) {
				this._width = 1;
			}
			if (this._height == 0) {
				this._height = 1;
			}
			if (viewWidth < this._width) {
				viewWidth = this._width;
			}
			if (viewHeight < this._height) {
				viewHeight = this._height;
			}
			this.resize(w, h, true);
		}
		return this;
	}

	/**
	 * 设定指定对象到图层最前
	 * 
	 * @param sprite
	 */
	public Sprites sendToFront(ISprite sprite) {
		if (_closed) {
			return this;
		}
		if (this._size <= 1 || this._sprites[0] == sprite) {
			return this;
		}
		if (_sprites[0] == sprite) {
			return this;
		}
		for (int i = 0; i < this._size; i++) {
			if (this._sprites[i] == sprite) {
				this._sprites = CollectionUtils.cut(this._sprites, i);
				this._sprites = CollectionUtils.expand(this._sprites, 1, false);
				this._sprites[0] = sprite;
				if (_sortableChildren) {
					this.sortSprites();
				}
				break;
			}
		}
		return this;
	}

	/**
	 * 设定指定对象到图层最后
	 * 
	 * @param sprite
	 */
	public Sprites sendToBack(ISprite sprite) {
		if (_closed) {
			return this;
		}
		if (this._size <= 1 || this._sprites[this._size - 1] == sprite) {
			return this;
		}
		if (_sprites[this._size - 1] == sprite) {
			return this;
		}
		for (int i = 0; i < this._size; i++) {
			if (this._sprites[i] == sprite) {
				this._sprites = CollectionUtils.cut(this._sprites, i);
				this._sprites = CollectionUtils.expand(this._sprites, 1, true);
				this._sprites[this._size - 1] = sprite;
				if (_sortableChildren) {
					this.sortSprites();
				}
				break;
			}
		}
		return this;
	}

	/**
	 * 按所在层级排序
	 * 
	 */
	public Sprites sortSprites() {
		if (this._closed) {
			return this;
		}
		if (this._size <= 1) {
			return this;
		}
		if (this._sprites.length != this._size) {
			ISprite[] sprs = CollectionUtils.copyOf(this._sprites, this._size);
			spriteLayerSorter.sort(sprs);
			this._sprites = sprs;
		} else {
			spriteLayerSorter.sort(this._sprites);
		}
		return this;
	}

	public Sprites setSortableChildren(boolean v) {
		this._sortableChildren = v;
		return this;
	}

	public boolean isSortableChildren() {
		return this._sortableChildren;
	}

	/**
	 * 扩充当前集合容量
	 * 
	 * @param capacity
	 */
	private void expandCapacity(int capacity) {
		if (_sprites.length < capacity) {
			ISprite[] bagArray = new ISprite[capacity];
			System.arraycopy(_sprites, 0, bagArray, 0, _size);
			_sprites = bagArray;
		}
	}

	/**
	 * 压缩当前集合容量
	 * 
	 * @param capacity
	 */
	private void compressCapacity(int capacity) {
		if (capacity + this._size < _sprites.length) {
			ISprite[] newArray = new ISprite[this._size + capacity];
			System.arraycopy(_sprites, 0, newArray, 0, this._size);
			_sprites = newArray;
		}
	}

	/**
	 * 查找指定位置的精灵对象
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ISprite find(int x, int y) {
		if (_closed) {
			return null;
		}
		ISprite[] snapshot = _sprites;
		for (int i = snapshot.length - 1; i >= 0; i--) {
			ISprite child = snapshot[i];
			if (child != null) {
				RectBox rect = child.getCollisionBox();
				if (rect != null && rect.contains(x, y)) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * 查找指定名称的精灵对象
	 * 
	 * @param name
	 * @return
	 */
	public ISprite find(String name) {
		if (_closed) {
			return null;
		}
		ISprite[] snapshot = _sprites;
		for (int i = snapshot.length - 1; i >= 0; i--) {
			ISprite child = snapshot[i];
			if (child != null) {
				String childName = child.getName();
				if (name.equals(childName)) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * 按照上一个精灵的x,y位置,另起一行添加精灵,并偏移指定位置
	 * 
	 * @param spr
	 * @param offX
	 * @param offY
	 * @return
	 */
	public ISprite addPadding(ISprite spr, float offX, float offY) {
		return addPadding(spr, offX, offY, 2);
	}

	/**
	 * 按照上一个精灵的y轴,另起一行添加精灵
	 * 
	 * @param spr
	 * @return
	 */
	public ISprite addCol(ISprite spr) {
		return addPadding(spr, 0, 0, 1);
	}

	/**
	 * 按照上一个精灵的y轴,另起一行添加精灵,并让y轴偏移指定位置
	 * 
	 * @param spr
	 * @param offY
	 * @return
	 */
	public ISprite addCol(ISprite spr, float offY) {
		return addPadding(spr, 0, offY, 1);
	}

	/**
	 * 按照上一个精灵的x轴,另起一行添加精灵
	 * 
	 * @param spr
	 * @return
	 */
	public ISprite addRow(ISprite spr) {
		return addPadding(spr, 0, 0, 0);
	}

	/**
	 * 按照上一个精灵的x轴,另起一行添加精灵,并将x轴偏移指定位置
	 * 
	 * @param spr
	 * @param offX
	 * @return
	 */
	public ISprite addRow(ISprite spr, float offX) {
		return addPadding(spr, offX, 0, 0);
	}

	/**
	 * 按照上一个精灵的x,y位置,另起一行添加精灵,并偏移指定位置
	 * 
	 * @param spr
	 * @param offX
	 * @param offY
	 * @param code
	 * @return
	 */
	public ISprite addPadding(ISprite spr, float offX, float offY, int code) {
		if (_closed) {
			return spr;
		}
		if (spr == null) {
			return null;
		}
		if (this == spr) {
			return spr;
		}

		float maxX = 0;
		float maxY = 0;

		ISprite tag = null;

		if (_size == 1) {
			ISprite cp = _sprites[0];
			if (cp != null && cp.getY() >= _newLineHeight) {
				maxX = cp.getX();
				maxY = cp.getY();
				tag = cp;
			}
		} else {
			for (int i = 0; i < _size; i++) {
				ISprite c = _sprites[i];
				if (c != null && c != spr && c.getY() >= _newLineHeight) {
					float oldMaxX = maxX;
					float oldMaxY = maxY;
					maxX = MathUtils.max(maxX, c.getX());
					maxY = MathUtils.max(maxY, c.getY());
					if (oldMaxX != maxX || oldMaxY != maxY) {
						tag = c;
					}
				}
			}

		}
		if (tag == null && _size > 0) {
			tag = _sprites[_size - 1];
		}

		if (tag != null && tag != spr) {
			switch (code) {
			case 0:
				spr.setLocation(maxX + tag.getWidth() + offX, maxY + offY);
				break;
			case 1:
				spr.setLocation(0 + offX, maxY + tag.getHeight() + offY);
				break;
			default:
				spr.setLocation(maxX + tag.getWidth() + offX, maxY + tag.getHeight() + offY);
				break;
			}

		} else {
			switch (code) {
			case 0:
				spr.setLocation(maxX + offX, maxY + offY);
				break;
			case 1:
				spr.setLocation(0 + offX, maxY + offY);
				break;
			default:
				spr.setLocation(maxX + offX, maxY + offY);
				break;
			}
		}

		add(spr);
		_newLineHeight = spr.getY();
		return spr;
	}

	/**
	 * 在指定索引处插入一个精灵
	 * 
	 * @param index
	 * @param sprite
	 * @return
	 */
	public boolean add(int index, ISprite sprite) {
		if (_closed) {
			return false;
		}
		if (sprite == null) {
			return false;
		}
		if (index > this._size) {
			index = this._size;
		}
		if (index == this._size) {
			return this.add(sprite);
		} else {
			if (sprite.getWidth() > getWidth()) {
				setViewWindow(viewX, viewY, (int) MathUtils.max(sprite.getWidth(), LSystem.viewSize.width), _height);
			}
			if (sprite.getHeight() > getHeight()) {
				setViewWindow(viewX, viewY, _width, (int) MathUtils.max(sprite.getWidth(), LSystem.viewSize.width));
			}
			System.arraycopy(this._sprites, index, this._sprites, index + 1, this._size - index);
			this._sprites[index] = sprite;
			if (++this._size >= this._sprites.length) {
				expandCapacity((_size + 1) * 2);
			}
			if (_sortableChildren) {
				sortSprites();
			}
			sprite.setState(State.ADDED);
			sprite.setSprites(this);
		}
		boolean result = _sprites[index] != null;
		return result;
	}

	public Sprites addAt(ISprite child, float x, float y) {
		if (_closed) {
			return this;
		}
		if (child != null) {
			child.setLocation(x, y);
			add(child);
		}
		return this;
	}

	public ISprite getSprite(int index) {
		if (_closed) {
			return null;
		}
		if (index < 0 || index > _size || index >= _sprites.length) {
			return null;
		}
		return _sprites[index];
	}

	/**
	 * 返回位于顶部的精灵
	 * 
	 * @return
	 */
	public ISprite getTopSprite() {
		if (_closed) {
			return null;
		}
		if (_size > 0) {
			return _sprites[0];
		}
		return null;
	}

	/**
	 * 返回位于底部的精灵
	 * 
	 * @return
	 */
	public ISprite getBottomSprite() {
		if (_closed) {
			return null;
		}
		if (_size > 0) {
			return _sprites[_size - 1];
		}
		return null;
	}

	/**
	 * 顺序添加精灵
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean add(ISprite sprite) {
		if (_closed) {
			return false;
		}
		if (sprite == null) {
			return false;
		}
		if (contains(sprite)) {
			return false;
		}
		sprite.setSprites(this);
		if (sprite.getWidth() > getWidth()) {
			setViewWindow(viewX, viewY, (int) MathUtils.max(sprite.getWidth(), LSystem.viewSize.width), _height);
		}
		if (sprite.getHeight() > getHeight()) {
			setViewWindow(viewX, viewY, _width, (int) MathUtils.max(sprite.getHeight(), LSystem.viewSize.height));
		}
		if (this._size == this._sprites.length) {
			expandCapacity((_size + 1) * 2);
		}
		boolean result = (_sprites[_size++] = sprite) != null;
		if (_sortableChildren) {
			sortSprites();
		}
		sprite.setState(State.ADDED);
		return result;
	}

	/**
	 * 顺序添加精灵
	 * 
	 * @param sprite
	 * @return
	 */
	public Sprites append(ISprite sprite) {
		add(sprite);
		return this;
	}

	/**
	 * 返回一组拥有指定标签的精灵
	 * 
	 * @param tags
	 * @return
	 */
	public TArray<ISprite> findTags(Object... tags) {
		if (_closed) {
			return null;
		}
		TArray<ISprite> list = new TArray<ISprite>();
		final int size = this._size;
		for (Object tag : tags) {
			for (int i = size - 1; i > -1; i--) {
				if (this._sprites[i] instanceof ISprite) {
					ISprite sp = (ISprite) this._sprites[i];
					if (sp.getTag() == tag || tag.equals(sp.getTag())) {
						list.add(sp);
					}
				}
			}
		}
		return list;
	}

	/**
	 * 返回一组没有指定标签的精灵
	 * 
	 * @param tags
	 * @return
	 */
	public TArray<ISprite> findNotTags(Object... tags) {
		if (_closed) {
			return null;
		}
		TArray<ISprite> list = new TArray<ISprite>();
		final int size = this._size;
		for (Object tag : tags) {
			for (int i = size - 1; i > -1; i--) {
				if (this._sprites[i] instanceof ISprite) {
					ISprite sp = this._sprites[i];
					if (sp != null) {
						if (!tag.equals(sp.getTag())) {
							list.add(sp);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * 返回一组指定名的精灵
	 * 
	 * @param names
	 * @return
	 */
	public TArray<ISprite> findNames(String... names) {
		if (_closed) {
			return null;
		}
		TArray<ISprite> list = new TArray<ISprite>();
		final int size = this._size;
		for (String name : names) {
			for (int i = size - 1; i > -1; i--) {
				if (this._sprites[i] instanceof ISprite) {
					ISprite sp = this._sprites[i];
					if (sp != null) {
						if (name.equals(sp.getName())) {
							list.add(sp);
						}
					}
				}
			}
		}
		return list;
	}

	public TArray<ISprite> findNameContains(String... names) {
		if (_closed) {
			return null;
		}
		TArray<ISprite> list = new TArray<ISprite>();
		final int size = this._size;
		for (String name : names) {
			for (int i = size - 1; i > -1; i--) {
				if (this._sprites[i] instanceof ISprite) {
					ISprite sp = this._sprites[i];
					if (sp != null) {
						String childName = sp.getName();
						if (childName.contains(name)) {
							list.add(sp);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * 返回一组没有指定名的精灵
	 * 
	 * @param names
	 * @return
	 */
	public TArray<ISprite> findNotNames(String... names) {
		if (_closed) {
			return null;
		}
		TArray<ISprite> list = new TArray<ISprite>();
		final int size = this._size;
		for (String name : names) {
			for (int i = size - 1; i > -1; i--) {
				ISprite child = this._sprites[i];
				if (child != null) {
					if (child instanceof ISprite) {
						ISprite sp = (ISprite) child;
						if (!name.equals(sp.getName())) {
							list.add(sp);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * 检查指定精灵是否存在
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean contains(ISprite sprite) {
		if (_closed) {
			return false;
		}
		if (sprite == null) {
			return false;
		}
		if (_sprites == null) {
			return false;
		}
		for (int i = 0; i < _size; i++) {
			ISprite sp = _sprites[i];
			boolean exist = (sp != null);
			if (exist && sprite.equals(sp)) {
				return true;
			}
			if (exist && sp instanceof Entity) {
				Entity superEntity = (Entity) sp;
				for (int j = 0; j < superEntity.getChildCount(); j++) {
					boolean superExist = (superEntity.getChildByIndex(j) != null);
					if (superExist && sp.equals(superEntity.getChildByIndex(j))) {
						return true;
					}
				}
			} else if (exist && sp instanceof Sprite) {
				Sprite superSprite = (Sprite) sp;
				for (int j = 0; j < superSprite.size(); j++) {
					boolean superExist = (superSprite.getChildByIndex(j) != null);
					if (superExist && sp.equals(superSprite.getChildByIndex(j))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 返回指定位置内的所有精灵
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public TArray<ISprite> contains(float x, float y, float w, float h) {
		TArray<ISprite> sprites = new TArray<ISprite>();
		if (_closed) {
			return sprites;
		}
		if (_sprites == null) {
			return sprites;
		}
		for (int i = 0; i < _size; i++) {
			ISprite sp = _sprites[i];
			if (sp != null) {
				if (sp.inContains(x, y, w, h)) {
					sprites.add(sp);
				}
			}
		}
		return sprites;
	}

	/**
	 * 返回包含指定位置的所有精灵
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public TArray<ISprite> contains(float x, float y) {
		return contains(x, y, 1f, 1f);
	}

	/**
	 * 返回包含指定精灵位置的所有精灵
	 * 
	 * @param sprite
	 * @return
	 */
	public TArray<ISprite> containsSprite(ISprite sprite) {
		if (sprite == null) {
			return new TArray<ISprite>(0);
		}
		return contains(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
	}

	/**
	 * 返回指定位置内的所有精灵
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public TArray<ISprite> intersects(float x, float y, float w, float h) {
		TArray<ISprite> sprites = new TArray<ISprite>();
		if (_closed) {
			return sprites;
		}
		if (_sprites == null) {
			return sprites;
		}
		for (int i = 0; i < _size; i++) {
			ISprite sp = _sprites[i];
			if (sp != null) {
				if (sp.getCollisionBox().intersects(x, y, w, h)) {
					sprites.add(sp);
				}
			}
		}
		return sprites;
	}

	/**
	 * 返回与指定位置相交的所有精灵
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public TArray<ISprite> intersects(float x, float y) {
		return intersects(x, y, 1f, 1f);
	}

	/**
	 * 返回与指定精灵位置相交的所有精灵
	 * 
	 * @param sprite
	 * @return
	 */
	public TArray<ISprite> intersectsSprite(ISprite sprite) {
		if (sprite == null) {
			return new TArray<ISprite>(0);
		}
		return intersects(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
	}

	/**
	 * 删除指定索引处精灵
	 * 
	 * @param index
	 * @return
	 */
	public ISprite remove(int index) {
		if (_closed) {
			return null;
		}
		ISprite removed = this._sprites[index];
		if (removed != null) {
			removed.setState(State.REMOVED);
			// 删除精灵同时，删除缓动动画
			if (removed instanceof ActionBind) {
				ActionControl.get().removeAllActions((ActionBind) removed);
			}
		}
		int size = this._size - index - 1;
		if (size > 0) {
			System.arraycopy(this._sprites, index + 1, this._sprites, index, size);
		}
		this._sprites[--this._size] = null;
		if (size == 0) {
			_sprites = new ISprite[0];
		}
		return removed;
	}

	/**
	 * 清空所有精灵
	 * 
	 */
	public Sprites removeAll() {
		if (_closed) {
			return this;
		}
		clear();
		this._sprites = new ISprite[0];
		return this;
	}

	/**
	 * 删除指定精灵
	 * 
	 * @param sprite
	 * @return
	 */
	public boolean remove(ISprite sprite) {
		if (_closed) {
			return false;
		}
		if (sprite == null) {
			return false;
		}
		if (_sprites == null) {
			return false;
		}
		boolean removed = false;
		for (int i = _size; i > 0; i--) {
			ISprite spr = _sprites[i - 1];
			if ((sprite == spr) || (sprite.equals(spr))) {
				spr.setState(State.REMOVED);
				// 删除精灵同时，删除缓动动画
				if (spr instanceof ActionBind) {
					ActionControl.get().removeAllActions((ActionBind) spr);
				}
				removed = true;
				_size--;
				_sprites[i - 1] = _sprites[_size];
				_sprites[_size] = null;
				if (_size == 0) {
					_sprites = new ISprite[0];
				} else {
					compressCapacity(CollectionUtils.INITIAL_CAPACITY);
				}
				return removed;
			}
		}
		return removed;
	}

	/**
	 * 删除指定名称的精灵
	 * 
	 * @param name
	 * @return
	 */
	public boolean removeName(String name) {
		if (_closed) {
			return false;
		}
		if (name == null) {
			return false;
		}
		if (_sprites == null) {
			return false;
		}
		boolean removed = false;
		for (int i = _size; i > 0; i--) {
			ISprite spr = _sprites[i - 1];
			if ((name.equals(spr.getName()))) {
				spr.setState(State.REMOVED);
				// 删除精灵同时，删除缓动动画
				if (spr instanceof ActionBind) {
					ActionControl.get().removeAllActions((ActionBind) spr);
				}
				removed = true;
				_size--;
				_sprites[i - 1] = _sprites[_size];
				_sprites[_size] = null;
				if (_size == 0) {
					_sprites = new ISprite[0];
				} else {
					compressCapacity(CollectionUtils.INITIAL_CAPACITY);
				}
				return removed;
			}
		}
		return removed;
	}

	/**
	 * 删除指定范围内精灵
	 * 
	 * @param startIndex
	 * @param endIndex
	 */
	public Sprites remove(int startIndex, int endIndex) {
		if (_closed) {
			return this;
		}
		if (endIndex - startIndex > 0) {
			for (int i = startIndex; i < endIndex && i < _sprites.length; i++) {
				ISprite spr = _sprites[i];
				if (spr != null) {
					spr.setState(State.REMOVED);
					// 删除精灵同时，删除缓动动画
					if (spr instanceof ActionBind) {
						ActionControl.get().removeAllActions((ActionBind) spr);
					}
				}
			}
		}
		int numMoved = this._size - endIndex;
		System.arraycopy(this._sprites, endIndex, this._sprites, startIndex, numMoved);
		int newSize = this._size - (endIndex - startIndex);
		while (this._size != newSize) {
			this._sprites[--this._size] = null;
		}
		if (_size == 0) {
			_sprites = new ISprite[0];
		}
		return this;
	}

	public PointI getMinPos() {
		if (_closed) {
			return new PointI(0, 0);
		}
		PointI p = new PointI(0, 0);
		for (int i = 0; i < _size; i++) {
			ISprite sprite = _sprites[i];
			p.x = MathUtils.min(p.x, sprite.x());
			p.y = MathUtils.min(p.y, sprite.y());
		}
		return p;
	}

	public PointI getMaxPos() {
		if (_closed) {
			return new PointI(0, 0);
		}
		PointI p = new PointI(0, 0);
		for (int i = 0; i < _size; i++) {
			ISprite sprite = _sprites[i];
			p.x = MathUtils.max(p.x, sprite.x());
			p.y = MathUtils.max(p.y, sprite.y());
		}
		return p;
	}

	/**
	 * 清空当前精灵集合
	 * 
	 */
	@Override
	public void clear() {
		if (_closed) {
			return;
		}
		for (int i = 0; i < _sprites.length; i++) {
			ISprite removed = _sprites[i];
			if (removed != null) {
				removed.setState(State.REMOVED);
				// 删除精灵同时，删除缓动动画
				if (removed instanceof ActionBind) {
					ActionControl.get().removeAllActions((ActionBind) removed);
				}
			}
			_sprites[i] = null;
		}
		_size = 0;
		return;
	}

	/**
	 * 刷新事务
	 * 
	 * @param elapsedTime
	 */
	public void update(long elapsedTime) {
		if (!_visible || _closed) {
			return;
		}

		boolean listerner = (sprListerner != null);
		for (int i = _size - 1; i > -1; i--) {
			ISprite child = _sprites[i];
			if (child != null && child.isVisible()) {
				try {
					child.update(elapsedTime);
					if (listerner) {
						sprListerner.update(child);
					}
					if (_autoSortLayer) {
						_currentPoshash = LSystem.unite(_currentPoshash, child.getX());
						_currentPoshash = LSystem.unite(_currentPoshash, child.getY());
						_currentPoshash = LSystem.unite(_currentPoshash, child.getOffsetX());
						_currentPoshash = LSystem.unite(_currentPoshash, child.getOffsetY());
					}
				} catch (Throwable cause) {
					LSystem.error("Sprites update() exception", cause);
				}
			}
		}
		if (_autoSortLayer) {
			if (this._size <= 1) {
				return;
			}
			if (_currentPoshash != _lastPosHash) {
				if (this._sprites.length != this._size) {
					ISprite[] sprs = CollectionUtils.copyOf(this._sprites, this._size);
					spriteXYSorter.sort(sprs);
					this._sprites = sprs;
				} else {
					spriteXYSorter.sort(this._sprites);
				}
				_lastPosHash = _currentPoshash;
			}
		}
	}

	/**
	 * 单纯渲染精灵
	 * 
	 * @param g
	 */
	public void paint(final GLEx g, final float minX, final float minY, final float maxX, final float maxY) {
		if (!_visible || _closed) {
			return;
		}
		float spriteX;
		float spriteY;
		float spriteWidth;
		float spriteHeight;
		for (int i = 0; i < this._size; i++) {
			ISprite spr = this._sprites[i];
			if (spr != null && spr.isVisible()) {
				if (_limitViewWindows) {
					spriteX = minX + spr.getX();
					spriteY = minY + spr.getY();
					spriteWidth = spr.getWidth();
					spriteHeight = spr.getHeight();
					if (spriteX + spriteWidth < minX || spriteX > maxX || spriteY + spriteHeight < minY
							|| spriteY > maxY) {
						continue;
					}
				}
				if (_createShadow && spr.showShadow()) {
					_spriteShadow.drawShadow(g, spr, 0f, 0f);
				}
				spr.createUI(g);
			}
		}
	}

	public void paintPos(final GLEx g, final float offsetX, final float offsetY) {
		if (_closed) {
			return;
		}
		if (!_visible) {
			return;
		}
		for (int i = 0; i < this._size; i++) {
			ISprite spr = this._sprites[i];
			if (spr != null && spr.isVisible()) {
				if (_createShadow && spr.showShadow()) {
					_spriteShadow.drawShadow(g, spr, offsetX, offsetY);
				}
				spr.createUI(g, offsetX, offsetY);
			}
		}
	}

	/**
	 * 创建UI图像
	 * 
	 * @param g
	 */
	public void createUI(final GLEx g) {
		createUI(g, 0, 0);
	}

	/**
	 * 创建UI图像
	 * 
	 * @param g
	 */
	public void createUI(final GLEx g, final int x, final int y) {
		if (_closed) {
			return;
		}
		if (!_visible) {
			return;
		}

		final float newScrollX = _scrollX;
		final float newScrollY = _scrollY;

		final int drawWidth = _width;
		final int drawHeight = _height;

		final float startX = MathUtils.scroll(newScrollX, drawWidth);
		final float startY = MathUtils.scroll(newScrollY, drawHeight);

		final boolean update = (startX != 0f || startY != 0f);

		if (update) {
			g.translate(startX, startY);
		}
		float minX, minY, maxX, maxY;
		if (this._isViewWindowSet) {
			minX = x + this.viewX;
			maxX = minX + this.viewWidth;
			minY = y + this.viewY;
			maxY = minY + this.viewHeight;
		} else {
			minX = x;
			maxX = x + this._width;
			minY = y;
			maxY = y + this._height;
		}
		boolean offset = (minX != 0 || minY != 0);
		if (offset) {
			g.translate(minX, minY);
		}
		for (int i = 0; i < this._size; i++) {
			ISprite spr = this._sprites[i];
			if (spr != null && spr.isVisible()) {
				if (_limitViewWindows) {
					int layerX = spr.x();
					int layerY = spr.y();
					float layerWidth = spr.getWidth() + 1;
					float layerHeight = spr.getHeight() + 1;
					if (layerX + layerWidth < minX || layerX > maxX || layerY + layerHeight < minY || layerY > maxY) {
						continue;
					}
				}
				if (_createShadow && spr.showShadow()) {
					_spriteShadow.drawShadow(g, spr, 0f, 0f);
				}
				spr.createUI(g);
			}
		}
		if (offset) {
			g.translate(-minX, -minY);
		}
		if (update) {
			g.translate(-startX, -startY);
		}
	}

	public Sprites addSpriteGroup(LTexture tex, int count) {
		for (int i = 0; i < count; i++) {
			add(new Sprite(tex));
		}
		return this;
	}

	public Sprites addEntityGroup(LTexture tex, int count) {
		for (int i = 0; i < count; i++) {
			add(new Entity(tex));
		}
		return this;
	}

	public Sprites addSpriteGroup(String path, int count) {
		for (int i = 0; i < count; i++) {
			add(new Sprite(path));
		}
		return this;
	}

	public Sprites addEntityGroup(String path, int count) {
		for (int i = 0; i < count; i++) {
			add(new Entity(path));
		}
		return this;
	}

	public Sprites addEntityGroup(Created<? extends IEntity> s, int count) {
		if (s == null) {
			return this;
		}
		for (int i = 0; i < count; i++) {
			add(s.make());
		}
		return this;
	}

	public Sprites addSpriteGroup(Created<? extends ISprite> s, int count) {
		if (s == null) {
			return this;
		}
		for (int i = 0; i < count; i++) {
			add(s.make());
		}
		return this;
	}

	public float getX() {
		return viewX;
	}

	public float getY() {
		return viewY;
	}

	public float getStageX() {
		return (getX() - getScreenX());
	}

	public float getStageY() {
		return (getX() - getScreenX());
	}

	/**
	 * 设定精灵集合在屏幕中的位置与大小
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Sprites setViewWindow(int x, int y, int width, int height) {
		this._isViewWindowSet = true;
		this.viewX = x;
		this.viewY = y;
		this.viewWidth = width;
		this.viewHeight = height;
		return this;
	}

	/**
	 * 设定精灵集合在屏幕中的位置
	 * 
	 * @param x
	 * @param y
	 */
	public Sprites setLocation(int x, int y) {
		this._isViewWindowSet = true;
		this.viewX = x;
		this.viewY = y;
		return this;
	}

	public SpriteControls createSpriteControls() {
		if (_closed) {
			return new SpriteControls();
		}
		SpriteControls controls = null;
		if (_sprites != null && _size > 0) {
			controls = new SpriteControls(_sprites);
		} else {
			controls = new SpriteControls();
		}
		return controls;
	}

	public SpriteControls controls() {
		return createSpriteControls();
	}

	public SpriteControls findNamesToSpriteControls(String... names) {
		if (_closed) {
			return new SpriteControls();
		}
		SpriteControls controls = null;
		if (_sprites != null) {
			TArray<ISprite> sps = findNames(names);
			controls = new SpriteControls(sps);
		} else {
			controls = new SpriteControls();
		}
		return controls;
	}

	public SpriteControls findNameContainsToSpriteControls(String... names) {
		if (_closed) {
			return new SpriteControls();
		}
		SpriteControls controls = null;
		if (_sprites != null) {
			TArray<ISprite> sps = findNameContains(names);
			controls = new SpriteControls(sps);
		} else {
			controls = new SpriteControls();
		}
		return controls;
	}

	public SpriteControls findNotNamesToSpriteControls(String... names) {
		if (_closed) {
			return new SpriteControls();
		}
		SpriteControls controls = null;
		if (_sprites != null) {
			TArray<ISprite> sps = findNotNames(names);
			controls = new SpriteControls(sps);
		} else {
			controls = new SpriteControls();
		}
		return controls;
	}

	public SpriteControls findTagsToSpriteControls(Object... o) {
		if (_closed) {
			return new SpriteControls();
		}
		SpriteControls controls = null;
		if (_sprites != null) {
			TArray<ISprite> sps = findTags(o);
			controls = new SpriteControls(sps);
		} else {
			controls = new SpriteControls();
		}
		return controls;
	}

	public SpriteControls findNotTagsToSpriteControls(Object... o) {
		if (_closed) {
			return new SpriteControls();
		}
		SpriteControls controls = null;
		if (_sprites != null) {
			TArray<ISprite> sps = findNotTags(o);
			controls = new SpriteControls(sps);
		} else {
			controls = new SpriteControls();
		}
		return controls;
	}

	public ISprite[] getSprites() {
		if (_sprites == null) {
			return null;
		}
		return CollectionUtils.copyOf(this._sprites, this._size);
	}

	/**
	 * 删除符合指定条件的精灵并返回操作的集合
	 * 
	 * @param query
	 * @return
	 */
	public TArray<ISprite> remove(QueryEvent<ISprite> query) {
		final TArray<ISprite> result = new TArray<ISprite>();
		for (int i = _sprites.length - 1; i > -1; i--) {
			ISprite sprite = _sprites[i];
			if (sprite != null) {
				if (query.hit(sprite)) {
					result.add(sprite);
					remove(i);
				}
			}
		}
		return result;
	}

	/**
	 * 查找符合指定条件的精灵并返回操作的集合
	 * 
	 * @param query
	 * @return
	 */
	public TArray<ISprite> find(QueryEvent<ISprite> query) {
		final TArray<ISprite> result = new TArray<ISprite>();
		for (int i = _sprites.length - 1; i > -1; i--) {
			ISprite sprite = _sprites[i];
			if (sprite != null) {
				if (query.hit(sprite)) {
					result.add(sprite);
				}
			}
		}
		return result;
	}

	/**
	 * 删除指定条件的精灵并返回操作的集合
	 * 
	 * @param query
	 * @return
	 */
	public TArray<ISprite> delete(QueryEvent<ISprite> query) {
		final TArray<ISprite> result = new TArray<ISprite>();
		for (int i = _sprites.length - 1; i > -1; i--) {
			ISprite sprite = _sprites[i];
			if (sprite != null) {
				if (query.hit(sprite)) {
					result.add(sprite);
					remove(i);
				}
			}
		}
		return result;
	}

	/**
	 * 查找符合指定条件的精灵并返回操作的集合
	 * 
	 * @param query
	 * @return
	 */
	public TArray<ISprite> select(QueryEvent<ISprite> query) {
		final TArray<ISprite> result = new TArray<ISprite>();
		for (int i = _sprites.length - 1; i > -1; i--) {
			ISprite sprite = _sprites[i];
			if (sprite != null) {
				if (query.hit(sprite)) {
					result.add(sprite);
				}
			}
		}
		return result;
	}

	private void addRect(TArray<RectBox> rects, RectBox child) {
		if (child.width > 1 && child.height > 1) {
			if (!rects.contains(child)) {
				rects.add(child);
			}
		}
	}

	private void addAllRect(TArray<RectBox> rects, ISprite spr) {
		if (spr instanceof Entity) {
			if (spr.isContainer()) {
				Entity ns = (Entity) spr;
				TArray<IEntity> childs = ns._childrens;
				for (int i = childs.size - 1; i > -1; i--) {
					IEntity cc = childs.get(i);
					if (cc != null) {
						addRect(rects, ns.getCollisionBox().cpy().add(cc.getCollisionBox()));
					}
				}
			} else {
				addRect(rects, spr.getCollisionBox());
			}
		} else if (spr instanceof Sprite) {
			if (spr.isContainer()) {
				Sprite ns = (Sprite) spr;
				TArray<ISprite> childs = ns._childrens;
				for (int i = childs.size - 1; i > -1; i--) {
					ISprite cc = childs.get(i);
					if (cc != null) {
						addRect(rects, ns.getCollisionBox().cpy().add(cc.getCollisionBox()));
					}
				}
			} else {
				addRect(rects, spr.getCollisionBox());
			}
		} else {
			addRect(rects, spr.getCollisionBox());
		}
	}

	public DirtyRectList getDirtyList() {
		final TArray<RectBox> rects = new TArray<RectBox>();
		ISprite[] childs = _sprites;
		if (childs != null) {
			for (int i = childs.length - 1; i > -1; i--) {
				ISprite spr = childs[i];
				if (spr != null) {
					addAllRect(rects, spr);
				}
			}
		}
		_dirtyList.clear();
		for (RectBox rect : rects) {
			if (rect.width > 1 && rect.height > 1) {
				_dirtyList.add(rect);
			}
		}
		return _dirtyList;
	}

	public Sprites setSpritesShadow(ISpritesShadow s) {
		this._spriteShadow = s;
		if (_spriteShadow != null) {
			_createShadow = true;
		} else {
			_createShadow = false;
		}
		return this;
	}

	public ISpritesShadow shadow() {
		return _spriteShadow;
	}

	public boolean getSpritesShadow() {
		return _createShadow;
	}

	@Override
	public int size() {
		return this._size;
	}

	public RectBox getBoundingBox() {
		return new RectBox(this.viewX, this.viewY, this.viewWidth, this.viewHeight);
	}

	public int getHeight() {
		return _height;
	}

	public int getWidth() {
		return _width;
	}

	public Sprites hide() {
		setVisible(false);
		return this;
	}

	public Sprites show() {
		setVisible(true);
		return this;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public void setVisible(boolean v) {
		this._visible = v;
	}

	public SpriteListener getSprListerner() {
		return sprListerner;
	}

	public Sprites setSprListerner(SpriteListener sprListerner) {
		this.sprListerner = sprListerner;
		return this;
	}

	public Screen getScreen() {
		return _screen;
	}

	public float getScreenX() {
		return _screen == null ? 0 : _screen.getX();
	}

	public float getScreenY() {
		return _screen == null ? 0 : _screen.getY();
	}

	public Sprites scrollBy(float x, float y) {
		this._scrollX += x;
		this._scrollY += y;
		return this;
	}

	public Sprites scrollTo(float x, float y) {
		this._scrollX = x;
		this._scrollY = y;
		return this;
	}

	public float scrollX() {
		return this._scrollX;
	}

	public float scrollY() {
		return this._scrollY;
	}

	public Sprites scrollX(float x) {
		this._scrollX = x;
		return this;
	}

	public Sprites scrollY(float y) {
		this._scrollY = y;
		return this;
	}

	public Margin margin(boolean vertical, float left, float top, float right, float bottom) {
		float size = vertical ? getHeight() : getWidth();
		if (_closed) {
			return new Margin(size, vertical);
		}
		if (_margin == null) {
			_margin = new Margin(size, vertical);
		} else {
			_margin.setSize(size);
			_margin.setVertical(vertical);
		}
		_margin.setMargin(left, top, right, bottom);
		_margin.clear();
		for (int i = 0; i < _size; i++) {
			ISprite spr = _sprites[i];
			if (spr != null) {
				_margin.addChild(spr);
			}
		}
		return _margin;
	}

	/**
	 * 遍历Sprites中所有精灵对象并反馈给Callback
	 * 
	 * @param callback
	 */
	public Sprites forChildren(Callback<ISprite> callback) {
		if (callback == null) {
			return this;
		}
		for (ISprite child : this._sprites) {
			if (child != null) {
				callback.onSuccess(child);
			}
		}
		return this;
	}

	public Sprites resize(float width, float height, boolean forceResize) {
		if (_resizeListener != null) {
			_resizeListener.onResize(this);
		}
		if (forceResize || (this._width != (int) width && this._height != (int) height)) {
			this._width = (int) width;
			this._height = (int) height;
			for (ISprite child : this._sprites) {
				if (child != null) {
					child.onResize();
				}
			}
		}
		return this;
	}

	public ResizeListener<Sprites> getResizeListener() {
		return _resizeListener;
	}

	public Sprites setResizeListener(ResizeListener<Sprites> listener) {
		this._resizeListener = listener;
		return this;
	}

	public boolean isLimitViewWindows() {
		return _limitViewWindows;
	}

	public Sprites setLimitViewWindows(boolean limit) {
		this._limitViewWindows = limit;
		return this;
	}

	public Sprites rect(RectBox rect) {
		return rect(rect, 0);
	}

	public Sprites rect(RectBox rect, int shift) {
		rect(this, rect, shift);
		return this;
	}

	public Sprites triangle(Triangle2f t) {
		return triangle(t, 1);
	}

	public Sprites triangle(Triangle2f t, int stepRate) {
		triangle(this, t, stepRate);
		return this;
	}

	public Sprites circle(Circle circle) {
		return circle(circle, -1f, -1f);
	}

	public Sprites circle(Circle circle, float startAngle, float endAngle) {
		circle(this, circle, startAngle, endAngle);
		return this;
	}

	public Sprites ellipse(Ellipse e) {
		return ellipse(e, -1f, -1f);
	}

	public Sprites ellipse(Ellipse e, float startAngle, float endAngle) {
		ellipse(this, e, startAngle, endAngle);
		return this;
	}

	public Sprites line(Line e) {
		line(this, e);
		return this;
	}

	public Sprites rotateAround(XY point, float angle) {
		rotateAround(this, point, angle);
		return this;
	}

	public Sprites rotateAroundDistance(XY point, float angle, float distance) {
		rotateAroundDistance(this, point, angle, distance);
		return this;
	}

	public String getName() {
		return this._sprites_name;
	}

	@Override
	public boolean isEmpty() {
		return _size == 0 || _sprites == null;
	}

	public Sprites setAutoYLayer(boolean y) {
		spriteXYSorter.setSortY(y);
		return this;
	}

	public boolean isAutoSortXYLayer() {
		return _autoSortLayer;
	}

	public Sprites setAutoSortXYLayer(boolean sort) {
		this._autoSortLayer = sort;
		return this;
	}

	@Override
	public String toString() {
		return super.toString() + " " + "[name=" + _sprites_name + ", total=" + size() + "]";
	}

	public boolean isClosed() {
		return _closed;
	}

	@Override
	public void close() {
		if (_closed) {
			return;
		}
		this._visible = this._createShadow = false;
		this._autoSortLayer = false;
		if (_spriteShadow != null) {
			_spriteShadow.close();
		}
		this._newLineHeight = 0;
		for (ISprite spr : _sprites) {
			if (spr != null) {
				spr.close();
			}
		}
		clear();
		this._closed = true;
		this._sprites = null;
		this._resizeListener = null;
		LSystem.popSpritesPool(this);
	}

}
