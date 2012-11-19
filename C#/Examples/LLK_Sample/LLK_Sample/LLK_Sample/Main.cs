using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Content;
using Microsoft.Xna.Framework.GamerServices;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Microsoft.Xna.Framework.Input.Touch;
using Microsoft.Xna.Framework.Media;
using Loon;
using Loon.Core.Graphics;

namespace LLK_Sample
{
    public class Main : LFXPlus
    {
        public override void OnMain()
        {
            //����LGameʹ�õ�Ĭ������(�����д˲�����LGameĬ�ϵ�DrawString֮�ຯ���޷�ʹ��(PS:XNAConfig����
            //Ĭ�ϰ�ʱ���ɻ�ò���Ӣ������֧��))
            XNAFont = new LFont("Content/Fonts", "black", 0, 20);
            MaxScreen(480, 320);
            //�趨��ʼ����ĻΪ�������Զ������Ϸ����Ϊ��Ļ��С
            Initialization(true, LMode.Fill);
            //֡��60
            SetFPS(60);
            //��ʾ֡��
            SetShowFPS(true);
            //����Screen
            SetScreen(new LLKScreen());
            //��ʾScreen
            ShowScreen();
        }

        public override void OnGameResumed()
        {

        }

        public override void OnGamePaused()
        {

        }
    }
}
