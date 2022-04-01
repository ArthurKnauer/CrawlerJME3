/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.math;

import static architect.Constants.EPSILON;
import architect.math.segments.LineSegment;
import architect.math.segments.Orientation;
import static architect.math.segments.Orientation.Horizontal;
import static architect.math.segments.Orientation.Vertical;
import architect.math.segments.Side;
import static architect.math.segments.Side.*;
import architect.room.RoomRect;
import static java.lang.Math.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 *
 * @author VTPlusAKnauer
 */
public class Rectangle {

    public final static Rectangle ZERO = new Rectangle(0, 0, 0, 0);
    public final float minX, minY, maxX, maxY, width, height;

    private EnumMap<Side, LineSegment> sideLineSegments;

    public Rectangle(Rectangle rectangle) {
        this(rectangle.minX, rectangle.minY, rectangle.width, rectangle.height);
    }

    public Rectangle(float x, float y, float width, float height) {
        if (width < 0) {
            throw new IllegalArgumentException("width < 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height < 0");
        }

        minX = x;
        minY = y;
        maxX = minX + width;
        maxY = minY + height;
        this.width = width;
        this.height = height;
    }

    public Rectangle(RoomRect rr) {
        minX = rr.minX;
        minY = rr.minY;
        maxX = rr.maxX;
        maxY = rr.maxY;
        this.width = rr.width;
        this.height = rr.height;
    }

