/**
 * 
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
package loon.action.collision;

import loon.geom.Vector2f;

public class CollisionPointQuery implements CollisionQuery {

	private float x;

	private float y;

	private String flag;

	private Vector2f offsetLocation;

	public CollisionPointQuery init(float x, float y, String flag, Vector2f offset) {
		this.x = offsetX(x);
		this.y = offsetY(y);
		this.flag = flag;
		this.offsetLocation = offset;
		return this;
	}

	private float offsetX(float x) {
		if (offsetLocation == null) {
			return x;
		}
		return x + offsetLocation.x;
	}

	private float offsetY(float y) {
		if (offsetLocation == null) {
			return y;
		}
		return y + offsetLocation.y;
	}

	@Override
	public boolean checkCollision(CollisionObject actor) {
		return this.flag != null && !flag.equals(actor.getObjectFlag()) ? false : actor.containsPoint(this.x, this.y);
	}

	@Override
	public void setOffsetPos(Vector2f offset) {
		offsetLocation = offset;
	}

	@Override
	public Vector2f getOffsetPos() {
		return offsetLocation;
	}
}
