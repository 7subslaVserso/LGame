/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
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

public class BattlePlayer {
	
	private String id;

	private boolean heads;

	private boolean callMade;

	public BattlePlayer(String id) {
		this.id = id;
		this.heads = false;
		callMade = false;
	}

	public String getId() {
		return id;
	}

	public boolean isHeads() {
		return heads;
	}

	public void setHeads(boolean heads) {
		this.heads = heads;
		this.callMade = true;
	}

	public boolean isCallMade() {
		return callMade;
	}

	public void resetForNewRound() {
		heads = false;
		callMade = false;
	}

	public boolean isWinner(boolean flippedHeads) {
		return (callMade && flippedHeads == heads);
	}
}
