/**
 * Copyright 2008 - 2020 The Loon Game Engine Authors
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
package loon.action.behaviors;

import loon.LRelease;

public abstract class Behavior<T> implements LRelease {

	public TaskStatus status = TaskStatus.Invalid;

	public abstract TaskStatus update(T context);

	public void invalidate() {
		this.status = TaskStatus.Invalid;
	}

	public abstract void onStart();

	public abstract void onEnd();

	public TaskStatus tick(T context) {
		if (status == TaskStatus.Invalid) {
			onStart();
		}
		status = update(context);
		if (status != TaskStatus.Running) {
			onEnd();
		}
		return status;
	}

	@Override
	public void close() {
		onEnd();
		status = TaskStatus.Invalid;
	}
}
