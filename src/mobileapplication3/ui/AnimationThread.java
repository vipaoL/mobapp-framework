/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.ui;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Mathh;

/**
 *
 * @author vipaol
 */
public class AnimationThread implements Runnable {
    public static final int FPS = 20;
    public static final int FRAME_MILLIS = 1000 / FPS;
    public static final int FP_MATH_MULTIPLIER = 1000;
    
    private Thread thread = null;
    private AnimationWorker feedback;
    private boolean isRunning = false;
    private int framesCount;
    private int frameTime;
    private int x, y, targetX, targetY;
    private int leftLimitX, rightLimitX, topLimitY, bottomLimitY;
    private int vX = 0, vY = 0;
    private int aX, aY;
    
    public AnimationThread(AnimationWorker feedback) {
        this.feedback = feedback;
    }
    
    public void animate(int currX, int currY, int targetX, int targetY, int durationMillis, int leftLimitX, int rightLimitX, int topLimitY, int bottomLimitY) {
    	animate(currX, currY, targetX, targetY, durationMillis, leftLimitX, rightLimitX, topLimitY, bottomLimitY, FRAME_MILLIS);
    }
    
    public void animate(int currX, int currY, int targetX, int targetY, int durationMillis, int leftLimitX, int rightLimitX, int topLimitY, int bottomLimitY, int frameTime) {
    	Logger.log("dx:"+(targetX-currX)+" dy:"+(targetY-currY)+" t:"+durationMillis+" dt:" + frameTime);
        if (currY == targetY && currX == targetX) {
            return;
        }

        this.frameTime = Math.max(frameTime, 5);

        this.leftLimitX = leftLimitX * FP_MATH_MULTIPLIER;
        this.rightLimitX = rightLimitX * FP_MATH_MULTIPLIER;
        this.topLimitY = topLimitY * FP_MATH_MULTIPLIER;
        this.bottomLimitY = bottomLimitY * FP_MATH_MULTIPLIER;
        
        x = currX * FP_MATH_MULTIPLIER;
        y = currY * FP_MATH_MULTIPLIER;
        this.targetX = targetX * FP_MATH_MULTIPLIER;
        this.targetY = targetY * FP_MATH_MULTIPLIER;

        framesCount = durationMillis / frameTime;
        if (framesCount == 0) {
        	onStep(this.targetX, this.targetY);
        	return;
        }

        int sX = this.targetX - x;
        int sY = this.targetY - y;
        
        vX = 2*sX/(framesCount);
        vY = 2*sY/(framesCount);
        
        aX = 2*sX/(framesCount*framesCount);
        aY = 2*sY/(framesCount*framesCount);

        int vX = this.vX;
        int vY = this.vY;
        int x = this.x;
        int y = this.y;
        for (int i = 0; i < framesCount; i++) {
            vX -= aX;
            vY -= aY;
            x += vX;
            y += vY;
        }
        this.x += this.targetX - x;
        this.y += this.targetY - y;

        this.targetX = Mathh.constrain(this.leftLimitX, this.targetX, this.rightLimitX);
        this.targetY = Mathh.constrain(this.topLimitY, this.targetY, this.bottomLimitY);
        
        if (!isRunning) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void run() {
        isRunning = true;
        for (int i = 0; i < framesCount; i++) {
            long iterationStartMillis = System.currentTimeMillis();
            
            vX -= aX;
            vY -= aY;
            x += vX;
            y += vY;
            
            onStep(x, y);

            if (!Mathh.nonStrictIneq(leftLimitX, x, rightLimitX) || !Mathh.nonStrictIneq(topLimitY, y, bottomLimitY)) {
                break;
            }
            
            try {
                Thread.sleep(Math.max(0, frameTime - (System.currentTimeMillis() - iterationStartMillis)));
            } catch (InterruptedException ex) {
                isRunning = false;
                return;
            }
        }
        onStep(targetX, targetY);
        isRunning = false;
    }

    private void onStep(int xFP, int yFP) {
        xFP = Mathh.constrain(leftLimitX, xFP, rightLimitX);
        yFP = Mathh.constrain(topLimitY, yFP, bottomLimitY);
        feedback.onStep(xFP / FP_MATH_MULTIPLIER, yFP / FP_MATH_MULTIPLIER);
    }

    public interface AnimationWorker {
        public void onStep(int newX, int newY);
    }
}