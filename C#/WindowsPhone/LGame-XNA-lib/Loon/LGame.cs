using System;
using System.Text;
using System.Threading;
using System.Globalization;
using System.Diagnostics;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Loon.Core.Graphics.Opengl;
using Loon.Core;
using Loon.Core.Geom;
using Loon.Core.Graphics;
using Loon.Utils;
namespace Loon
{
 
    public abstract class LGame : Game , XNABind
    {

        public void OnGameExit()
        {
            this.Exit();
        }

        public bool UseXNA
        {
            set;
            get;
        }

        private GraphicsDeviceManager graphics;

        private LGameXNA2DActivity m_init;

        public LGame()
        {
            graphics = new GraphicsDeviceManager(this);
            Content.RootDirectory = "";
            if (useXNAListener)
            {
                xna_listener.Create(this);
            }
        }

        /// <summary>
        /// LGame��ʼ����������
        /// </summary>
        public abstract void OnMain();

        public abstract void OnGameResumed();

        public abstract void OnGamePaused();

        private XNAListener xna_listener;

        private bool useXNAListener;

        public void SetXNAListener(XNAListener l)
        {
            if (l != null)
            {
                this.xna_listener = l;
                this.useXNAListener = true;
            }
            else
            {
                this.useXNAListener = false;
            }
        }

        public XNAListener GetXNAListener()
        {
            return xna_listener;
        }

        private LSetting.Listener _listener;

        private DisplayMode m_displayMode;

        private int width, height, maxWidth, maxHeight;

        private LMode m_mode = LMode.Fill;

        private GameType m_type ;

        public GameType GetGameType()
        {
            return m_type;
        }

        protected override void Initialize()
        {
            this.UseXNA = false;
            GL.device = base.GraphicsDevice;
            this.OnMain();
            if (m_type == null)
            {
                throw new Exception("Not LSetting Register ! ");
            }
            this.MaxScreen(m_type.setting.width, m_type.setting.height);
            this.OnCreate(m_type.setting.landscape, m_type.setting.full);
            this.m_init = new LGameXNA2DActivity(this, width, height);
            this.m_init.SetContent(Content);
            this.m_init.OnCreate(new XNABundle(this));
            base.Initialize();
        }

        public void MaxScreen(int w, int h)
        {
            LSystem.MAX_SCREEN_WIDTH = m_type.setting.width;
            LSystem.MAX_SCREEN_HEIGHT = m_type.setting.height;
        }

        public void Register(LSetting setting, Type clazz,
              params object[] args)
        {
            if (m_type == null)
            {
                m_type = new GameType();
            }
            if (setting != null)
            {
                m_type.setting = setting;
            }
            this.m_type.mainType = clazz;
            this.m_type.args = args;
            this._listener = setting.listener;
        }

        public void SetLandscape(bool l)
        {
            if (m_type != null)
            {
                m_type.setting.landscape = l;
            }
        }

        public void SetFullScreen(bool f)
        {
            if (m_type != null)
            {
                m_type.setting.full = f;
            }
        }

        public void OnStateLog(Loon.Utils.Debugging.Log log)
        {
            StringBuilder sbr = new StringBuilder();
            sbr.Append("Mode:").Append(m_mode);
            log.I(sbr.ToString());
            sbr.Clear();
            sbr.Append("Width:").Append(width).Append(",Height:" + height);
            log.I(sbr.ToString());
            sbr.Clear();
            sbr.Append("MaxWidth:").Append(maxWidth)
                    .Append(",MaxHeight:" + maxHeight);
            log.I(sbr.ToString());
            sbr.Clear();
            sbr.Append("Scale:").Append(IsScale());
            log.I(sbr.ToString());
            sbr = null;
        }

