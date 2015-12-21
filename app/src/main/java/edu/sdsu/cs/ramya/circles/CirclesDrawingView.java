package edu.sdsu.cs.ramya.circles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class CirclesDrawingView extends View {

    private final float mCoefficientOfRestitution;
    boolean isLongPress;
    private GestureDetector gestureDetector;
    private ArrayList<Circle> circlesArray;
    private Circle currentCircle;
    private Circle existingCircle;
    private Paint circlePaint;
    private Paint backgroundPaint;
    private long previousTime = 0;
    private long timeDelta;
    private float viewWidth;
    private float viewHeight;
    private boolean isAccelerometerEnabled = false;
    private int radius;


    public CirclesDrawingView(Context context) {
        this(context, null);
    }

    public CirclesDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        circlesArray = new ArrayList<>();
        isLongPress = false;
        gestureDetector = new GestureDetector(context, new GestureListener());
        gestureDetector.setIsLongpressEnabled(true);
        circlePaint = new Paint();
        circlePaint.setColor(getResources().getColor(R.color.red));
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(R.color.background));
        mCoefficientOfRestitution = (float) 0.9;
        timeDelta = 0;
        viewHeight = 0;
        viewWidth = 0;
        radius = 30;
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        PointF currentCoordinates = new PointF(event.getX(), event.getY());
        switch (actionCode) {
            case MotionEvent.ACTION_DOWN:
                handleDownEvent(currentCoordinates);
                break;
            case MotionEvent.ACTION_UP:
                handleUpEvent();
                break;
            case MotionEvent.ACTION_CANCEL:
                handleCancelEvent();
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(currentCoordinates);
                break;
        }
        gestureDetector.onTouchEvent(event);
        return true;

    }

    private void handleCancelEvent() {
        isLongPress = false;
        currentCircle = null;
    }

    private void handleUpEvent() {
        isLongPress = false;
        currentCircle.setShouldEnlarge(false);
        currentCircle = null;
    }

    private void handleDownEvent(PointF currentCoordinates) {
        boolean isInside = false;
        currentCircle = new Circle(currentCoordinates, radius);
        if (circlesArray.size() > 0) {
            for (Circle circle : circlesArray) {
                if (isInsideCircle(circle.getCenter(), currentCircle.getCenter(),
                                   circle.getRadius()))
                {
                    existingCircle = circle;
                    isInside = true;
                }
            }
        }
        if (!isInside) {
            currentCircle.setShouldEnlarge(true);
            circlesArray.add(currentCircle);
            invalidate();
        } else {
            currentCircle = existingCircle;
        }
    }

    private void handleMoveEvent(PointF currentCoordinates) {
        if (currentCircle != null) {
            float radius = currentCircle.getRadius();
            if (!isCircleOutOfBounds(currentCoordinates, radius)) {
                currentCircle.setCenter(currentCoordinates);
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) {
            viewWidth = canvas.getWidth();
            viewHeight = canvas.getHeight();
        }
        if (circlesArray.size() == 0) {
            canvas.drawColor(getResources().getColor(R.color.background));
            return;
        }
        //Calculating time between subsequent draws
        long currentTime = System.nanoTime();
        //Converting to milli seconds
        timeDelta = (currentTime - previousTime) / 1000000;
       // Log.d("timeDelta", "" + timeDelta);
        previousTime = currentTime;
        canvas.drawPaint(backgroundPaint);

        //handling long press
        if (isLongPress && currentCircle.shouldCircleEnlarge()) {
            currentCircle.setRadius(currentCircle.getRadius() + 3);
            invalidate();
        }

        //Detect collision with boundary and resolve collision
        for (Circle circle : circlesArray) {
            float xPosition = 0;
            float yPosition = 0;

            //bounce back if circle hits the boundary
            bounceBack(circle, timeDelta, canvas.getWidth(), canvas.getHeight());

            //if circle is on fling it has some velocity
            if (circle.getVx() != 0 || circle.getVy() != 0) {
                xPosition += circle.getCenter().x + timeDelta * circle.getVx();
                yPosition += circle.getCenter().y + timeDelta * circle.getVy();
                circle.setCenter(xPosition, yPosition);
            } else {
                xPosition = circle.getCenter().x;
                yPosition = circle.getCenter().y;
            }
            //Draw circle
            canvas.drawCircle(xPosition, yPosition, circle.getRadius(), circlePaint);
        }

        //Collision Detection between circles and resolution
        for (int i = 0; i < circlesArray.size(); i++) {
            for (int j = i + 1; j < circlesArray.size(); j++) {
                if (circlesArray.get(i).collide(circlesArray.get(j), timeDelta)) {
                    collisionResolve(i, j);
                }
            }
        }
        postInvalidate();
    }

    public void collisionResolve(int circle1Index, int circle2Index) {
        //Reference : http://www.vobarian.com/collisions/2dcollisions2.pdf
        VectorComponent initialVector = new VectorComponent(
                circlesArray.get(circle1Index).getCenter(),
                circlesArray.get(circle2Index).getCenter());
        VectorComponent initialUnitNormal = new VectorComponent(initialVector.getXComponent(),
                initialVector.getYComponent());
        VectorComponent initialUnitTangent = new VectorComponent(initialVector.getXComponent(),
                initialVector.getYComponent());

        //Calculating unit normal and unit tangent between the two circles
        initialUnitNormal.unitVectorNormalize();
        initialUnitTangent.unitVectorTangent();

        VectorComponent velocityVector1 = new VectorComponent(circlesArray.get(circle1Index).getVx(),
                circlesArray.get(circle1Index).getVy());

        VectorComponent velocityVector2 = new VectorComponent(circlesArray.get(circle2Index).getVx(),
                circlesArray.get(circle2Index).getVy());

        //resolving the velocity vectors, v1 and v2, into normal and tangential components
        float dotProductNormal1 = VectorComponent.GetDotProduct(initialUnitNormal, velocityVector1);
        float dotProductTangent1 = VectorComponent.GetDotProduct(initialUnitTangent, velocityVector1);
        float dotProductNormal2 = VectorComponent.GetDotProduct(initialUnitNormal, velocityVector2);
        float dotProductTangent2 = VectorComponent.GetDotProduct(initialUnitTangent, velocityVector2);

        //Calculating mass
        float massCircle1 = (float) Math.pow(circlesArray.get(circle1Index).getRadius(), 2);
        float massCircle2 = (float) Math.pow(circlesArray.get(circle2Index).getRadius(), 2);
        float sumOfMasses = massCircle1 + massCircle2;
        float differenceOfMasses = massCircle1 - massCircle2;
        float finalNormalVelocity1 = ((dotProductNormal1 * differenceOfMasses) +
                (2 * massCircle2 * dotProductNormal2)) / sumOfMasses;
        float finalNormalVelocity2 = ((dotProductNormal2 * -1 * differenceOfMasses) +
                (2 * massCircle1 * dotProductNormal1)) / sumOfMasses;

        VectorComponent finalNormalVelocityVector1 = new VectorComponent(
                                                     initialUnitNormal.getXComponent(),
                                                     initialUnitNormal.getYComponent());
        VectorComponent finalNormalVelocityVector2 = new VectorComponent(
                                                     initialUnitNormal.getXComponent(),
                                                     initialUnitNormal.getYComponent());
        VectorComponent finalTangentVelocityVector1 = new VectorComponent(
                                                      initialUnitTangent.getXComponent(),
                                                      initialUnitTangent.getYComponent());
        VectorComponent finalTangentVelocityVector2 = new VectorComponent(
                                                      initialUnitTangent.getXComponent(),
                                                      initialUnitTangent.getYComponent());

        //Converting the scalar normal and tangential velocities into vectors
        finalNormalVelocityVector1.MultiplyScalar(finalNormalVelocity1);
        finalNormalVelocityVector2.MultiplyScalar(finalNormalVelocity2);
        finalTangentVelocityVector1.MultiplyScalar(dotProductTangent1);
        finalTangentVelocityVector2.MultiplyScalar(dotProductTangent2);

        //Setting final velocities after collision by adding normal and tangent components
        circlesArray.get(circle1Index).setVx(finalNormalVelocityVector1.getXComponent() +
                finalTangentVelocityVector1.getXComponent());
        circlesArray.get(circle1Index).setVy(finalNormalVelocityVector1.getYComponent() +
                finalTangentVelocityVector1.getYComponent());
        circlesArray.get(circle2Index).setVx(finalNormalVelocityVector2.getXComponent() +
                finalTangentVelocityVector2.getXComponent());
        circlesArray.get(circle2Index).setVy(finalNormalVelocityVector2.getYComponent() +
                finalTangentVelocityVector2.getYComponent());

        //Calculating minimum translation distance to avoid circles overlapping in next draw
        calculateMinimumTranslationDistance(circle1Index, circle2Index);

    }

    private void calculateMinimumTranslationDistance(int circle1Index, int circle2Index) {
        VectorComponent positionDifferenceVector = new VectorComponent
                (circlesArray.get(circle2Index).getCenter(),
                        circlesArray.get(circle1Index).getCenter());
        float distanceBetweenCenters = positionDifferenceVector.GetDistance();
        float radiusSum = circlesArray.get(circle1Index).getRadius() +
                circlesArray.get(circle2Index).getRadius();
        if (distanceBetweenCenters != 0) {
            positionDifferenceVector.MultiplyScalar((radiusSum - distanceBetweenCenters) /
                    distanceBetweenCenters);
        }

        float translationDistanceX1 = circlesArray.get(circle1Index).getCenter().x;
        float translationDistanceY1 = circlesArray.get(circle1Index).getCenter().y;
        float translationDistanceX2 = circlesArray.get(circle2Index).getCenter().x;
        float translationDistanceY2 = circlesArray.get(circle2Index).getCenter().y;

        translationDistanceX1 = translationDistanceX1 +
                (positionDifferenceVector.getXComponent() / 2);
        translationDistanceY1 = translationDistanceY1 +
                (positionDifferenceVector.getYComponent() / 2);
        translationDistanceX2 = translationDistanceX2 -
                (positionDifferenceVector.getXComponent() / 2);
        translationDistanceY2 = translationDistanceY2 -
                (positionDifferenceVector.getYComponent() / 2);
        PointF newCenter1 = new PointF(translationDistanceX1, translationDistanceY1);
        PointF newCenter2 = new PointF(translationDistanceX2, translationDistanceY2);

        if (!isCircleOutOfBounds(newCenter1, circlesArray.get(circle1Index).getRadius())) {
            circlesArray.get(circle1Index).setCenter(newCenter1);
        }
        if (!isCircleOutOfBounds(newCenter2, circlesArray.get(circle2Index).getRadius())) {
            circlesArray.get(circle2Index).setCenter(newCenter2);
        }

    }

    private boolean isInsideCircle(PointF center, PointF newCenter, int radius) {
        double distance = Math.sqrt((Math.pow((newCenter.x - center.x), 2)) +
                Math.pow((newCenter.y - center.y), 2));
        return (int) distance < radius;
    }

    private boolean isCircleOutOfBounds(PointF center, float circleRadius) {
        return !(center.x + circleRadius <= viewWidth && center.x - circleRadius >=0 &&
                center.y + circleRadius <= viewHeight &&
                center.y - circleRadius >= 0);
    }

    private void bounceBack(Circle circle, float timeDelta, float canvasWidth, float canvasHeight) {
        if (circle.getCenter().x + circle.getRadius() +
                timeDelta * circle.getVx() >= canvasWidth - 1) {
            circle.setVx(-1 * mCoefficientOfRestitution * circle.getVx());
        }

        if (circle.getCenter().x + timeDelta * circle.getVx() - circle.getRadius() <= 1) {
            circle.setVx(-1 * mCoefficientOfRestitution * circle.getVx());
        }

        if (circle.getCenter().y + circle.getRadius() +
                timeDelta * circle.getVy() >= canvasHeight - 1) {
            circle.setVy(-1 * mCoefficientOfRestitution * circle.getVy());
        }
        if (circle.getCenter().y + timeDelta * circle.getVy() - circle.getRadius() <= 1) {
            circle.setVy(-1 * mCoefficientOfRestitution * circle.getVy());
        }
    }

    public void accelerate(float xAcceleration, float yAcceleration, float timeDeltaSeconds) {
        if (isAccelerometerEnabled) {
            float accelerationFactor = (float) 0.2;
            for (Circle circle : circlesArray) {
                if (circle.getVx() != 0 && circle.getVy() != 0) {
                    float newVelocityX = circle.getVx() -
                            (accelerationFactor * xAcceleration * timeDeltaSeconds);
                    circle.setVx(newVelocityX);
                    float newVelocityY = circle.getVy() +
                            (accelerationFactor * yAcceleration * timeDeltaSeconds);
                    circle.setVy(newVelocityY);
                }
            }
        }
    }

    public void clearView() {
        circlesArray.clear();
        invalidate();
    }

    public void enableAccelerometer(boolean isEnabled) {
        isAccelerometerEnabled = isEnabled;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            if (currentCircle.shouldCircleEnlarge()) {
                isLongPress = true;
                currentCircle.setRadius(currentCircle.getRadius() + 3);
                invalidate();
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            PointF coordinates = new PointF(e2.getX(), e2.getY());
            if (circlesArray.size() > 0) {
                for (Circle c : circlesArray) {
                    if (isInsideCircle(c.getCenter(), coordinates, c.getRadius())) {
                        c.setVx(velocityX / 4000);
                        //saving velocity in pixels/mSecs
                        c.setVy(velocityY / 4000);
                        invalidate();
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
