/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
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
package loon.fx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import loon.LSysException;
import loon.LTexture;
import loon.canvas.Canvas;
import loon.canvas.Image;
import loon.geom.Affine2f;
import loon.opengl.Mesh;
import loon.opengl.MeshData;
import loon.utils.MathUtils;

public class JavaFXMesh implements Mesh {

	private MeshData mesh;

	/**
	 * 矩阵
	 */
	private Affine2f transform;

	/**
	 * 绘图环境
	 */
	private JavaFXCanvas _canvas;

	private ColorAdjust adjust;

	private Lighting lighting;

	private Light light;

	private int lastTint = -1;

	private int mode = 0;

	public JavaFXMesh(Canvas canvas) {
		if (canvas == null) {
			throw new LSysException("Canvas is null !");
		}
		this._canvas = (JavaFXCanvas) canvas;
		this.adjust = new ColorAdjust();
		this.lighting = new Lighting();
		this.lighting.setDiffuseConstant(1);
		this.lighting.setSpecularConstant(0);
		this.lighting.setSpecularExponent(0);
		lighting.setSurfaceScale(0);
	}

	/**
	 * 将mesh数据渲染到Canvas上面
	 * 
	 * 
	 */
	@Override
	public void paint() {
		if (mesh != null) {
			if (mode == 0) {
				renderWithIndexes(mesh);
			} else {
				renderNoIndexes(mesh);
			}
		}
	}

	/**
	 * 无顶点索引的模式
	 * 
	 * @param mesh
	 * 
	 */
	public void renderNoIndexes(MeshData mesh) {
		int i, len = mesh.amount == -1 ? mesh.vertices.length / 2 : mesh.amount;
		int index;
		for (i = 0; i < len - 2; i++) {
			index = i * 2;
			this.renderDrawTriangle(mesh, index, (index + 2), (index + 4));
		}
	}

	/**
	 * 使用顶点索引模式绘制
	 * 
	 * @param mesh
	 * 
	 */
	public void renderWithIndexes(MeshData mesh) {
		int[] indexes = mesh.indexes;
		int i, len = mesh.amount == -1 ? indexes.length : mesh.amount;
		for (i = 0; i < len; i += 3) {
			int index0 = indexes[i] * 2;
			int index1 = indexes[i + 1] * 2;
			int index2 = indexes[i + 2] * 2;
			this.renderDrawTriangle(mesh, index0, index1, index2);
		}
	}

