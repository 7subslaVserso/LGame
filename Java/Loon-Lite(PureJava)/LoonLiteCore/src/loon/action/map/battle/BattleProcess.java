/**
 * Copyright 2008 - 2023 The Loon Game Engine Authors
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
package loon.action.map.battle;

import java.util.Comparator;

import loon.geom.BooleanValue;
import loon.utils.TArray;
import loon.utils.processes.RealtimeProcess;
import loon.utils.timer.LTimerContext;

public class BattleProcess extends RealtimeProcess {

	public static abstract class TurnBeginEvent extends BattleTurnProcessEvent {

		public TurnBeginEvent() {
			super(BattleState.TurnBeginState);
		}

	}

	public static abstract class TurnPlayerEvent extends BattleTurnProcessEvent {

		public TurnPlayerEvent() {
			super(BattleState.TurnPlayerState);
		}

	}

	public static abstract class TurnEnemyEvent extends BattleTurnProcessEvent {

		public TurnEnemyEvent() {
			super(BattleState.TurnEnemyState);
		}

	}

	public static abstract class TurnNpcEvent extends BattleTurnProcessEvent {

		public TurnNpcEvent() {
			super(BattleState.TurnNpcState);
		}

	}

	public static abstract class TurnOtherEvent extends BattleTurnProcessEvent {

		public TurnOtherEvent() {
			super(BattleState.TurnOtherState);
		}

	}

	public static abstract class TurnEndEvent extends BattleTurnProcessEvent {

		public TurnEndEvent() {
			super(BattleState.TurnEndState);
		}

	}

	public static abstract class TurnDoneEvent extends BattleTurnProcessEvent {

		public TurnDoneEvent() {
			super(BattleState.TurnDoneState);
		}

	}

	static class EventComparator implements Comparator<BattleEvent> {

		@Override
		public int compare(BattleEvent o1, BattleEvent o2) {
			if (o1 == null || o2 == null) {
				return 0;
			}
			return o2.getState().getPriority() - o1.getState().getPriority();
		}

	}

	private final static EventComparator _sortEvents = new EventComparator();

	private final BooleanValue _actioning = new BooleanValue();

	private TArray<BattleState> _states;

	private TArray<BattleEvent> _events;

	private BattleEvent _enforceEvent;

	private BattleState _stateCurrent;

	private BattleState _stateCompleted;

	private BattleResults _result;

	private boolean _pause;

	private boolean _enforce;

	private boolean _loop;

	public BattleProcess() {
		this._states = new TArray<BattleState>();
		this._events = new TArray<BattleEvent>();
		this._result = BattleResults.Running;
		this.setDelay(0);
	}

	protected boolean runBattleEvent(final BattleEvent turnEvent, final long elapsedTime) {
		final BattleState state = turnEvent.getState();
		this._stateCurrent = state;
		if (_stateCompleted != state) {
			if (turnEvent != null) {
				if (!turnEvent.start(elapsedTime)) {
					return false;
				}
				if (!turnEvent.process(elapsedTime)) {
					return false;
				}
				if (!turnEvent.end(elapsedTime)) {
					return false;
				}
				if (!turnEvent.completed()) {
					return false;
				}
				turnEvent.reset();
			}
			_stateCompleted = state;
		}
		return true;
	}

	protected boolean updateBattleEvent(final BattleEvent turnEvent, final long elapsedTime) {
		final BattleState state = turnEvent.getState();
		if (!_states.contains(state)) {
			this._stateCurrent = state;
			if (_stateCompleted != state) {
				if (turnEvent != null) {
					if (!turnEvent.start(elapsedTime)) {
						return false;
					}
					if (!turnEvent.process(elapsedTime)) {
						return false;
					}
					if (!turnEvent.end(elapsedTime)) {
						return false;
					}

					if (!turnEvent.completed()) {
						return false;
					}
					turnEvent.reset();
				}
				_stateCompleted = state;
				_states.add(state);
			}
		}
		return true;
	}

	/**
	 * 强制跳去指定事件
	 * 
	 * @param name
	 * @return
	 */
	public BattleProcess jumpTo(String name) {
		BattleEvent eve = null;
		for (BattleEvent e : _events) {
			if (e != null && name.equalsIgnoreCase(e.getState().getName())) {
				eve = e;
			}
		}
		return jumpTo(eve);
	}

	public BattleProcess jumpTo(BattleState state) {
		BattleEvent eve = null;
		for (BattleEvent e : _events) {
			if (e != null && e.getState().equals(state)) {
				eve = e;
			}
		}
		return jumpTo(eve);
	}

	protected BattleProcess jumpTo(BattleEvent eve) {
		if (eve != null) {
			eve.reset();
		}
		_enforceEvent = eve;
		_stateCurrent = null;
		_stateCompleted = null;
		_enforce = true;
		return this;
	}

	@Override
	public void run(LTimerContext time) {
		update(time.timeSinceLastUpdate);
	}

	public void update(long elapsedTime) {
		if (_pause) {
			return;
		}
		if (_result != BattleResults.Running) {
			return;
		}
		if (_enforce) {
			if (!runBattleEvent(_enforceEvent, elapsedTime)) {
				return;
			}
			this._enforce = false;
			this._enforceEvent = null;
		} else {
			if (!_loop) {
				return;
			}
			for (BattleEvent e : _events) {
				if (e != null) {
					if (!updateBattleEvent(e, elapsedTime)) {
						return;
					}
				}
			}
		}
		_states.clear();
		_stateCurrent = null;
		_stateCompleted = null;
	}

	public BattleProcess noLoop() {
		this._loop = false;
		return this;
	}

	public boolean isloop() {
		return _loop;
	}

	public BattleProcess setLoop(boolean l) {
		this._loop = l;
		return this;
	}

	public BattleProcess removeEvent(BattleEvent e) {
		if (e == null) {
			return this;
		}
		_events.remove(e);
		_events.sort(_sortEvents);
		return this;
	}

	public BattleProcess addEvent(BattleEvent e) {
		if (e == null) {
			return this;
		}
		_events.add(e);
		_events.sort(_sortEvents);
		return this;
	}

	public BattleProcess cleanUp() {
		this._states.clear();
		this._events.clear();
		this._result = BattleResults.Running;
		this._actioning.set(false);
		this._enforce = false;
		this._enforceEvent = null;
		this._stateCurrent = null;
		this._stateCompleted = null;
		this.setDelay(0);
		return this;
	}

	public boolean isCurrentBegin() {
		return isCurrentState(BattleState.TurnBeginState);
	}

	public boolean isCurrentPlayer() {
		return isCurrentState(BattleState.TurnPlayerState);
	}

	public boolean isCurrentEnemy() {
		return isCurrentState(BattleState.TurnEnemyState);
	}

	public boolean isCurrentNpc() {
		return isCurrentState(BattleState.TurnNpcState);
	}

	public boolean isCurrentEnd() {
		return isCurrentState(BattleState.TurnEndState);
	}

	public boolean isCurrentState(BattleState state) {

		if (state == null) {
			return false;
		}
		return state.equals(currentState());
	}

	public BattleState currentState() {
		if (_stateCurrent == null) {
			if (_events.size > 0) {
				return (_stateCurrent = _events.first().getState());
			}
		}
		return _stateCurrent;
	}

	public BattleState previousState() {
		return _stateCompleted;
	}

	public boolean isPaused() {
		return _pause;
	}

	public BattleProcess setPause(boolean p) {
		this._pause = p;
		return this;
	}

	public TArray<BattleState> getStates() {
		return _states;
	}

	public BattleResults getResult() {
		return _result;
	}

	public BattleProcess setResult(BattleResults result) {
		this._result = result;
		return this;
	}

	public BattleProcess setActioning(boolean a) {
		_actioning.set(a);
		return this;
	}

	public boolean isActioning() {
		return _actioning.get();
	}

	public BooleanValue getActioning() {
		return _actioning;
	}

}
