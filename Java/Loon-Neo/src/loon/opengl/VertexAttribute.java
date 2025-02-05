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
package loon.opengl;

import loon.opengl.VertexAttributes.Usage;

public final class VertexAttribute {

	public final int usage;

	public final int numComponents;

	public final boolean normalized;

	public final int type;

	public int offset;

	public String alias;

	public int unit;
	private final int usageIndex;

	public VertexAttribute(int usage, int numComponents, String alias) {
		this(usage, numComponents, alias, 0);
	}

	public VertexAttribute(int usage, int numComponents, String alias, int index) {
		this(usage, numComponents, usage == Usage.ColorPacked ? GL20.GL_UNSIGNED_BYTE : GL20.GL_FLOAT,
				usage == Usage.ColorPacked, alias, index);
	}

	private VertexAttribute(int usage, int numComponents, int type, boolean normalized, String alias) {
		this(usage, numComponents, type, normalized, alias, 0);
	}

	private VertexAttribute(int usage, int numComponents, int type, boolean normalized, String alias, int index) {
		this.usage = usage;
		this.numComponents = numComponents;
		this.type = type;
		this.normalized = normalized;
		this.alias = alias;
		this.unit = index;
		this.usageIndex = Integer.numberOfTrailingZeros(usage);
	}

	public static VertexAttribute Position() {
		return new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE);
	}

	public static VertexAttribute TexCoords(int unit) {
		return new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + unit, unit);
	}

	public static VertexAttribute Normal() {
		return new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE);
	}

	public static VertexAttribute Color() {
		return ColorPacked();
	}

	public static VertexAttribute ColorPacked() {
		return new VertexAttribute(Usage.ColorPacked, 4, GL20.GL_UNSIGNED_BYTE, true, ShaderProgram.COLOR_ATTRIBUTE);
	}

	public static VertexAttribute ColorUnpacked() {
		return new VertexAttribute(Usage.Color, 4, GL20.GL_FLOAT, false, ShaderProgram.COLOR_ATTRIBUTE);
	}

	public static VertexAttribute Tangent() {
		return new VertexAttribute(Usage.Tangent, 3, ShaderProgram.TANGENT_ATTRIBUTE);
	}

	public static VertexAttribute Binormal() {
		return new VertexAttribute(Usage.BiNormal, 3, ShaderProgram.BINORMAL_ATTRIBUTE);
	}

	public static VertexAttribute BoneWeight(int unit) {
		return new VertexAttribute(Usage.BoneWeight, 2, "a_boneWeight" + unit, unit);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof VertexAttribute)) {
			return false;
		}
		return equals((VertexAttribute) obj);
	}

	public boolean equals(final VertexAttribute other) {
		return other != null && usage == other.usage && numComponents == other.numComponents
				&& alias.equals(other.alias) && unit == other.unit;
	}

	public int getKey() {
		return (usageIndex << 8) + (unit & 0xFF);
	}

	@Override
	public int hashCode() {
		int result = getKey();
		result = 541 * result + numComponents;
		result = 541 * result + alias.hashCode();
		return result;
	}
}
