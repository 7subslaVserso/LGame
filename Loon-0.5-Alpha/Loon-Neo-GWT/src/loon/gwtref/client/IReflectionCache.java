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
package loon.gwtref.client;

public interface IReflectionCache {

	public Type forName (String name);

	public Object newArray (Type componentType, int size);

	public int getArrayLength (Type type, Object obj);

	public Object getArrayElement (Type type, Object obj, int i);

	public void setArrayElement (Type type, Object obj, int i, Object value);

	public Object get (Field field, Object obj) throws IllegalAccessException;

	public void set (Field field, Object obj, Object value) throws IllegalAccessException;

	public Object invoke (Method m, Object obj, Object[] params);
}
