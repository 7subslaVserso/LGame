namespace Loon.Action.Sprite
{
    using System;
    using System.Collections.Generic;
    using System.Runtime.CompilerServices;
    using Loon.Core;
    using Loon.Core.Graphics.Opengl;
    using Loon.Utils;
    using Loon.Core.Graphics;

    public class Animation : LRelease
    {

        public interface AnimationListener
        {
            void OnComplete(Animation animation);
        }

        public AnimationListener Listener;

        public void SetAnimationListener(AnimationListener l)
        {
            this.Listener = l;
        }

        public AnimationListener GetAnimationListener()
        {
            return this.Listener;
        }

        internal bool isRunning;

        private List<AnimationFrame> frames;

        internal int loopCount, loopPlay;

        internal int currentFrameIndex;

        internal long animTime, totalDuration;

        internal int size;

        public Animation()
            : this(new List<AnimationFrame>(CollectionUtils.INITIAL_CAPACITY), 0)
        {

        }

        public Animation(Animation a)
        {
            this.isRunning = a.isRunning;
            this.frames = new List<Animation.AnimationFrame>(a.frames);
            this.loopCount = a.loopCount;
            this.loopPlay = a.loopPlay;
            this.currentFrameIndex = a.currentFrameIndex;
            this.animTime = a.animTime;
            this.totalDuration = a.totalDuration;
            this.size = frames.Count;
        }

        private Animation(List<AnimationFrame> frames, long totalDuration)
        {
            this.loopCount = -1;
            this.frames = frames;
            this.size = frames.Count;
            this.totalDuration = totalDuration;
            this.isRunning = true;
            Start();
        }

        /**
         * ת��ָ���ļ�Ϊ����ͼ��
         * 
         * @param fileName
         * @param width
         * @param height
         * @param timer
         * @return
         */
        public static Animation GetDefaultAnimation(string fileName, int width,
                int height, int timer)
        {
            return Animation.GetDefaultAnimation(
                    TextureUtils.GetSplitTextures(fileName, width, height), -1,
                    timer);
        }

        /**
         * ת��ָ���ļ�Ϊ����ͼ��
         * 
         * @param fileName
         * @param width
         * @param height
         * @param timer
         * @param filterColor
         * @return
         */
        public static Animation GetDefaultAnimation(string fileName, int width,
                int height, int timer, LColor filterColor)
        {
            return Animation.GetDefaultAnimation(
                    TextureUtils.GetSplitTextures(
                            TextureUtils.FilterColor(fileName, filterColor), width,
                            height), -1, timer);
        }

        /**
         * ת��ָ���ļ�Ϊ����ͼ��
         * 
         * @param fileName
         * @param maxFrame
         * @param width
         * @param height
         * @param timer
         * @return
         */
        public static Animation GetDefaultAnimation(string fileName, int maxFrame,
                int width, int height, int timer)
        {
            return Animation.GetDefaultAnimation(
                    TextureUtils.GetSplitTextures(fileName, width, height),
                    maxFrame, timer);
        }

        /**
         * ת��һ��ImageΪ����ͼ��
         * 
         * @param images
         * @param maxFrame
         * @param width
         * @param height
         * @param timer
         * @return
         */
        public static Animation GetDefaultAnimation(LTexture[] images,
                int maxFrame, int timer)
        {
            if (images == null)
            {
                return new Animation();
            }
            Animation animation = new Animation();
            if (maxFrame != -1)
            {
                for (int i = 0; i < maxFrame; i++)
                {
                    animation.AddFrame(images[i], timer);
                }
            }
            else
            {
                int size = images.Length;
                for (int i = 0; i < size; i++)
                {
                    animation.AddFrame(images[i], timer);
                }
            }
            return animation;
        }

        /**
         * ��¡һ����������
         */
        public virtual object Clone()
        {
            return new Animation(frames, totalDuration);
        }

        /**
         * ���һ������ͼ��
         * 
         * @param image
         * @param timer
         */

        [MethodImpl(MethodImplOptions.Synchronized)]
        public virtual void AddFrame(LTexture image, long timer)
        {
            totalDuration += timer;
            frames.Add(new AnimationFrame(image, totalDuration));
            size++;
        }

        /**
         * ���һ������ͼ��
         * 
         * @param fileName
         * @param timer
         */

        [MethodImpl(MethodImplOptions.Synchronized)]
        public virtual void AddFrame(string fileName, long timer)
        {
            AddFrame(LTextures.LoadTexture(fileName), timer);
        }


        [MethodImpl(MethodImplOptions.Synchronized)]
        public virtual void Start()
        {
            animTime = 0;
            if (size > 0)
            {
                currentFrameIndex = 0;
            }
        }

        public virtual void Reset()
        {
            animTime = 0;
            currentFrameIndex = 0;
            loopPlay = 0;
            loopCount = -1;
            isRunning = true;
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public virtual void Update(long timer)
        {
            if (loopCount != -1 && loopPlay > loopCount)
            {
                return;
            }
            if (isRunning)
            {
                if (size > 0)
                {
                    animTime += timer;
                    if (animTime > totalDuration)
                    {
                        if (Listener != null)
                        {
                            Listener.OnComplete(this);
                        }
                        animTime = animTime % totalDuration;
                        currentFrameIndex = 0;
                        loopPlay++;
                    }
                    for (; animTime > GetFrame(currentFrameIndex).endTimer; )
                    {
                        currentFrameIndex++;
                    }
                }
            }
        }

        /**
         * ���ص�ǰ����ͼ��
         * 
         * @return
         */
        public virtual LTexture GetSpriteImage()
        {
            if (size == 0)
            {
                return null;
            }
            else
            {
                return GetFrame(currentFrameIndex).image;
            }
        }

        /**
         * ���ص�ǰ����ͼ��
         * 
         * @param index
         * @return
         */
        public virtual LTexture GetSpriteImage(int index)
        {
            if (index < 0 || index >= size)
            {
                return null;
            }
            else
            {
                return GetFrame(index).image;
            }
        }

        /**
         * ���ص�ǰ�������
         * 
         * @param i
         * @return
         */
        private AnimationFrame GetFrame(int index)
        {
            if (index < 0)
            {
                return (AnimationFrame)frames[0];
            }
            else if (index >= size)
            {
                return (AnimationFrame)frames[size - 1];
            }
            return (AnimationFrame)frames[index];
        }

        /**
         * �趨ֹͣ״̬
         * 
         * @param isStop
         */
        public virtual void SetRunning(bool runing)
        {
            this.isRunning = runing;
        }

        /**
         * ���ض���״̬
         * 
         * @param isStop
         */
        public virtual bool IsRunning()
        {
            return this.isRunning;
        }

        /**
         * ���ص�ǰ��������
         * 
         * @return
         */
        public virtual int GetCurrentFrameIndex()
        {
            return this.currentFrameIndex;
        }

        public virtual void SetCurrentFrameIndex(int index)
        {
            this.currentFrameIndex = index;
        }

        public virtual int GetTotalFrames()
        {
            return size;
        }

        public virtual int GetLoopCount()
        {
            return loopCount;
        }

        public virtual void SetLoopCount(int loopCount)
        {
            this.loopCount = loopCount;
        }

        private class AnimationFrame : LRelease
        {

            internal LTexture image;

            internal long endTimer;

            public AnimationFrame(LTexture image, long endTimer)
            {
                this.image = image;
                this.endTimer = endTimer;
            }

            public void Dispose()
            {
                if (image != null)
                {
                    LTexture father = image.GetParent();
                    if (father != null && !father.IsClose())
                    {
                        father.Destroy();
                    }
                    else if (image != null && !image.IsClose())
                    {
                        image.Destroy();
                    }
                }
            }
        }

        public void Dispose()
        {
            if (frames != null)
            {
                foreach (AnimationFrame frame in frames)
                {
                    if (frame != null)
                    {
                        frame.Dispose();
                    }
                }
                frames.Clear();
            }
            this.size = 0;
        }
    }
}
