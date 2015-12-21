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
package loon.robovm;

import org.robovm.apple.uikit.UIInterfaceOrientation;

public abstract class RoboVMOrientEvent {

  public static class WillRotate extends RoboVMOrientEvent {
  
    public final UIInterfaceOrientation toOrient;

    public final double duration;

    public WillRotate (UIInterfaceOrientation toOrient, double duration) {
      this.toOrient = toOrient;
      this.duration = duration;
    }
  }

  public static class DidRotate extends RoboVMOrientEvent {

    public final UIInterfaceOrientation fromOrient;

    public DidRotate (UIInterfaceOrientation fromOrient) {
      this.fromOrient = fromOrient;
    }
  }
}
