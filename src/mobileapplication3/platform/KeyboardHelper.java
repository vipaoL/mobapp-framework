// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

public class KeyboardHelper {
    private final Object tillPressed = new Object();
    private int lastKey, pressCount;
    private boolean pressState;
    private Thread repeatThread;
    private long lastEvent;

    private boolean isRunning = false;

    private final IKeyboardListener listener;

    public KeyboardHelper(IKeyboardListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (repeatThread != null && repeatThread.isAlive()) {
            return;
        }
        isRunning = true;
        pressState = false;
        pressCount = 1;
        lastKey = 0;

        repeatThread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (isRunning && Thread.currentThread() == repeatThread) {
                        // Wait until a key is pressed
                        synchronized (tillPressed) {
                            while (!pressState && isRunning) {
                                tillPressed.wait();
                            }
                        }

                        if (!isRunning) {
                            break;
                        }

                        try {
                            // Wait a delay and start repeating. This thread is interrupted when the key is released
                            Thread.sleep(500);
                            while (isRunning && Thread.currentThread() == repeatThread && pressState) {
                                listener.handleKeyRepeated(lastKey, pressCount);
                                Thread.sleep(150);
                            }
                        } catch (InterruptedException ignored) { }
                    }
                } catch (InterruptedException ignored) { }
            }
        });
        repeatThread.start();
    }

    public void stop() {
        isRunning = false;
        if (repeatThread != null) {
            Thread t = repeatThread;
            repeatThread = null;
            t.interrupt();
        }
    }

    public void keyPressed(int k) {
        if (!isLastEventOld() && k == lastKey) {
            pressCount++;
        } else {
            pressCount = 1;
        }

        updateLastEventTime();
        lastKey = k;

        synchronized (tillPressed) {
            pressState = true;
            tillPressed.notify();
        }
        listener.handleKeyPressed(k, pressCount);
    }

    public void keyReleased(int k) {
        updateLastEventTime();
        if (k == lastKey) {
            pressState = false;
        } else {
            pressCount = 0;
        }

        if (repeatThread != null) {
            repeatThread.interrupt();
        }
        listener.handleKeyReleased(k, pressCount);
    }

    private boolean isLastEventOld() {
        return System.currentTimeMillis() - lastEvent > 200;
    }

    private void updateLastEventTime() {
        lastEvent = System.currentTimeMillis();
    }

    public interface IKeyboardListener {
        void handleKeyPressed(int keyCode, int count);
        void handleKeyReleased(int keyCode, int count);
        void handleKeyRepeated(int keyCode, int count);
    }
}