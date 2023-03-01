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
package loon;

import loon.canvas.Canvas;
import loon.canvas.Image;
import loon.canvas.LColor;
import loon.geom.Affine2f;
import loon.opengl.GLEx;
import loon.opengl.Mesh;
import loon.opengl.MeshData;

/**
 * 这是一个针对单独纹理的批量渲染类,默认绑定在特定Texture上运行（_texture.geTexturetBatch即可获得）,<br>
 * 方便针对特定纹理的缓存以及渲染.
 */
public class LTextureBatch implements LRelease {

	private boolean isClosed;

	public boolean isCacheLocked;

	private Cache lastCache;

	/**
	 * 纯Java环境版本使用的是Image存储缓存图像,创建太多Cache可能会耗尽内存
	 */
	public static class Cache implements LRelease {

		public float x = 0;

		public float y = 0;

		protected Image _image;

		public Cache(LTextureBatch batch) {
			Image img = batch._buffer.snapshot();
			_image = Image.createImage(img.getWidth(), img.getHeight());
			_image.draw(img, 0, 0, img.getWidth(), img.getHeight());
		}

		public Image get() {
			return _image;
		}

		public boolean isClosed() {
			return _image == null || _image.isClosed();
		}

		@Override
		public void close() {
			if (_image != null) {
				_image.close();
			}
		}
	}

	private Mesh _mesh;

	private boolean isInitMesh;

	private Canvas _buffer;

	private Affine2f _glexAffine;

	private Affine2f _displayAffine;

	private LColor _color = new LColor();

	protected int count = 0;

	protected boolean drawing = false;

	public int maxSpritesInBatch = 0;

	protected boolean isLoaded;

	protected LTexture _texture;

	private int vertexIdx;

	private int texWidth, texHeight;

	private float tx, ty;

	private MeshData _meshdata;

	public LTextureBatch(LTexture tex) {
		this.setTexture(tex);
	}

	public LTextureBatch begin() {
		if (!isLoaded) {
			isLoaded = true;
		}
		if (!_texture.isLoaded()) {
			_texture.loadTexture();
		}
		if (drawing) {
			throw new LSysException("TextureBatch.end must be called before begin.");
		}
		if (!isInitMesh) {
			if (_buffer == null) {
				_buffer = LSystem.base().graphics().createCanvas(LSystem.viewSize.getWidth(),
						LSystem.viewSize.getHeight());
			}
			if (_meshdata == null) {
				_meshdata = new MeshData();
			}
			if (_mesh == null) {
				_mesh = LSystem.base().makeMesh(_buffer);
			}
			this._mesh.setMesh(_meshdata);
			this.isInitMesh = true;
		}
		_meshdata.texture = _texture;
		_glexAffine.set(LSystem.base().graphics().getViewAffine());
		LSystem.mainEndDraw();
		if (!isCacheLocked) {
			vertexIdx = 0;
		}
		drawing = true;
		return this;
	}

	public LTextureBatch end() {
		if (!isLoaded) {
			return this;
		}
		if (!drawing) {
			throw new LSysException("TextureBatch.begin must be called before end.");
		}
		if (vertexIdx > 0) {
			submit();
		}
		drawing = false;
		LSystem.mainBeginDraw();
		return this;
	}

	public LTextureBatch setLocation(float tx, float ty) {
		this.tx = tx;
		this.ty = ty;
		return this;
	}

	public LTextureBatch setTexture(LTexture tex2d) {
		this._texture = tex2d;
		this.texWidth = (int) _texture.width();
		this.texHeight = (int) _texture.height();
		return this;
	}

	public int getTextureWidth() {
		return texWidth;
	}

	public int getTextureHeight() {
		return texHeight;
	}

	public LTexture toTexture() {
		return _texture;
	}

	public LTextureBatch setColor(LColor tint) {
		_color.setColor(tint);
		return this;
	}

	public LTextureBatch setColor(float r, float g, float b, float a) {
		_color.setColor(r, g, b, a);
		return this;
	}

	public LColor getColor() {
		return _color.cpy();
	}

	private LTextureBatch checkDrawing() {
		if (!drawing) {
			throw new LSysException("Not implemented begin !");
		}
		return this;
	}

