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
package loon.utils.cache;

import loon.LSysException;
import loon.utils.Array;
import loon.utils.MathUtils;

public abstract class Pool<T> {
	
	public final int max;

	public int peak;

	private final Array<T> freeObjects;
	
	public Pool () {
		this(Integer.MAX_VALUE);
	}
	
	public Pool (int max) {
		this.freeObjects = new Array<T>();
		this.max = max;
	}

	abstract protected T newObject ();

	public T obtain () {
		return freeObjects.size() == 0 ? newObject() : freeObjects.pop();
	}

	public void free (T object) {
		if (object == null) throw new LSysException("object cannot be null.");
		if (freeObjects.size() < max) {
			freeObjects.add(object);
			peak = MathUtils.max(peak, freeObjects.size());
		}
		if (object instanceof Poolable) ((Poolable)object).reset();
	}

	public void freeAll (Array<T> objects) {
		if (objects == null){
			throw new LSysException("object cannot be null.");
		}
		for (;objects.hashNext();) {
			T object = objects.next();
			if (object == null){
				continue;
			}
			if (freeObjects.size() < max) {
				freeObjects.add(object);
			}
			if (object instanceof Poolable){
				((Poolable)object).reset();
			}
		}
		objects.stopNext();
		peak = MathUtils.max(peak, freeObjects.size());
	}

	public void clear () {
		freeObjects.clear();
	}

	public int getFree () {
		return freeObjects.size();
	}

	static public interface Poolable {
		public void reset ();
	}
}
