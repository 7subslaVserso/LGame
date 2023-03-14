package loon.an.window;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import loon.an.JavaANGame;
import loon.utils.MathUtils;
import loon.utils.TimeUtils;

public class JavaANAppLoop extends Thread {

        private final Lock _lock = new ReentrantLock();

        private long _totalTicks;

        private int _tickRate;

        private float _deltaTime;
        private float _processTime;
        private float _delayException;

        private JavaANLoop _loop;
        private JavaANGame _game;

        public JavaANAppLoop(final JavaANGame game, final JavaANLoop loop, final int fps) {
            this._game = game;
            this._loop = loop;
            this._tickRate = fps;
        }

        @Override
        public void run() {
            final boolean wasActive = _game.isActive();
            for (; _loop.get();) {
                ++this._totalTicks;
                final long start = System.nanoTime();
                Lock threadLock = this.getLock();
                threadLock.lock();
                try {
                    _loop.process(wasActive);
                } finally {
                    threadLock.unlock();
                }
                this._processTime = TimeUtils.nanosToMillis(System.nanoTime() - start);
                float sleep;
                try {
                    sleep = this.sleep();
                } catch (InterruptedException e) {
                    break;
                }
                this._deltaTime = (long) (sleep + this._processTime);
            }
        }

        public long toTicks(final int milliseconds) {
            return this.toTicks(milliseconds, getTickRate());
        }

        public long toTicks(final int milliseconds, int updateRate) {
            return (long) (updateRate / 1000f * milliseconds);
        }

        protected long toDelayTime(long start) {
            return ((TimeUtils.nanoTime() - start) / 1000000L);
        }

        public JavaANAppLoop terminate() {
            this.interrupt();
            try {
                this.join();
            } catch (InterruptedException ex) {
            }
            return this;
        }

        public void close() {
            this.terminate();
        }

        public long getTicks() {
            return this._totalTicks;
        }

        public int getTickRate() {
            return this._tickRate;
        }

        public float getDeltaTime() {
            return this._deltaTime;
        }

        public float getProcessTime() {
            return this._processTime;
        }

        public JavaANAppLoop setTickRate(int tickRate) {
            this._tickRate = tickRate;
            return this;
        }

        protected long getExpectedDelta() {
            return (long) (1000f / this._tickRate);
        }

        protected float sleep() throws InterruptedException {
            float delay = MathUtils.max(0, this.getExpectedDelta() - this.getProcessTime());
            long sleepDelay = MathUtils.round(delay);
            this._delayException += delay - sleepDelay;
            if (MathUtils.abs(this._delayException) > 1) {
                long exceptionAdjustment = (long) this._delayException;
                sleepDelay += exceptionAdjustment;
                this._delayException -= exceptionAdjustment;
            }
            if (sleepDelay > 0) {
                sleep(sleepDelay);
            }
            return delay;
        }

        public Lock getLock() {
            return this._lock;
        }

}
