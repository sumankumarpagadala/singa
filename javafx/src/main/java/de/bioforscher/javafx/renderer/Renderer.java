package de.bioforscher.javafx.renderer;

import de.bioforscher.mathematics.geometry.edges.Line;
import de.bioforscher.mathematics.geometry.edges.LineSegment;
import de.bioforscher.mathematics.geometry.edges.Parabola;
import de.bioforscher.mathematics.vectors.Vector2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by Christoph on 28.08.2016.
 */
public interface Renderer {

    /**
     * Returns the assigned GraphicContext.
     *
     * @return The assigned GraphicContext.
     */
    default GraphicsContext getGraphicsContext() {
        return getCanvas().getGraphicsContext2D();
    }

    /**
     * Returns the Canvas assigned to this Renderer.
     *
     * @return The Canvas assigned to this Renderer.
     */
    Canvas getCanvas();

    /**
     * Draws a point (filled circle) where the {@link Vector2D} is positioned. The point is centered on the vector.<br>
     * <ul>
     * <li> The radius of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the FillColor (set by {@link GraphicsContext#setFill(Paint)}).</li>
     * </ul>
     *
     * @param point The position of the point.
     */
    default void drawPoint(Vector2D point) {
        getGraphicsContext().fillOval(
                point.getX() - getGraphicsContext().getLineWidth() / 2.0,
                point.getY() - getGraphicsContext().getLineWidth() / 2.0,
                getGraphicsContext().getLineWidth(),
                getGraphicsContext().getLineWidth());
    }

    /**
     * Connects the points given in the List in order of their appearance with a line.<br>
     * <ul>
     * <li> The line width of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the StrokeColor (set by {@link GraphicsContext#setStroke(Paint)}).</li>
     * </ul>
     *
     * @param vectors The points to be connected with a line.
     */
    default void connectPoints(List<Vector2D> vectors) {
        getGraphicsContext().strokePolyline(
                vectors.stream().mapToDouble(Vector2D::getX).toArray(),
                vectors.stream().mapToDouble(Vector2D::getY).toArray(),
                vectors.size()
        );
    }

    /**
     * Draws a straight by connecting the given start and end points.
     * <ul>
     * <li> The line width of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the StrokeColor (set by {@link GraphicsContext#setStroke(Paint)}).</li>
     * </ul>
     *
     * @param start The starting point.
     * @param end The ending point.
     */
    default void drawStraight(Vector2D start, Vector2D end) {
        getGraphicsContext().strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    /**
     * Draws the given line segment.
     * <ul>
     * <li> The line width of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the StrokeColor (set by {@link GraphicsContext#setStroke(Paint)}).</li>
     * </ul>
     * @param lineSegment The line segment.
     */
    default void drawLineSegment(LineSegment lineSegment) {
        drawStraight(lineSegment.getStartingPoint(), lineSegment.getEndingPoint());
    }

    /**
     * Draws the given line. The line is drawn over the whole displayed Canvas. 
     * <ul>
     * <li> The line width of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the StrokeColor (set by {@link GraphicsContext#setStroke(Paint)}).</li>
     * </ul>
     * @param line The line.
     */
    default void drawLine(Line line) {
        final double minX = 0;
        final double maxX = getCanvas().getWidth();
        final double minY = 0;
        final double maxY = getCanvas().getHeight();

        Vector2D start;
        Vector2D end;
        if (line.isHorizontal()) {
            start = new Vector2D(minX, line.getYIntercept());
            end = new Vector2D(maxX, line.getYIntercept());
        } else if (line.isVertical()) {
            start = new Vector2D(line.getXIntercept(), minY);
            end = new Vector2D(line.getXIntercept(), maxY);
        } else {
            start = line.getInterceptWithLine(new Line(0, 0));
            end = line.getInterceptWithLine(new Line(maxY, 0));
        }
        drawStraight(start, end);
    }

    /**
     * Draws the given parabola. The parabola is drawn over the whole displayed Canvas.
     * <ul>
     * <li> The line width of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the StrokeColor (set by {@link GraphicsContext#setStroke(Paint)}).</li>
     * </ul>
     * @param parabola The parabola.
     */
    default void drawParabola(Parabola parabola) {
        drawParabola(parabola, 20);
    }

    /**
     * Draws the given parabola. The parabola is drawn over the whole displayed Canvas. The sampling depth gives the
     * number of points that are connected to draw the parabola. The higher the depth, the finer the parabola is drawn.
     * <ul>
     * <li> The line width of the point is determined by the LineWidth (set by
     * {@link GraphicsContext#setLineWidth(double)}).</li>
     * <li> The color is determined by the StrokeColor (set by {@link GraphicsContext#setStroke(Paint)}).</li>
     * </ul>
     * @param parabola The parabola.
     * @param samplingDepth The number of points that are connected to draw the parabola.
     */
    default void drawParabola(Parabola parabola, int samplingDepth) {
        final double minX = 0;
        final double maxX = getCanvas().getWidth();
        final double maxY = getCanvas().getHeight();

        List<Vector2D> list = new ArrayList<>();

        Vector2D leftMost;
        Vector2D rightMost;
        if (!parabola.isOpenTowardsXAxis()) {
            // calculate intercepts with horizontal line with the y intercept at maximal displayable y value
            SortedSet<Vector2D> xIntercepts = parabola.getIntercepts(new Line(maxY, 0));
            if (xIntercepts.first().getX() < minX) {
                leftMost = new Vector2D(minX, parabola.getYValue(minX));
            } else {
                leftMost = xIntercepts.first();
            }
            list.add(leftMost);
            if (xIntercepts.last().getX() > maxX) {
                rightMost = new Vector2D(maxX, parabola.getYValue(maxX));
            } else {
                rightMost = xIntercepts.last();
            }
            list.add(rightMost);
        } else {
            // calculate intercepts with x axis
            SortedSet<Double> xIntercepts = parabola.getXIntercepts();
            if (xIntercepts.first() < minX) {
                leftMost = new Vector2D(minX, parabola.getYValue(minX));
            } else {
                leftMost = new Vector2D(xIntercepts.first(), 0);
            }
            list.add(leftMost);
            if (xIntercepts.last() > maxX) {
                rightMost = new Vector2D(maxX, parabola.getYValue(maxX));
            } else {
                rightMost = new Vector2D(xIntercepts.last(), 0);
            }
            list.add(rightMost);
        }

        final double maximalExtend = leftMost.distanceTo(rightMost);
        final double offset = maximalExtend / samplingDepth;

        for (double currentX = leftMost.getX() + offset; currentX < rightMost.getX(); currentX += offset) {
            list.add(new Vector2D(currentX, parabola.getYValue(currentX)));
        }

        list.sort(Comparator.comparing(Vector2D::getX));
        connectPoints(list);
    }

}