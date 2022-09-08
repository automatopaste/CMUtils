package cmu.shaders.particles;


import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores data necessary for rendering and creating segments
 */
public class SegmentedPath {
    protected final List<SegmentData> segments;
    protected final Vector2f[] initialVertices; // first two vertices of path relative to origin point
    public float width;
    public final Vector2f start;
    public int numSegments;

    public SegmentedPath(Vector2f start, float width) {
        this.width = width;
        this.start = start;

        segments = new ArrayList<>();
        numSegments = 0;
        initialVertices = new Vector2f[2];
        initialVertices[0] = new Vector2f(start.x, start.y + width);
        initialVertices[1] = new Vector2f(start.x, start.y - width);
    }

    public void addSegment(Vector2f to, float angle) {
        if (numSegments == 0) {
//            float angle = VectorUtils.getFacing(to);
            initialVertices[0] = VectorUtils.rotate(initialVertices[0], angle);
            initialVertices[1] = VectorUtils.rotate(initialVertices[1], angle);
        }

//        float angle = VectorUtils.getFacing(to);
        SegmentData segmentData = new SegmentData(to, width, angle);
        segments.add(segmentData);

        numSegments = segments.size();
    }

    public void setFinalSegmentLocation(Vector2f location) {
        segments.get(numSegments - 1).to.set(location);
    }

    public void clearSegments() {
        segments.clear();
        numSegments = 0;
    }

    /**
     * Get model transforms of segment vertices relative to world coordinates (model matrices)
     * @param input view matrix
     * @return vertex transform matrices
     */
    public Matrix4f[] getVertexTransforms(Matrix4f input) {
        if (segments.isEmpty()) return new Matrix4f[0];

        Matrix4f[] out = new Matrix4f[2 + (2 * segments.size())];

        out[0] = new Matrix4f(input).translate(initialVertices[0]);
        out[1] = new Matrix4f(input).translate(initialVertices[1]);

        Vector2f cumulative = new Vector2f(start);
        for (int j = 0; j < segments.size(); j++) {
            SegmentData segment = segments.get(j);
            Vector2f.add(segment.to, cumulative, cumulative);
            Vector2f[] vertices = segment.vertices;

            int j2 = (j * 2) + 2;
            Vector2f v1 = Vector2f.add(vertices[0], cumulative, new Vector2f());
            out[j2] = new Matrix4f(input).translate(new Vector3f(v1.x, v1.y, 0f));
            Vector2f v2 = Vector2f.add(vertices[1], cumulative, new Vector2f());
            out[j2 + 1] = new Matrix4f(input).translate(new Vector3f(v2.x, v2.y, 0f));
        }

        return out;
    }

    public List<SegmentData> getSegments() {
        return segments;
    }

    public static class SegmentData {
        public Vector2f to; // relative to previous vertex of path
        public Vector2f[] vertices; // v1, v2

        public SegmentData(Vector2f to, float width, float angle) {
            this.to = to;
            vertices = new Vector2f[] {
                    VectorUtils.rotate(new Vector2f(0f, width), angle), VectorUtils.rotate(new Vector2f(0f, -width), angle)
            };
        }
    }
}
