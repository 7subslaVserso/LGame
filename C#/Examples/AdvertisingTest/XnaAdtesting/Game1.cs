using Loon;
using Loon.Utils.Debug;
using Loon.Core.Graphics;
using Microsoft.Xna.Framework;
using Microsoft.Advertising.Mobile.Xna;
using System.Diagnostics;
using System.Device.Location;
using System;

namespace LGameAd
{
    /// <summary>
    /// ����XNA����������չʾ���
    /// </summary>
    public class ADListener : XNAListener
    {
        //Advertising�����ñ�ǣ�΢��Ӳ�Թ涨��ֻ�д������������Advertising���ԣ�
        private static readonly string ApplicationId = "test_client";

        //��浥ԪID������ʱֻ֧��4����ʾģʽ������Image480_80��Image480_80��Image300_50��TextAd����ʽID������Զ��塣��
        private static readonly string AdUnitId = "Image480_80";

        private DrawableAd bannerAd;

        //���������λ��(����ͨ��GPS/AGPS�ҵ����ֻ�������λ��)
        private GeoCoordinateWatcher gcw = null;

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��Game��Ĺ���
        /// </summary>
        /// <param name="game"></param>
        public void Create(Game game)
        {

        }

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��Initialize������
        /// </summary>
        public void Initialize(Game game)
        {
            //��ʼ��AdGameComponent�������������ӵ���Ϸ��
            AdGameComponent.Initialize(game, ApplicationId);
            game.Components.Add(AdGameComponent.Current);
            //����һ���µĹ��
            CreateAd(game);
        }

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��LoadContent������
        /// </summary>
        public void LoadContent(Game game)
        {

        }

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��UnloadContent������
        /// </summary>
        public void UnloadContent(Game game)
        {

        }

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��Updatet�ĵ��ã�ÿ֡ѭ��ʱ������ã�
        /// </summary>
        public void Update(Game game, GameTime gameTime)
        {

        }

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��Draw�ĵ��ã�ÿ֡ѭ��ʱ������ã�
        /// </summary>
        public void Draw(Game game, GameTime gameTime)
        {

        }

        /// <summary>
        /// �������
        /// </summary>
        private void CreateAd(Game game)
        {
            // ����ָ����С�Ĺ�����
            int width = 480;
            int height = 80;
            // ��λ����Ļ�����Ϸ�
            int x = (game.GraphicsDevice.Viewport.Bounds.Width - width) / 2;
            int y = 5;

            bannerAd = AdGameComponent.Current.CreateAd(AdUnitId, new Rectangle(x, y, width, height), true);

            // ��ӹ���¼�����
            bannerAd.ErrorOccurred += new EventHandler<Microsoft.Advertising.AdErrorEventArgs>(bannerAd_ErrorOccurred);
            bannerAd.AdRefreshed += new EventHandler(bannerAd_AdRefreshed);

            // ����������������(��GPS��λ�ɹ���ż���)
            AdGameComponent.Current.Enabled = false;

            // ������λ��
            this.gcw = new GeoCoordinateWatcher();
            // ������λ���
            this.gcw.PositionChanged += new EventHandler<GeoPositionChangedEventArgs<GeoCoordinate>>(gcw_PositionChanged);
            this.gcw.StatusChanged += new EventHandler<GeoPositionStatusChangedEventArgs>(gcw_StatusChanged);
            this.gcw.Start();
        }

        private void bannerAd_AdRefreshed(object sender, EventArgs e)
        {
            Log.DebugWrite("Ad received successfully");
        }

        private void bannerAd_ErrorOccurred(object sender, Microsoft.Advertising.AdErrorEventArgs e)
        {
            Log.DebugWrite("Ad error: " + e.Error.Message);
        }

        private void gcw_PositionChanged(object sender, GeoPositionChangedEventArgs<GeoCoordinate> e)
        {

            this.gcw.Stop();

            bannerAd.LocationLatitude = e.Position.Location.Latitude;
            bannerAd.LocationLongitude = e.Position.Location.Longitude;

            AdGameComponent.Current.Enabled = true;

            Log.DebugWrite("Device lat/long: " + e.Position.Location.Latitude + ", " + e.Position.Location.Longitude);
        }

        private void gcw_StatusChanged(object sender, GeoPositionStatusChangedEventArgs e)
        {
            if (e.Status == GeoPositionStatus.Disabled || e.Status == GeoPositionStatus.NoData)
            {
                AdGameComponent.Current.Enabled = true;
                Log.DebugWrite("GeoCoordinateWatcher Status :" + e.Status);
            }
        }

        /// <summary>
        /// LGame�����ӿڣ�����������׼XNA��Dispose�ĵ��ã���Ϸ����ʱ�Ż���õ���
        /// </summary>
        public void Dispose(Game game, bool disposing)
        {
            if (disposing)
            {
                if (this.gcw != null)
                {
                    this.gcw.Dispose();
                    this.gcw = null;
                }
            }
        }
    }

    public class Game1 : LFXPlus
    {
        public override void OnMain()
        {

            //����LGameĬ����Դ(�����д˲�����LGame���õ�ģ�ⰴť֮�๦���޷�ʹ��)
            XNAConfig.Load("assets/loon.def");
            //���������ļ����˴���Ԥ����õ�xnb�ļ���Ҳ���Լ���Content�µģ�
            XNAFont = new LFont("assets", "black", 0, 20);

            //ע��AD����(��׼XNA�¼�����)
            SetXNAListener(new ADListener());

            //�趨��������
            LSetting setting = new LSetting();
            setting.fps = 60;
            setting.width = 480;
            setting.height = 320;
            setting.showFPS = true;
            setting.landscape = true;
            //ע���ʼScreen
            Register(setting, typeof(ScreenTest));

        }

        public override void OnGameResumed()
        {

        }

        public override void OnGamePaused()
        {

        }
    }
}
