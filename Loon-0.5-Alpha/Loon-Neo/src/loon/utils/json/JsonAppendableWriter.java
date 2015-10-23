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
package loon.utils.json;

import java.io.Flushable;
import java.io.IOException;

final class JsonAppendableWriter extends JsonWriterBase<JsonAppendableWriter> implements
    JsonSink<JsonAppendableWriter> {
  JsonAppendableWriter(Appendable appendable) {
    super(appendable);
  }

  public void done() throws JsonWriterException {
    super.doneInternal();
    if (appendable instanceof Flushable) {
      try {
        ((Flushable) appendable).flush();
      } catch (IOException e) {
        throw new JsonWriterException(e);
      }
    }
  }
}