	public boolean checkTexture(final LTexture _texture) {
		if (!isLoaded || isCacheLocked) {
			return false;
		}
		if (isClosed) {
			return false;
		}
		if (_texture == null) {
			return false;
		}
		checkDrawing();
		if (!_texture.isLoaded()) {
			_texture.loadTexture();
		}
		return true;
	}

	public LTextureBatch clear() {
		_buffer.clear();
		return this;
	}

	public LTextureBatch submit() {
		if (isClosed) {
			return this;
		}
		GLEx gl = LSystem.base().display().GL();
		if (gl != null) {
			Canvas canvas = gl.getCanvas();
			canvas.setTransform(_displayAffine);
			canvas.draw(_buffer.snapshot(), 0f, 0f);
			gl.synchTransform();
		}
		return this;
	}

	public LTextureBatch commit(final float x, final float y) {
		return commit(x, y, 0f);
	}

	public LTextureBatch commit(float x, float y, float rotation) {
		return commit(x, y, 1f, 1f, 0f, 0f, 0f, false, false, false);
	}

	public LTextureBatch commit(float x, float y, float rotation, boolean flipX, boolean flipY, boolean flipZ) {
		return commit(x, y, 1f, 1f, 0f, 0f, rotation, flipX, flipY, flipZ);
	}

	public LTextureBatch commit(float x, float y, float sx, float sy, float ax, float ay, float rotation, boolean flipX,
			boolean flipY, boolean flipZ) {
		if (isClosed) {
			return this;
		}
		GLEx gl = LSystem.base().display().GL();
		if (gl != null) {

			final Image image = _buffer.snapshot();
			final int width = image.getWidth();
			final int height = image.getHeight();

			final boolean oriDirty = (ax != 0 || ay != 0);
			final boolean rotDirty = (rotation != 0);
			final boolean scaleDirty = !(sx == 1 && sy == 1);

			if (rotDirty || oriDirty || scaleDirty) {
				_displayAffine.idt();
				if (oriDirty) {
					_displayAffine.translate(ax, ay);
				}
				if (rotDirty) {
					float centerX = x + width / 2;
					float centerY = y + height / 2;
					_displayAffine.translate(centerX, centerY);
					_displayAffine.preRotate(rotation);
					_displayAffine.translate(-centerX, -centerY);
				}
				if (scaleDirty) {
					float centerX = x + width / 2;
					float centerY = y + height / 2;
					_displayAffine.translate(centerX, centerY);
					_displayAffine.preScale(sx, sy);
					_displayAffine.translate(-centerX, -centerY);
				}

				if (flipZ) {
					flipX = !flipX;
					flipY = !flipY;
				}
				if (flipX || flipY) {
					if (flipX && flipY) {
						Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_ROT180, width, height);
					} else if (flipX) {
						Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_MIRROR, width, height);
					} else if (flipY) {
						Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_MIRROR_ROT180, width, height);
					}
				}

