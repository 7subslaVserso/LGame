package loon.core.graphics.opengl;

import loon.core.geom.Matrix4;
import loon.core.graphics.device.LColor;
import loon.core.graphics.opengl.VertexAttributes.Usage;
import loon.utils.collection.Array;

public class GLBatch {
	private int primitiveType;
	private int vertexIdx;
	private int numSetTexCoords;
	private final int maxVertices;
	private int numVertices;

	private Mesh mesh;
	private ShaderProgram shader;
	private boolean ownsShader;
	private int numTexCoords;
	private int vertexSize;
	private int normalOffset;
	private int colorOffset;
	private int texCoordOffset;
	private final Matrix4 projModelView = new Matrix4();
	float[] vertices;
	private String[] shaderUniformNames;

	public GLBatch(boolean hasNormals, boolean hasColors, int numTexCoords) {
		this(5000, hasNormals, hasColors, numTexCoords, null);
		ownsShader = true;
	}

	public GLBatch(int maxVertices, boolean hasNormals, boolean hasColors,
			int numTexCoords) {
		this(maxVertices, hasNormals, hasColors, numTexCoords, null);
		ownsShader = true;
	}

	private boolean hasNormals, hasColors;

	public GLBatch(int maxVertices, boolean hasNormals, boolean hasColors,
			int numTexCoords, ShaderProgram shader) {
		this.maxVertices = maxVertices;
		this.numTexCoords = numTexCoords;
		this.shader = shader;
		this.hasNormals = hasNormals;
		this.hasColors = hasColors;

	}

	private VertexAttribute[] buildVertexAttributes(boolean hasNormals,
			boolean hasColor, int numTexCoords) {
		Array<VertexAttribute> attribs = new Array<VertexAttribute>();
		attribs.add(new VertexAttribute(Usage.Position, 3,
				ShaderProgram.POSITION_ATTRIBUTE));
		if (hasNormals) {
			attribs.add(new VertexAttribute(Usage.Normal, 3,
					ShaderProgram.NORMAL_ATTRIBUTE));
		}
		if (hasColor) {
			attribs.add(new VertexAttribute(Usage.ColorPacked, 4,
					ShaderProgram.COLOR_ATTRIBUTE));
		}
		for (int i = 0; i < numTexCoords; i++) {
			attribs.add(new VertexAttribute(Usage.TextureCoordinates, 2,
					ShaderProgram.TEXCOORD_ATTRIBUTE + i));
		}
		VertexAttribute[] array = new VertexAttribute[attribs.size()];
		for (int i = 0; i < attribs.size(); i++) {
			array[i] = attribs.get(i);
		}
		return array;
	}

	public void setShader(ShaderProgram shader) {
		if (ownsShader) {
			this.shader.dispose();
		}
		this.shader = shader;
		ownsShader = false;
	}

	public void begin(Matrix4 projModelView, int primitiveType) {
		if (shader == null) {
			VertexAttribute[] attribs = buildVertexAttributes(hasNormals,
					hasColors, numTexCoords);
			mesh = new Mesh(false, maxVertices, 0, attribs);
			vertices = new float[maxVertices
					* (mesh.getVertexAttributes().vertexSize / 4)];
			vertexSize = mesh.getVertexAttributes().vertexSize / 4;
			normalOffset = mesh.getVertexAttribute(Usage.Normal) != null ? mesh
					.getVertexAttribute(Usage.Normal).offset / 4 : 0;
			colorOffset = mesh.getVertexAttribute(Usage.ColorPacked) != null ? mesh
					.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
			texCoordOffset = mesh.getVertexAttribute(Usage.TextureCoordinates) != null ? mesh
					.getVertexAttribute(Usage.TextureCoordinates).offset / 4
					: 0;

			shaderUniformNames = new String[numTexCoords];
			for (int i = 0; i < numTexCoords; i++) {
				shaderUniformNames[i] = "u_sampler" + i;
			}
			shader = createDefaultShader(hasNormals, hasColors, numTexCoords);
		}
		this.numSetTexCoords = 0;
		this.vertexIdx = 0;
		this.numVertices = 0;
		this.projModelView.set(projModelView);
		this.primitiveType = primitiveType;
	}

	public void color(LColor color) {
		vertices[vertexIdx + colorOffset] = color.toFloatBits();
	}

	public void color(float r, float g, float b, float a) {
		vertices[vertexIdx + colorOffset] = LColor.toFloatBits(r, g, b, a);
	}

	public void texCoord(float u, float v) {
		final int idx = vertexIdx + texCoordOffset;
		vertices[idx + numSetTexCoords] = u;
		vertices[idx + numSetTexCoords + 1] = v;
		numSetTexCoords += 2;
	}

	public void normal(float x, float y, float z) {
		final int idx = vertexIdx + normalOffset;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = z;
	}

	public void vertex(float x, float y) {
		vertex(x, y, 0);
	}

	public void vertex(float x, float y, float z) {
		final int idx = vertexIdx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = z;

		numSetTexCoords = 0;
		vertexIdx += vertexSize;
		numVertices++;
	}

	public void flush() {
		if (numVertices == 0) {
			return;
		}
		shader.begin();
		shader.setUniformMatrix("u_projModelView", projModelView);
		for (int i = 0; i < numTexCoords; i++) {
			shader.setUniformi(shaderUniformNames[i], i);
		}
		mesh.setVertices(vertices, 0, vertexIdx);
		mesh.render(shader, primitiveType);
		shader.end();
	}

	public void end() {
		flush();
	}

	public int getNumVertices() {
		return numVertices;
	}

	public int getMaxVertices() {
		return maxVertices;
	}

	public void dispose() {
		if (ownsShader && shader != null) {
			shader.dispose();
		}
		mesh.dispose();
	}

	static public ShaderProgram createDefaultShader(boolean hasNormals,
			boolean hasColors, int numTexCoords) {
		String vertexShader = GLEx.createVertexShader(hasNormals, hasColors,
				numTexCoords);
		String fragmentShader = GLEx.createFragmentShader(hasNormals,
				hasColors, numTexCoords);
		ShaderProgram program = new ShaderProgram(vertexShader, fragmentShader);
		return program;
	}
}
