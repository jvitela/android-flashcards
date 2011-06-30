package com.jvitela.flashcards;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.graphics.Camera;
import android.graphics.Matrix;

/**
 * @author jonathan
 * An animation that rotates the view on the Y axis between two specified angles.
 * This animation also adds a translation on the Z axis (depth) to improve the effect.
 */
public class Rotate3dAnimation extends Animation {
    protected final float mFromDegrees;
    protected final float mToDegrees;
    protected final float mCenterX;
    protected final float mCenterY;
    protected final float mDepthZ;
    protected final boolean mAnimationIn;
    protected Camera mCamera;

    /**
     * Creates a new 3D rotation on the Y axis. The rotation is defined by its
     * start angle and its end angle. Both angles are in degrees. The rotation
     * is performed around a center point on the 2D space, definied by a pair
     * of X and Y coordinates, called centerX and centerY. When the animation
     * starts, a translation on the Z axis (depth) is performed. The length
     * of the translation can be specified, as well as whether the translation
     * should be reversed in time.
     *
     * @param fromDegrees the start angle of the 3D rotation
     * @param toDegrees the end angle of the 3D rotation
     * @param centerX the X center of the 3D rotation
     * @param centerY the Y center of the 3D rotation
     * @param animationIn true if its an "enters" animation ( translation is reversed,and clips when back-faced ), false otherwise
     */
    public Rotate3dAnimation(	float fromDegrees, float toDegrees,
    							float centerX, float centerY, 
    							float depthZ, boolean animationIn ) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDepthZ = depthZ;
        mAnimationIn = animationIn;
    }
    
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float degrees = mFromDegrees + ((mToDegrees - mFromDegrees) * interpolatedTime);

        Matrix matrix = t.getMatrix();

        mCamera.save();
        if( mAnimationIn ) {
        	mCamera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        } else {
        	mCamera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        }
        mCamera.rotateY(degrees);
        mCamera.getMatrix(matrix);
        mCamera.restore();

        matrix.preTranslate(-mCenterX, -mCenterY);
        matrix.postTranslate(mCenterX, mCenterY);

		if( mAnimationIn ) {
	        if( interpolatedTime<=0.5f )
	        	t.setAlpha( 0.0f );
	        else 
	        	t.setAlpha( 1.0f );        	
        }
        else {
	        if( interpolatedTime<=0.5f )
	        	t.setAlpha( 1.0f );
	        else 
	        	t.setAlpha( 0.0f );
        }
    }
   
}
