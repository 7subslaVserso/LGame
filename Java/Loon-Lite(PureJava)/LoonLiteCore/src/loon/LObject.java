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

import java.util.Comparator;

import loon.action.ActionBind;
import loon.action.ActionControl;
import loon.action.ActionEvent;
import loon.action.collision.CollisionHelper;
import loon.action.map.Field2D;
import loon.canvas.Alpha;
import loon.component.layout.LayoutAlign;
import loon.geom.RectBox;
import loon.geom.Vector2f;
import loon.geom.XY;
import loon.utils.MathUtils;
import loon.utils.StringKeyValue;
import loon.utils.StringUtils;
import loon.utils.reply.Callback;
import loon.utils.reply.Var;
import loon.utils.reply.VarView;

/**
 * 一个通用的Loon对象,除Screen外,Loon中所有可移动并展示的对象都继承于此类
 */
public abstract class LObject<T> implements Comparator<T>, XY, ZIndex {

	private static int _SYS_GLOBAL_SEQNO = 0;

	public final static int allLObjects() {
		return _SYS_GLOBAL_SEQNO;
	}

	public static enum State {
		UNKNOWN, REMOVED, ADDED, DISPOSED
	}

	protected final VarView<State> _objectState = Var.create(State.UNKNOWN);

	public final void setState(State state) {
		((Var<State>) this._objectState).update(state);
	}

	public final State getState() {
		return this._objectState.get();
	}

	public final boolean isDisposed() {
		return _objectState.get() == State.DISPOSED;
	}

	public final boolean isRemoved() {
		return _objectState.get() == State.REMOVED;
	}

	public final boolean isAdded() {
		return _objectState.get() == State.ADDED;
	}

	public final boolean isUnknown() {
		return _objectState.get() == State.UNKNOWN;
	}

	// 无状态
	public static final int NOT = -1;
	// 真
	public static final int TRUE = 1;
	// 假
	public static final int FALSE = 2;

	protected T _objectSuper = null;

	public void setSuper(T s) {
		this._objectSuper = s;
	}

	public T getSuper() {
		return this._objectSuper;
	}

	public boolean hasSuper() {
		return this._objectSuper != null;
	}

	public boolean hasParent() {
		return hasSuper();
	}

	public T getParent() {
		return getSuper();
	}

	public void setParent(final T e) {
		setSuper(e);
	}

	/**
	 * 添加一个独立事件，并选择是否暂不启动
	 * 
	 * @param action
	 * @param obj
	 * @param paused
	 */
	public final static void addActionEvent(ActionEvent action, ActionBind obj, boolean paused) {
		ActionControl.get().addAction(action, obj, paused);
	}

	/**
	 * 添加一个独立事件
	 * 
	 * @param action
	 * @param obj
	 */
	public final static void addActionEvent(ActionEvent action, ActionBind obj) {
		ActionControl.get().addAction(action, obj);
	}

	/**
	 * 删除所有和指定对象有关的独立事件
	 * 
	 * @param actObject
	 */
	public final static void removeActionEvents(ActionBind actObject) {
		ActionControl.get().removeAllActions(actObject);
	}

	/**
	 * 获得当前独立事件总数
	 * 
	 * @return
	 */
	public final static int getActionEventCount() {
		return ActionControl.get().getCount();
	}

	/**
	 * 删除指定的独立事件
	 * 
	 * @param tag
	 * @param actObject
	 */
	public final static void removeActionEvent(Object tag, ActionBind actObject) {
		ActionControl.get().removeAction(tag, actObject);
	}

	/**
	 * 删除指定的独立事件
	 * 
	 * @param action
	 */
	public final static void removeActionEvent(ActionEvent action) {
		ActionControl.get().removeAction(action);
	}

	/**
	 * 获得制定的独立事件
	 * 
	 * @param tag
	 * @param actObject
	 * @return
	 */
	public final static ActionEvent getActionEvent(Object tag, ActionBind actObject) {
		return ActionControl.get().getAction(tag, actObject);
	}

	/**
	 * 停止对象对应的自动事件
	 * 
	 * @param actObject
	 */
	public final static void stopActionEvent(ActionBind actObject) {
		ActionControl.get().stop(actObject);
	}

	/**
	 * 设定指定角色暂停状态
	 * 
	 * @param pause
	 * @param actObject
	 */
	public final static void pauseActionEvent(boolean pause, ActionBind actObject) {
		ActionControl.get().paused(pause, actObject);
	}