        public void OnCreate(bool m_landscape,bool m_fullscreen)
        {
            if (!m_landscape)
            {
                if (LSystem.MAX_SCREEN_HEIGHT > LSystem.MAX_SCREEN_WIDTH)
                {
                    int tmp_height = LSystem.MAX_SCREEN_HEIGHT;
                    LSystem.MAX_SCREEN_HEIGHT = LSystem.MAX_SCREEN_WIDTH;
                    LSystem.MAX_SCREEN_WIDTH = tmp_height;
                }
            }

            RectBox d = GetScreenDimension();

            LSystem.SCREEN_LANDSCAPE = m_landscape;

            this.CheckDisplayMode();

            //���д���������Ļ��СΪLSetting�趨�ߴ�

            this.maxWidth = (int)d.GetWidth();
            this.maxHeight = (int)d.GetHeight();

            if (m_landscape && (d.GetWidth() > d.GetHeight()))
            {
                maxWidth = (int)d.GetWidth();
                maxHeight = (int)d.GetHeight();
            }
            else if (m_landscape && (d.GetWidth() < d.GetHeight()))
            {
                maxHeight = (int)d.GetWidth();
                maxWidth = (int)d.GetHeight();
            }
            else if (!m_landscape && (d.GetWidth() < d.GetHeight()))
            {
                maxWidth = (int)d.GetWidth();
                maxHeight = (int)d.GetHeight();
            }
            else if (!m_landscape && (d.GetWidth() > d.GetHeight()))
            {
                maxHeight = (int)d.GetWidth();
                maxWidth = (int)d.GetHeight();
            }

            if (m_mode != LMode.Max)
            {
                if (m_landscape)
                {
                    this.width = LSystem.MAX_SCREEN_WIDTH;
                    this.height = LSystem.MAX_SCREEN_HEIGHT;
                }
                else
                {
                    this.width = LSystem.MAX_SCREEN_HEIGHT;
                    this.height = LSystem.MAX_SCREEN_WIDTH;
                }
            }
            else
            {
                if (m_landscape)
                {
                    this.width = maxWidth >= LSystem.MAX_SCREEN_WIDTH ? LSystem.MAX_SCREEN_WIDTH
                            : maxWidth;
                    this.height = maxHeight >= LSystem.MAX_SCREEN_HEIGHT ? LSystem.MAX_SCREEN_HEIGHT
                            : maxHeight;
                }
                else
                {
                    this.width = maxWidth >= LSystem.MAX_SCREEN_HEIGHT ? LSystem.MAX_SCREEN_HEIGHT
                            : maxWidth;
                    this.height = maxHeight >= LSystem.MAX_SCREEN_WIDTH ? LSystem.MAX_SCREEN_WIDTH
                            : maxHeight;
                }
            }

            if (m_mode == LMode.Fill)
            {

                LSystem.scaleWidth = ((float)maxWidth) / width;
                LSystem.scaleHeight = ((float)maxHeight) / height;

            }
            else if (m_mode == LMode.FitFill)
            {

                RectBox res = GraphicsUtils.FitLimitSize(width, height,
                        maxWidth, maxHeight);
                maxWidth = res.width;
                maxHeight = res.height;
                LSystem.scaleWidth = ((float)maxWidth) / width;
                LSystem.scaleHeight = ((float)maxHeight) / height;

            }
            else if (m_mode == LMode.Ratio)
            {

                maxWidth = MeasureSpec.GetSize(maxWidth);
                maxHeight = MeasureSpec.GetSize(maxHeight);

                float userAspect = (float)width / (float)height;
                float realAspect = (float)maxWidth / (float)maxHeight;

                if (realAspect < userAspect)
                {
                    maxHeight = MathUtils.Round(maxWidth / userAspect);
                }
                else
                {
                    maxWidth = MathUtils.Round(maxHeight * userAspect);
                }

                LSystem.scaleWidth = ((float)maxWidth) / width;
                LSystem.scaleHeight = ((float)maxHeight) / height;

            }
            else if (m_mode == LMode.MaxRatio)
            {

                maxWidth = MeasureSpec.GetSize(maxWidth);
                maxHeight = MeasureSpec.GetSize(maxHeight);

                float userAspect = (float)width / (float)height;
                float realAspect = (float)maxWidth / (float)maxHeight;

                if ((realAspect < 1 && userAspect > 1)
                        || (realAspect > 1 && userAspect < 1))
                {
                    userAspect = (float)height / (float)width;
                }

                if (realAspect < userAspect)
                {
                    maxHeight = MathUtils.Round(maxWidth / userAspect);
                }
                else
                {
                    maxWidth = MathUtils.Round(maxHeight * userAspect);
                }

                LSystem.scaleWidth = ((float)maxWidth) / width;
                LSystem.scaleHeight = ((float)maxHeight) / height;

            }
            else
            {
                LSystem.scaleWidth = 1f;
                LSystem.scaleHeight = 1f;
            }

            if (LSystem.screenRect == null)
            {
                LSystem.screenRect = new RectBox(0, 0, width, height);
            }
            else
            {
                LSystem.screenRect.SetBounds(0, 0, width, height);
            }

            //PS:width��heightΪ��������Ϸ������С����maxWidth��maxHeightΪʵ�ʵ��ֻ���Ļ��С

            graphics.PreferredBackBufferFormat = m_displayMode.Format;
            graphics.PreferredBackBufferWidth = maxWidth;
            graphics.PreferredBackBufferHeight = maxHeight;

            if (m_landscape)
            {
                graphics.SupportedOrientations = DisplayOrientation.LandscapeLeft | DisplayOrientation.LandscapeRight;
            }
            else
            {
                graphics.SupportedOrientations = DisplayOrientation.Portrait;
            }

            //������Ⱦ����ʾ��ͬ��
            graphics.SynchronizeWithVerticalRetrace = false;
            graphics.PreferMultiSampling = true;
            graphics.PreparingDeviceSettings += new EventHandler<PreparingDeviceSettingsEventArgs>(Inner_deviceSettings);
#if WINDOWS
            graphics.IsFullScreen = false;
            IsMouseVisible = true;
#elif XBOX || WINDOWS_PHONE
            //ȫ��
            if (m_fullscreen)
            {
                graphics.IsFullScreen = true;
            }
            else
            {
                graphics.IsFullScreen = false;
            }
#endif
            base.IsFixedTimeStep = false;

            graphics.ApplyChanges();
        }

