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
package loon.utils.reply;

public interface VarView<T>
{
    interface Listener<T> extends Bypass.GoListener {
        void onChange (T value, T oldValue);
    }

    T get ();

    <M> VarView<M> map (Function<? super T, M> func);

    <M> VarView<M> flatMap (Function<? super T, ? extends VarView<M>> func);

    Connection connect (Listener<? super T> listener);

    Connection connectNotify (Listener<? super T> listener);

    void disconnect (Listener<? super T> listener);

    Connection connect (ActView.Listener<? super T> listener);

    Connection connectNotify (ActView.Listener<? super T> listener);

    Connection connect (Port<? super T> listener);

    Connection connectNotify (Port<? super T> listener);
}
