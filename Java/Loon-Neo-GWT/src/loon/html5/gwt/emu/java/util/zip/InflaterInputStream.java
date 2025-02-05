/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package java.util.zip;

import java.io.IOException;
import java.io.InputStream;

public class InflaterInputStream extends InputStream {
	private InputStream in;

	public InflaterInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		throw new RuntimeException("InflaterInputStream not supported in GWT");
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (in != null) {
			in.close();
		}
	}
}
