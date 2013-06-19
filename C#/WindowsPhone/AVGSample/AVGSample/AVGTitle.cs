using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Loon.Core.Graphics;
using Loon.Core.Graphics.Component;
using Loon.Core.Input;
using Loon.Core.Timer;
using Loon.Core.Graphics.Opengl;
using Loon.Action.Sprite.Effect;
using Loon.Core.Event;
using Loon.Core;

namespace AVGTest
{

    public class AVGTitle : Screen
    {

        LButton start, end;

        LPaper title;

        class StartClick : ClickListener
        {

            ActionKey action = new ActionKey(
                    ActionKey.DETECT_INITIAL_PRESS_ONLY);

            public void DoClick(LComponent comp)
            {
                if (!action.IsPressed())
                {
                    action.Press();
                    if (comp.Tag is Screen)
                    {
                        Screen screen = (Screen)comp.Tag;
                        screen.ReplaceScreen(new MyAVGScreen(), MoveMethod.FROM_LEFT);
                    }
                }
            }

            public void DownClick(LComponent comp, float x, float y)
            {
             
            }

            public void UpClick(LComponent comp, float x, float y)
            {

            }

            public void DragClick(LComponent comp, float x, float y)
            {

            }
        }

        public AVGTitle()
        {

        }

        public override LTransition OnTransition()
        {
            return LTransition.NewArc();
        }

        public override void OnLoad()
        {
            // ����ָ������ͼ
           // SetBackground("assets/back2.png");

            // �������
            SetBackground("assets/back1.png");

            // ����һ����ʼ��ť�����տ�191����57�ֽⰴťͼ�����趨��Click�¼�
            start = new LButton("assets/title_start.png", 191, 57);
            // �趨��ťλ��Ϊx=2,y=5
            start.SetLocation(2, 5);
            // �趨�˰�ť������
            start.SetEnabled(false);
            start.Tag = this;
            start.Click = new StartClick();
            // ��Ӱ�ť
            Add(start);

            // ����һ����¼��ȡ��ť�����տ�160����56�ֽⰴťͼ
            LButton btn2 = new LButton("assets/title_load.png", 160, 56);
            // �趨��ťλ��Ϊx=2,y=startλ������
            btn2.SetLocation(2, start.GetY() + start.GetHeight() + 20);
            // �趨�˰�ť������
            btn2.SetEnabled(false);
            // ��Ӱ�ť
            Add(btn2);

            // ����һ���������ð�ť�����տ�215����57�ֽⰴťͼ
            LButton btn3 = new LButton("assets/title_option.png", 215, 57);
            // �趨��ťλ��Ϊx=2,y=btn2λ������
            btn3.SetLocation(2, btn2.GetY() + btn2.GetHeight() + 20);
            // �趨�˰�ť������
            btn3.SetEnabled(false);
            // ��Ӱ�ť
            Add(btn3);

            // ����һ���˳���ť�����տ�142����57�ֽⰴťͼ�����趨��Click�¼�
            end = new LButton("assets/title_end.png", 142, 57);
            // �趨��ťλ��Ϊx=2,y=btn3λ������
            end.SetLocation(2, btn3.GetY() + btn3.GetHeight() + 20);
            // �趨�˰�ť������
            end.SetEnabled(false);
            // ��Ӱ�ť
            Add(end);
            // ����һ������
            title = new LPaper("assets/title.png", -200, 0);
            // ��ӱ���
            Add(title);
        }

        public override void Alter(LTimerContext c)
        {
            // ��ʼ�����
            if (IsOnLoadComplete())
            {
                // ����δ�ﵽ�����Ե
                if (title.GetX() + title.GetWidth() + 25 <= GetWidth())
                {
                    // ���������ƶ�����ɫ�޽ǡ�����
                    title.Move_right(3);
                }
                else
                {
                    // �趨��ʼ��ť����
                    start.SetEnabled(true);
                    // �趨������ť����
                    end.SetEnabled(true);
                }
            }
        }

        public override void Draw(GLEx g)
        {
        }

        public override void TouchDown(LTouch touch)
        {

        }

        public override void TouchDrag(LTouch e)
        {

        }

        public override void TouchMove(LTouch e)
        {

        }

        public override void TouchUp(LTouch touch)
        {

        }


    }
}