	public void renderDrawTriangle(MeshData mesh, int index0, int index1, int index2) {

		float[] uvs = mesh.uvs;
		float[] vertices = mesh.vertices;
		LTexture texture = mesh.texture;

		Image source = texture.getImage();

		float textureWidth = texture.pixelWidth();
		float textureHeight = texture.pixelHeight();
		float sourceWidth = source.getWidth();
		float sourceHeight = source.getHeight();

		// uv数据
		float u0 = 1f;
		float u1 = 1f;
		float u2 = 1f;
		float v0 = 1f;
		float v1 = 1f;
		float v2 = 1f;

		if (mesh.useUvTransform) {
			Affine2f ut = mesh.uvTransform;

			u0 = ((uvs[index0] * ut.m00) + (uvs[index0 + 1] * ut.m10) + ut.tx) * sourceWidth;
			u1 = ((uvs[index1] * ut.m00) + (uvs[index1 + 1] * ut.m10) + ut.tx) * sourceWidth;
			u2 = ((uvs[index2] * ut.m00) + (uvs[index2 + 1] * ut.m10) + ut.tx) * sourceWidth;
			v0 = ((uvs[index0] * ut.m01) + (uvs[index0 + 1] * ut.m11) + ut.ty) * sourceHeight;
			v1 = ((uvs[index1] * ut.m01) + (uvs[index1 + 1] * ut.m11) + ut.ty) * sourceHeight;
			v2 = ((uvs[index2] * ut.m01) + (uvs[index2 + 1] * ut.m11) + ut.ty) * sourceHeight;
		} else {
			u0 = uvs[index0] * sourceWidth;
			u1 = uvs[index1] * sourceWidth;
			u2 = uvs[index2] * sourceWidth;
			v0 = uvs[index0 + 1] * sourceHeight;
			v1 = uvs[index1 + 1] * sourceHeight;
			v2 = uvs[index2 + 1] * sourceHeight;
		}

		// 绘制顶点数据
		float x0 = vertices[index0];
		float x1 = vertices[index1];
		float x2 = vertices[index2];
		float y0 = vertices[index0 + 1];
		float y1 = vertices[index1 + 1];
		float y2 = vertices[index2 + 1];

		if (mesh.canvasPadding > 0) {// 扩展区域，解决黑边问题
			float paddingX = mesh.canvasPadding;
			float paddingY = mesh.canvasPadding;
			float centerX = (x0 + x1 + x2) / 3;
			float centerY = (y0 + y1 + y2) / 3;

			float normX = x0 - centerX;
			float normY = y0 - centerY;

			float dist = MathUtils.sqrt((normX * normX) + (normY * normY));

			x0 = centerX + ((normX / dist) * (dist + paddingX));
			y0 = centerY + ((normY / dist) * (dist + paddingY));

			normX = x1 - centerX;
			normY = y1 - centerY;

			dist = MathUtils.sqrt((normX * normX) + (normY * normY));
			x1 = centerX + ((normX / dist) * (dist + paddingX));
			y1 = centerY + ((normY / dist) * (dist + paddingY));

			normX = x2 - centerX;
			normY = y2 - centerY;

			dist = MathUtils.sqrt((normX * normX) + (normY * normY));
			x2 = centerX + ((normX / dist) * (dist + paddingX));
			y2 = centerY + ((normY / dist) * (dist + paddingY));
		}

		final GraphicsContext context = _canvas.context;

		context.save();
		if (transform != null) {
			context.transform(transform.m00, transform.m01, transform.m10, transform.m11, transform.tx, transform.ty);
		}

		// 创建三角形裁剪区域
		context.beginPath();

		context.moveTo(x0, y0);
		context.lineTo(x1, y1);
		context.lineTo(x2, y2);

		context.closePath();

		context.clip();

		// 计算矩阵，将图片变形到合适的位置
		float delta = (u0 * v1) + (v0 * u2) + (u1 * v2) - (v1 * u2) - (v0 * u1) - (u0 * v2);
		float dDelta = 1 / delta;
		float deltaA = (x0 * v1) + (v0 * x2) + (x1 * v2) - (v1 * x2) - (v0 * x1) - (x0 * v2);
		float deltaB = (u0 * x1) + (x0 * u2) + (u1 * x2) - (x1 * u2) - (x0 * u1) - (u0 * x2);
		float deltaC = (u0 * v1 * x2) + (v0 * x1 * u2) + (x0 * u1 * v2) - (x0 * v1 * u2) - (v0 * u1 * x2)
				- (u0 * x1 * v2);
		float deltaD = (y0 * v1) + (v0 * y2) + (y1 * v2) - (v1 * y2) - (v0 * y1) - (y0 * v2);
		float deltaE = (u0 * y1) + (y0 * u2) + (u1 * y2) - (y1 * u2) - (y0 * u1) - (u0 * y2);
		float deltaF = (u0 * v1 * y2) + (v0 * y1 * u2) + (y0 * u1 * v2) - (y0 * v1 * u2) - (v0 * u1 * y2)
				- (u0 * y1 * v2);

		context.transform(deltaA * dDelta, deltaD * dDelta, deltaB * dDelta, deltaE * dDelta, deltaC * dDelta,
				deltaF * dDelta);

		context.drawImage(((JavaFXImage) source).buffer, texture.widthRatio() * sourceWidth,
				texture.heightRatio() * sourceHeight, textureWidth, textureHeight, texture.widthRatio() * sourceWidth,
				texture.heightRatio() * sourceHeight, textureWidth, textureHeight);

		context.restore();

	}

	@Override
	public MeshData getMesh() {
		return mesh;
	}

	@Override
	public void setMesh(MeshData mesh) {
		this.mesh = mesh;
	}

	@Override
	public void setIndices(int[] inds) {
		mesh.indexes = inds;
	}