        public void SetXNASleepTime(int time)
        {
            if (time <= 0)
            {
                InactiveSleepTime = TimeSpan.Zero;
            }
            else
            {
                InactiveSleepTime = TimeSpan.FromSeconds(time);
            }
        }

        public void SetXNAFPS(int fps)
        {
            if (fps <= 0)
            {
                TargetElapsedTime = TimeSpan.Zero;
            }
            else
            {
                if (30 == fps)
                {
                    TargetElapsedTime = TimeSpan.FromTicks(333333);
                }
                else
                {
                    try
                    {
                        TargetElapsedTime = System.TimeSpan.FromTicks((System.TimeSpan.TicksPerSecond / fps) + 1);
                    }
                    catch
                    {
                        TargetElapsedTime = TimeSpan.FromTicks(333333);
                    }
                }
            }
        }

        public bool IsScale()
        {
            return LSystem.scaleWidth != 1f || LSystem.scaleHeight != 1f;
        }

        void Inner_deviceSettings(object sender, PreparingDeviceSettingsEventArgs e)
        {
            e.GraphicsDeviceInformation.PresentationParameters.PresentationInterval = PresentInterval.One;
        }

        public RectBox GetScreenDimension()
        {
            CheckDisplayMode();
            return new RectBox(0, 0, m_displayMode.Width, m_displayMode.Height);
        }

        private void CheckDisplayMode()
        {
            if (m_displayMode == null)
            {
                this.m_displayMode = GraphicsAdapter.DefaultAdapter.CurrentDisplayMode;
            }
        }

        protected override void LoadContent()
        {
            GraphicsDevice.Clear(Color.Black);
            this.m_init.LoadApp();
            if (useXNAListener)
            {
                xna_listener.LoadContent(this);
            }
        }

        /// <summary>
        /// �ں�XNAע��ʱ���õ�LGame API
        /// </summary>
        /// <param name="disposing"></param>
        protected override void Dispose(bool disposing)
        {
            base.Dispose(disposing);
            if (disposing)
            {
                useXNAListener = false;
                if (this.xna_listener != null)
                {
                    this.xna_listener.Dispose(this, disposing);
                }
            }
        }

        /// <summary>
        /// �ں�XNA��ֹͣ״̬�ָ�ʱ���õ�LGame API
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        protected override void OnActivated(object sender, EventArgs args)
        {
            if (_listener != null)
            {
                _listener.OnResume();
            }
            this.m_init.ResumeApp();
            base.OnActivated(sender, args);
        }

        /// <summary>
        /// �ں�XNA��ʱֹͣʱ���õ�LGame API
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        protected override void OnDeactivated(object sender, EventArgs args)
        {
            if (_listener != null)
            {
                _listener.OnPause();
            }
            this.m_init.PauseApp();
            base.OnDeactivated(sender, args);
        }

        /// <summary>
        /// �ں�XNA׼���ر�ʱ���õ�LGame API
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        protected override void OnExiting(object sender, EventArgs args)
        {
            if (_listener != null)
            {
                _listener.OnExit();
            }
           this.m_init.FinishApp();
           base.OnExiting(sender, args);
        }

        /// <summary>
        /// �ں�XNA��Դж��ʱ���õ�LGame API
        /// </summary>
        protected override void UnloadContent()
        {
            this.m_init.UnloadApp();
            if (useXNAListener)
            {
                xna_listener.UnloadContent(this);
            }
            if (Content != null)
            {
                Content.Unload();
            }
        }

        /// <summary>
        /// �ں�XNA��Ⱦͼ��ʱ���õ�LGame API
        /// </summary>
        /// <param name="gameTime"></param>
        protected override void Draw(GameTime gameTime)
        {
            this.m_init.OnDraw(gameTime);
            this.m_init.RequestRender();
            if (useXNAListener)
            {
                xna_listener.Draw(this, gameTime);
            }
            if (UseXNA)
            {
                base.Draw(gameTime);
            }
        }

        /// <summary>
        /// �ں�XNAˢ������ʱ���õ�LGame API
        /// </summary>
        /// <param name="gameTime"></param>
        protected override void Update(GameTime gameTime)
        {
            this.m_init.OnUpdate(gameTime);
            if (useXNAListener)
            {
                xna_listener.Update(this,gameTime);
            }
            if (UseXNA)
            {
                base.Update(gameTime);
            }
        }
    }
}
