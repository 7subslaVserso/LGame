/**
 * Copyright 2008 - 2012
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
 * @version 0.3.3
 */
package loon.action.map.heuristics;

import loon.action.map.AStarFindHeuristic;
import loon.utils.MathUtils;

public class DiagonalShort implements AStarFindHeuristic {

	@Override
	public float getScore(float sx, float sy, float tx, float ty) {
		float diagonal = MathUtils.min(MathUtils.abs(sx - tx), MathUtils.abs(sy - ty));
		float straight = (MathUtils.abs(sx - tx) + MathUtils.abs(sy - ty));
		return 2f * diagonal + (straight - 2f * diagonal);
	}

	@Override
	public int getType() {
		return DIAGONAL_SHORT;
	}

	@Override
	public String toString() {
		return "DiagonalShort";
	}
}
