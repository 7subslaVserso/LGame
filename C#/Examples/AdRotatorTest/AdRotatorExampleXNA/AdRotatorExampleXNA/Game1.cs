using System;
using System.Windows;
using AdRotatorXNA;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Loon;
using Loon.Core.Graphics;

namespace AdRotatorExampleXNA
{
    /// <summary>
    /// ����XNA������(��΢����һ��LGame����ν��XNA��������������˵��LGame-XNA����ʵ����һ��XNA�ķ�װ��ס����Դ˼�������ʵ�����ã�����
    /// ��LGame������Ϻ󣬰�XNAӦ�еĲ���Ȩ���ڼ�������ʾ��������~)
    /// </summary>
    public class ADListener : XNAListener 
    {

        public void Create(Game gamne)
        {

        }

        public void Initialize(Game game)
        {
            // ��ʼ��������
            AdRotatorXNAComponent.Initialize(game);
            
            //Ӳ����Ļ�����������Щ

            //AdRotatorXNAComponent.Current.PubCenterAppId = "test_client";
            //AdRotatorXNAComponent.Current.PubCenterAdUnitId = "Image480_80";

            //AdRotatorXNAComponent.Current.AdDuplexAppId = "0";

            //AdRotatorXNAComponent.Current.InneractiveAppId = "DavideCleopadre_ClockAlarmNightLight_WP7";

            //AdRotatorXNAComponent.Current.MobFoxAppId = "474b65a3575dcbc261090efb2b996301";
            //AdRotatorXNAComponent.Current.MobFoxIsTest = true;

            //��ȡ�����ļ��Ļ�����������Щ(����Ϊ��ȡAdDuplex�Ĳ��Թ�棬AdRotatorҲ֧��Admob���)

            //��λ���λ��
            AdRotatorXNAComponent.Current.AdPosition = new Vector2(0,720);

            //�趨Ĭ�ϵĹ��ͼƬ
            AdRotatorXNAComponent.Current.DefaultHouseAdImage = game.Content.Load<Texture2D>(@"Content/AdRotatorDefaultAdImage");

            //�����Ĭ�Ϲ��ʱ��ָ��˲�����
            AdRotatorXNAComponent.Current.DefaultHouseAdClick += new AdRotatorXNAComponent.DefaultHouseAdClickEventHandler(Current_DefaultHouseAdClick);

            //����ѡ����õ�Ч���ĵ�������
            AdRotatorXNAComponent.Current.SlidingAdDirection = SlideDirection.None;

            //ѡ�񱾵صĹ�������ļ���ַ����Բ�ͬ�Ĺ���̣��˴�����Ч����ͬ���Ծ��������ṩ�����÷�ʽΪ׼��
            AdRotatorXNAComponent.Current.DefaultSettingsFileUri = "defaultAdSettings.xml";

            //�趨Զ�������ļ�����ѡ��Ǳ��
            AdRotatorXNAComponent.Current.SettingsUrl = "http://xna-uk.net/adrotator/XNAdefaultAdSettingsV2.xml";

            //��ӹ�������XNA���浱��
            game.Components.Add(AdRotatorXNAComponent.Current);

        }

        void Current_DefaultHouseAdClick()
        {
            try
            {
                MessageBox.Show("�ǳ���л������С�ܵĹ��^_^");
            }
            catch { }
        }

        public void LoadContent(Game game)
        {

        }

        public void UnloadContent(Game game)
        {

        }

        public void Update(Game game,GameTime gameTime)
        {
 
        }

        public void Draw(Game game, GameTime gameTime)
        {

        }

        public void Dispose(Game game, bool close)
        {

        }

    }

    public class Game1 : LFXPlus
    {
        public override void OnMain()
        {

            //����LGameĬ����Դ(�����д˲�����LGame���õ�ģ�ⰴť֮�๦���޷�ʹ��)
            XNAConfig.Load("content/loon.def");
            //���������ļ����˴���Ԥ����õ�xnb�ļ� PS�����Զ�����Դ�ļ�������ΪContentʱ��
            //�������Զ��ͱ�׼��Content�ļ��кϲ�������������ʾ����Windowsϵͳ�������ļ�
            //����Сд��
            XNAFont = new LFont("content", "black", 0, 20);

            //ע��AD����(��׼XNA�¼�����)
            SetXNAListener(new ADListener());

            //�趨��������
            LSetting setting = new LSetting();
            setting.fps = 60;
            setting.width = 480;
            setting.height = 320;
            setting.showFPS = true;
            setting.landscape = false;
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