				Affine2f.multiply(_glexAffine, _displayAffine, _displayAffine);
			} else {
				_displayAffine.set(_glexAffine);
			}
			gl.getCanvas().setTransform(_displayAffine);
			gl.getCanvas().draw(_buffer.snapshot(), x, y);
			gl.synchTransform();
		}
		return this;
	}

	public boolean isDrawing() {
		return drawing;
	}

	public LTextureBatch lock() {
		this.isCacheLocked = true;
		return this;
	}

	public LTextureBatch unLock() {
		this.isCacheLocked = false;
		return this;
	}

	public boolean postLastCache() {
		if (lastCache != null) {
			postCache(lastCache, _color, 0f);
			return true;
		}
		return false;
	}

	public Cache getLastCache() {
		return lastCache;
	}

	public boolean existCache() {
		return lastCache != null && !lastCache.isClosed();
	}

	public Cache newCache() {
		if (isLoaded) {
			return (lastCache = new Cache(this));
		} else {
			return null;
		}
	}

	public boolean disposeLastCache() {
		if (lastCache != null) {
			lastCache.close();
			lastCache = null;
			return true;
		}
		return false;
	}

	public LTextureBatch draw(float x, float y) {
		return draw(x, y, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height) {
		return draw(x, y, width, height, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight) {
		return draw(x, y, width, height, srcX, srcY, srcWidth, srcHeight, _color);
	}

	public LTextureBatch draw(float x, float y, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, _texture.width() / 2, _texture.height() / 2, _texture.width(), _texture.height(),
				1f, 1f, 0f, 0, 0, _texture.width(), _texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, 0, 0, _texture.width(),
				_texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float x1, float y1, float x2, float y2,
			LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, x1, y1, x2, y2, false, false,
				update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float rotation, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, _texture.width() / 2, _texture.height() / 2, _texture.width(), _texture.height(),
				1f, 1f, rotation, 0, 0, _texture.width(), _texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float rotation, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, rotation, 0, 0, _texture.width(),
				_texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float srcX, float srcY, float srcWidth, float srcHeight, float rotation,
			LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, _texture.width() / 2, _texture.height() / 2, _texture.width(), _texture.height(),
				1f, 1f, rotation, srcX, srcY, srcWidth, srcHeight, false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, float rotation) {
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, rotation, srcX, srcY, srcWidth,
				srcHeight, false, false, _color);
	}

	public LTextureBatch draw(float x, float y, float originX, float originY, float width, float height, float scaleX,
			float scaleY, float rotation, float srcX, float srcY, float srcWidth, float srcHeight, boolean flipX,
			boolean flipY) {
		return draw(x, y, -1f, -1f, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth,
				srcHeight, flipX, flipY, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY) {
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY, LColor color) {
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY, color);
	}

	public LTextureBatch draw(float x, float y, float pivotX, float pivotY, float originX, float originY, float width,
			float height, float scaleX, float scaleY, float rotation, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY, LColor color) {

		if (!checkTexture(_texture)) {
			return this;
		}

		boolean rotDirty = (rotation != 0 || (pivotX != -1 && pivotY != -1));

		boolean oriDirty = (originX != 0 || originY != 0);

		boolean scaleDirty = !(scaleX == 1 && scaleY == 1);

		if (flipX || flipY || rotDirty || oriDirty || scaleDirty) {
			_displayAffine.idt();
			if (oriDirty) {
				_displayAffine.translate(originX, originY);
			}
			if (rotDirty) {
				float centerX = x + width / 2;
				float centerY = y + height / 2;
				if (pivotX != -1 && pivotY != -1) {
					centerX = x + pivotX;
					centerX = y + pivotY;
				}
				_displayAffine.translate(centerX, centerY);
				_displayAffine.preRotate(rotation);
				_displayAffine.translate(-centerX, -centerY);
			}
			if (scaleDirty) {
				float centerX = x + width / 2;
				float centerY = y + height / 2;
				if (pivotX != -1 && pivotY != -1) {
					centerX = x + pivotX;
					centerX = y + pivotY;
				}
				_displayAffine.translate(centerX, centerY);
				_displayAffine.preScale(scaleX, scaleY);
				_displayAffine.translate(-centerX, -centerY);
			}

			if (flipX || flipY) {
				if (flipX && flipY) {
					Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_ROT180, width, height);
				} else if (flipX) {
					Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_MIRROR, width, height);
				} else if (flipY) {
					Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_MIRROR_ROT180, width, height);
				}
			}

			Affine2f.multiply(_glexAffine, _displayAffine, _displayAffine);
		} else {
			_displayAffine.set(_glexAffine);
		}

		int argb = this._color.getABGR();
		if (color != null) {
			argb = LColor.combine(argb, color.getARGB());
		}

		if (srcX == 0 && srcY == 0 && srcWidth == width && srcHeight == height) {
			float u2 = _texture.widthRatio();
			float uv = _texture.heightRatio();
			_mesh.paint(argb, _displayAffine, x, y, x + width, y + height, _texture.xOff(), _texture.yOff(), u2, uv);
		} else {
			float displayWidth = _texture.getDisplayWidth();
			float displayHeight = _texture.getDisplayHeight();

			float xOff = 0f;
			float yOff = 0f;
			float widthRatio = 1f;
			float heightRatio = 1f;

			if (_texture.getParent() == null) {
				xOff = ((srcX / displayWidth) * _texture.widthRatio()) + _texture.xOff();
				yOff = ((srcY / displayHeight) * _texture.heightRatio()) + _texture.yOff();
				widthRatio = ((srcWidth / displayWidth) * _texture.widthRatio()) + xOff;
				heightRatio = ((srcHeight / displayHeight) * _texture.heightRatio()) + yOff;
			} else {
				LTexture forefather = LTexture.firstFather(_texture);
				displayWidth = forefather.getDisplayWidth();
				displayHeight = forefather.getDisplayHeight();
				xOff = ((srcX / displayWidth) * forefather.widthRatio()) + forefather.xOff() + _texture.xOff();
				yOff = ((srcY / displayHeight) * forefather.heightRatio()) + forefather.yOff() + _texture.yOff();
				widthRatio = ((srcWidth / displayWidth) * forefather.widthRatio()) + xOff;
				heightRatio = ((srcHeight / displayHeight) * forefather.heightRatio()) + yOff;
			}
			_mesh.paint(argb, _displayAffine, x, y, x + width, y + height, xOff, yOff, widthRatio, heightRatio);
		}
		return this;
	}

	public LTextureBatch setImageColor(LColor c) {
		if (c == null) {
			return this;
		}
		_color.setColor(c);
		return this;
	}

	private boolean checkUpdateColor(LColor c) {
		return c != null && !LColor.white.equals(c);
	}

	public LTextureBatch postCache(LColor color, float rotation) {
		if (lastCache != null) {
			postCache(lastCache, color, rotation);
		}
		return this;
	}

	public LTextureBatch postCache(Cache cache, LColor color, float x, float y) {
		return postCache(cache, color, x, y, 1f, 1f, 0f, 0f, 0f, false, false, false);
	}

	public LTextureBatch postCache(Cache cache, LColor color, float rotation) {
		return postCache(cache, color, tx, ty, 1f, 1f, 0f, 0f, 0f, false, false, false);
	}

	public LTextureBatch postCache(Cache cache, LColor color, float x, float y, float sx, float sy, float ax, float ay,
			float rotation) {
		return postCache(cache, color, x, y, sx, sy, ax, ay, rotation, false, false, false);
	}

	public LTextureBatch postCache(Cache cache, LColor color, float x, float y, float sx, float sy, float ax, float ay,
			float rotation, boolean flipX, boolean flipY, boolean flipZ) {
		GLEx gl = LSystem.base().display().GL();

		if (gl != null) {

			final int width = cache._image.getWidth();
			final int height = cache._image.getHeight();

			final boolean oriDirty = (ax != 0 || ay != 0);
			final boolean rotDirty = (rotation != 0);
			final boolean scaleDirty = !(sx == 1 && sy == 1);

			if (rotDirty || oriDirty || scaleDirty) {
				_displayAffine.idt();
				if (oriDirty) {
					_displayAffine.translate(ax, ay);
				}
				if (rotDirty) {
					float centerX = x + width / 2;
					float centerY = y + height / 2;
					_displayAffine.translate(centerX, centerY);
					_displayAffine.preRotate(rotation);
					_displayAffine.translate(-centerX, -centerY);
				}
				if (scaleDirty) {
					float centerX = x + width / 2;
					float centerY = y + height / 2;
					_displayAffine.translate(centerX, centerY);
					_displayAffine.preScale(sx, sy);
					_displayAffine.translate(-centerX, -centerY);
				}

				if (flipZ) {
					flipX = !flipX;
					flipY = !flipY;
				}
				if (flipX || flipY) {
					if (flipX && flipY) {
						Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_ROT180, width, height);
					} else if (flipX) {
						Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_MIRROR, width, height);
					} else if (flipY) {
						Affine2f.transform(_displayAffine, x, y, Affine2f.TRANS_MIRROR_ROT180, width, height);
					}
				}

				Affine2f.multiply(_glexAffine, _displayAffine, _displayAffine);
			} else {
				_displayAffine.set(_glexAffine);
			}

			Canvas canvas = gl.getCanvas();
			canvas.setTransform(_displayAffine);
			canvas.draw(cache._image, x, y, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
			gl.synchTransform();
		}
		return this;
	}

	public int getTextureID() {
		if (_texture != null) {
			return _texture.getID();
		}
		return -1;
	}

	public int getTextureHashCode() {
		if (_texture != null) {
			return _texture.hashCode();
		}
		return -1;
	}

	public boolean closed() {
		return isClosed;
	}

	public boolean isClosed() {
		return closed();
	}

	@Override
	public void close() {
		isClosed = true;
		isLoaded = false;
		isCacheLocked = false;
		isInitMesh = false;
		if (lastCache != null) {
			lastCache.close();
		}
		LSystem.disposeBatchCache(this, false);
	}

	public LTextureBatch destroy() {
		if (_texture != null) {
			_texture.close(true);
		}
		if (_buffer != null) {
			_buffer.close();
		}
		return this;
	}

}