	@Override
	public void setVertices(float[] vers) {
		mesh.vertices = vers;
	}

	protected static double colorMap(double value, double start, double stop, double targetStart, double targetStop) {
		return targetStart + (targetStop - targetStart) * ((value - start) / (stop - start));
	}

	protected ColorAdjust getAdjust(Color paint) {
		double hue = colorMap((paint.getHue() + 180) % 360, 0, 360, -1, 1);
		adjust.setHue(hue);
		double saturation = paint.getSaturation();
		adjust.setSaturation(saturation);
		double brightness = colorMap(paint.getBrightness(), 0, 1, -1, 0);
		adjust.setBrightness(brightness);
		return adjust;
	}

	public void save() {
		_canvas.context.save();
	}

	@Override
	public void restore() {
		_canvas.context.restore();
	}

	@Override
	public void transform(float m00, float m01, float m10, float m11, float tx, float ty) {
		_canvas.context.transform(m00, m01, m10, m11, tx, ty);
	}

	@Override
	public void transform(Affine2f aff) {
		_canvas.context.transform(aff.m00, aff.m01, aff.m10, aff.m11, aff.tx, aff.ty);
	}

	@Override
	public void paint(int tint, Affine2f aff, float left, float top, float right, float bottom, float sl, float st,
			float sr, float sb) {
		paint(tint, aff.m00, aff.m01, aff.m10, aff.m11, aff.tx, aff.ty, left, top, right, bottom, sl, st, sr, sb);
	}

	@Override
	public void paint(int tint, float m00, float m01, float m10, float m11, float tx, float ty, float left, float top,
			float right, float bottom, float sl, float st, float sr, float sb) {
		LTexture texture = mesh.texture;
		Image img = texture.getSourceImage();

		if (img != null) {

			int r = (tint & 0x00FF0000) >> 16;
			int g = (tint & 0x0000FF00) >> 8;
			int b = (tint & 0x000000FF);
			int a = (tint & 0xFF000000) >> 24;

			if (a < 0) {
				a += 256;
			}
			if (a == 0) {
				a = 255;
			}

			final GraphicsContext context = _canvas.context;
			final Color paint = Color.rgb(r, g, b);
			final float canvasAlpha = (float) context.getGlobalAlpha();
			final float alpha = ((float) a / 255f) * canvasAlpha;
			if (alpha != 1f) {
				context.setGlobalAlpha(alpha);
			}
			context.setTransform(m00, m01, m10, m11, tx, ty);
			if (!texture.isChild() && sl == 0f && st == 0f && sr == 1f && sb == 1f) {
				if (tint == -1 && alpha == 1f) {
					context.drawImage(((JavaFXImage) img).buffer, left, top, (right - left), (bottom - top));
				} else {
					context.save();
					if (tint != lastTint || light == null) {
						light = new Light.Distant(65, 65, paint);
						lastTint = tint;
					}
					lighting.setLight(light);
					context.setEffect(lighting);
					context.drawImage(((JavaFXImage) img).buffer, left, top, (right - left), (bottom - top));
					context.restore();
				}
			} else {
				float textureWidth = texture.getDisplayWidth();
				float textureHeight = texture.getDisplayHeight();
				float dstX = textureWidth * (sl);
				float dstY = textureHeight * (st);
				float dstWidth = textureWidth * (sr);
				float dstHeight = textureHeight * (sb);
				if (tint == -1 && alpha == 1f) {
					context.drawImage(((JavaFXImage) img).buffer, dstX, dstY, dstWidth - dstX, dstHeight - dstY, left,
							top, (right - left), (bottom - top));
				} else {
					context.save();
					if (tint != lastTint || light == null) {
						light = new Light.Distant(65, 65, paint);
						lastTint = tint;
					}
					lighting.setLight(light);
					context.setEffect(lighting);
					context.drawImage(((JavaFXImage) img).buffer, dstX, dstY, dstWidth - dstX, dstHeight - dstY, left,
							top, (right - left), (bottom - top));
					context.restore();
				}
			}
			if (alpha != 1f) {
				context.setGlobalAlpha(canvasAlpha);
			}
		}
	}
}
