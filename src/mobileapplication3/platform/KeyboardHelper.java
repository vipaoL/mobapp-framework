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

                        // Wait 500 ms before starting to repeat
                        boolean aborted = false;
                        synchronized (tillPressed) {
                            long targetTime = System.currentTimeMillis() + 500;
                            while (pressState && isRunning) {
                                long timeLeft = targetTime - System.currentTimeMillis();
                                if (timeLeft <= 0) {
                                    break;
                                }
                                tillPressed.wait(timeLeft);
                            }
                            if (!pressState || !isRunning) {
                                aborted = true;
                            }
                        }

                        if (aborted) {
                            continue;
                        }

                        // Repeat
                        while (isRunning && Thread.currentThread() == repeatThread) {
                            synchronized (tillPressed) {
                                if (!pressState) {
                                    break;
                                }
                            }

                            listener.handleKeyRepeated(lastKey, pressCount);

                            synchronized (tillPressed) {
                                long targetTime = System.currentTimeMillis() + 150;
                                while (isRunning && Thread.currentThread() == repeatThread && pressState) {
                                    long timeLeft = targetTime - System.currentTimeMillis();
                                    if (timeLeft <= 0) {
                                        break;
                                    }
                                    tillPressed.wait(timeLeft);
                                }
                                if (!pressState) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (InterruptedException ignored) { }
            }
        });
        repeatThread.start();
    }

    public void stop() {
        synchronized (tillPressed) {
            repeatThread = null;
            isRunning = false;
            pressState = false;
            tillPressed.notifyAll();
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
            tillPressed.notifyAll();
        }
        listener.handleKeyPressed(k, pressCount);
    }

    public void keyReleased(int k) {
        updateLastEventTime();

        synchronized (tillPressed) {
            if (k == lastKey) {
                pressState = false;
            } else {
                pressCount = 0;
            }
            tillPressed.notifyAll();
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