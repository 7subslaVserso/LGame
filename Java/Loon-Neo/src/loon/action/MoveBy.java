package loon.action;

import loon.utils.Easing.EasingMode;
import loon.utils.timer.EaseTimer;

public class MoveBy extends ActionEvent {

	private int _speed = 1;

	private float _startX = -1, _startY = -1, _endX, _endY;

	private EaseTimer easeTimer;

	public MoveBy(float endX, float endY, float duration, float delay,
			EasingMode easing) {
		this(-1, -1, endX, endY, 0, duration, delay, easing, 0, 0);
	}

	public MoveBy(float endX, float endY, EasingMode easing) {
		this(-1, -1, endX, endY, 0, 1f, 1f / 60f, easing, 0, 0);
	}

	public MoveBy(float endX, float endY, float duration, EasingMode easing) {
		this(-1, -1, endX, endY, 0, duration, 1f / 60f, easing, 0, 0);
	}

	public MoveBy(float endX, float endY, int speed) {
		this(-1, -1, endX, endY, speed, 1f, 1f / 60f, EasingMode.Linear, 0, 0);
	}

	public MoveBy(float endX, float endY, int speed, EasingMode easing,
			float sx, float sy) {
		this(-1, -1, endX, endY, speed, 1f, 1f / 60f, easing, sx, sy);
	}

	public MoveBy(float startX, float startY, float endX, float endY,
			int speed, float duration, float delay, EasingMode easing,
			float sx, float sy) {
		this._startX = startX;
		this._startY = startY;
		this._endX = endX;
		this._endY = endY;
		this._speed = speed;
		this.offsetX = sx;
		this.offsetY = sy;
		this.easeTimer = new EaseTimer(duration, delay, easing);
		this.setDelay(0);
	}

	@Override
	public void update(long elapsedTime) {
		synchronized (original) {
			if (_speed == 0) {
				easeTimer.update(elapsedTime);
				if (easeTimer.isCompleted()) {
					_isCompleted = true;
					return;
				}
				original.setLocation(
						_startX + (_endX - _startX) * easeTimer.getProgress()
								+ offsetX, _startY + (_endY - _startY)
								* easeTimer.getProgress() + offsetY);
			} else {
				float x = original.getX();
				float y = original.getY();
				int dirX = (int) (_endX - _startX);
				int dirY = (int) (_endY - _startY);
				int count = 0;
				if (dirX > 0) {
					if (x >= _endX) {
						count++;
					} else {
						x += _speed;
					}
				} else if (dirX < 0) {
					if (x <= _endX) {
						count++;
					} else {
						x -= _speed;
					}
				} else {
					count++;
				}
				if (dirY > 0) {
					if (y >= _endY) {
						count++;
					} else {
						y += _speed;
					}
				} else if (dirY < 0) {
					if (y <= _endY) {
						count++;
					} else {
						y -= _speed;
					}
				} else {
					count++;
				}
				original.setLocation(x + offsetX, y + offsetY);
				_isCompleted = (count == 2);
			}
		}

	}

	@Override
	public void onLoad() {
		if (original != null) {
			if (_startX == -1) {
				_startX = original.getX();
			}
			if (_startY == -1) {
				_startY = original.getY();
			}
		}
	}

	@Override
	public boolean isComplete() {
		return _isCompleted;
	}

	@Override
	public ActionEvent cpy() {
		MoveBy move = new MoveBy(_startX, _startY, _endX, _endY, _speed,
				easeTimer.getDuration(), easeTimer.getDelay(),
				easeTimer.getEasingMode(), offsetX, offsetY);
		move.set(this);
		return move;
	}

	@Override
	public ActionEvent reverse() {
		MoveBy move = new MoveBy(_endX, _endY, _startX, _startY, _speed,
				easeTimer.getDuration(), easeTimer.getDelay(),
				easeTimer.getEasingMode(), offsetX, offsetY);
		move.set(this);
		return move;
	}

	@Override
	public String getName() {
		return "moveby";
	}

}
