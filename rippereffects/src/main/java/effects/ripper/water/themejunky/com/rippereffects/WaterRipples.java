package effects.ripper.water.themejunky.com.rippereffects;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Random;

public class WaterRipples extends GdxTest implements InputProcessor {
	static final float DAMPING = 0.94f;
	static final float DISPLACEMENT = -25.0f;
	static final short HEIGHT = (short) 50;
	static final float INV_HEIGHT = 0.02f;
	static final float INV_WIDTH = 0.02f;
	static final int RADIUS = 1;
	static final float TICK = 0.043f;
	static final short WIDTH = (short) 50;
	public static boolean audio_enable = false;
	public static String background_path = "stones.jpg";
	static TextureData backupdata = null;
	static String current_path = "";
	static int debug_tmp = 0;
	public static WaterRipples instance = null;
	public static int portrait = 0;
	public static int ripple_size = RADIUS;
	float accel = 0.0f;
	float accel_x = 0.0f;
	float accel_y = 0.0f;
	float accel_z = 0.0f;
	float accum;
	SpriteBatch batch;
	Bitmap bmpBackground = null;
	Bitmap bmpFrame = null;
	PerspectiveCamera camera;
	float[][] curr;
	boolean initialized = false;
	float[][] intp;
	float[][] last;
	long lastTick = TimeUtils.nanoTime();
	Mesh mesh;
	Plane plane = new Plane(new Vector3(), new Vector3(1.0f, 0.0f, 0.0f), new Vector3(0.0f, 1.0f, 0.0f));
	Vector3 point = new Vector3();
	int prev_drop = 0;
	int prev_splash = 0;
	long prev_splash_time = 0;
	final long prev_splash_time_limit = 500;
	Pixmap pxm = null;
	Pixmap pxm_rot = null;
	Random rand = new Random();
	Rect rect1 = new Rect(85, 80, 272, 376);
	Rect rect2 = new Rect(49, 45, 414, 309);
	int rot = 0;
	Sound[] sound_drop = new Sound[4];
	Sound[] sound_splash = new Sound[5];
	Texture texture = null;
	Texture texture_illegal = null;
	boolean texture_illegal_loaded = false;
	boolean texture_update = true;
	long touch_motion_length = 0;
	int touch_start_x = 0;
	int touch_start_y = 0;
	float[] vertices;

