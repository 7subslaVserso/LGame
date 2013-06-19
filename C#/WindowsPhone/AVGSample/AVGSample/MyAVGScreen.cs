using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Loon.Core.Graphics.Component;
using Loon.Action.Avg;
using Loon.Core.Graphics.Opengl;
using Loon.Core.Graphics;
using Loon.Action.Avg.Drama;

namespace AVGTest
{
    public class MyAVGScreen : AVGScreen
    {

        LPaper roleName;

        String flag = "�Զ�������.";

        String[] selects = { "������ǧ˧��˧��" };

        int type;

        public MyAVGScreen()
            : base("assets/script/s1.txt", AVGDialog.GetRMXPDialog("assets/w6.png",
                460, 150))
        {

        }

        public override void OnLoading()
        {
            roleName = new LPaper("assets/name0.png", 25, 25);
            LeftOn(roleName);
            roleName.SetLocation(5, 15);
            Add(roleName);
        }

        public override void DrawScreen(GLEx g)
        {
            switch (type)
            {
                case 1:
                    g.SetAntiAlias(true);
                    g.DrawSixStart(LColor.yellow, 130, 100, 100);
                    g.SetAntiAlias(false);
                    break;
            }
            g.ResetColor();
        }

        LButton yes;

        public override void InitCommandConfig(Command command)
        {
            // ��ʼ��ʱԤ�����
            command.SetVariable("p", "assets/p.png");
            command.SetVariable("sel0", selects[0]);
        }

        public override void InitMessageConfig(LMessage message)
        {

        }

        public override void InitSelectConfig(LSelect select)
        {
        }

        class YesClick : ClickListener
        {
            public void DoClick(LComponent comp)
            {
             
            }

            public void DownClick(LComponent comp, float x, float y)
            {

                if (comp.Tag is AVGScreen)
                {
                    AVGScreen screen = (AVGScreen)comp.Tag;
                    // �������
                    screen.SetLocked(false);
                    // �����¼�
                    // click();
                    // ɾ����ǰ��ť
                    screen.Remove(comp);
                }
            }

            public void UpClick(LComponent comp, float x, float y)
            {

            }

            public void DragClick(LComponent comp, float x, float y)
            {

            }
        }

        public override bool NextScript(String mes)
        {

            // �Զ��������Щ�Զ�������Ϊ��ͻ��д�������ģ�ʵ�ʲ��Ƽ���
            if (roleName != null)
            {
                if ("noname".Equals(mes, StringComparison.InvariantCultureIgnoreCase))
                {
                    roleName.SetVisible(false);
                }
                else if ("name0".Equals(mes, StringComparison.InvariantCultureIgnoreCase))
                {
                    roleName.SetVisible(true);
                    roleName.SetBackground("assets/name0.png");
                    roleName.SetLocation(5, 15);
                }
                else if ("name1".Equals(mes, StringComparison.InvariantCultureIgnoreCase))
                {
                    roleName.SetVisible(true);
                    roleName.SetBackground("assets/name1.png");
                    roleName.SetLocation(GetWidth() - roleName.GetWidth() - 5, 15);
                }
            }
            if ((flag + "����").Equals(mes, StringComparison.InvariantCultureIgnoreCase))
            {
                // ��ӽű��¼���ǣ���Ҫ�����ִ�У�
                SetScrFlag(true);
                type = 1;
                return false;
            }
            else if ((flag + "ȥ���ɣ�����").Equals(mes, StringComparison.InvariantCultureIgnoreCase))
            {
                type = 0;
            }
            else if ((flag + "�������").Equals(mes, StringComparison.InvariantCultureIgnoreCase))
            {
                message.SetVisible(false);
                SetScrFlag(true);
                // ǿ�������ű�
                SetLocked(true);
                yes = new LButton("assets/dialog_yes.png", 112, 33);
                yes.Tag = this;
                yes.Click = new YesClick();
                CenterOn(yes);
                Add(yes);
                return false;
            }
            return true;
        }

        public override void OnExit()
        {
            // ���·��ر��⻭��
            SetScreen(new AVGTitle());
        }

        public override void OnSelect(String message, int type)
        {
            if (selects[0].Equals(message, StringComparison.InvariantCultureIgnoreCase))
            {
                command.SetVariable("sel0", Convert.ToString(type));
            }
        }

    }
}