	/**
	 * 设置是否暂停自动事件运行
	 * 
	 * @param pause
	 */
	public final static void pauseActionEvent(boolean pause) {
		ActionControl.get().setPause(pause);
	}

	/**
	 * 获得是否暂停了独立事件运行
	 * 
	 * @return
	 */
	public final static boolean isPauseActionEvent() {
		return ActionControl.get().isPause();
	}

	/**
	 * 启动指定对象对应的对立事件
	 * 
	 * @param actObject
	 */
	public final static void startActionEvent(ActionBind actObject) {
		ActionControl.get().start(actObject);
	}

	/**
	 * 停止独立事件运行用线程
	 * 
	 */
	public final static void stopActionEvent() {
		ActionControl.get().stop();
	}

	// 附注用的Tag标记对象(加上什么都可以,传参也行,标记对象也行)
	public Object Tag = null;

	private Object _collisionData = null;

	protected float _objectAlpha = 1f;

	protected float _objectRotation, _previousRotation;

	protected RectBox _objectRect;

	protected String _objectName;

	protected String _object_flag;

	protected final Vector2f _objectLocation = new Vector2f(0, 0);

	protected final Vector2f _objectPreviousLocation = new Vector2f(0, 0);

	protected int _objectLayer;

	private int _objectStatus = NOT;

	private int _objectSeqNo = 0;

	public LObject() {
		this._objectSeqNo = _SYS_GLOBAL_SEQNO;
		this._objectRotation = 0;
		this._previousRotation = 0;
		this._objectLayer = 0;
		this._objectAlpha = 1f;
		_SYS_GLOBAL_SEQNO++;
	}

	public final int getSequenceNo() {
		return _objectSeqNo;
	}

	public final LObject<T> setStatus(int status) {
		this._objectStatus = status;
		return this;
	}

	public final LObject<T> setLife(int status) {
		return setStatus(status);
	}

	public final LObject<T> addLife() {
		return setStatus(_objectStatus++);
	}

	public final int getStatus() {
		return this._objectStatus;
	}

	public final LObject<T> removeLife() {
		return setStatus(_objectStatus--);
	}

	public final int getLife() {
		return getStatus();
	}

	public final LObject<T> setObjectFlag(String flag) {
		this._object_flag = flag;
		return this;
	}

	public final String getObjectFlag() {
		return StringUtils.isEmpty(this._object_flag) ? getName() : this._object_flag;
	}

	public void setTransparency(int a) {
		setAlpha(a / 255f);
	}

	public int getTransparency() {
		return (int) (_objectAlpha * 255);
	}

	public Alpha getAlphaObject() {
		return new Alpha(_objectAlpha);
	}

	public void setAlpha(float a) {
		this._objectAlpha = MathUtils.clamp(a, 0f, 1f);
	}

	public float getAlpha() {
		return this._objectAlpha;
	}

	public void setRotation(float r) {
		if (r == this._objectRotation) {
			return;
		}
		this._previousRotation = this._objectRotation;
		this._objectRotation = MathUtils.fixRotation(r);
		if (_objectRect != null) {
			_objectRect.setBounds(
					MathUtils.getBounds(_objectLocation.x, _objectLocation.y, getWidth(), getHeight(), r, _objectRect));
		} else {
			_objectRect = MathUtils.getBounds(_objectLocation.x, _objectLocation.y, getWidth(), getHeight(), r,
					_objectRect);
		}
	}

	public void rotateBy(float r) {
		if (r != 0f) {
			setRotation(this._objectRotation + r);
		}
	}

	public boolean isRotated() {
		return _objectRotation != _previousRotation;
	}

	public float getPreviousRotation() {
		return this._previousRotation;
	}

	public float getRotation() {
		return _objectRotation;
	}

	public abstract void update(long elapsedTime);