	public void dispose() {
		int i;
		super.dispose();
		if (this.mesh != null) {
			this.mesh.dispose();
		}
		this.mesh = null;
		if (this.batch != null) {
			this.batch.dispose();
		}
		this.batch = null;
		if (this.texture != null) {
			this.texture.dispose();
		}
		this.texture = null;
		if (this.pxm == null) {
			this.pxm = null;
		}
		for (i = 0; i < this.sound_drop.length; i += RADIUS) {
			if (this.sound_drop[i] != null) {
				this.sound_drop[i].dispose();
			}
		}
		for (i = 0; i < this.sound_splash.length; i += RADIUS) {
			if (this.sound_splash[i] != null) {
				this.sound_splash[i].dispose();
			}
		}
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void create() {
		Calendar c = Calendar.getInstance();
		WaterRipples.instance = this;
		camera = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
		camera.position.set((WIDTH) / 2.0f, (HEIGHT) / 2.0f, WIDTH / 2.0f);
		camera.near = 0.1f;
		camera.far = 1000;
		last = new float[WIDTH + 1][HEIGHT + 1];
		curr = new float[WIDTH + 1][HEIGHT + 1];
		intp = new float[WIDTH + 1][HEIGHT + 1];
		vertices = new float[(WIDTH + 1) * (HEIGHT + 1) * 5];
		mesh = new Mesh(false, (WIDTH + 1) * (HEIGHT + 1), WIDTH * HEIGHT * 6,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"));
		System.out.println(WaterRipples.background_path);
		texture = new Texture(WaterRipples.background_path);
		composeTexture();

		createIndices();
		updateVertices(curr);
		initialized = true;

		batch = new SpriteBatch();
		Gdx.input.setInputProcessor(this);

		
		/*
		for (int i = 1; i <= sound_splash.length; i++) {
			String filename = String.format("sound/splash%d.mp3", i);
			sound_splash[i - 1] = null;
			sound_splash[i - 1] = Gdx.audio.newSound(Gdx.files.internal(filename));
		}
		for (int i = 1; i <= sound_drop.length; i++) {
			sound_drop[i - 1] = null;
			String filename = String.format("sound/drop%d.mp3", i);
			sound_drop[i - 1] = Gdx.audio.newSound(Gdx.files.internal(filename));
		}
		
		*/
	}

	private void createIndices() {
		Calendar c = Calendar.getInstance();
		short[] indices = new short[15000];
		int idx = 0;
		int y = 0;
		while (y < 50) {
			short vidx = (short) (y * 51);
			int idx2 = idx;
			for (int x = 0; x < 50; x += RADIUS) {
				idx = idx2 + RADIUS;
				indices[idx2] = vidx;
				idx2 = idx + RADIUS;
				indices[idx] = (short) (vidx + RADIUS);
				idx = idx2 + RADIUS;
				indices[idx2] = (short) ((vidx + 50) + RADIUS);
				idx2 = idx + RADIUS;
				indices[idx] = (short) (vidx + RADIUS);
				idx = idx2 + RADIUS;
				indices[idx2] = (short) ((vidx + 50) + 2);
				idx2 = idx + RADIUS;
				indices[idx] = (short) ((vidx + 50) + RADIUS);
				vidx = (short) (vidx + RADIUS);
			}
			y += RADIUS;
			idx = idx2;
		}
		this.mesh.setIndices(indices);
	}

	private void updateVertices(float[][] curr) {
		int idx = 0;
		int y = 0;
		while (y <= 50) {
			int x = 0;
			int idx2 = idx;
			while (x <= 50) {
				float xOffset = 0.0f;
				float yOffset = 0.0f;
				if (x > 0 && x < 50 && y > 0 && y < 50) {
					xOffset = curr[x - 1][y] - curr[x + RADIUS][y];
					yOffset = curr[x][y - 1] - curr[x][y + RADIUS];
				}
				idx = idx2 + RADIUS;
				this.vertices[idx2] = (float) x;
				idx2 = idx + RADIUS;
				this.vertices[idx] = (float) y;
				idx = idx2 + RADIUS;
				this.vertices[idx2] = 0.0f;
				idx2 = idx + RADIUS;
				this.vertices[idx] = (((float) x) + xOffset) * 0.02f;
				idx = idx2 + RADIUS;
				this.vertices[idx2] = (((float) y) + yOffset) * 0.02f;
				x += RADIUS;
				idx2 = idx;
			}
			y += RADIUS;
			idx = idx2;
		}
		this.mesh.setVertices(this.vertices);
	}

	private void updateWater() {
		Calendar c = Calendar.getInstance();
		int y = 0;
		while (y < 51) {
			int x = 0;
			while (x < 51) {
				if (x > 0 && x < 50 && y > 0 && y < 50) {
					this.curr[x][y] = ((((this.last[x - 1][y] + this.last[x + RADIUS][y]) + this.last[x][y + RADIUS])
							+ this.last[x][y - 1]) / 4.0f) - this.curr[x][y];
				}
				float[] fArr = this.curr[x];
				fArr[y] = fArr[y] * DAMPING;
				x += RADIUS;
			}
			y += RADIUS;
		}
	}

	private void interpolateWater(float alpha) {
		for (int y = 0; y < 50; y += RADIUS) {
			for (int x = 0; x < 50; x += RADIUS) {
				this.intp[x][y] = (this.last[x][y] * alpha) + ((1.0f - alpha) * this.curr[x][y]);
			}
		}
	}

	private void touchWater(Vector3 point, int rad) {
		Calendar c = Calendar.getInstance();
		for (int y = Math.max(0, ((int) point.y) - rad); y < Math.min(50, ((int) point.y) + rad); y += RADIUS) {
			for (int x = Math.max(0, ((int) point.x) - rad); x < Math.min(50, ((int) point.x) + rad); x += RADIUS) {
				float val = this.curr[x][y] + (DISPLACEMENT * Math.max(0.0f,
						(float) Math
								.cos((1.5707963267948966d * Math.sqrt((double) point.dst2((float) x, (float) y, 0.0f)))
										/ ((double) rad))));
				if (val < DISPLACEMENT) {
					val = DISPLACEMENT;
				} else if (val > 25.0f) {
					val = 25.0f;
				}
				this.curr[x][y] = val;
			}
		}
	}

	public void render() {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		((GL10) gl).glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		this.camera.update();
		((GL10) gl).glMatrixMode(GL10.GL_PROJECTION);
		((GL10) gl).glLoadMatrixf(this.camera.combined.val, 0);
		((GL10) gl).glMatrixMode(GL10.GL_MODELVIEW);
		((GL10) gl).glLoadIdentity();
		this.accum += Gdx.graphics.getDeltaTime();
		while (this.accum > TICK) {
			for (int i = 0; i < 5; i += RADIUS) {
				if (Gdx.input.isTouched(i)) {
					int tx;
					int ty;
					if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
						tx = Gdx.input.getX(i);
						ty = Gdx.input.getY(i);
					} else {
						tx = (int) (((1.0f * ((float) Gdx.input.getY(i))) / ((float) Gdx.graphics.getHeight()))
								* ((float) Gdx.graphics.getWidth()));
						ty = Gdx.graphics.getHeight()
								- ((int) (((1.0f * ((float) Gdx.input.getX(i))) / ((float) Gdx.graphics.getWidth()))
										* ((float) Gdx.graphics.getHeight())));
					}
					Intersector.intersectRayPlane(this.camera.getPickRay((float) tx, (float) ty), this.plane,
							this.point);
					touchWater(this.point, ripple_size * RADIUS);
				}
			}
			if (Gdx.input.isTouched() && audio_enable) {
				if (this.touch_motion_length < 50) {
					this.sound_drop[this.prev_drop].stop();
					this.prev_drop = ((int) (Math.random() * 1000.0d)) % this.sound_drop.length;
					this.sound_drop[this.prev_drop].play();
				}
				if (this.touch_start_x == 0 && this.touch_start_y == 0) {
					this.touch_start_x = Gdx.input.getX(0);
					this.touch_start_y = Gdx.input.getY(0);
				} else {
					this.touch_motion_length = (long) (((Gdx.input.getX(0) - this.touch_start_x)
							* (Gdx.input.getX(0) - this.touch_start_x))
							+ ((Gdx.input.getY(0) - this.touch_start_y) * (Gdx.input.getY(0) - this.touch_start_y)));
					this.touch_start_x = Gdx.input.getX(0);
					this.touch_start_y = Gdx.input.getY(0);
				}
				if (System.currentTimeMillis() - this.prev_splash_time > 500
						&& this.touch_motion_length > ((long) (Gdx.graphics.getWidth() * 2))) {
					this.sound_splash[this.prev_splash].stop();
					this.prev_splash = ((int) (Math.random() * 1000.0d)) % this.sound_drop.length;
					this.sound_splash[this.prev_splash].play(((float) this.touch_motion_length) / 500.0f);
					this.prev_splash_time = System.currentTimeMillis();
				}
			} else {
				this.touch_start_x = 0;
				this.touch_start_y = 0;
			}
			updateWater();
			float[][] tmp = this.curr;
			this.curr = this.last;
			this.last = tmp;
			this.accum -= TICK;
		}
		interpolateWater(this.accum / TICK);
		updateVertices(this.intp);
		((GL10) gl).glEnable(GL10.GL_TEXTURE_2D);
		if (Gdx.graphics.getWidth() < Gdx.graphics.getHeight()) {
			((GL10) gl).glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
			((GL10) gl).glTranslatef(-50.0f, 0.0f, 0.0f);
		}
		if (this.texture == null && backupdata != null) {
			this.texture = new Texture(backupdata);
			this.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		this.texture.bind();
		this.mesh.render(4);
		this.batch.begin();
		this.batch.end();
	}

	public boolean keyDown(int keycode) {
		return false;
	}

	public boolean keyUp(int keycode) {
		return false;
	}

	public boolean keyTyped(char character) {
		return false;
	}

	public boolean touchDown(int x, int y, int pointer, int newParam) {
		return false;
	}

	public boolean touchUp(int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	public boolean needsGL10() {
		return false;
	}

	public boolean mouseMoved(int x, int y) {
		return false;
	}

	public boolean scrolled(int amount) {
		return false;
	}

	public static Pixmap bitmapToPixmap(Bitmap bitmap) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		if (!bitmap.compress(CompressFormat.PNG, 0, outStream)) {
			return null;
		}
		byte[] img = outStream.toByteArray();
		return new Pixmap(img, 0, img.length);
	}

	public void updateTexture() {
		this.texture_update = true;
	}

	private void composeTexture() {
		try {
			Bitmap bmp_tmp = Bitmap.createBitmap(512, 512, Config.ARGB_8888);
			Bitmap bmp_txture = Bitmap.createBitmap(1024, 1024, Config.RGB_565);
			Matrix matrix = new Matrix();
			this.texture = new Texture(background_path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pause() {
	}

	public void resume() {
	}
}