    public static Rectangle fromMinMax(float minX, float minY, float maxX, float maxY) {
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean isUnionARectangle(Rectangle rect) {
        return isTouchingSide(rect)
                && (leftAndRightEqual(rect) || topAndBottomEqual(rect));
    }

    public boolean leftAndRightEqual(Rectangle rect) {
        return sidePosEqual(Left, rect) && sidePosEqual(Right, rect);
    }

    public boolean topAndBottomEqual(Rectangle rect) {
        return sidePosEqual(Top, rect) && sidePosEqual(Bottom, rect);
    }

    public boolean sidePosEqual(Side side, Rectangle rect) {
        return sidePosEquals(side, rect.sidePos(side));
    }

    public boolean leftOrRightEqual(float x) {
        return sidePosEquals(Left, x) || sidePosEquals(Right, x);
    }

    public boolean topOrBottomEqual(float y) {
        return sidePosEquals(Top, y) || sidePosEquals(Bottom, y);
    }

    public boolean sidePosEquals(Side side, float pos) {
        return abs(sidePos(side) - pos) < EPSILON;
    }

    public float sidePos(Side side) {
        switch (side) {
            case Left:
                return minX;
            case Top:
                return maxY;
            case Right:
                return maxX;
            case Bottom:
                return minY;
            default:
                throw new IllegalArgumentException(side + " is not a valid side");
        }
    }

    public LineSegment sideLineSegment(Side side) {
        if (sideLineSegments == null) {
            sideLineSegments = new EnumMap<>(Side.class);
            sideLineSegments.put(Side.Left, new LineSegment(minY, maxY, minX, Vertical));
            sideLineSegments.put(Side.Right, new LineSegment(minY, maxY, maxX, Vertical));
            sideLineSegments.put(Side.Top, new LineSegment(minX, maxX, maxY, Horizontal));
            sideLineSegments.put(Side.Bottom, new LineSegment(minX, maxX, minY, Horizontal));
        }
        return sideLineSegments.get(side);
    }

    public Vector2D cornerPoint(Side verticalSide, Side horizontalSide) {
        return new Vector2D(sidePos(verticalSide), sidePos(horizontalSide));
    }

    public float sideLength(Side side) {
        return side.isLeftOrRight ? height : width;
    }

    public float sideLength(Orientation orientation) {
        return orientation == Vertical ? height : width;
    }

    public float shortestSideLength() {
        return min(width, height);
    }

    public float inverseAspectRatio() {
        return (width > height) ? (height / width) : (width / height);
    }

    public float area() {
        return width * height;
    }

    public Vector2D centerPoint() {
        return new Vector2D(centerX(), centerY());
    }

    public float centerX() {
        return minX + width * 0.5f;
    }

    public float centerY() {
        return minY + height * 0.5f;
    }

    public Rectangle scaledByOffset(float distX, float distY) {
        return Rectangle.fromMinMax(minX - distX, minY - distY, maxX + distX, maxY + distY);
    }

    public Rectangle scaledToContain(Rectangle rect) {
        float newMinX = min(minX, rect.minX);
        float newMinY = min(minY, rect.minY);
        float newMaxX = max(maxX, rect.maxX);
        float newMaxY = max(maxY, rect.maxY);
        return Rectangle.fromMinMax(newMinX, newMinY, newMaxX, newMaxY);
    }

    public Rectangle trimmedLeastToNotOverlap(Rectangle rect) {
        Rectangle result = this;

        if (overlaps(rect)) {
            float areaLossIfScaledWidth = height * xOverlap(rect);
            float areaLossIfScaledHeight = width * yOverlap(rect);

            if (areaLossIfScaledWidth < areaLossIfScaledHeight) {
                boolean trimLeft = maxX - rect.maxX > rect.minX - minX;
                if (trimLeft) {
                    result = movedMinXTo(rect.maxX);
                } else {
                    result = movedMaxXTo(rect.minX);
                }
            } else {
                boolean trimBottom = maxY - rect.maxY > rect.minY - minY;
                if (trimBottom) {
                    result = movedMinYTo(rect.maxY);
                } else {
                    result = movedMaxYTo(rect.minY);
                }
            }
        }

        return result;
    }

    public Rectangle scaledToOverlapEntireWidth(Rectangle rect) {
        float newMinX = min(minX, rect.minX);
        float newMaxX = max(maxX, rect.maxX);
        return new Rectangle(newMinX, minY, newMaxX - newMinX, height);
    }

    public Rectangle scaledToOverlapEntireHeight(Rectangle rect) {
        float newMinY = min(minY, rect.minY);
        float newMaxY = max(maxY, rect.maxY);
        return new Rectangle(minX, newMinY, width, newMaxY - newMinY);
    }

    public Rectangle movedInside(Rectangle container) {
        if (width > container.width || height > container.height) {
            throw new IllegalArgumentException("Cant' move " + this + " inside " + container + ", which is smaller");
        }

        float newMinX = minX;
        float newMinY = minY;

        if (furtherRightThan(container)) {
            newMinX = container.maxX - width;
        } else if (furtherLeftThan(container)) {
            newMinX = container.minX;
        }

        if (furtherUpThan(container)) {
            newMinY = container.maxY - height;
        } else if (furtherDownThan(container)) {
            newMinY = container.minY;
        }

        return movedTo(newMinX, newMinY);
    }

    public Rectangle movedOutside(Rectangle rect, EnumSet<Side> canMove, Rectangle stayInsideRect) {
        Vector2D resolve = shortestCollisionResolve(rect, canMove, stayInsideRect);
        return moved(resolve.x, resolve.y);
    }

    public Rectangle movedOutside(Rectangle rect, Rectangle stayInsideRect) {
        Vector2D resolve = shortestCollisionResolve(rect, Side.allSides, stayInsideRect);
        return moved(resolve.x, resolve.y);
    }

    //TODO: refactor this
    public Vector2D shortestCollisionResolve(Rectangle rect, EnumSet<Side> canMove, Rectangle stayInsideRect) {
        Vector2D noResolve = new Vector2D(0, 0);
        if (!overlaps(rect)) {
            return noResolve;
        }

        float moveRight = rect.maxX - minX;
        float moveLeft = maxX - rect.minX;
        float moveUp = rect.maxY - minY;
        float moveDown = maxY - rect.minY;

        if (stayInsideRect != null) { // collision cannot be resolved if this rect hast to move outside of stayInsideRect
            if (moveRight > EPSILON && rect.maxX + moveRight > stayInsideRect.maxX + EPSILON) {
                moveRight = Float.MAX_VALUE;
            }
            if (moveLeft > EPSILON && rect.minX - moveLeft < stayInsideRect.minX - EPSILON) {
                moveLeft = Float.MAX_VALUE;
            }
            if (moveUp > EPSILON && rect.maxY + moveUp > stayInsideRect.maxY + EPSILON) {
                moveUp = Float.MAX_VALUE;
            }
            if (moveDown > EPSILON && rect.minY - moveDown < stayInsideRect.minY - EPSILON) {
                moveDown = Float.MAX_VALUE;
            }
        }

        if (!canMove.contains(Side.Left)) {
            moveLeft = Float.MAX_VALUE;
        }
        if (!canMove.contains(Side.Top)) {
            moveUp = Float.MAX_VALUE;
        }
        if (!canMove.contains(Side.Right)) {
            moveRight = Float.MAX_VALUE;
        }
        if (!canMove.contains(Side.Bottom)) {
            moveDown = Float.MAX_VALUE;
        }

        if (moveLeft == Float.MAX_VALUE && moveRight == Float.MAX_VALUE && moveUp == Float.MAX_VALUE && moveDown == Float.MAX_VALUE) {
            return noResolve; // no resolve possible return null vector 
        }

        float resolveX = 0, resolveY = 0;

        if (moveRight < moveLeft) {
            if (moveUp < moveDown) {
                if (moveRight < moveUp) {
                    resolveX = moveRight;
                } else {
                    resolveY = moveUp;
                }
            } else if (moveRight < moveDown) {
                resolveX = moveRight;
            } else {
                resolveY = -moveDown;
            }
        } else if (moveUp < moveDown) {
            if (moveLeft < moveUp) {
                resolveX = -moveLeft;
            } else {
                resolveY = moveUp;
            }
        } else if (moveLeft < moveDown) {
            resolveX = -moveLeft;
        } else {
            resolveY = -moveDown;
        }

        return new Vector2D(resolveX, resolveY);
    }

    public Rectangle movedTo(Vector2D to) {
        return movedTo(to.x, to.y);
    }

    public Rectangle movedTo(float x, float y) {
        return new Rectangle(x, y, width, height);
    }

    public Rectangle moved(Vector2D move) {
        return moved(move.x, move.y);
    }

    public Rectangle moved(float distX, float distY) {
        return new Rectangle(minX + distX, minY + distY, width, height);
    }

    public Rectangle movedSideTo(Side side, float pos) {
        switch (side) {
            case Left:
                return movedMinXTo(pos);
            case Bottom:
                return movedMinYTo(pos);
            case Right:
                return movedMaxXTo(pos);
            case Top:
                return movedMaxYTo(pos);
            default:
                throw new IllegalArgumentException(side + " is not a valid side");
        }
    }

    public Rectangle movedMinXTo(float newMinX) {
        return Rectangle.fromMinMax(newMinX, minY, maxX, maxY);
    }

    public Rectangle movedMinYTo(float newMinY) {
        return Rectangle.fromMinMax(minX, newMinY, maxX, maxY);
    }

    public Rectangle movedMaxXTo(float newMaxX) {
        return Rectangle.fromMinMax(minX, minY, newMaxX, maxY);
    }

    public Rectangle movedMaxYTo(float newMaxY) {
        return Rectangle.fromMinMax(minX, minY, maxX, newMaxY);
    }

    public LineSegment commonPerimeterLine(Rectangle neighbor) {
        Side mySide = sideTouchingNeighbor(neighbor);
        Side hisSide = mySide.opposite();

        LineSegment mySideLine = sideLineSegment(mySide);
        LineSegment hisSideLine = sideLineSegment(hisSide);
        return mySideLine.overlap(hisSideLine);
    }

    public Side sideTouchingNeighbor(Rectangle neighbor) {
        if (!isTouchingSide(neighbor)) {
            throw new IllegalArgumentException(this + " is not touching " + neighbor);
        }

        if (overlapsX(neighbor)) {
            if (furtherUpThan(neighbor)) {
                return Side.Bottom;
            } else {
                return Side.Top;
            }
        } else {
            if (furtherRightThan(neighbor)) {
                return Side.Left;
            } else {
                return Side.Right;
            }
        }
    }

    public Vector2D cornerTouchingNeighbor(Rectangle rect) {
        float cornerX = minX < rect.minX ? maxX : rect.maxX;
        float cornerY = minY < rect.minY ? maxY : rect.maxY;
        return new Vector2D(cornerX, cornerY);
    }

    public Rectangle movedDistOrUntilTouch(Vector2D move, Rectangle toAvoid) {
        float distXClamped = clampDistXIfMoveCollides(move.x, toAvoid);
        float distYClamped = clampDistYIfMoveCollides(move.y, toAvoid);

        return moved(distXClamped, distYClamped);
    }

    private float clampDistXIfMoveCollides(float distX, Rectangle toAvoid) {
        if (abs(distX) > 0 && overlapsY(toAvoid)) {
            float dist = -xOverlap(toAvoid);
            if (dist < -EPSILON) {
                throw new IllegalArgumentException("Rectangle " + this + " already overlaps " + toAvoid);
            }

            if (distX < 0 && furtherRightThan(toAvoid)) {
                distX = max(distX, -dist);
            } else if (distX > 0 && furtherLeftThan(toAvoid)) {
                distX = min(distX, dist);
            }
        }
        return distX;
    }

    private float clampDistYIfMoveCollides(float distY, Rectangle toAvoid) {
        if (abs(distY) > 0 && overlapsX(toAvoid)) {
            float distYToTouch = -yOverlap(toAvoid);
            if (distYToTouch < -EPSILON) {
                throw new IllegalArgumentException("Rectangle " + this + " already overlaps " + toAvoid);
            }

            if (distY < 0 && furtherUpThan(toAvoid)) {
                distY = max(distY, -distYToTouch);
            } else if (distY > 0 && furtherDownThan(toAvoid)) {
                distY = min(distY, distYToTouch);
            }
        }
        return distY;
    }

    public boolean furtherThan(Rectangle rect, Side side) {
        switch (side) {
            case Left:
                return furtherLeftThan(rect);
            case Bottom:
                return furtherDownThan(rect);
            case Right:
                return furtherRightThan(rect);
            case Top:
                return furtherUpThan(rect);
            default:
                throw new IllegalArgumentException(side + " is not a valid side");
        }
    }

    public boolean furtherLeftThan(Rectangle rect) {
        return minX < rect.minX - EPSILON;
    }

    public boolean furtherRightThan(Rectangle rect) {
        return maxX > rect.maxX + EPSILON;
    }

    public boolean furtherUpThan(Rectangle rect) {
        return maxY > rect.maxY + EPSILON;
    }

    public boolean furtherDownThan(Rectangle rect) {
        return minY < rect.minY - EPSILON;
    }

    public boolean contains(float x, float y) {
        return (x > minX - EPSILON && x < maxX + EPSILON
                && y > minY - EPSILON && y < maxY + EPSILON);
    }

    public boolean contains(Rectangle rect) {
        return xOverlap(rect) > rect.width - EPSILON
                && yOverlap(rect) > rect.height - EPSILON;
    }

    public float manhattanDistBetweenCenters(Rectangle rect) {
        float distX = abs(centerX() - rect.centerX());
        float distY = abs(centerY() - rect.centerY());
        return distX + distY;
    }

    public float manhattanDistBetweenSides(Rectangle rect) {
        float distX = -min(0, xOverlap(rect));
        float distY = -min(0, yOverlap(rect));
        return distX + distY;
    }

    public boolean overlaps(Rectangle rect) {
        return minOverlap(rect) > EPSILON;
    }

    public float minOverlap(Rectangle rect) {
        return min(xOverlap(rect), yOverlap(rect));
    }

    public float overlapArea(Rectangle rect) {
        float xOverlapClamped = max(0, xOverlap(rect));
        float yOverlapClamped = max(0, yOverlap(rect));
        return xOverlapClamped * yOverlapClamped;
    }

    public float commonPerimeter(Rectangle rect) {
        return commonXPerimeter(rect) + commonYPerimeter(rect);
    }

    public float commonXPerimeter(Rectangle rect) {
        if (yOverlap(rect) > -EPSILON) {
            return max(0, xOverlap(rect));
        } else {
            return 0;
        }
    }

    public float commonYPerimeter(Rectangle rect) {
        if (xOverlap(rect) > -EPSILON) {
            return max(0, yOverlap(rect));
        } else {
            return 0;
        }
    }

    public boolean isTouchingSide(Rectangle rect) {
        float xOverlap = xOverlap(rect);
        float yOverlap = yOverlap(rect);
        return (abs(xOverlap) < EPSILON && yOverlap > EPSILON)
                || (abs(yOverlap) < EPSILON && xOverlap > EPSILON);
    }

    public boolean isTouchingOnlyCorner(Rectangle rect) {
        return (abs(xOverlap(rect)) < EPSILON && abs(yOverlap(rect)) < EPSILON);
    }

    public float overlap(LineSegment line) {
        if (line.isHorizontal()) {
            return xOverlap(line.min, line.max);
        } else {
            return yOverlap(line.min, line.max);
        }
    }

    public boolean overlapsX(Rectangle rect) {
        return xOverlap(rect) > EPSILON;
    }

    public boolean overlapsY(Rectangle rect) {
        return yOverlap(rect) > EPSILON;
    }

    public float xOverlap(Rectangle rect) {
        return xOverlap(rect.minX, rect.maxX);
    }

    public float yOverlap(Rectangle rect) {
        return yOverlap(rect.minY, rect.maxY);
    }

    public float xOverlap(float left, float right) {
        return min(maxX, right) - max(minX, left);
    }

    public float yOverlap(float bottom, float top) {
        return min(maxY, top) - max(minY, bottom);
    }

    public boolean edgeAlignedIfScaled(Rectangle rect, Side edge) {
        switch (edge) {
            case Left:
                return rect.minX < minX - EPSILON;
            case Bottom:
                return rect.minY < minY - EPSILON;
            case Right:
                return rect.maxX > maxX + EPSILON;
            case Top:
                return rect.maxY > maxY + EPSILON;
        }
        return false;
    }

    public float nearestSidePos(float pos, Orientation lineOrientation) {
        if (lineOrientation == Vertical) {
            return abs(minX - pos) < abs(maxX - pos) ? minX : maxX;
        } else {
            return abs(minY - pos) < abs(maxY - pos) ? minY : maxY;
        }
    }

    public boolean intersects(float pos, Orientation lineOrientation) {
        if (lineOrientation == Vertical) {
            return intersectsX(pos);
        } else {
            return intersectsY(pos);
        }
    }

    public boolean intersectsX(float x) {
        return minX < x + EPSILON && maxX > x - EPSILON;
    }

    public boolean intersectsY(float y) {
        return minY < y + EPSILON && maxY > y - EPSILON;
    }

    public boolean touchesX(float x) {
        return abs(minX - x) < EPSILON || abs(maxX - x) < EPSILON;
    }

    public boolean touchesY(float y) {
        return abs(minY - y) < EPSILON || abs(maxY - y) < EPSILON;
    }

    public boolean touches(float x, float y) {
        return (x > minX - EPSILON && x < maxX + EPSILON && y > minY - EPSILON && y < maxY + EPSILON);
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.US));
        return "[" + df.format(minX) + ", " + df.format(maxX) + "]x [" + df.format(minY) + ", " + df.format(maxY) + "]y";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (minX * 100);
        hash = 43 * hash + (int) (minY * 100);
        hash = 43 * hash + (int) (maxX * 100);
        hash = 43 * hash + (int) (maxY * 100);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rectangle other = (Rectangle) obj;
        return abs(minX - other.minX) < EPSILON
                && abs(minY - other.minY) < EPSILON
                && abs(maxX - other.maxX) < EPSILON
                && abs(maxY - other.maxY) < EPSILON;
    }

    //TODO: refactor
    public ArrayList<Rectangle> cut(Rectangle cutter) {
        if (overlaps(cutter)) {
            ArrayList<Float> xCuts = new ArrayList<>(4);
            ArrayList<Float> yCuts = new ArrayList<>(4);
            xCuts.add(minX);
            if (cutter.minX > minX + EPSILON) {
                xCuts.add(cutter.minX);
            }
            if (cutter.maxX < maxX - EPSILON) {
                xCuts.add(cutter.maxX);
            }
            xCuts.add(maxX);
            yCuts.add(minY);
            if (cutter.minY > minY + EPSILON) {
                yCuts.add(cutter.minY);
            }
            if (cutter.maxY < maxY - EPSILON) {
                yCuts.add(cutter.maxY);
            }
            yCuts.add(maxY);

            ArrayList<Rectangle> cutlets = new ArrayList<>(6);
            for (int x = 0; x < xCuts.size() - 1; x++) {
                for (int y = 0; y < yCuts.size() - 1; y++) {
                    float x1 = xCuts.get(x);
                    float x2 = xCuts.get(x + 1);
                    float y1 = yCuts.get(y);
                    float y2 = yCuts.get(y + 1);
                    cutlets.add(new Rectangle(x1, y1, x2 - x1, y2 - y1));
                }
            }
            return cutlets;
        }
        return null;
    }

    //TODO: refactor
    public static ArrayList<Rectangle> nonOverlappingSet(ArrayList<Rectangle> rects) {
        ArrayList<Rectangle> result = new ArrayList<>(rects.size());
        result.add(rects.get(0));

        for (int r = 1; r < rects.size(); r++) {
            Rectangle inputRect = rects.get(r);

            ArrayList<Rectangle> freshRects = new ArrayList<>();
            freshRects.add(inputRect);

            for (Rectangle solid : result) {
                ArrayList<Rectangle> cutFreshRects = new ArrayList<>();
                for (Rectangle fresh : freshRects) {
                    if (fresh.overlaps(solid)) {
                        ArrayList<Rectangle> cuts = fresh.cut(solid);
                        for (Rectangle cut : cuts) {
                            if (!cut.overlaps(solid)) {
                                cutFreshRects.add(cut);
                            }
                        }
                    } else {
                        cutFreshRects.add(fresh);
                    }
                }
                freshRects = cutFreshRects;
            }

            result.addAll(freshRects);
        }

        return result;
    }

    public static boolean rectanglesOverlap(Collection<? extends Rectangle> collectionA,
                                            Collection<? extends Rectangle> collectionB) {
        return (collectionA.stream()
                .anyMatch(rectA -> (collectionB.stream().anyMatch(rectB -> rectA.overlaps(rectB)))));
    }

    public static Rectangle findBoundingRect(Collection<? extends Rectangle> rects) {
        if (rects.isEmpty()) {
            return ZERO;
        }

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (Rectangle rect : rects) {
            minX = min(minX, rect.minX);
            minY = min(minY, rect.minY);
            maxX = max(maxX, rect.maxX);
            maxY = max(maxY, rect.maxY);
        }

        return fromMinMax(minX, minY, maxX, maxY);
    }

    public static float sumArea(Collection<? extends Rectangle> rects) {
        return (float) rects.stream().mapToDouble((rect) -> rect.area()).sum();
    }
}
