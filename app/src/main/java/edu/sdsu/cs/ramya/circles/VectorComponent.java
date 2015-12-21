package edu.sdsu.cs.ramya.circles;

import android.graphics.PointF;

/**
 * Created by sarathbollepalli on 4/2/15.
 */
public class VectorComponent {

        private float xComponent;
        private float yComponent;

        public float getXComponent() {
            return xComponent;
        }

        public float getYComponent() {
            return yComponent;
        }

        public VectorComponent(float x, float y) {
            this.xComponent = x;
            this.yComponent = y;
        }

        public VectorComponent(PointF p1, PointF p2) {
            this.xComponent = p2.x - p1.x;
            this.yComponent = p2.y - p1.y;
        }


        public float GetDistance()
        {
            double distance = (float) (Math.pow(xComponent,2)+Math.pow(yComponent,2));
            distance = Math.sqrt(distance);
            return (float) distance;
        }

        public void unitVectorNormalize()
        {
            float dist= GetDistance();
            if(dist !=0)
            {
                xComponent = xComponent / dist;
                yComponent = yComponent / dist;
            }

        }

        public void unitVectorTangent()
        {
            unitVectorNormalize();
            float swap = xComponent;
            xComponent = -yComponent;
            yComponent = swap;
        }

        public static float GetDotProduct(VectorComponent a,VectorComponent b)
        {
            return a.xComponent*b.xComponent + a.yComponent*b.yComponent;
        }

        public void MultiplyScalar(float scalar)
        {
            xComponent = xComponent*scalar;
            yComponent  = yComponent*scalar;
        }

}