	public void centerOnScreen() {
		LObject.centerOn(this, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public void bottomOnScreen() {
		LObject.bottomOn(this, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public void leftOnScreen() {
		LObject.leftOn(this, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public void rightOnScreen() {
		LObject.rightOn(this, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public void topOnScreen() {
		LObject.topOn(this, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public RectBox getCollisionArea() {
		return getRect(getX(), getY(), getWidth(), getHeight());
	}

	protected RectBox setRect(RectBox rect) {
		if (_objectRect == null) {
			_objectRect = rect;
		} else {
			_objectRect.setBounds(rect);
		}
		return this._objectRect;
	}

	protected RectBox getRect(float x, float y, float w, float h) {
		if (_objectRect == null) {
			_objectRect = new RectBox(x, y, w, h);
		} else {
			_objectRect.setBounds(x, y, w, h);
		}
		return _objectRect;
	}

	public boolean isContains(LObject<T> o) {
		return CollisionHelper.contains(this.getCollisionArea(), o.getCollisionArea());
	}

	public boolean isIntersects(LObject<T> o) {
		return CollisionHelper.isRectToRect(getCollisionArea(), o.getCollisionArea());
	}

	public boolean isIntersectsCircle(LObject<T> o) {
		return CollisionHelper.isRectToCirc(getCollisionArea(), o.getCollisionArea());
	}

	public boolean isCircleIntersectsCircle(LObject<T> o) {
		return CollisionHelper.isCircToCirc(getCollisionArea(), o.getCollisionArea());
	}

	public float getDistance(LObject<T> o) {
		return CollisionHelper.getDistance(getCollisionArea(), o.getCollisionArea());
	}

	public void setName(String name) {
		this._objectName = name;
	}

	public String getName() {
		if (_objectName == null) {
			_objectName = getClass().getName();
			int idx = _objectName.lastIndexOf('.');
			if (idx != -1 && idx > 0) {
				_objectName = _objectName.substring(idx + 1).intern();
			} else {
				_objectName = "LObject";
			}
		}
		return _objectName;
	}

	@Override
	public int getLayer() {
		return _objectLayer;
	}

	public void setLayer(int z) {
		this._objectLayer = z;
	}

	public void setZ(int z) {
		this.setZOrder(z);
	}

	public void setZOrder(int z) {
		int orderZ = z;
		if (this._objectSuper != null && this._objectSuper instanceof ZIndex) {
			orderZ = z - MathUtils.abs(((ZIndex) this._objectSuper).getLayer());
		}
		setLayer(-orderZ);
	}

	public int getZOrder() {
		return MathUtils.abs(getLayer());
	}

	public int getZ() {
		return getZOrder();
	}

	/**
	 * 上一个经过的X坐标
	 * 
	 * @return
	 */
	public float getPreviousX() {
		return _objectPreviousLocation.x;
	}

	/**
	 * 上一个经过的Y坐标
	 * 
	 * @return
	 */
	public float getPreviousY() {
		return _objectPreviousLocation.y;
	}

	public int previousX() {
		return _objectPreviousLocation.x();
	}

	public int previousY() {
		return _objectPreviousLocation.y();
	}

	/**
	 * 判定当前坐标是否发生了移动
	 * 
	 * @return
	 */
	public boolean hasMoved() {
		return (_objectPreviousLocation.x != _objectLocation.x || _objectPreviousLocation.y != _objectLocation.y);
	}

	/**
	 * 同步保存上一个移动地址
	 */
	protected void syncPreviousPos() {
		_objectPreviousLocation.set(_objectLocation);
	}

	public void move_45D_up() {
		move_45D_up(1);
	}

	public void move_45D_up(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.UP, multiples);
	}

	public void move_45D_left() {
		move_45D_left(1);
	}

	public void move_45D_left(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.LEFT, multiples);
	}

	public void move_45D_right() {
		move_45D_right(1);
	}

	public void move_45D_right(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.RIGHT, multiples);
	}

	public void move_45D_down() {
		move_45D_down(1);
	}

	public void move_45D_down(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.DOWN, multiples);
	}

	public void move_up() {
		move_up(1);
	}

	public void move_up(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.TUP, multiples);
	}

	public void move_left() {
		move_left(1);
	}

	public void move_left(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.TLEFT, multiples);
	}

	public void move_right() {
		move_right(1);
	}

	public void move_right(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.TRIGHT, multiples);
	}

	public void move_down() {
		move_down(1);
	}

	public void move_down(int multiples) {
		syncPreviousPos();
		_objectLocation.move_multiples(Field2D.TDOWN, multiples);
	}

	public void move(Vector2f v) {
		move(v.x, v.y);
	}

	public void move(float x, float y) {
		syncPreviousPos();
		_objectLocation.move(x, y);
	}

	public void setLocation(XY local) {
		setLocation(local.getX(), local.getY());
	}

	public void setLocation(Vector2f v) {
		setLocation(v.x, v.y);
	}

	public void setPosition(Vector2f position) {
		if (position != null) {
			this.setLocation(position);
		} else {
			this.setLocation(0f, 0f);
		}
	}

	public void setPosition(float x, float y) {
		setLocation(x, y);
	}

	public Vector2f getPosition() {
		return this._objectLocation;
	}

	public void pos(float x, float y) {
		setLocation(x, y);
	}

	public void setLocation(float x, float y) {
		syncPreviousPos();
		_objectLocation.setLocation(x, y);
	}

	public int x() {
		return _objectLocation.x();
	}

	public int y() {
		return _objectLocation.y();
	}

	@Override
	public float getX() {
		return _objectLocation.getX();
	}

	@Override
	public float getY() {
		return _objectLocation.getY();
	}

	public void setX(Integer x) {
		setX(x.intValue());
	}

	public void setX(float x) {
		syncPreviousPos();
		_objectLocation.setX(x);
	}

	public void setY(Integer y) {
		setY(y.intValue());
	}

	public void setY(float y) {
		syncPreviousPos();
		_objectLocation.setY(y);
	}

	public void nextXY(float nextValue) {
		setX(getX() + nextValue);
		setY(getY() + nextValue);
	}

	public Vector2f getRoundedLocation(float x, float y) {
		float nx = getX() + x;
		float ny = getY() + y;
		if ((nx + ny) % 2 == 1) {
			y++;
		}
		return Vector2f.at(nx, ny);
	}

	public Vector2f getLocation() {
		return _objectLocation;
	}

	public static void centerOn(final LObject<?> object, float w, float h) {
		object.setLocation(w / 2 - object.getWidth() / 2, h / 2 - object.getHeight() / 2);
	}

	public static void centerTopOn(final LObject<?> object, float w, float h) {
		object.setLocation(w / 2 - object.getWidth() / 2, 0);
	}

	public static void centerBottomOn(final LObject<?> object, float w, float h) {
		object.setLocation(w / 2 - object.getWidth() / 2, h - object.getHeight());
	}

	public static void topOn(final LObject<?> object, float w, float h) {
		object.setLocation(w / 2 - object.getWidth() / 2, 0);
	}

	public static void topLeftOn(final LObject<?> object, float w, float h) {
		object.setLocation(0, 0);
	}

	public static void topRightOn(final LObject<?> object, float w, float h) {
		object.setLocation(w - object.getWidth(), 0);
	}

	public static void bottomLeftOn(final LObject<?> object, float w, float h) {
		object.setLocation(0, h - object.getHeight());
	}

	public static void bottomRightOn(final LObject<?> object, float w, float h) {
		object.setLocation(w - object.getWidth(), h - object.getHeight());
	}

	public static void leftOn(final LObject<?> object, float w, float h) {
		object.setLocation(0, h / 2 - object.getHeight() / 2);
	}

	public static void rightOn(final LObject<?> object, float w, float h) {
		object.setLocation(w - object.getWidth(), h / 2 - object.getHeight() / 2);
	}

	public static void bottomOn(final LObject<?> object, float w, float h) {
		object.setLocation(w / 2 - object.getWidth() / 2, h - object.getHeight());
	}

	public void moveOn(final LayoutAlign align, final LObject<?> obj) {
		if (align != null && obj != null) {
			switch (align) {
			case Left:
				this.leftOn(obj);
				break;
			case Right:
				this.rightOn(obj);
				break;
			case Center:
				this.centerOn(obj);
				break;
			case Top:
				this.topOn(obj);
				break;
			case Bottom:
				this.bottomOn(obj);
				break;
			case TopLeft:
				this.topLeftOn(obj);
				break;
			case TopRight:
				this.topRightOn(obj);
				break;
			case BottomLeft:
				this.bottomLeftOn(obj);
				break;
			case BottomRight:
				this.bottomRightOn(obj);
				break;
			}
		}
	}

	public void centerOn(final LObject<?> object) {
		centerOn(object, getWidth(), getHeight());
	}

	public void topOn(final LObject<?> object) {
		topOn(object, getWidth(), getHeight());
	}

	public void leftOn(final LObject<?> object) {
		leftOn(object, getWidth(), getHeight());
	}

	public void rightOn(final LObject<?> object) {
		rightOn(object, getWidth(), getHeight());
	}

	public void bottomOn(final LObject<?> object) {
		bottomOn(object, getWidth(), getHeight());
	}

	public void topLeftOn(final LObject<?> object) {
		topLeftOn(object, getWidth(), getHeight());
	}

	public void topRightOn(final LObject<?> object) {
		topRightOn(object, getWidth(), getHeight());
	}

	public void bottomLeftOn(final LObject<?> object) {
		bottomLeftOn(object, getWidth(), getHeight());
	}

	public void bottomRightOn(final LObject<?> object) {
		bottomRightOn(object, getWidth(), getHeight());
	}

	/**
	 * 调用时以指定速度向指定X轴移动(到目标X后停止)
	 * 
	 * 注意,此函数没有调用缓动的moveTo方法,只有调用时才会累加移动,不能自动累加
	 * 
	 * @param destX
	 * @param speed
	 * @return
	 */
	public final LObject<T> moveToX(float destX, float speed) {
		if (this.getX() == destX) {
			return this;
		}
		int dir = (this.getX() > destX) ? -1 : 1;
		this.setX(this.getX() + speed * dir);
		if (dir == 1 && this.getX() >= destX || dir == -1 && this.getX() <= destX) {
			this.setX(destX);
		}
		return this;
	}

	/**
	 * 调用时以指定速度向指定Y轴移动(到目标Y后停止)
	 * 
	 * 注意,此函数没有调用缓动的moveTo方法,只有调用时才会累加移动,不能自动累加
	 * 
	 * @param destY
	 * @param speed
	 * @return
	 */
	public final LObject<T> moveToY(float destY, float speed) {
		if (this.getY() == destY) {
			return this;
		}
		int dir = (this.getY() > destY) ? -1 : 1;
		this.setY(this.getY() + speed * dir);
		if (dir == 1 && this.getY() >= destY || dir == -1 && this.getY() <= destY) {
			this.setY(destY);
		}
		return this;
	}

	/**
	 * 调用时以指定速度向指定方向移动(到目标后停止),移动结束后Callback自身
	 * 
	 * 注意,此函数没有调用缓动的moveTo方法,只有调用时才会累加移动,不能自动累加
	 * 
	 * @param destX
	 * @param destY
	 * @param speed
	 * @param callback
	 * @return
	 */
	public final LObject<T> moveTo(float destX, float destY, float speed, Callback<LObject<T>> callback) {
		if (this.getX() == destX && this.getY() == destY && callback != null) {
			callback.onSuccess(this);
			return this;
		}
		this.moveToX(destX, speed);
		this.moveToY(destY, speed);
		if (this.getX() == destX && this.getY() == destY && callback != null) {
			callback.onSuccess(this);
		}
		return this;
	}

	public final void setCollisionData(Object data) {
		this._collisionData = data;
	}

	public final Object getCollisionData() {
		return _collisionData;
	}

	public boolean landscape() {
		return this.getHeight() < this.getWidth();
	}

	public boolean portrait() {
		return this.getHeight() >= this.getWidth();
	}

	public abstract float getWidth();

	public abstract float getHeight();

	public float getContainerX() {
		return LSystem.getProcess() == null ? 0 : LSystem.getProcess().getX();
	}

	public float getContainerY() {
		return LSystem.getProcess() == null ? 0 : LSystem.getProcess().getY();
	}

	public float getContainerWidth() {
		return LSystem.viewSize.width();
	}

	public float getContainerHeight() {
		return LSystem.viewSize.height();
	}

	public Object getTag() {
		return Tag;
	}

	public void setTag(Object t) {
		this.Tag = t;
	}

	@Override
	public int compare(T o1, T o2) {
		if (o1 == null || o2 == null) {
			return 0;
		}
		if (o1 instanceof ZIndex && o2 instanceof ZIndex) {
			int diff = MathUtils.abs(((ZIndex) o1).getLayer()) - MathUtils.abs(((ZIndex) o2).getLayer());
			if (diff > 0) {
				return 1;
			}
			if (diff < 0) {
				return -1;
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		return _objectSeqNo;
	}

	@Override
	public String toString() {
		return new StringKeyValue("LObject").kv("sequence", _objectSeqNo).comma().kv("name", getName()).comma()
				.kv("state", _objectState.get()).comma()
				.kv("super", _objectSuper == null ? "empty" : _objectSuper.getClass()).comma()
				.kv("pos", _objectLocation).comma().kv("size", _objectRect).comma().kv("alpha", _objectAlpha).comma()
				.kv("rotation", _objectRotation).comma().kv("layer", _objectLayer).comma().kv("tag", Tag).toString();
	}

}
