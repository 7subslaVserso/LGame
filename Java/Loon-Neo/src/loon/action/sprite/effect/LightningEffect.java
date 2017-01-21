package loon.action.sprite.effect;

import loon.LSystem;
import loon.LTexture;
import loon.action.sprite.Entity;
import loon.action.sprite.SpriteBatch;
import loon.canvas.LColor;
import loon.geom.Vector2f;
import loon.opengl.BlendState;
import loon.opengl.GLEx;
import loon.opengl.LTexturePack;
import loon.utils.TArray;

public class LightningEffect extends Entity {

	private LTexture lightningSegment, halfCircle, pixel;
	private LTexturePack pack;

	private static LightningEffect instance;

	final static LightningEffect make() {
		return new LightningEffect();
	}

	final static LightningEffect get() {
		if (instance != null) {
			return instance;
		}
		synchronized (LightningEffect.class) {
			if (instance == null) {
				instance = make();
			}
			return instance;
		}
	}

	private SpriteBatch _batch;
	private TArray<ILightning> lists = new TArray<ILightning>(10);

	LightningEffect() {

	}

	public static LightningEffect addBolt(Vector2f s, Vector2f e, LColor c) {
		return addBolt(new Vector2f[] { s }, new Vector2f[] { e }, c);
	}

	// screen.add(LightningEffect.addBolt(Vector2f.at(33, 33),
	// Vector2f.at(300, 300),LColor.red));
	public static LightningEffect addBolt(Vector2f[] s, Vector2f[] e, LColor c) {
		TArray<ILightning> lights = new TArray<ILightning>();
		for (int i = 0; i < e.length; i++) {
			LightningBolt branch = new LightningBolt(s[i], e[i], c);
			lights.add(branch);
		}
		return new LightningEffect(lights);
	}

	public static LightningEffect addBranch(Vector2f s, Vector2f e, LColor c) {
		return addBranch(new Vector2f[] { s }, new Vector2f[] { e }, c);
	}

	// screen.add(LightningEffect.addBranch(Vector2f.at(33, 33),
	// Vector2f.at(300, 300),LColor.red));
	public static LightningEffect addBranch(Vector2f[] s, Vector2f[] e, LColor c) {
		TArray<ILightning> lights = new TArray<ILightning>();
		for (int i = 0; i < e.length; i++) {
			LightningBranch branch = new LightningBranch(s[i], e[i], c);
			lights.add(branch);
		}
		return new LightningEffect(lights);
	}

	public static LightningEffect addRandom(int count, Vector2f s, Vector2f e, LColor c) {
		return addRandom(count, new Vector2f[] { s }, new Vector2f[] { e }, c);
	}

	// screen.add(LightningEffect.addRandom(30,Vector2f.at(33, 33),
	// Vector2f.at(300, 300),LColor.red));
	public static LightningEffect addRandom(int count, Vector2f[] s, Vector2f[] e, LColor c) {
		TArray<ILightning> lights = new TArray<ILightning>();
		for (int i = 0; i < e.length; i++) {
			LightningRandom branch = new LightningRandom(count, s[i], e[i], c);
			lights.add(branch);
		}
		return new LightningEffect(lights);
	}

	public LightningEffect(TArray<ILightning> lights) {
		this.setLocation(0, 0);
		this.setRepaint(true);
		this.lists.addAll(lights);
	}

	@Override
	public void onUpdate(long elapsedTime) {
		for (ILightning bolt : lists) {
			bolt.update(elapsedTime);
		}
	}

	@Override
	public void repaint(GLEx g, float offsetX, float offsetY) {
		if (isDisposed()) {
			return;
		}
		if (_batch == null) {
			_batch = new SpriteBatch(1000);
		}
		_batch.begin();
		_batch.setBlendState(BlendState.Additive);
		for (ILightning bolt : lists) {
			bolt.draw(_batch,offsetX,offsetY);
		}
		_batch.end();
	}

	public LTexture getLightningSegment() {
		if (lightningSegment == null || lightningSegment.isClose()) {
			loadLightning();
		}
		return lightningSegment;
	}

	public LTexture getHalfCircle() {
		if (halfCircle == null || halfCircle.isClose()) {
			loadLightning();
		}
		return halfCircle;
	}

	public LTexture getPixel() {
		if (pixel == null || pixel.isClose()) {
			loadLightning();
		}
		return pixel;
	}

	public void loadLightning() {
		loadLightning(new LTexturePack(LSystem.FRAMEWORK_IMG_NAME + "natural.txt"));
	}

	public void loadLightning(LTexturePack p) {
		this.pack = p;
		this.lightningSegment = pack.getTexture("loon_lightning");
		this.halfCircle = pack.getTexture("loon_halfcircle");
		this.pixel = pack.getTexture("loon_pixel");
	}

	@Override
	public void close() {
		super.close();
		if (lists != null) {
			for (ILightning light : lists) {
				if (light != null) {
					light.close();
				}
			}
			lists.clear();
		}
		if (_batch != null) {
			_batch.close();
			_batch = null;
		}
		if (this.lightningSegment != null) {
			this.lightningSegment.close();
			this.lightningSegment = null;
		}
		if (this.halfCircle != null) {
			this.halfCircle.close();
			this.halfCircle = null;
		}
		if (this.pixel != null) {
			this.pixel.close();
			this.pixel = null;
		}
	}

}
